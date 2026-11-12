"""Hand-painted stern Guts face, guided by the user's reference, not a photo decal."""
from pathlib import Path
import numpy as np
from PIL import Image,ImageDraw,ImageFilter
folder=Path(__file__).resolve().parents[2]/'artwork/characters';size=512
Y,X=np.mgrid[0:size,0:size];u=X/(size-1);v=Y/(size-1);shade=1-.13*np.abs(u-.48)/.52-.055*v+.045*np.exp(-((u-.4)**2+(v-.4)**2)/.12)
base=np.clip(np.array([232,184,155])[None,None,:]*shade[:,:,None],0,255).astype(np.uint8);im=Image.fromarray(base);d=ImageDraw.Draw(im)
def pts(p):return [(int(x*size),int(y*size)) for x,y in p]
def line(p,fill,width):d.line(pts(p),fill=fill,width=width,joint='curve')
def polygon(p,fill):d.polygon(pts(p),fill=fill)
# Angular jaw/temple shadows, understated rather than rosy cheeks.
polygon([(0,.32),(.12,.54),(.11,.74),(.27,.96),(0,1)],(192,143,116));polygon([(1,.27),(.9,.52),(.89,.77),(.7,1),(1,1)],(181,131,106))
# Heavy angular brows, closed scarred right eye and a narrow dark open left eye.
polygon([(.10,.26),(.39,.34),(.40,.39),(.12,.31)],(42,37,32));polygon([(.6,.34),(.9,.25),(.91,.3),(.6,.39)],(42,37,32))
line([(.13,.435),(.24,.455),(.39,.425)],(72,51,41),4)
polygon([(.61,.435),(.88,.385),(.86,.46),(.64,.49)],(225,215,191));line([(.61,.435),(.88,.385)],(43,37,31),5);line([(.64,.49),(.86,.46)],(119,83,63),2)
d.ellipse((int(.73*size),int(.414*size),int(.785*size),int(.475*size)),fill=(53,55,48));d.ellipse((int(.749*size),int(.421*size),int(.768*size),int(.475*size)),fill=(26,28,25))
line([(.21,.3),(.235,.44),(.25,.63)],(150,103,85),3);line([(.225,.30),(.25,.44),(.265,.62)],(239,191,160),2)
# Sculpted nose lighting and a firm, unsmiling mouth.
polygon([(.48,.42),(.48,.62),(.42,.67),(.51,.665)],(174,123,96));polygon([(.53,.48),(.57,.64),(.51,.69),(.61,.65)],(155,105,81));line([(.43,.69),(.5,.705),(.59,.683)],(128,88,67),2)
line([(.35,.80),(.49,.785),(.66,.79)],(99,67,52),3);line([(.4,.835),(.56,.84),(.62,.825)],(194,141,112),2);line([(.4,.92),(.58,.925)],(205,155,125),2)
im=im.filter(ImageFilter.GaussianBlur(.5));im.save(folder/'guts-face-paint.png');print(folder/'guts-face-paint.png')
