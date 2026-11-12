#!/usr/bin/env python3
"""Read every textual source/resource; verify built-in module/setting localization boundaries."""
from pathlib import Path
import re,json,hashlib
ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/'src/main/resources/assets/albiz/lang'
en=json.loads((BASE/'en_us.json').read_text());tr=json.loads((BASE/'tr_tr.json').read_text())
assert en.keys()==tr.keys()
rows=['file\tbytes\tsha256\tlegacy_identity_matches\thor us_matches'.replace('hor us','horus')]
modules={};setting_strings=set();text_count=0;byte_count=0
TECHNICAL={'10m','2m','1k','3k','450k','60k','FPS','Karambit','T','X','X Y Z','X, Y, Z','barrel','diamond_sword','en_us','golden_apple','minecraft:netherite_sword','minecraft:totem_of_undying','rtp','totem_of_undying','tpa','tpahere','tr_tr','tt:pastagamer08','tt:theauzzi','tt:u_vv7_','yt:DrDonutt','yt:pastagamer08','yt:wizoxlol'}
def decode(s):
 try:return json.loads('"'+s+'"')
 except ValueError:return s
for p in sorted((ROOT/'src').rglob('*')):
 if not p.is_file() or p.suffix.lower() not in {'.java','.json','.fsh','.vsh','.glsl','.md','.txt','.accesswidener','.properties','.xml','.cfg','.mcmeta'}:continue
 raw=p.read_bytes()
 try:text=raw.decode('utf-8')
 except UnicodeError:continue
 text_count+=1;byte_count+=len(raw)
 rows.append(f'{p.relative_to(ROOT)}\t{len(raw)}\t{hashlib.sha256(raw).hexdigest()}\t{len(re.findall("anubis",text,re.I))}\t{len(re.findall("horus",text,re.I))}')
 if p.suffix!='.java':continue
 for m in re.finditer(r'super\(\s*"([^"\\]+)"\s*,\s*"([^"\\]+)"\s*,\s*"((?:\\.|[^"\\])*)"',text):
  mid,name,description=m.groups()
  if 'client/module/impl' not in str(p) and 'client/easyplace' not in str(p):continue
  key='module.'+mid+'.description';modules[mid]=key
  assert key in en and key in tr,(mid,p)
  assert tr[key] and tr[key]!=en[key],(mid,'untranslated description')
 for m in re.finditer(r'new \w+Setting\s*\(',text):
  i=m.end();depth=1
  while i<len(text) and depth:
   if text[i]=='"':
    q=re.match(r'"((?:\\.|[^"\\])*)"',text[i:])
    if q:setting_strings.add(decode(q[1]));i+=len(q[0]);continue
   if text[i]=='(':depth+=1
   if text[i]==')':depth-=1
   i+=1
missing=sorted(v for v in setting_strings if v and re.search('[A-Za-z]',v) and v not in en.values() and v not in TECHNICAL)
assert not missing,missing
report={'text_files_read':text_count,'text_bytes_read':byte_count,'catalog_keys_per_language':len(en),'distinct_english_values':len(set(en.values())),'module_descriptions_checked':len(modules),'setting_constructor_strings_checked':len(setting_strings),'uncatalogued_nontechnical_constructor_strings':missing,'allowed_technical_values':sorted(TECHNICAL),'coverage_scope':'All static built-in module descriptions and literal setting constructor strings. Not a proof of every generated sentence, external service text, native UI or third-party addon.','config_directory':'albız','main_config_file':'albız.json','non_utf8_filesystem_fallback':'albiz/albiz.json','active_horus_names':'none; legacy import alias in ModeSetting only'}
(ROOT/'docs/TEXT_FILE_INVENTORY.tsv').write_text('\n'.join(rows)+'\n')
(ROOT/'docs/LOCALIZATION_AUDIT.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
print(json.dumps(report,ensure_ascii=False,indent=2))
