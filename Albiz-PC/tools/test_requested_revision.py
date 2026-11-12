import unittest,json,zipfile
from pathlib import Path
R=Path(__file__).resolve().parents[1];J=R/'src/main/java/net/albiz/client';RES=R/'src/main/resources'
class RequestedRevision(unittest.TestCase):
 def test_abyz_has_real_scan_consumer(self):
  s=(J/'module/impl/render/AbyzDebugModule.java').read_text();self.assertIn('implements ChunkScanner.Consumer',s);self.assertIn('ChunkScanner.register(this)',s);self.assertIn('class_2767',s);self.assertIn('workingCrafter',s)
 def test_not_chat_editor(self):
  s=(J/'port/soberui/mixin/ChatScreenMixin.java').read_text();self.assertNotIn('@Inject',s)
  s=(J/'gui/HudEditorScreen.java').read_text();self.assertIn('method_25406',s);self.assertIn('editorRelease()',s);self.assertNotIn('ImGuiManager',s)
 def test_real_logo_baked_not_fake_label(self):
  self.assertTrue((RES/'assets/albiz/textures/brand/discovery_mark.png').is_file());s=(J/'util/LogoMarker.java').read_text();self.assertIn('AlbizLogoGeometry.segments()',s)
 def test_first_run_flags_local_and_gate_saved(self):
  for flag in ['tr','gb']:self.assertTrue((RES/f'assets/albiz/textures/flags/{flag}.png').is_file())
  self.assertIn('LanguageChoiceFile.write',(J/'i18n/LanguageChoice.java').read_text())
 def test_audio_event_streams_actual_asset(self):
  d=json.loads((RES/'assets/albiz/sounds.json').read_text())['chunk_alert']['sounds'][0];self.assertTrue(d['stream']);self.assertEqual(d['name'],'albiz:alerts/chunk_found');self.assertEqual((RES/'assets/albiz/sounds/alerts/chunk_found.ogg').read_bytes()[:4],b'OggS')
 def test_no_duplicate_nested_jars_in_source_metadata(self):
  self.assertNotIn('jars',json.loads((RES/'fabric.mod.json').read_text()))
 def test_v6_plate_geometry_is_chunk_square(self):
  s=(R/'larp-addon/src/main/java/net/albizlarp/LarpV6.java').read_text();self.assertIn('dx=-4;dx<4',s);self.assertIn('dz=-4;dz<4',s);self.assertIn('(s.x()+dx)*16',s);self.assertIn('new BooleanSetting("Player Render Distance Plates",false)',s)
 def test_v5_no_old_simplistic_score_only(self):
  s=(R/'larp-addon/src/main/java/net/albizlarp/LarpV5.java').read_text()
  for item in ['PacketAppliedEvent','PacketSendEvent','class_2649','class_2672','synergized','sections()']:self.assertIn(item,s)
if __name__=='__main__':unittest.main()
