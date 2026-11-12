"""Repaint the baked head with the stern, hand-painted reference-guided face.
Projects onto the actual curved head/eye geometry, keeping armor, hair and UVs.
Only the ANC atlas payload is replaced; geometry/LODs/rig remain identical.
"""
from pathlib import Path
import json,sys,struct,zlib
import numpy as np
from PIL import Image,ImageFilter
root=Path(__file__).resolve().parents[2];folder=root/'artwork/characters'
ref=Image.open(folder/'guts-face-paint.png').convert('RGB')
ref=np.asarray(ref);tex=np.array(Image.open(folder/'guts-atlas.png').convert('RGBA'));h,w=tex.shape[:2];j=json.loads((folder/'guts-preview.json').read_text());depth=np.full((h,w),-1e9)
for s in j['surfaces']:
 if s['bone']!='HEAD':continue
 p=np.array(s['positions']).reshape(-1,3)+s['pivot'];norm=np.array(s['normals']).reshape(-1,3);uv=np.array(s['uv']).reshape(-1,2)*[w-1,h-1]
 for q in np.asarray(s['quads']).reshape(-1,4):
  for ids in [(q[0],q[1],q[2]),(q[0],q[2],q[3])]:
   if len(set(ids))<3:continue
   coords=p[list(ids)]
   if coords[:,1].min()>26.4 or coords[:,1].max()<22.65:continue
   a,b,c=uv[list(ids)];x0=max(0,int(min(a[0],b[0],c[0])));x1=min(w-1,int(max(a[0],b[0],c[0]))+1);y0=max(0,int(min(a[1],b[1],c[1])));y1=min(h-1,int(max(a[1],b[1],c[1]))+1)
   if x1<x0 or y1<y0:continue
   den=(b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1])
   if abs(den)<1e-7:continue
   Y,X=np.mgrid[y0:y1+1,x0:x1+1];u=((b[1]-c[1])*(X-c[0])+(c[0]-b[0])*(Y-c[1]))/den;v=((c[1]-a[1])*(X-c[0])+(a[0]-c[0])*(Y-c[1]))/den;t=1-u-v;P=u[:,:,None]*coords[0]+v[:,:,None]*coords[1]+t[:,:,None]*coords[2];px,py,pz=P.transpose(2,0,1)
   mask=(u>=-.005)&(v>=-.005)&(t>=-.005)&(pz>.4)&(py>22.65)&(py<26.35)&(np.abs(px-.55)<1.65)&(pz>depth[y0:y1+1,x0:x1+1])
   U=(px-(-1.1))/3.3;V=(26.35-py)/3.7
   color=ref[np.clip((V*(ref.shape[0]-1)).astype(int),0,ref.shape[0]-1),np.clip((U*(ref.shape[1]-1)).astype(int),0,ref.shape[1]-1)]
   patch=tex[y0:y1+1,x0:x1+1];is_hair=(X>390)&(X<930)&(Y>520)&(Y<1100)&(patch[:,:,:3].max(axis=2)<115);mask&=~is_hair;patch[:,:,:3][mask]=color[mask];depth[y0:y1+1,x0:x1+1][mask]=pz[mask]
Image.fromarray(tex).save(folder/'guts-atlas.png')
# ANCv3 stores zlib-compressed, per-channel horizontal-delta ARGB bytes.
argb=tex[:,:,[3,0,1,2]].astype(np.int16);filtered=argb.copy();filtered[:,1:]-=argb[:,:-1];packed=zlib.compress((filtered%256).astype(np.uint8).tobytes(),9)
p=root/'src/main/resources/assets/albiz/characters/guts.anc';data=p.read_bytes();needle=struct.pack('>II',w,h);at=data.rfind(needle);assert at>=0;old=struct.unpack_from('>I',data,at+8)[0];assert at+12+old==len(data);p.write_bytes(data[:at]+needle+struct.pack('>I',len(packed))+packed);print('repainted Guts atlas; ANC',p.stat().st_size)
