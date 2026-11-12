from pathlib import Path
import zipfile,sys,json
root=Path(__file__).resolve().parent
jar=Path(sys.argv[1]) if len(sys.argv)>1 else root.parent/'PC'/'Albiz-Client.jar'
with zipfile.ZipFile(jar) as z:
 for n in z.namelist():
  if n.startswith(('assets/','data/')) or n in ['fabric.mod.json','albiz.mixins.json','albiz.soberui.mixins.json','albiz.indigo.mixins.json','albiz.easyplace.mixins.json','LICENSE_anubis','SECURITY.md','albiz.accesswidener']:
   if n.endswith('/'):continue
   dest=root/'src/main/resources'/n;dest.parent.mkdir(parents=True,exist_ok=True)
   data=z.read(n)
   if n=='fabric.mod.json':
    meta=json.loads(data);meta.pop('jars',None);meta['name']='Albiz Client';meta.get('custom',{}).pop('albiz:profile',None);data=(json.dumps(meta,indent=2)+'\n').encode()
   dest.write_bytes(data)
print('Restored build resources from',jar)
