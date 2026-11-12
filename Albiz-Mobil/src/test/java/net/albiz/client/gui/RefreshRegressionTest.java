package net.albiz.client.gui;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;import net.albiz.client.port.sober.render.Color;
class RefreshRegressionTest {
 @Test void hsvPrimaryAndSecondarySectors(){int[] expected={0xffff0000,0xffffff00,0xff00ff00,0xff00ffff,0xff0000ff,0xffff00ff};for(int i=0;i<6;i++)assertEquals(expected[i],Color.fromHsv(i/6f,1,1,255).packed());}
 @Test void hsvWrapAndGray(){assertEquals(Color.fromHsv(.9f,1,1,255),Color.fromHsv(-.1f,1,1,255));assertEquals(0xff808080,Color.fromHsv(.4f,0,.5f,255).packed());}
 @Test void hsvRoundTrips(){for(int i=0;i<100;i++){var c=new Color(i*53%256,i*97%256,i*193%256,123);float[] hsv=c.toHsv();var b=Color.fromHsv(hsv[0],hsv[1],hsv[2],123);assertEquals(c,b);}}
 @Test void portraitScaleUsesWidth(){float s=ViewportLayout.scale(1080,2340,1.25f);assertTrue(s<=1080f/520);assertTrue(s<2340f/1080*1.25f);}
 @Test void commonScreensFit(){for(int[] wh:new int[][]{{360,740},{740,360},{1080,2340},{2340,1080},{1920,1080},{1280,720},{800,600}}){float s=ViewportLayout.scale(wh[0],wh[1],2);assertTrue(460*s+32*s<=wh[0]);assertTrue(430*s<=wh[1]);assertTrue(Float.isFinite(s));}}
 @Test void invalidUserScaleIsSafe(){assertTrue(Float.isFinite(ViewportLayout.scale(1080,2340,Float.NaN)));}
 @Test void allGlintPatternsAreBoundedAndNonconstant(){for(String p:new String[]{"Stripes","Wave","Stars","Dots"}){int min=255,max=0;for(int y=0;y<64;y++)for(int x=0;x<64;x++){int v=GlintPatterns.luma(p,x,y,64,64);assertTrue(v>=0&&v<=255);min=Math.min(min,v);max=Math.max(max,v);}assertTrue(max-min>100,p);}}
}
