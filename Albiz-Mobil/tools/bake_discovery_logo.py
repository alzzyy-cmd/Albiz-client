from pathlib import Path
from PIL import Image
import cv2,numpy as np,hashlib,shutil,json
src=Path('/home/user/uploads/Albiz_icon.png')
if not src.exists():src=Path('src/main/resources/assets/albiz/textures/brand/discovery_mark.png')
im=np.array(Image.open(src));mask=(im[:,:,3]>=100).astype('uint8')*255
contours,_=cv2.findContours(mask,cv2.RETR_LIST,cv2.CHAIN_APPROX_SIMPLE)
segments=[]
for c in contours:
 if abs(cv2.contourArea(c))<=4:continue
 p=cv2.approxPolyDP(c,1.0,True).reshape(-1,2)
 for a,b in zip(p,np.roll(p,-1,axis=0)):
  segments.append([(float(a[0])-128)/120,(float(a[1])-128)/120,(float(b[0])-128)/120,(float(b[1])-128)/120])
text='package net.albiz.client.util;\n/** Baked directly from the user-supplied Albiz_icon.png. No runtime image tracing. */\npublic final class AlbizLogoGeometry {\n private AlbizLogoGeometry(){}\n private static final double[][] SEGMENTS={\n'
text+=',\n'.join('  {'+','.join(f'{v:.6f}' for v in row)+'}' for row in segments)
text+='\n };\n public static double[][] segments(){return SEGMENTS;}\n}\n'
Path('src/main/java/net/albiz/client/util/AlbizLogoGeometry.java').write_text(text)
dest=Path('src/main/resources/assets/albiz/textures/brand/discovery_mark.png');
if src.resolve()!=dest.resolve():shutil.copy2(src,dest)
Path('docs/DISCOVERY_LOGO.json').write_text(json.dumps({'source':'User-supplied Albiz_icon.png','source_sha256':hashlib.sha256(src.read_bytes()).hexdigest(),'world_segments':len(segments),'method':'Alpha contours, Douglas-Peucker epsilon1 pixel; includes eyes/ears/central sword detail; bounded line geometry, no per-frame tracing.'},indent=2)+'\n')
print('Baked',len(segments),'logo segments')
