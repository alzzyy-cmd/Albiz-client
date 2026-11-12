import json,re,unittest
from pathlib import Path
R=Path(__file__).resolve().parents[1];J=R/'src/main/java/net/albiz/client';U=J/'port/soberui'
class Bugs(unittest.TestCase):
 def test_no_decompiler_stubs_or_locale_sensitive_identifiers(self):
  for p in U.rglob('*.java'):
   text=p.read_text();self.assertNotIn('Decompilation failed',text,str(p));self.assertNotRegex(text,r'\.to(?:Lower|Upper)Case\(\)',str(p))
 def test_all_main_menu_styles_have_albiz_branding(self):
  for name in ['HomeScreen','VanillaPlusScreen','SoberScreen','MenuScreen']:
   text=(U/'gui'/f'{name}.java').read_text()
   for literal in re.findall(r'"([^"\n]*)"',text):self.assertNotRegex(literal,r'SOBERCLIENT|SOBER CLIENT|SoberClient|discord.gg/soberclient',name)
 def test_settings_and_multiplayer_failure_state(self):
  text=(J/'gui/ClickGuiRenderer.java').read_text();self.assertIn('renderNativeMenuOptions(pad,contentW)',text);self.assertIn('menu.style.set',text)
  screen=(U/'gui/MultiplayerGridScreen.java').read_text();self.assertIn('if(renderFailed){if(key==256)',screen);self.assertGreaterEqual(screen.count('finally{matrices.popMatrix();}'),3);self.assertIn('finally{context.method_44380();}',screen)
 def test_language_switch_and_branding_do_not_rename_stored_style(self):
  text=(U/'modules/MainMenu.java').read_text();self.assertIn('Sober("Albız")',text)
  en=json.loads((R/'src/main/resources/assets/albiz/lang/en_us.json').read_text());tr=json.loads((R/'src/main/resources/assets/albiz/lang/tr_tr.json').read_text())
  for phrase in ['Menu Style','Server List','IN MENU','QUICK JOIN','Choose the Albız title screen: Albız, Vanilla+, Home, Island or Classic. Changes apply next time you return to the title screen.']:
   keys=[k for k,v in en.items() if v==phrase];self.assertTrue(keys,phrase);self.assertTrue(all(tr[k]!=phrase for k in keys),phrase)
if __name__=='__main__':unittest.main()
