import unittest,json,re
from pathlib import Path
R=Path(__file__).resolve().parents[1]/'src/main/resources/assets/albiz'
class RefreshAssets(unittest.TestCase):
 def test_totem_model_and_texture_closure(self):
  items=list((R/'items/totem').glob('*.json'));self.assertEqual(len(items),97)
  for p in items:
   doc=json.loads(p.read_text());model=doc['model']['model'];self.assertEqual(model.split(':')[0],'albiz')
   mp=R/'models'/(model.split(':')[1]+'.json');self.assertTrue(mp.is_file(),str(mp))
   for value in json.loads(mp.read_text()).get('textures',{}).values():
    if value.startswith('albiz:'):self.assertTrue((R/'textures'/(value[6:]+'.png')).is_file(),value)
 def test_sound_events_resolve_and_all_recordings_are_vorbis(self):
  sounds=json.loads((R/'sounds.json').read_text());keys={k:v for k,v in sounds.items() if k.startswith('keys.')};count=0
  for event,data in keys.items():
   for s in data['sounds']:
    path=R/'sounds'/(s['name'].split(':')[1]+'.ogg');self.assertTrue(path.is_file());b=path.read_bytes();self.assertEqual(b[:4],b'OggS');self.assertIn(b'vorbis',b[:200]);count+=1
  self.assertEqual(count,116)
  for p in ['aula','fb67','60he_v2','frost65']:
   for kind in ['press','release','space']:self.assertIn('keys.'+p+'.'+kind,keys)
 def test_shader_include_closure(self):
  for p in (R/'shaders').rglob('*'):
   if p.is_file():
    for include in re.findall(r'#moj_import\s+<albiz:([^>]+)>',p.read_text()):self.assertTrue((R/'shaders/include'/include).is_file(),include)
 def test_removed_modules_not_registered_and_mandatory_features_present(self):
  provider=(R.parents[4]/'src/main/java/net/albiz/client/module/BuiltinModuleProvider.java').read_text()
  for name in ['FloatingRelicsModule','TotemSpiritModule','HandAuraModule','ColorGradingModule','BlockEntityDebugModule','BetterSusChunkModule','SusChunkFinderModule']:self.assertNotIn('new '+name+'(',provider)
  self.assertIn('new ChunkScannerModule(',provider) # Restored by the new explicit 1.0 request.
  self.assertIn('new StorageEspModule(',provider) # Restored on the owner's request, 2026-11-12.
  for name in ['SpawnerBeam','CustomTotemsModule','CreamyKeysModule','StaffHudModule','SkyModule','HandModelModule','HandsModule','HitParticlesModule','WeaponInspectModule']:self.assertIn('new '+name+'(' if name in ['HandModelModule','HandsModule','HitParticlesModule','WeaponInspectModule'] else name+'(',provider)
if __name__=='__main__':unittest.main()
