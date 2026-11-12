from pathlib import Path
import zipfile,json,hashlib,base64,subprocess
R=Path(__file__).resolve().parents[1];D=R.parent/'Albiz-1.0-Friends';old=R.parent/'Albiz-1.0-Updated'
h=lambda b:hashlib.sha256(b).hexdigest()
manifest={'profiles':{},'note':'Reconstructs the previously delivered 1.0 JAR contents from Updated JARs plus deltas; no installed game/config files are touched.'}
with zipfile.ZipFile(D/'Rollback.zip','w',zipfile.ZIP_DEFLATED,compresslevel=9) as out:
 for profile in ['PC','Mobile']:
  with zipfile.ZipFile(old/profile/'Albiz-Client.jar') as before,zipfile.ZipFile(D/profile/'Albiz-Client.jar') as after:
   entries=[]
   for i in before.infolist():
    if i.is_dir():continue
    b=before.read(i.filename);different=i.filename not in after.namelist() or b!=after.read(i.filename)
    if different:out.writestr('files/'+profile+'/'+i.filename,b)
    entries.append({'name':i.filename,'sha256':h(b),'delta':different,'time':list(i.date_time),'attr':i.external_attr,'comment':base64.b64encode(i.comment).decode(),'extra':base64.b64encode(i.extra).decode()})
   manifest['profiles'][profile]={'entries':entries,'original_archive_sha256':h((old/profile/'Albiz-Client.jar').read_bytes())}
 out.writestr('manifest.json',json.dumps(manifest))
 out.write(old/'Rollback.zip','Previous-generation-Rollback.zip')
 out.write(old/'Albiz-1.0-source.zip','Previous-source.zip');out.write(old/'README.md','Previous-README.md');out.write(old/'Larp addon.jar','Previous-Larp addon.jar')
 out.writestr('restore_previous.py', '''from pathlib import Path
import zipfile,json,hashlib,base64,sys
root=Path(__file__).resolve().parent
base=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else root.parent
output=Path(sys.argv[2]).resolve() if len(sys.argv)>2 else root/'Restored-Previous-1.0'
manifest=json.loads((root/'manifest.json').read_text())
for profile,meta in manifest['profiles'].items():
 source=base/profile/'Albiz-Client.jar';dest=output/profile/'Albiz-Client.jar';dest.parent.mkdir(parents=True,exist_ok=True)
 if source.resolve()==dest.resolve():raise ValueError('Refusing to overwrite the updated input JAR')
 with zipfile.ZipFile(source) as current,zipfile.ZipFile(dest,'w') as restored:
  for e in meta['entries']:
   n=e['name'];b=(root/'files'/profile/n).read_bytes() if e['delta'] else current.read(n)
   if hashlib.sha256(b).hexdigest()!=e['sha256']:raise ValueError('Wrong base or corrupted delta: '+n)
   i=zipfile.ZipInfo(n,tuple(e['time']));i.compress_type=zipfile.ZIP_DEFLATED;i.external_attr=e['attr'];i.comment=base64.b64decode(e['comment']);i.extra=base64.b64decode(e['extra']);restored.writestr(i,b,compresslevel=9)
 print('Restored',dest)
import shutil
for src,dst in [('Previous-source.zip','Albiz-1.0-source.zip'),('Previous-README.md','README.md'),('Previous-Larp addon.jar','Larp addon.jar'),('Previous-generation-Rollback.zip','Rollback.zip')]:
 shutil.copy2(root/src,output/dst)
print('Previous client contents restored; installed mods/config were not changed.')
''')
 out.writestr('README.md','''# Geri dönüş / Rollback\nExtract this ZIP into a folder alongside Updated PC/ and Mobile/. Run `python restore_previous.py ..`. Optional arguments: path-to-updated-folder and output-folder. It verifies every restored file against the previous content SHA256. The previous source and Larp addon are included. Does not change installed mods or config automatically.\n\nBu ZIP'i Updated PC/Mobile klasörlerinin yanındaki Rollback klasörüne çıkarıp `python restore_previous.py ..` çalıştır. Önceki teslim ayrı klasöre üretilir; kurulu oyuna/config'e dokunulmaz.\n''')
T=R.parent/'.cache/rollback-verification';T.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(D/'Rollback.zip') as z:assert z.testzip() is None;z.extractall(T)
subprocess.run(['python',str(T/'restore_previous.py'),str(D),str(T/'output')],check=True)
for profile in ['PC','Mobile']:
 with zipfile.ZipFile(old/profile/'Albiz-Client.jar') as a,zipfile.ZipFile(T/'output'/profile/'Albiz-Client.jar') as b:
  assert set(a.namelist())==set(b.namelist())
  assert all(a.read(n)==b.read(n) for n in a.namelist())
print('PASS both previous client JAR contents restored byte-for-byte, including old source/addon.')
print('Rollback bytes',(D/'Rollback.zip').stat().st_size)
