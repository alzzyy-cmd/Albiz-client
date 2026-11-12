#!/usr/bin/env python3
"""Package the expanded (possibly symlink-backed) source, never cache links."""
from pathlib import Path
import os,zipfile,hashlib,shutil,json,xml.etree.ElementTree as ET
R=Path(__file__).resolve().parents[1];D=R.parent/'Albiz-1.0-classic-gui-items';D.mkdir(exist_ok=True)
hashfile=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
jar=D/'albiz-1.0-anubis-panels.jar';shutil.copy2(R/'build/libs/albiz-1.0.jar',jar)
base=json.loads((R/'docs/PROTECTED_BASELINE.json').read_text());assert all(hashfile(R/p)==h for p,h in base.items())
suites=ET.Element('testsuites');counts={k:0 for k in ['tests','failures','errors','skipped']}
for p in sorted((R/'build/test-results/test').glob('TEST-*.xml')):
 s=ET.parse(p).getroot();suites.append(s)
 for k in counts:counts[k]+=int(s.get(k,0))
assert counts['tests']>=140 and counts['failures']==counts['errors']==0
suites.attrib.update({k:str(v) for k,v in counts.items()});ET.ElementTree(suites).write(R/'docs/TEST_RESULTS.xml',encoding='utf-8',xml_declaration=True)
shutil.copy2(R.parent/'new-reference-audit/anubis-panels-build.log',R/'docs/BUILD.log');shutil.copy2(R.parent/'new-reference-audit/anubis-panels-python.log',R/'docs/PYTHON_TESTS.log')
with zipfile.ZipFile(jar) as z:
 assert z.testzip() is None
 names=set(z.namelist());mixins=0
 for name in ['albiz.mixins.json','albiz.soberui.mixins.json']:
  data=json.loads(z.read(name));prefix=data['package'].replace('.','/')+'/'
  for cl in data.get('client',[])+data.get('mixins',[]):assert prefix+cl.replace('.','/')+'.class' in names,(name,cl);mixins+=1
 root='assets/albiz/models/fortnite_pack/'
 items=[n.strip() for n in z.read(root+'items.txt').decode().splitlines() if n.strip()]
 for n in items:assert root+n+'.png' in names,n
 for n in ['net/albiz/client/port/soberui/gui/VisualPresetsScreen.class','net/albiz/client/port/soberui/modules/CsPickaxe.class','net/albiz/client/port/soberui/modules/CustomModels.class','assets/albiz/textures/gui/dusk_backdrop.png','LICENSE','NOTICE.md','THIRD_PARTY_PORTS.md']:assert n in names,n
 proof={'revision':'anubis-original-panels','java':counts,'python_tests':28,'protected_sha256_unchanged':len(base),'registered_mixin_class_entries_verified':mixins,'fortnite_pack_images_verified':len(items),'jar_sha256':hashfile(jar),'jar_bytes':jar.stat().st_size,'jar_crc':'pass','anubis_gui_restore':json.loads((R/'docs/ANUBIS_PANEL_RESTORE.json').read_text()),'scope':'Original independent Anubis category panels, not the later library GUI; retained native menus/HUD; Dusk; larger logo; Turkish presentation; thumbnail totems/pickaxes/custom models. Appearance only.','reported_errors':{'watery_locale_id':'Locale.ROOT helper; Turkish-locale CPU regression passes','sprite_decompilation_stub':'replaced; 24 declared PNG contours exercised by production CPU builder','multiplayer_crash':'custom initialization/state cleanup guards added; full reported crash trace unavailable, runtime reproduction still required'},'not_tested':['Minecraft launch','OpenGL/Vulkan rendering','Android/Zalith','network/server flows','FPS','visual/screenshot parity','resource reload in a running game'],'limits':{'mesh_cache':64,'sprite_cache':64,'thumbnail_cache':64,'pending_sprites':32,'executor_queue':32,'model_triangles':60000,'file_MiB':16,'PNG_dimension':2048,'sprite_texture_dimension':256}}
(R/'docs/GUI_ITEMS_CHECKS.json').write_text(json.dumps(proof,ensure_ascii=False,indent=2)+'\n')
shutil.copy2(R/'README.md',D/'OKU-BENI.md');shutil.copy2(R/'docs/GUI_ITEMS_CHECKS.json',D/'KONTROLLER.json')
source=D/'albiz-1.0-classic-gui-items-source.zip'
exclude={'.git','.gradle','.cache','build','__pycache__','logs','node_modules','dist','.venv'};entries=0
with zipfile.ZipFile(source,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as z:
 for folder,dirs,files in os.walk(R,followlinks=True):
  dirs[:]=sorted(d for d in dirs if d not in exclude)
  for name in sorted(files):
   p=Path(folder)/name
   if p.is_file():z.write(p,'Albiz/'+str(p.relative_to(R)));entries+=1
with zipfile.ZipFile(source) as z:
 assert z.testzip() is None
 for n in ['Albiz/src/main/java/net/albiz/client/port/soberui/gui/VisualPresetsScreen.java','Albiz/src/main/resources/assets/albiz/models/fortnite_pack/items.txt','Albiz/README.md','Albiz/gradlew','Albiz/build.gradle']:assert n in z.namelist()
 assert all(((i.external_attr>>16)&0o170000)!=0o120000 for i in z.infolist()),'Archive must contain real files, not symlinks'
(D/'SHA256SUMS.txt').write_text(''.join(hashfile(p)+'  '+p.name+'\n' for p in [jar,source,D/'OKU-BENI.md',D/'KONTROLLER.json']))
print(json.dumps({'output':str(D),'java':counts,'protected':len(base),'pack_images':len(items),'source_entries':entries,'jar_bytes':jar.stat().st_size,'source_bytes':source.stat().st_size},indent=2))
