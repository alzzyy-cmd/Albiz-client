"""CPU rasterization of the ACTUAL loaded ANC geometry/UV atlas, no concept art."""
import json,sys,math
from pathlib import Path
import numpy as np
from PIL import Image,ImageDraw
root=Path(__file__).resolve().parents[2];name=sys.argv[1];yaw=float(sys.argv[2]) if len(sys.argv)>2 else 0
j=json.loads((root/f'artwork/characters/{name}-preview.json').read_text());tex=np.asarray(Image.open(root/f'artwork/characters/{name}-atlas.png').convert('RGBA'))
w,h=620,1000;output=np.empty((h,w,3),np.uint8)
for y in range(h):output[y]=[int(20+7*y/h),int(23+8*y/h),int(31+11*y/h)]
zbuf=np.full((h,w),-1e9);ang=math.radians(yaw);rot=np.array([[math.cos(ang),0,math.sin(ang)],[0,1,0],[-math.sin(ang),0,math.cos(ang)]])
light=np.array([-.4,.65,1]);light/=np.linalg.norm(light);scale=28
for s in j['surfaces']:
 p=(np.array(s['positions']).reshape(-1,3)+s['pivot'])@rot.T;n=np.array(s['normals']).reshape(-1,3)@rot.T;uv=np.array(s['uv']).reshape(-1,2);screen=p.copy();screen[:,0]=p[:,0]*scale+w/2;screen[:,1]=(31-p[:,1])*scale+35
 for q in np.array(s['quads']).reshape(-1,4):
  for tri in [(q[0],q[1],q[2]),(q[0],q[2],q[3])]:
   if len(set(tri))<3:continue
   a,b,c=screen[list(tri)];x0=max(0,int(min(a[0],b[0],c[0])));x1=min(w-1,int(max(a[0],b[0],c[0]))+1);y0=max(0,int(min(a[1],b[1],c[1])));y1=min(h-1,int(max(a[1],b[1],c[1]))+1)
   if x1<x0 or y1<y0:continue
   den=(b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1])
   if abs(den)<1e-6:continue
   Y,X=np.mgrid[y0:y1+1,x0:x1+1];X=X+.5;Y=Y+.5
   u=((b[1]-c[1])*(X-c[0])+(c[0]-b[0])*(Y-c[1]))/den;v=((c[1]-a[1])*(X-c[0])+(a[0]-c[0])*(Y-c[1]))/den;t=1-u-v
   depth=u*a[2]+v*b[2]+t*c[2];mask=(u>=0)&(v>=0)&(t>=0)&(depth>zbuf[y0:y1+1,x0:x1+1])
   if not mask.any():continue
   uv0,uv1,uv2=uv[list(tri)];U=u*uv0[0]+v*uv1[0]+t*uv2[0];V=u*uv0[1]+v*uv1[1]+t*uv2[1]
   color=tex[np.clip((V*(tex.shape[0]-1)).astype(int),0,tex.shape[0]-1),np.clip((U*(tex.shape[1]-1)).astype(int),0,tex.shape[1]-1)]
   mask&=color[:,:,3]>100
   na,nb,nc=n[list(tri)];nx=u*na[0]+v*nb[0]+t*nc[0];ny=u*na[1]+v*nb[1]+t*nc[1];nz=u*na[2]+v*nb[2]+t*nc[2];norm=np.sqrt(nx*nx+ny*ny+nz*nz)+1e-9;shade=.52+.48*np.maximum(0,(nx*light[0]+ny*light[1]+nz*light[2])/norm)
   patch=output[y0:y1+1,x0:x1+1];patch[mask]=np.clip(color[:,:,:3]*shade[:,:,None],0,255).astype(np.uint8)[mask];zbuf[y0:y1+1,x0:x1+1][mask]=depth[mask]
im=Image.fromarray(output);d=ImageDraw.Draw(im);d.text((22,20),f'ALBIZ / {name.upper()} / actual ANC mesh / {j["triangles"]:,} triangles / {yaw:g} deg',fill=(236,233,225));dest=root/f'artwork/characters/{name}-{int(yaw)}.png';im.save(dest);print(dest)
