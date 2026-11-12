import unittest,json,hashlib
from pathlib import Path
R=Path(__file__).resolve().parents[1];J=R/'src/main/java/net/albiz/client';U=J/'port/soberui';A=R/'src/main/resources/assets/albiz'
class GuiRestore(unittest.TestCase):
 def test_old_gui_is_not_replaced_but_menu_multiplayer_and_hud_remain(self):
  mix=(J/'mixin/MixinMinecraft.java').read_text();self.assertNotIn('SoberUi.menu()',mix);self.assertIn('SoberUi.title()',mix);self.assertIn('MultiplayerGridScreen.replace',mix)
  self.assertIn('SoberUi::render',(J/'AlbizClientMod.java').read_text())
 def test_our_dusk_texture_not_animated_space_shader(self):
  s=(U/'gui/SoberScreen.java').read_text().split('public static void paintSpace(',1)[1].split('public void method_25420',1)[0];self.assertIn('DuskBackdrop.draw',s);self.assertNotIn('CleanBackdrop.draw',s);self.assertIn('148.0f * this.s',(U/'gui/SoberScreen.java').read_text());self.assertTrue((A/'textures/gui/dusk_backdrop.png').is_file())
 def test_item_visuals_registered_before_canonical_config_load(self):
  s=(J/'AlbizClientMod.java').read_text();self.assertLess(s.index('SoberUi.installVisuals()'),s.index('ModuleConfigStore.getInstance().load()'))
  src=(U/'SoberUi.java').read_text();self.assertIn('"custom-item-models"',src);self.assertIn('"cs-pickaxe"',src)
 def test_bounded_previews_and_no_secret_input_changes(self):
  s=(U/'gui/VisualPresetsScreen.java').read_text();self.assertIn('int first=',s);self.assertIn('first+((h-74)/cell+2)*cols',s);self.assertIn('method_1507(parent)',s)
  cache=(U/'model/ModelIcon.java').read_text();self.assertIn('CACHE.size()>64',cache);self.assertIn('16_666_667L',cache);self.assertIn('method_4615(old.getValue())',cache)
  base=json.loads((R/'docs/PROTECTED_BASELINE.json').read_text())
  for name,h in base.items():self.assertEqual(hashlib.sha256((R/name).read_bytes()).hexdigest(),h,name)
 def test_totem_visual_button_and_our_new_model_assets(self):
  self.assertIn('VisualPresetsScreen',(J/'module/impl/render/CustomTotemsModule.java').read_text());self.assertTrue((A/'models/fortnite_pack/items.txt').is_file());self.assertTrue((A/'textures/models/white.png').is_file())
 def test_hud_editor_uses_current_hud_backend_not_old_layout(self):
  s=(J/'gui/ClickGuiRenderer.java').read_text().split('private void renderHudStudio(',1)[1].split('private static boolean hudStudioBlocked()',1)[0];self.assertIn('nativeHudControls',s);self.assertNotIn('studioInput',s);self.assertIn('modules.Hud.class',s)
if __name__=='__main__':unittest.main()
