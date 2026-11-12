"""Static integration/resource regressions. These are NOT Minecraft/GL screenshot tests."""
import unittest,json,re,hashlib
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];SRC=ROOT/'src/main/java/net/albiz/client';UI=SRC/'port/soberui';RES=ROOT/'src/main/resources';AS=RES/'assets/albiz'
class NativeUiPort(unittest.TestCase):
 def test_native_mixins_are_registered(self):
  mod=json.loads((RES/'fabric.mod.json').read_text());self.assertIn('albiz.soberui.mixins.json',mod['mixins'])
  config=json.loads((RES/'albiz.soberui.mixins.json').read_text())
  for name in config['client']:self.assertTrue((UI/'mixin'/f'{name}.java').is_file(),name)
  self.assertIn('ChatScreenMixin',config['client']);self.assertIn('ScreenMixin',config['client'])
  self.assertNotIn('MixinDuskBackdropScreen',json.loads((RES/'albiz.mixins.json').read_text())['client'])
 def test_global_route_and_no_render_error_vanilla_loop(self):
  mix=(SRC/'mixin/MixinMinecraft.java').read_text();self.assertIn('MultiplayerGridScreen.replace',mix);self.assertIn('SoberUi.title()',mix);self.assertNotIn('screen.getClass()==net.albiz.client.gui.ImGuiScreen.class',mix)
  render=(UI/'gui/MultiplayerGridScreen.java').read_text().split('public void method_25394(',1)[1].split('private void paint(',1)[0]
  self.assertNotIn('new class_500',render);self.assertIn('renderFailed=true',render)
 def test_native_totem_thumbnail_component_and_bridge(self):
  totem=(UI/'modules/CustomTotems.java').read_text();self.assertIn('presetIcon(int index)',totem);self.assertIn('class_9334.field_54199',totem);self.assertIn('"albiz"',totem)
  view=(UI/'TotemView.java').read_text();self.assertIn('actual.set(presetName(index))',view);self.assertIn('actual.serialize()',view)
  pane=(UI/'gui/SettingsPane.java').read_text();self.assertIn('presetIcon',pane)
 def test_item_block_picker_live_canonical_adapters(self):
  bridge=(UI/'NativeSelections.java').read_text();self.assertIn('s.notifyChanged()',bridge);self.assertIn('s.enabledBlocks()',bridge)
  pane=(UI/'gui/SettingsPane.java').read_text();self.assertIn('new ItemPickerScreen',pane);self.assertIn('new BlockPickerScreen',pane);self.assertIn('action.run()',pane);dropdown=(UI/'gui/DropdownScreen.java').read_text();self.assertIn('new ItemPickerScreen',dropdown);self.assertIn('action.run()',dropdown)
  self.assertIn('field_41178',(UI/'gui/ItemPickerScreen.java').read_text());self.assertIn('TargetCard.render(context,tick.method_60637(true))',(UI/'SoberUi.java').read_text())
 def test_no_startup_proxy_overwrite_and_secret_export(self):
  text=(UI/'SoberClient.java').read_text();self.assertIn('m->m instanceof AlbizModuleView || m instanceof TotemView',text);self.assertIn('if(!secret(m,setting))s.add',text);self.assertIn('if(!secret(m,setting)&&s.has',text)
 def test_native_font_texture_resources(self):
  for path in ['textures/brand/lockup.png','textures/gui/brand.png','textures/gui/logo_128.png','font/montserrat-bold.ttf','shaders/icon_sdf.fsh','shaders/sdf_text.fsh','licenses/SOBER-FONT-LICENSES.txt']:
   self.assertTrue((AS/path).is_file(),path)
  for p in (AS/'font').glob('*.json'):
   for provider in json.loads(p.read_text()).get('providers',[]):
    name=provider.get('file','')
    if name.startswith('albiz:'):self.assertTrue((AS/'font'/name[6:]).is_file(),str(p)+':'+name)
 def test_accounts_credits_and_protected_reset(self):
  screen=(UI/'gui/SoberScreen.java').read_text();self.assertIn('"Accounts"',screen);self.assertIn('"Credits"',screen)
  popup=(SRC/'gui/mainmenu/CreditsPopup.java').read_text()
  for name in ['Alzzyy','Luvzi','Rosy']:self.assertIn(name,popup)
  reset=(UI/'ResetAll.java').read_text();self.assertIn('Files.copy',reset);self.assertIn('!(module instanceof AlbizModuleView)',reset)
if __name__=='__main__':unittest.main()
