"""Recompute smooth, area-weighted normals from each ANC LOD's own triangles.
The downloaded source had face normals rotated relative to its geometry.
Only the signed-byte normal field is changed; topology, bounds, UVs and rig remain intact.
"""
from pathlib import Path
import struct,json,sys
import numpy as np
root=Path(__file__).resolve().parents[2];name=sys.argv[1];path=root/f'src/main/resources/assets/albiz/characters/{name}.anc';data=bytearray(path.read_bytes());at=0

def read(fmt):
 global at
 out=struct.unpack_from('>'+fmt,data,at);at+=struct.calcsize('>'+fmt);return out[0] if len(out)==1 else out

def smooth(pos,tri):
 n=np.zeros_like(pos);faces=np.cross(pos[tri[:,1]]-pos[tri[:,0]],pos[tri[:,2]]-pos[tri[:,0]])
 for i in range(3):np.add.at(n,tri[:,i],faces)
 n/=np.maximum(np.linalg.norm(n,axis=1)[:,None],1e-10);return n
assert read('I')==1095648072 and read('I')==3
read('4f');first,colored=read('2?');levels,parts=read('2I')
for part in range(parts):
 read('b');read('6f');bounds=np.array(read('6f'));read('2f');jointed=read('?')
 if jointed:read('3f')
 for lod in range(levels):
  for layer in range(2):
   verts=read('I');p=np.frombuffer(data,dtype='>u2',count=verts*3,offset=at).reshape(-1,3).astype(float)/65535*(bounds[3:]-bounds[:3])+bounds[:3];at+=verts*6;normal_at=at;at+=verts*3;at+=verts*4+(verts*3 if colored else 0)+(verts if jointed else 0);count=read('I');dtype='>u4' if verts>65535 else '>u2';tri=np.frombuffer(data,dtype=dtype,count=count*3,offset=at).reshape(-1,3).copy();at+=count*3*(4 if verts>65535 else 2)
   if verts and count:data[normal_at:normal_at+verts*3]=np.rint(smooth(p,tri)*127).astype('int8').tobytes()
path.write_bytes(data)
preview=root/f'artwork/characters/{name}-preview.json';j=json.loads(preview.read_text())
for s in j['surfaces']:
 p=np.array(s['positions']).reshape(-1,3);q=np.array(s['quads']).reshape(-1,4);t=np.concatenate((q[:,[0,1,2]],q[:,[0,2,3]]));t=t[(t[:,0]!=t[:,1])&(t[:,1]!=t[:,2])&(t[:,0]!=t[:,2])];s['normals']=smooth(p,t).reshape(-1).tolist()
preview.write_text(json.dumps(j,separators=(',',':')));print('repaired all LOD normals:',name)
