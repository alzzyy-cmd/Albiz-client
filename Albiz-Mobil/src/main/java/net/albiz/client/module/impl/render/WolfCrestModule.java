package net.albiz.client.module.impl.render;

import java.util.Arrays;
import net.albiz.client.module.setting.BooleanSetting;
import net.albiz.client.module.setting.ColorSetting;
import net.albiz.client.module.setting.ModeSetting;
import net.albiz.client.module.setting.NumberSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_9848;

@Environment(EnvType.CLIENT)
public final class WolfCrestModule extends HeadCosmeticModule {
   private static final String STYLE_NEMES = "Wolf";
   private static final String STYLE_KHEPRESH = "Altai";
   private static final String STYLE_SUN_DISC = "Red Moon";
   private final ModeSetting style = this.addSetting("style", new ModeSetting("Style", "Wolf", "Wolf", "Altai", "Red Moon", "Fang Helmet", "Night Helmet", "Albiz Cape", "Heart Cape", "Smile Cape", "Crown Cape", "Lightning Cape", "Moon Cape", "Paw Cape", "Sword Cape", "Wolf Set"));
   private final NumberSetting stripes = this.addSetting("stripes", new NumberSetting("Stripes", 5.0F, 3.0F, 9.0F).step(1.0F));
   private final BooleanSetting collar = this.addSetting("collar", new BooleanSetting("Shoulder Guard", true));
   private final NumberSetting size = this.addSetting("size", new NumberSetting("Size", 1.0F, 0.8F, 1.3F).step(0.05F));
   private final ModeSetting color = this.addSetting("color", new ModeSetting("Color", "Theme", "Theme", "Manual"));
   private final ColorSetting gold = this.addSetting("manual-gold", new ColorSetting("Red Color", 0xFFE3262E));
   private final ColorSetting inlay = this.addSetting("manual-inlay", new ColorSetting("Inlay Color", 0xFF24152E));
   private final ColorSetting gem = this.addSetting("manual-gem", new ColorSetting("Gem Color", 0xFFFF6971));
   private final BooleanSetting gemGlow = this.addSetting("gem-glow", new BooleanSetting("Gem Glow", true));
   private final BooleanSetting glint = this.addSetting("glint", new BooleanSetting("Glint", true));
   private final BooleanSetting friends = new BooleanSetting("On Friends", false);
   private final SteppeMesh[] meshes = new SteppeMesh[4];
   private long styleRevision = -1L;
   private long stripesRevision = -1L;
   private long collarRevision = -1L;

   public WolfCrestModule() {
      super("wolf-crest", "Wolf Crest", "Local-only distinct wolf helmets, wolf chest set and eight emblem capes. Not shared with other players.");
      this.stripes.visibleWhen(() -> this.style.is("Wolf"));
      this.collar.visibleWhen(() -> this.style.is("Wolf Set"));
      this.gold.visibleWhen(() -> this.color.is("Manual"));
      this.inlay.visibleWhen(() -> this.color.is("Manual"));
      this.gem.visibleWhen(() -> this.color.is("Manual"));
   }

   @Override
   void reset() {
      super.reset();
      Arrays.fill(this.meshes, null);
      this.styleRevision = -1L;
   }

   @Override
   SteppeMesh headMesh(boolean helmet) {
      this.refreshMeshes();
      int slot = helmet ? 1 : 0;
      SteppeMesh mesh = this.meshes[slot];
      if (mesh == null) {
         mesh = SteppeMesh.head(this.styleIndex(), this.stripes.getInt(), helmet);
         this.meshes[slot] = mesh;
      }

      return mesh;
   }

   @Override
   SteppeMesh bodyMesh(boolean chest) {
      this.refreshMeshes();
      int slot = chest ? 3 : 2;
      SteppeMesh mesh = this.meshes[slot];
      if (mesh == null) {
         mesh = SteppeMesh.body(this.styleIndex(), this.stripes.getInt(), chest, this.collar.get());
         this.meshes[slot] = mesh;
      }

      return mesh;
   }

   private void refreshMeshes() {
      long s = this.style.revision();
      long n = this.stripes.revision();
      long c = this.collar.revision();
      if (s != this.styleRevision || n != this.stripesRevision || c != this.collarRevision) {
         this.styleRevision = s;
         this.stripesRevision = n;
         this.collarRevision = c;
         Arrays.fill(this.meshes, null);
      }
   }

   private int styleIndex() {
      String[] styles={"Wolf","Altai","Red Moon","Fang Helmet","Night Helmet","Albiz Cape","Heart Cape","Smile Cape","Crown Cape","Lightning Cape","Moon Cape","Paw Cape","Sword Cape","Wolf Set"};
      for(int i=0;i<styles.length;i++)if(this.style.is(styles[i]))return i;
      return 0;
   }

   @Override
   float scale() {
      return this.size.get();
   }

   @Override
   int[] palette() {
      int[] palette = new int[5];
      if (this.color.is("Manual")) {
         int metal = this.gold.argb() | 0xFF000000;
         palette[0] = metal;
         palette[1] = this.inlay.argb() | 0xFF000000;
         palette[2] = this.gem.argb() | 0xFF000000;
         palette[3] = bronze(metal);
      } else {
         int accent = CosmeticAccent.argb();
         int metal = metal(accent);
         palette[0] = metal;
         palette[1] = class_9848.method_61321(accent, 0.78F);
         palette[2] = gem(accent);
         palette[3] = class_9848.method_61321(metal, 0.62F);
      }

      if(this.style.is("Albiz Cape"))palette[2]=0xFFE3262E;
      return palette;
   }

   @Override
   boolean glows() {
      return this.gemGlow.get();
   }

   @Override
   boolean glints() {
      return this.glint.get();
   }

   @Override
   boolean onFriends() {
      return this.friends.get();
   }

   private static int bronze(int argb) {
      int r = (int)((argb >> 16 & 0xFF) * 0.7F);
      int g = (int)((argb >> 8 & 0xFF) * 0.55F);
      int b = (int)((argb & 0xFF) * 0.4F);
      return 0xFF000000 | r << 16 | g << 8 | b;
   }
}
