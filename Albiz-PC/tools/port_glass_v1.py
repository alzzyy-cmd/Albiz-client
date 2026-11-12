from pathlib import Path
import re,zipfile,json
base=Path('/home/user/.cache/new-addon-audit');root=Path('src/main/java/net/albiz/client/port/soberui')
def port(src,dest):
 s=src.read_text();s=s[s.index('package '):];s=s.replace('dev.sober','net.albiz.client.port.soberui');s=re.sub(r'"sober(?=[:_/" ])','"albiz',s);s=s.replace('sober$', 'albizGlass$');dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text('// Adapted from SoberClient (Violet), GPL-3.0-only. See THIRD_PARTY_PORTS.md.\n'+s)
for n in ['GlassLayers','GlassGear']:port(base/f'sober/dev/sober/render/{n}.java',root/f'render/{n}.java')
for n in ['GameRendererAccessor','GuiRendererAccessor','GlassGearMixin','ItemGlassMixin']:port(base/f'hooks/dev/sober/mixin/{n}.java',root/f'mixin/{n}.java')
port(base/'sober/dev/sober/modules/Glass.java',root/'modules/Glass.java')
p=root/'SoberUi.java';s=p.read_text().replace('var models=new CustomModels();','var glass=new Glass();SoberClient.MODULES.addHidden(glass);net.albiz.client.module.ModuleRegistry.add(new ReferenceVisualModule("glass",glass));\n  var models=new CustomModels();');p.write_text(s)
p=root/'modules/Glass.java';s=p.read_text().replace('true)).onChanged','false)).onChanged');p.write_text(s)
with zipfile.ZipFile(base/'Sober.zip') as z:
 for n in z.namelist():
  if '/shaders/' in n and 'glass_entity' in n:
   target=Path('src/main/resources')/n.replace('assets/sober/','assets/albiz/');target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(z.read(n))
p=Path('src/main/resources/albiz-soberui.mixins.json');print('mixin exists',p.exists())
