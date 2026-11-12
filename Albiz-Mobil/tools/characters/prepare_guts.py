"""Prepare Gimora's CC BY-NC-SA 4.0 asset: retain Guts, PNG textures, relaxed arms.
Input is the publicly downloadable GLB, not a scraped non-downloadable viewer.
"""
import json,struct,io,sys
from pathlib import Path
import numpy as np
from PIL import Image
from scipy.spatial.transform import Rotation
p=Path(sys.argv[1]);data=p.read_bytes();n=struct.unpack_from('<I',data,12)[0];j=json.loads(data[20:20+n]);bin0=20+n+8;binary=bytearray(data[bin0:]);nodes=j['nodes']
# Puck and the separate display prop are not part of the playable body.
j['scenes'][0]['nodes']=[114,7,8,9,108]
nodes[113]['children']=[110]
# Replace the source's super-deformed head proportions with a smaller, longer head.
nodes[10]['scale']=[.58,.72,.62]
# Detached objects must be removed from traversal too (PosedMesh examines all meshes).
for idx in [4,111]:nodes[idx].pop('mesh',None);nodes[idx].pop('skin',None)
parents={c:i for i,n in enumerate(nodes) for c in n.get('children',[])}
def world(i):
 nd=nodes[i];m=np.eye(4);m[:3,:3]=Rotation.from_quat(nd.get('rotation',[0,0,0,1])).as_matrix()@np.diag(nd.get('scale',[1,1,1]));m[:3,3]=nd.get('translation',[0,0,0]);return world(parents[i])@m if i in parents else m
# Keep the creator's bind matrices. Rotate the posed joints, not the mesh vertices.
def point_down(i,child,side):
 w=world(i);v=world(child)[:3,3]-w[:3,3];v/=np.linalg.norm(v);target=np.array([side*.10,-1.,.015]);target/=np.linalg.norm(target)
 cross=np.cross(v,target);sn=np.linalg.norm(cross);cs=np.dot(v,target)
 if sn<1e-7:return
 delta=Rotation.from_rotvec(cross/sn*np.arctan2(sn,cs)).as_matrix();par=world(parents[i])[:3,:3];par/=np.linalg.norm(par,axis=0)
 old=Rotation.from_quat(nodes[i].get('rotation',[0,0,0,1])).as_matrix();nodes[i]['rotation']=Rotation.from_matrix(par.T@delta@par@old).as_quat().tolist()
for i,c,side in [(40,35,1),(35,32,1),(67,66,-1),(66,65,-1)]:point_down(i,c,side)
# Convert only the base-color images used by this client. Retain all source maps.
used_images={j['textures'][m['pbrMetallicRoughness']['baseColorTexture']['index']]['source'] for m in j['materials']}
for image_index,im in enumerate(j['images']):
 if image_index not in used_images:continue
 bv=j['bufferViews'][im['bufferView']];blob=binary[bv.get('byteOffset',0):bv.get('byteOffset',0)+bv['byteLength']];pic=Image.open(io.BytesIO(blob)).convert('RGBA');pic.thumbnail((1024,1024));buf=io.BytesIO();pic.save(buf,format='PNG');b=buf.getvalue()
 binary.extend(b'\0'*((-len(binary))%4));im['bufferView']=len(j['bufferViews']);j['bufferViews'].append({'buffer':0,'byteOffset':len(binary),'byteLength':len(b)});binary.extend(b);im['mimeType']='image/png'
# Convert triangle strips to indexed triangles for the existing ANCv3 baker.
for mesh in j['meshes']:
 for primitive in mesh['primitives']:
  if primitive.get('mode',4)!=5:continue
  a=j['accessors'][primitive['indices']];v=j['bufferViews'][a['bufferView']];off=v.get('byteOffset',0)+a.get('byteOffset',0);fmt={5123:'H',5125:'I',5121:'B'}[a['componentType']];indices=struct.unpack_from('<'+fmt*a['count'],binary,off);tri=[]
  for k in range(2,len(indices)):
   p,q,r=indices[k-2:k+1]
   if len({p,q,r})<3:continue
   tri.extend((q,p,r) if k%2 else (p,q,r))
  # Remove the source mesh's held sword; CS Knife's approved Guts Sword is separate.
  if mesh['name']=='003_003_0':
   def unpack_accessor(index):
    acc=j['accessors'][index];vw=j['bufferViews'][acc['bufferView']];dt={5121:np.uint8,5123:np.uint16,5125:np.uint32,5126:np.float32}[acc['componentType']];width={'SCALAR':1,'VEC2':2,'VEC3':3,'VEC4':4}[acc['type']];return np.ndarray((acc['count'],width),dtype=dt,buffer=binary,offset=vw.get('byteOffset',0)+acc.get('byteOffset',0),strides=(vw.get('byteStride',np.dtype(dt).itemsize*width),np.dtype(dt).itemsize)).copy()
   ids=unpack_accessor(primitive['attributes']['JOINTS_0']);wt=unpack_accessor(primitive['attributes']['WEIGHTS_0']);skin_joints=j['skins'][1]['joints'];sword_ids={k for k,node in enumerate(skin_joints) if nodes[node].get('name','').startswith('sword')};is_sword=np.isin(ids,list(sword_ids))*wt;mask=is_sword.sum(axis=1)>.4
   tris=np.asarray(tri).reshape(-1,3);tris=tris[~mask[tris].any(axis=1)];kept=np.unique(tris);newid={int(v):i for i,v in enumerate(kept)};tri=[newid[int(v)] for v in tris.flatten()]
   for key,index in list(primitive['attributes'].items()):
    acc=j['accessors'][index];ar=unpack_accessor(index)[kept];payload=ar.tobytes();binary.extend(b'\0'*((-len(binary))%4));vindex=len(j['bufferViews']);j['bufferViews'].append({'buffer':0,'byteOffset':len(binary),'byteLength':len(payload),'target':34962});binary.extend(payload);primitive['attributes'][key]=len(j['accessors']);outacc={k:v for k,v in acc.items() if k not in ['bufferView','byteOffset','min','max']};outacc.update(bufferView=vindex,count=len(kept));j['accessors'].append(outacc)
  raw_indices=struct.pack('<'+'I'*len(tri),*tri);binary.extend(b'\0'*((-len(binary))%4));view=len(j['bufferViews']);j['bufferViews'].append({'buffer':0,'byteOffset':len(binary),'byteLength':len(raw_indices),'target':34963});binary.extend(raw_indices);primitive['indices']=len(j['accessors']);j['accessors'].append({'bufferView':view,'componentType':5125,'count':len(tri),'type':'SCALAR'});primitive['mode']=4
j['asset']['copyright']='Berserk Guts Black Swordsman by Gimora; CC BY-NC-SA 4.0. Modified: detached props removed, relaxed arm pose, PNG conversion; see THIRD_PARTY_CHARACTERS.md.'
j['buffers'][0]['byteLength']=len(binary);raw=json.dumps(j,separators=(',',':')).encode();raw+=b' '*((-len(raw))%4);binary.extend(b'\0'*((-len(binary))%4));out=struct.pack('<III',0x46546c67,2,28+len(raw)+len(binary))+struct.pack('<II',len(raw),0x4e4f534a)+raw+struct.pack('<II',len(binary),0x004e4942)+binary
Path(sys.argv[2]).write_bytes(out);print('prepared',len(out),'bytes')
