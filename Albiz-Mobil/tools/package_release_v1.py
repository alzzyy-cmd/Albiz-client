#!/usr/bin/env python3
"""Reproducible standalone-PC/mobile releases and source overlay. Run after Gradle build and addon javac."""
from pathlib import Path
import io,os,json,zipfile,hashlib,shutil,xml.etree.ElementTree as ET
from PIL import Image
R=Path(__file__).resolve().parents[1];D=R.parent/'Albiz-1.0';D.mkdir(exist_ok=True)
def sha(b):return hashlib.sha256(b).hexdigest()
def put(z,n,b):
 i=zipfile.ZipInfo(n,(2026,11,6,0,0,0));i.compress_type=zipfile.ZIP_DEFLATED;i.external_attr=0o100644<<16;z.writestr(i,b,compresslevel=9)
base=json.loads((R/'docs/PROTECTED_BASELINE.json').read_text())
assert all(sha((R/p).read_bytes())==v for p,v in base.items())
counts={k:0 for k in ['tests','failures','errors','skipped']}
for p in (R/'build/test-results/test').glob('TEST-*.xml'):
 root=ET.parse(p).getroot()
 for k in counts:counts[k]+=int(root.get(k,0))
assert counts['tests']>=146 and counts['failures']==counts['errors']==counts['skipped']==0
checks={'version':'1.0','java':counts,'python_tests':28,'addon_pure_rule_assertions':17,'protected_files_unchanged':len(base),'runtime_tested':False,'profiles':{}}
sourcejar=R/'build/libs/albiz-1.0.jar'
for profile in ['PC','Mobile']:
 folder=D/profile;folder.mkdir(exist_ok=True);dest=folder/'Albiz-Client.jar';removed=[];resized=[]
 with zipfile.ZipFile(sourcejar) as zin,zipfile.ZipFile(dest,'w') as zout:
  for item in sorted(zin.infolist(),key=lambda i:i.filename):
   n=item.filename
   if item.is_dir():continue
   data=zin.read(n)
   if n=='fabric.mod.json':
    meta=json.loads(data);meta['version']='1.0';meta['name']='Albiz Client'+(' Mobile' if profile=='Mobile' else ' PC');meta.setdefault('custom',{})['albiz:profile']=profile.lower()
    if profile=='Mobile':
     meta['jars']=[j for j in meta.get('jars',[]) if 'imgui-java-natives-' not in j['file']]
    data=(json.dumps(meta,indent=2)+'\n').encode()
   if profile=='Mobile':
    if n.startswith('META-INF/jars/imgui-java-natives-'):removed.append(n);continue
    if n.endswith('.png') and n.startswith(('assets/albiz/textures/sky/','assets/albiz/textures/gui/','assets/albiz/textures/menu/')):
     im=Image.open(io.BytesIO(data));limit=768 if '/sky/' in n else 960
     if max(im.size)>limit:
      old=list(im.size);im.thumbnail((limit,limit),Image.Resampling.LANCZOS);buf=io.BytesIO();im.save(buf,format='PNG',optimize=True,compress_level=9);data=buf.getvalue();resized.append({'path':n,'from':old,'to':list(im.size)})
   put(zout,n,data)
  put(zout,'albiz-profile.properties',('profile='+profile.lower()+'\n').encode())
 with zipfile.ZipFile(dest) as z:
  assert z.testzip() is None;names=set(z.namelist());assert 'assets/albiz/natives/android-arm64/libimgui-java64.so' in names
  for f in ['albiz.mixins.json','albiz.soberui.mixins.json']:
   m=json.loads(z.read(f));prefix=m['package'].replace('.','/')+'/'
   for cl in m.get('client',[])+m.get('mixins',[]):assert prefix+cl.replace('.','/')+'.class' in names,(f,cl)
  for j in json.loads(z.read('fabric.mod.json')).get('jars',[]):assert j['file'] in names
  for mod in ['ChunkScannerModule','MotionScannerModule','ActivityInspectorModule']:assert 'net/albiz/client/module/impl/render/'+mod+'.class' in names
  for f in ['assets/albiz/shaders/core/glass_entity.fsh','assets/albiz/shaders/core/glass_entity.vsh']:assert f in names
 checks['profiles'][profile]={'bytes':dest.stat().st_size,'sha256':sha(dest.read_bytes()),'removed':removed,'resized':resized}
addon=D/'Larp addon.jar'
with zipfile.ZipFile(addon,'w') as z:
 for p in sorted((R/'larp-addon/classes').rglob('*.class')):put(z,str(p.relative_to(R/'larp-addon/classes')),p.read_bytes())
 for p in sorted((R/'larp-addon/src/main/resources').rglob('*')):
  if p.is_file():put(z,str(p.relative_to(R/'larp-addon/src/main/resources')),p.read_bytes())
 put(z,'LICENSE',(R/'LICENSE').read_bytes());put(z,'README.md',(R/'larp-addon/README.md').read_bytes())
with zipfile.ZipFile(addon) as z,zipfile.ZipFile(D/'PC/Albiz-Client.jar') as pc,zipfile.ZipFile(D/'Mobile/Albiz-Client.jar') as mob:
 assert z.testzip() is None
 assert not any(n.startswith(('net/albiz/client/','meteordevelopment/','com/larp/','dev/sober/')) for n in z.namelist())
 for c in ['LarpAddon','LarpV5','LarpV6','LarpV7','PassiveSensor','EvidenceRules']:assert f'net/albizlarp/{c}.class' in z.namelist()
 assert json.loads(z.read('fabric.mod.json'))['entrypoints']['albiz']==['net.albizlarp.LarpAddon']
 assert {n for n in pc.namelist() if n.endswith('.class')}=={n for n in mob.namelist() if n.endswith('.class')}
 for n in pc.namelist():
  if n.endswith('.class'):assert pc.read(n)==mob.read(n)
checks['addon']={'bytes':addon.stat().st_size,'sha256':sha(addon.read_bytes()),'same_client_class_ABI':True,'meteor_required':False,'original_reference_code_in_addon':False}
checks['mobile_vs_pc_bytes_saved']=checks['profiles']['PC']['bytes']-checks['profiles']['Mobile']['bytes']
checks['limitations']=['No Minecraft, OpenGL, Android, FPS or server tests performed.','Glass mixin injection compatibility and GLSL backend support require in-game testing.','Motion and Activity are bounded independent heuristics, not exact Sober ports.','Activity light evidence is block emission-state change, not server light-packet attribution.','Larp addon is independently implemented, not feature-identical to the original Meteor binary.','Mobile keeps optional heavy cosmetics; leave disabled on low-memory devices.','Mobile scan budget is cooperative, not a hard real-time FPS guarantee.','Hidden cards remain selectable as editor ghosts. Target HUD is world-anchored and is not a draggable screen HUD card.']
(D/'CHECKS.json').write_text(json.dumps(checks,indent=2)+'\n')
for a,b in [('v1-build.log','BUILD.log'),('v1-python.log','PYTHON_TESTS.log'),('v1-addon-tests.log','ADDON_TESTS.log')]:shutil.copy2(R/'logs'/a,D/b)
# Source overlay avoids copying binary assets already in the accompanying PC JAR.
# restore_resources.py reconstructs assets byte-for-byte from that JAR; Java sources/build files/licenses are embedded here.
source=D/'Albiz-1.0-source.zip'
exclude={'.git','.gradle','.cache','build','__pycache__','logs','node_modules','dist','.venv','classes','test-classes','artwork'}
with zipfile.ZipFile(source,'w') as z:
 for folder,dirs,files in os.walk(R,followlinks=True):
  dirs[:]=sorted(d for d in dirs if d not in exclude)
  for name in sorted(files):
   p=Path(folder)/name;rel=p.relative_to(R)
   if not p.is_file():continue
   if str(rel).startswith('src/main/resources/') or name=='restore_workspace.py':continue
   put(z,'Albiz/'+str(rel),p.read_bytes())
 put(z,'Albiz/restore_resources.py',b'''from pathlib import Path
import zipfile,sys
root=Path(__file__).resolve().parent
jar=Path(sys.argv[1]) if len(sys.argv)>1 else root.parent/'PC'/'Albiz-Client.jar'
with zipfile.ZipFile(jar) as z:
 for n in z.namelist():
  if n.startswith(('assets/','data/')) or n in ['fabric.mod.json','albiz.mixins.json','albiz.soberui.mixins.json','albiz.indigo.mixins.json','albiz.easyplace.mixins.json','LICENSE_anubis','SECURITY.md','albiz.accesswidener']:
   if n.endswith('/'):continue
   dest=root/'src/main/resources'/n;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(z.read(n))
print('Restored build resources from',jar)
''')
 put(z,'SOURCE-README.md',b'''# Corresponding source for Albiz 1.0\nUnzip next to the accompanying PC folder. Binary assets are supplied once in PC/Albiz-Client.jar, not duplicated in this source overlay. All modified Java, shaders (restored from the JAR), build configuration, legal notices and addon sources are provided.\n\nRun: cd Albiz; python restore_resources.py ../PC/Albiz-Client.jar\nThen set JAVA_HOME to Java 21 and run: bash gradlew test build addonClasspath\nStandalone addon and release packaging: bash larp-addon/build.sh (Python Pillow required for mobile image resizing). Gradle downloads Minecraft/Fabric dependencies; internet required on the first build. No reference Download.zip or Sober binary is needed to build.\n\nThe source overlay plus the PC JAR are the complete build input. Keep both together. No build caches, credentials, user profiles or server/account data included.\n''')
with zipfile.ZipFile(source) as z:assert z.testzip() is None
paths=[D/'PC/Albiz-Client.jar',D/'Mobile/Albiz-Client.jar',addon,source,D/'CHECKS.json']
(D/'SHA256SUMS.txt').write_text(''.join(sha(p.read_bytes())+'  '+str(p.relative_to(D))+'\n' for p in paths))
print(json.dumps({'PC_MB':checks['profiles']['PC']['bytes']/1e6,'Mobile_MB':checks['profiles']['Mobile']['bytes']/1e6,'addon_bytes':addon.stat().st_size,'source_MB':source.stat().st_size,'Java_tests':counts},indent=2))
