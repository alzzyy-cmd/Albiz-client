#!/usr/bin/env python3
"""Original Albiz procedural audio. Requires ffmpeg (or imageio-ffmpeg). No sampled voices."""
from pathlib import Path
import math,struct,wave,subprocess,tempfile,shutil
ROOT=Path(__file__).resolve().parents[1]
def main():
 ffmpeg=shutil.which('ffmpeg')
 if not ffmpeg:
  import imageio_ffmpeg
  ffmpeg=imageio_ffmpeg.get_ffmpeg_exe()
 target=ROOT/'src/main/resources/assets/albiz/sounds';target.mkdir(parents=True,exist_ok=True)
 for name,notes,duration in [('menu_click',[1400],.035),('module_on',[650,1000],.12),('module_off',[900,500],.12),('chunk_alert',[660,880,1320],.48)]:
  rate=44100;data=bytearray()
  for i in range(int(rate*duration)):
   t=i/rate;n=min(len(notes)-1,int(t/duration*len(notes)));local=(t/duration*len(notes))%1
   envelope=math.sin(math.pi*local)**2
   sample=.22*math.sin(2*math.pi*notes[n]*t)*envelope
   data+=struct.pack('<h',int(sample*32767))
  with tempfile.TemporaryDirectory() as tmp:
   p=Path(tmp)/'sound.wav'
   with wave.open(str(p),'wb') as w:w.setnchannels(1);w.setsampwidth(2);w.setframerate(rate);w.writeframes(data)
   subprocess.run([ffmpeg,'-loglevel','error','-y','-i',str(p),'-c:a','libvorbis','-q:a','4',str(target/(name+'.ogg'))],check=True)
if __name__=='__main__':main()
