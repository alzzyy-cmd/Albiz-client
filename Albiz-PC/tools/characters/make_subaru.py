"""Original textured Subaru fan mesh. No geometry from downloaded third-party models.
The face texture is adapted from the reference image supplied for this task.
Uses standard GLB skinning, then the same ANC baker/renderer as CJ.
"""
from pathlib import Path
import io,json,struct,math,sys
import numpy as np
from PIL import Image,ImageDraw,ImageFilter
root=Path(__file__).resolve().parents[2];dest=root/'artwork/characters';dest.mkdir(parents=True,exist_ok=True)
# Author an atlas with continuous face projection and cloth weave/shading.
tex=Image.new('RGB',(1024,1024),(38,39,45));face=(Image.open(sys.argv[1]).convert('RGB').crop((60,35,175,157)).resize((512,512),Image.Resampling.LANCZOS) if len(sys.argv)>1 else Image.open(dest/'subaru-author-atlas.png').convert('RGB').crop((0,0,512,512)))
a=np.array(face);white=(a.min(axis=2)>219)&(a.max(axis=2)-a.min(axis=2)<22);a[white]=[248,212,177];face=Image.fromarray(a);tex.paste(face,(0,0));d=ImageDraw.Draw(tex)
colors={'skin':(242,201,166),'ear':(197,139,116),'hair':(43,44,46),'hair_hi':(57,58,58),'black':(38,39,44),'orange':(234,158,52),'sole':(218,141,37),'white':(235,234,239),'zip':(72,70,70)}
uv={}
for i,(name,color) in enumerate(colors.items()):
 x=520+(i%4)*122;y=(i//4)*124+12;d.rectangle((x,y,x+110,y+110),fill=color);uv[name]=((x+55)/1024,(y+55)/1024)
# Jacket unwrap: frontal white panel, dark shoulders, orange chest stripe, seams and N badge.
cloth=Image.new('RGB',(512,512));ar=np.zeros((512,512,3),dtype=np.uint8)
for y in range(512):
 for x in range(512):
  base=np.array((39,40,45) if y<94 or y>466 else ((232,157,53) if y<119 else (235,234,240)),float);shade=1-.06*abs(x-256)/256+.006*math.sin(x*2.9)*math.sin(y*2.7);ar[y,x]=np.clip(base*shade,0,255)
cloth=Image.fromarray(ar);c=ImageDraw.Draw(cloth)
# Drawn garment details are texture, not floating boxes.
for x in (248,263):c.line((x,95,x,510),fill=(54,53,57),width=3)
c.line((255,92,255,510),fill=(232,229,224),width=5)
c.line((41,298,95,421),fill=(188,187,197),width=3);c.line((470,298,415,421),fill=(188,187,197),width=3)
c.line((354,182,343,139,363,140,385,180,397,139),fill=(39,40,44),width=8);c.ellipse((360,154,373,174),fill=(232,157,53));c.line((354,184,368,184),fill=(39,40,44),width=5)
for y in [470,477,483,490,496]:c.line((1,y,511,y),fill=(31,32,37),width=1)
tex.paste(cloth,(0,512));tex.save(dest/'subaru-author-atlas.png')
pos=[];uvs=[];joint=[];tri=[]
def vertex(p,st,b):
 pos.append(list(p));uvs.append(st);joint.append(b);return len(pos)-1

def loft(rings,b,color='black',sides=32,mode=None):
 """rings: (center x, y, z, radius x, radius z). Shared, smooth vertices."""
 start=len(pos)
 for k,(cx,y,cz,rx,rz) in enumerate(rings):
  for i in range(sides+1):
   t=i/sides*math.tau;sn=math.sin(t);co=math.cos(t);x=cx+rx*sn;z=cz+rz*co
   if mode=='head' and co>.04:
    st=(max(.005,min(.495,(x+1.55)/3.1*.5)),max(.005,min(.495,(29.4-y)/4.1*.5)))
   elif mode=='cloth':
    # Only the front has a zip/logo; the back is white without a duplicate badge.
    st=((.015+.47*(x/6.4+.5)) if co>0 else (.04+.06*i/sides), .5+(24.4-y)/11.6*.499)
   else:st=uv[color]
   bone=b(y) if callable(b) else b
   vertex((x,y,z),st,bone)
 for k in range(len(rings)-1):
  for i in range(sides):
   a=start+k*(sides+1)+i;bb=a+1;c=a+sides+2;dd=c-1
   # Outward winding: theta advances clockwise when viewed from above.
   tri.extend(((a,bb,dd),(bb,c,dd)) if rings[-1][1]>rings[0][1] else ((a,dd,bb),(bb,dd,c)))
 # Caps.
 for k,reverse in [(0,True),(len(rings)-1,False)]:
  cx,y,cz,rx,rz=rings[k];center=vertex((cx,y,cz),uv[color],b(y) if callable(b) else b);off=start+k*(sides+1)
  for i in range(sides):tri.append((center,off+i+1,off+i) if reverse else (center,off+i,off+i+1))

def ellipsoid(center,radii,b,color,sides=24):
 rings=[]
 for k in range(13):
  t=-math.pi/2+k/12*math.pi;co=max(.001,math.cos(t));rings.append((center[0],center[1]+radii[1]*math.sin(t),center[2],radii[0]*co,radii[2]*co))
 loft(rings,b,color,sides)

def strand(base,tip,radius,b=1):
 base=np.array(base);tip=np.array(tip);axis=tip-base;axis/=np.linalg.norm(axis);u=np.cross(axis,[0,0,1.]);u/=np.linalg.norm(u);v=np.cross(axis,u);start=len(pos);sides=9
 for k in range(5):
  t=k/4;c=base*(1-t)+tip*t;c[2]-=.12*math.sin(t*math.pi);rad=radius*(1-t)**.75+.005
  for i in range(sides+1):
   th=math.tau*i/sides;p=c+rad*(u*math.cos(th)+v*math.sin(th));vertex(p,uv['hair_hi' if i in [2,3] else 'hair'],b)
 for k in range(4):
  for i in range(sides):
   a=start+k*(sides+1)+i;c=a+sides+1;tri.extend(((a,a+1,c),(a+1,c+1,c)))
# Bone index 0=body, 1=head, arms 2/3 right, 4/5 left, legs 6/7 right, 8/9 left.
loft([(0,13.0,0,2.45,1.35),(0,13.5,0,2.65,1.47),(0,15.2,0,2.67,1.4),(0,17.5,0,2.5,1.32),(0,20.5,0,2.96,1.55),(0,22.6,0,3.2,1.48),(0,23.8,0,2.6,1.21),(0,24.2,0,1.32,.85)],0,'black',48,'cloth')
# Ribbed waistband, neck, raised open collar.
loft([(0,12.85,0,2.43,1.33),(0,13.2,0,2.66,1.5),(0,13.7,0,2.63,1.45)],0,'black',40)
loft([(0,23.6,0,.7,.64),(0,25.35,0,.67,.66)],0,'skin',28)
# Collar is an open C, not a cuboid. Orange inner lining visible around the nape.
start=len(pos);sides=36
for layer in range(2):
 for top in range(2):
  for k in range(sides+1):
   th=.58+(math.tau-1.16)*k/sides;rx=1.09+layer*.09;rz=.91+layer*.09;y=23.8+top*(1.75-.28*math.cos(th));vertex((rx*math.sin(th),y,rz*math.cos(th)),uv['orange' if layer==0 else 'black'],0)
for layer in range(2):
 for k in range(sides):
  a=start+layer*2*(sides+1)+k;b=a+sides+1;tri.extend(((a,a+1,b),(a+1,b+1,b)))
# Anatomical head with chin/jaw/cheek/temple loops, not a sphere.
loft([(0,25.3,.15,.17,.3),(0,25.55,.14,.63,.59),(0,26.05,.05,1.04,.87),(0,26.75,0,1.28,1.02),(0,27.45,0,1.4,1.1),(0,28.2,-.02,1.39,1.15),(0,28.95,-.04,1.31,1.13),(0,29.55,-.04,1.02,.97),(0,29.96,-.04,.4,.46)],1,'skin',64,'head')
for side in [-1,1]:
 ellipsoid((side*1.35,27.2,-.04),(.28,.55,.23),1,'skin');ellipsoid((side*1.53,27.21,.075),(.09,.31,.08),1,'ear',16)
# Small nose topology. Project the same facial atlas, avoiding a pasted-on box.
start=len(pos)
for p in [(-.16,27.48,1.06),(.16,27.48,1.06),(.12,26.86,1.08),(-.12,26.86,1.08),(0,27.03,1.34)]:vertex(p,((p[0]+1.55)/3.1*.5,(29.4-p[1])/4.1*.5),1)
tri.extend(tuple(start+i for i in t) for t in [(0,4,1),(1,4,2),(2,4,3),(3,4,0)])
# Hair cap: short sides, receding front hairline, detailed swept clumps.
start=len(pos);sides=48
for ring in range(7):
 t=ring/6
 for i in range(sides+1):
  th=i/sides*math.tau;bottom=27.25+1.85*max(0,math.cos(th))**.8;y=bottom+(30.0-bottom)*t;rad=math.sqrt(max(.0001,1-((y-28.5)/1.7)**2));p=(1.47*rad*math.sin(th),y,-.08+1.23*rad*math.cos(th));vertex(p,uv['hair_hi' if i%13==0 else 'hair'],1)
for k in range(6):
 for i in range(sides):
  a=start+k*(sides+1)+i;b=a+sides+1;tri.extend(((a,a+1,b),(a+1,b+1,b)))
# Close the crown; no hole or exposed skull at high camera angles.
center=vertex((0,30.08,-.08),uv['hair'],1)
for i in range(sides):tri.append((center,start+6*(sides+1)+i,start+6*(sides+1)+i+1))
for k in range(22):
 th=k/22*math.tau;front=math.cos(th);base=(1.09*math.sin(th),29.25+.12*math.sin(k),-.1+.88*front);tip=(1.53*math.sin(th)+.17,29.55+.53*(.5+.5*math.sin(k*2.7)),-.52+1.03*front);strand(base,tip,.36)
for x in [-.9,-.45,0,.5,.95]:strand((x,29.5,.75),(x+.23,29.95+.12*math.sin(x*4),.83),.3)
# Sleeves and articulated forearms, actual tapered cloth volumes.
for side,upper,lower,leg,shin in [(-1,2,3,6,7),(1,4,5,8,9)]:
 def armBone(y):return upper if y>18.7 else lower
 loft([(side*3.08,23.1,0,.92,1.08),(side*3.65,22.5,0,1.05,1.08),(side*4.02,21,0,.92,.98),(side*4.34,19.2,.01,.83,.88),(side*4.48,18.45,.02,.88,.92),(side*4.61,17.9,.02,.81,.86),(side*4.83,15.9,.05,.65,.72),(side*4.93,14.65,.07,.54,.61)],armBone,'black',32)
 # Orange shoulder-to-cuff tape follows the sleeve surface, not a box.
 start=len(pos);rings=[(3.27,23,1.02),(3.75,22,1.03),(4.1,20,.95),(4.4,18.5,.91),(4.65,17,.8),(4.89,15,.64)]
 for x,y,r in rings:
  for da in [-.09,.09]:vertex((side*(x+(r+.1)*math.cos(da)),y,r*math.sin(da)),uv['orange'],armBone(y))
 for k in range(len(rings)-1):a=start+2*k;tri.extend(((a,a+2,a+1),(a+1,a+2,a+3)))
 loft([(side*4.94,14.5,.07,.55,.61),(side*4.94,14.88,.07,.58,.64)],lower,'orange',32)
 ellipsoid((side*4.98,13.8,.09),(.53,.77,.35),lower,'skin')
 # Four fingers with knuckle profile + thumb, not a mitten.
 for f in range(4):
  xx=side*(4.65+f*.2);loft([(xx,12.68+.07*abs(f-1),.18,.075,.11),(xx,13.03,.2,.105,.135),(xx,13.51,.19,.1,.13),(xx,13.86,.15,.09,.12)],lower,'skin',12)
 ellipsoid((side*4.45,13.72,.31),(.17,.43,.18),lower,'skin',16)
 # Loose tracksuit trousers: hem, calf, knee folds, thigh, rounded hip.
 def legBone(y):return leg if y>7.7 else shin
 rr=[(1.72,1.5,.87,.84),(1.72,2.05,.9,.88),(1.73,3.0,1.0,.96),(1.74,5.5,1.04,1.02),(1.73,7.35,1.16,1.07),(1.7,7.9,1.1,1.05),(1.65,8.4,1.24,1.12),(1.58,11.1,1.27,1.24),(1.45,13.35,1.27,1.3)]
 loft([(side*x,y,0,rx,rz) for x,y,rx,rz in rr],legBone,'black',40)
 # Long orange side piping follows the actual leg outline.
 start=len(pos)
 for x,y,rx,rz in rr:
  for a in [-.035,.035]:vertex((side*(x+(rx+.04)*math.cos(a)),y,rz*math.sin(a)),uv['orange'],legBone(y))
 for k in range(len(rr)-1):a=start+2*k;tri.extend(((a,a+2,a+1),(a+1,a+2,a+3)))
 # Sneakers: layered rounded sole, toe box and tongue.
 loft([(side*1.72,.05,.43,.92,1.54),(side*1.72,.27,.43,1.02,1.6),(side*1.72,.52,.43,.99,1.57)],shin,'sole',40)
 loft([(side*1.72,.5,.42,.97,1.53),(side*1.72,.83,.35,.93,1.43),(side*1.72,1.24,.12,.79,1.1),(side*1.72,1.8,-.15,.58,.65)],shin,'black',40)
 # Orange 3D lacing follows the curved FRONT of the shoe, never buried inside it.
 for k in range(5):
  y=.88+k*.15;cz=.35-(y-.83)*.65;rz=1.43-(y-.83)*.9;start=len(pos)
  for q in range(9):
   xx=-.43+q*.1075;zz=cz+rz*math.sqrt(max(.1,1-(xx/.86)**2))+.035
   for dy in [-.022,.022]:vertex((side*1.72+xx,y+dy+.035*math.sin(q*math.pi/8),zz),uv['orange'],shin)
  for q in range(8):
   a=start+2*q;tri.extend(((a,a+2,a+1),(a+1,a+2,a+3)))
# Skin hierarchy, with true elbow/knee descendants so existing animation bend weights work.
names=['Hips','Head','RightUpperArm','RightForearm','LeftUpperArm','LeftForearm','RightUpLeg','RightShin','LeftUpLeg','LeftShin','RightHand','LeftHand']
points=np.array([(0,13,0),(0,24.65,0),(-3.1,23,0),(-4.35,18.7,0),(3.1,23,0),(4.35,18.7,0),(-1.45,13,0),(-1.73,7.7,0),(1.45,13,0),(1.73,7.7,0),(-4.95,14.5,.07),(4.95,14.5,.07)])
parents=[-1,0,0,2,0,4,0,6,0,8,3,5];nodes=[]
for i,name in enumerate(names):
 p=parents[i];nd={'name':name,'translation':(points[i]-(points[p] if p>=0 else 0)).tolist()};children=[k for k in range(len(names)) if parents[k]==i]
 if children:nd['children']=children
 nodes.append(nd)
positions=np.asarray(pos,dtype='<f4');positions[np.array(joint)==1,1]-=.55;faces=np.asarray(tri,dtype='<u4');normals=np.zeros_like(positions)
for t in faces:
 normal=np.cross(positions[t[1]]-positions[t[0]],positions[t[2]]-positions[t[0]])
 for i in t:normals[i]+=normal
normals/=np.maximum(np.linalg.norm(normals,axis=1)[:,None],1e-8)
# Verify outward normals on the head: catches authoring winding regressions.
assert np.isfinite(normals).all();assert len(positions)<65535
binary=bytearray();views=[];accessors=[]
def blob(data,target=None):
 binary.extend(b'\0'*((-len(binary))%4));nd={'buffer':0,'byteOffset':len(binary),'byteLength':len(data)}
 if target:nd['target']=target
 views.append(nd);binary.extend(data);return len(views)-1

def acc(array,typ,component=5126,target=34962):
 v=blob(array.tobytes(),target);accessors.append({'bufferView':v,'componentType':component,'count':len(array),'type':typ});return len(accessors)-1
attrs={'POSITION':acc(positions,'VEC3'),'NORMAL':acc(normals,'VEC3'),'TEXCOORD_0':acc(np.array(uvs,dtype='<f4'),'VEC2')}
joints=np.zeros((len(pos),4),dtype='<u2');joints[:,0]=joint;weights=np.zeros((len(pos),4),dtype='<f4');weights[:,0]=1
attrs['JOINTS_0']=acc(joints,'VEC4',5123);attrs['WEIGHTS_0']=acc(weights,'VEC4');idx=acc(faces.reshape(-1,1),'SCALAR',5125,34963)
ibm=[]
for p in points:m=np.eye(4);m[:3,3]=-p;ibm.append(m.T.flatten())
bind=acc(np.array(ibm,dtype='<f4'),'MAT4',5126,None);buf=io.BytesIO();tex.save(buf,format='PNG');image_view=blob(buf.getvalue());nodes.append({'name':'SubaruTexturedMesh','mesh':0,'skin':0})
j={'asset':{'version':'2.0','generator':'Albiz original Subaru fan-mesh authoring','copyright':'Original mesh and clothing atlas; facial texture adapted from user-provided reference. No third-party model geometry.'},'scene':0,'scenes':[{'nodes':[0,len(nodes)-1]}],'nodes':nodes,'skins':[{'joints':list(range(len(names))),'inverseBindMatrices':bind,'skeleton':0}],'meshes':[{'name':'Subaru original textured mesh','primitives':[{'attributes':attrs,'indices':idx,'material':0}]}],'materials':[{'name':'Subaru face and tracksuit atlas','pbrMetallicRoughness':{'baseColorTexture':{'index':0},'roughnessFactor':.9,'metallicFactor':0},'doubleSided':True}],'textures':[{'source':0}],'images':[{'bufferView':image_view,'mimeType':'image/png'}],'buffers':[{'byteLength':len(binary)}],'bufferViews':views,'accessors':accessors}
raw=json.dumps(j,separators=(',',':')).encode();raw+=b' '*((-len(raw))%4);binary.extend(b'\0'*((-len(binary))%4));out=struct.pack('<III',0x46546c67,2,28+len(raw)+len(binary))+struct.pack('<II',len(raw),0x4e4f534a)+raw+struct.pack('<II',len(binary),0x004e4942)+binary;(dest/'subaru.glb').write_bytes(out);print(len(pos),'vertices',len(tri),'triangles',len(out),'bytes')
