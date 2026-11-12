import base64,unittest
from read_capture import changes
class DecoderTest(unittest.TestCase):
 def row(self,encoding,data,count):
  return dict(event='section_update_batch',section_x=-1,section_y=-2,section_z=1,palette=[dict(block_id='minecraft:chest',encoding=encoding,cells_b64=base64.b64encode(data).decode(),count=count)])
 def test_sparse_negative_section(self):
  self.assertEqual([(-1,-17,31,'minecraft:chest')],list(changes(self.row('u16le',bytes([255,15]),1))))
 def test_bitmap(self):
  data=bytearray(512);data[0]=1;data[511]=128
  self.assertEqual([(-16,-32,16,'minecraft:chest'),(-1,-17,31,'minecraft:chest')],list(changes(self.row('bitmap-le',data,2))))
 def test_invalid_count(self):
  with self.assertRaises(ValueError):list(changes(self.row('u16le',bytes([0,0]),2)))
 def test_v1_compatible(self):
  self.assertEqual([(1,2,3,'minecraft:stone')],list(changes(dict(event='block_update',x=1,y=2,z=3,block_id='minecraft:stone'))))
if __name__=='__main__':unittest.main()
