import unittest
from pathlib import Path
R=Path(__file__).resolve().parents[1];J=R/'src/main/java/net/albiz/client'
class FriendsRevision(unittest.TestCase):
 def test_manual_staff_only(self):
  s=(J/'port/soberui/util/StaffList.java').read_text();online=s[s.index('public static List<Member> online'):s.index('public static void tick')];self.assertNotIn('learned.add',online);self.assertNotIn('matcher.match',online);self.assertIn('known.contains',online)
 def test_second_staff_manual(self):
  s=(J/'module/impl/misc/StaffHudModule.java').read_text();self.assertNotIn('matcher.match',s);self.assertIn('manualNames(names.get())',s)
 def test_friend_labels_local(self):
  s=(J/'module/impl/render/FriendLabelsModule.java').read_text();self.assertIn('FriendStore.getInstance().isFriend',s);self.assertIn('AlbizI18n.isTurkish()',s);self.assertIn('Show Friend Labels',s)
  for network in ['HttpClient','PacketSend','sendChatMessage','Socket']:self.assertNotIn(network,s)
 def test_cosmetics_local(self):self.assertIn(': false;', (J/'module/impl/render/HeadCosmeticModule.java').read_text())
 def test_distinct_styles(self):
  s=(J/'module/impl/render/SteppeMesh.java').read_text();self.assertIn('switch(style)',s);self.assertIn('AlbizLogoGeometry.segments()',s);self.assertIn('if(style>=5&&style!=13)',s)
 def test_manual_howl_controls(self):
  s=(J/'module/impl/client/SoundFeedbackModule.java').read_text();self.assertIn('new KeySetting("Howl Key",-1)',s);self.assertIn('new ActionSetting("Play Wolf Howl"',s);self.assertIn('8_000_000_000L',s)
 def test_abyz_received_evidence(self):
  s=(J/'module/impl/render/AbyzDebugModule.java').read_text();self.assertIn('ReceivedActivityRules.transition',s);self.assertIn('ReceivedActivityRules.spawner',s);self.assertIn('SoundFeedbackModule.abyzAlert()',s)
 def test_removed_modules(self):
  s=(J/'module/BuiltinModuleProvider.java').read_text();self.assertNotIn('new MediaBaseFinderModule()',s);self.assertIn('if (!net.albiz.client.platform.PlatformInfo.isMobile()) modules.add(new SpotifyHudModule())',s)
 def test_logo_red(self):self.assertIn('c=Color.fromPacked(0xFFE3262E)',(J/'util/LogoMarker.java').read_text())
if __name__=='__main__':unittest.main()
