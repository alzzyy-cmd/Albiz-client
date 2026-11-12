// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.modules;

import net.albiz.client.port.soberui.SoberClient;
import net.albiz.client.port.soberui.gui.Notifications;
import net.albiz.client.port.soberui.gui.Theme;
import net.albiz.client.port.soberui.model.ModelIcon;
import net.albiz.client.port.soberui.model.ModelLibrary;
import net.albiz.client.port.soberui.model.ModelManager;
import net.albiz.client.port.soberui.model.ModelMesh;
import net.albiz.client.port.soberui.model.SpriteModel;
import net.albiz.client.port.soberui.model.WaterIcon;
import net.albiz.client.port.soberui.model.WaterModels;
import net.albiz.client.port.soberui.module.Category;
import net.albiz.client.port.soberui.module.Module;
import net.albiz.client.port.soberui.module.Presets;
import net.albiz.client.port.soberui.modules.Sky;
import net.albiz.client.port.soberui.render.Color;
import net.albiz.client.port.soberui.setting.BoolSetting;
import net.albiz.client.port.soberui.setting.ColorSetting;
import net.albiz.client.port.soberui.setting.EnumSetting;
import net.albiz.client.port.soberui.setting.IntSetting;
import net.albiz.client.port.soberui.setting.ItemSetting;
import net.albiz.client.port.soberui.setting.Setting;
import net.albiz.client.port.soberui.setting.StringSetting;
import java.util.List;
import java.util.Objects;
import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_156;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_3489;
import net.minecraft.class_4587;
import net.minecraft.class_4608;
import net.minecraft.class_7923;
import net.minecraft.class_811;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public class CustomModels
extends Module
implements Presets {
    private static final class_1921 LAYER = class_12249.method_75994((class_2960)class_2960.method_60655((String)"albiz", (String)"textures/models/white.png"));
    public final Setting<String> model;
    public final Setting<String> lastModel;
    public final Setting<Replace> replace;
    public final ItemSetting item;
    public final Setting<Integer> size;
    public final Setting<Boolean> flip;
    public final Setting<Boolean> firstPerson;
    public final Setting<Boolean> thirdPerson;
    public final Setting<Boolean> inventoryIcon;
    public final Setting<Color> waterColor;
    public final Setting<Boolean> waterGlow;
    public final Setting<Integer> offsetX;
    public final Setting<Integer> offsetY;
    public final Setting<Integer> offsetZ;
    public final Setting<ColorMode> colorMode;
    public final Setting<Boolean> shine;
    public final Setting<Color> headColor;
    public final Setting<Color> handleColor;
    public final Setting<Integer> headSize;
    public final Setting<Boolean> openFolder;
    public final Setting<Boolean> reload;

    public CustomModels() {
        super("Custom Models", "Swaps pickaxes or any item for your own 3D models or item packs like Fortnite Pack and Watery.", Category.VISUALS);
        this.group("Model");
        this.model = this.add(new StringSetting("Model", "", "", true)).visible(() -> false);
        this.lastModel = this.add(new StringSetting("Last Model", "", "", true)).visible(() -> false);
        this.replace = this.add(new EnumSetting<Replace>("Replace", "Which items get the model: all pickaxes, one item of your choice, or both.", Replace.Pickaxes));
        this.item = (ItemSetting)this.add(new ItemSetting("Item", "Item that gets the model. Click and type its name, for example netherite_sword.", class_1802.field_8377)).visible(() -> this.replace.get() != Replace.Pickaxes);
        this.size = this.add(new IntSetting("Size", "Size of the model in percent.", 75, 30, 200));
        this.flip = this.add(new BoolSetting("Flip", "Mirrors the model if the head points the wrong way.", false));
        this.firstPerson = this.add(new BoolSetting("First Person", "Shows the model in your hand.", true));
        this.thirdPerson = this.add(new BoolSetting("Third Person", "Shows the model on players in third person.", true));
        this.inventoryIcon = this.add(new BoolSetting("Inventory Icon", "Shows the model as the item icon in your hotbar and inventories.", true));
        this.group("Watery");
        this.waterColor = this.add(new ColorSetting("Watery Color", "Color of the Watery Sword, Pickaxe, Shovel, Trident and Totem.", new Color(40, 110, 255, 255))).visible(() -> ModelManager.isWatery(this.model.get()));
        this.waterGlow = this.add(new BoolSetting("Watery Glow", "A soft glowing rim around the Watery items.", true)).visible(() -> ModelManager.isWatery(this.model.get()));
        this.group("Position");
        this.offsetX = this.add(new IntSetting("Offset X", "Moves the model left or right in first person.", 0, -60, 60));
        this.offsetY = this.add(new IntSetting("Offset Y", "Moves the model up or down in first person.", 0, -60, 60));
        this.offsetZ = this.add(new IntSetting("Offset Z", "Moves the model closer or further away in first person.", 0, -60, 60));
        this.group("Colors");
        this.colorMode = this.add(new EnumSetting<ColorMode>("Color Mode", "FNCS look, two colors for head and handle, one color, your sky, rainbow, or the colors stored in the model.", ColorMode.FNCS));
        this.shine = this.add(new BoolSetting("Shine", "A metallic shine runs over the model.", true));
        this.headColor = this.add(new ColorSetting("Head Color", "Color of the pickaxe head.", new Color(232, 64, 76, 255))).visible(() -> this.colorMode.get() == ColorMode.TwoTone || this.colorMode.get() == ColorMode.Single);
        this.handleColor = this.add(new ColorSetting("Handle Color", "Color of the handle.", new Color(244, 244, 250, 255))).visible(() -> this.colorMode.get() == ColorMode.TwoTone);
        this.headSize = this.add(new IntSetting("Head Size", "How much of the model counts as the head, in percent.", 35, 5, 95)).visible(() -> this.colorMode.get() == ColorMode.TwoTone || this.colorMode.get() == ColorMode.Sky);
        this.group("Files");
        this.openFolder = this.add(new BoolSetting("Open Folder", "Opens config/albız/models. Put .stl or .obj files there.", false));
        this.openFolder.onChanged(v -> {
            if (!v.booleanValue()) {
                return;
            }
            class_156.method_668().method_672(ModelManager.folder().toFile());
            this.openFolder.set(false);
        });
        this.reload = this.add(new BoolSetting("Reload Models", "Loads the model files again.", false));
        this.reload.onChanged(v -> {
            if (!v.booleanValue()) {
                return;
            }
            ModelManager.reload();
            Notifications.push("Custom Models", ModelManager.files().size() + " models found.", Notifications.Type.SUCCESS);
            this.reload.set(false);
        });
    }

    @Override
    public void onEnable() {
        if (this.model.get().isEmpty() && !this.lastModel.get().isEmpty()) {
            this.model.set(this.lastModel.get());
        }
    }

    @Override
    public void onDisable() {
        this.lastModel.set(this.model.get());
        this.model.set("");
    }

    private ModelMesh mesh() {
        return ModelManager.get(this.model.get());
    }

    private static String itemId(class_1799 stack) {
        return class_7923.field_41178.method_10221(stack.method_7909()).method_12832();
    }

    private SpriteModel sprite(class_1799 stack) {
        String name = this.model.get();
        if (ModelManager.isPack(name)) {
            return ModelManager.packSprite(name, CustomModels.itemId(stack));
        }
        if (ModelManager.isSprite(name)) {
            return ModelManager.sprite(name);
        }
        return null;
    }

    public boolean matches(class_1799 stack) {
        if (stack.method_7960() || this.model.get().isEmpty()) {
            return false;
        }
        if (ModelManager.isPack(this.model.get())) {
            return ModelManager.packHas(this.model.get(), CustomModels.itemId(stack));
        }
        return switch (this.replace.get().ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> stack.method_31573(class_3489.field_42614);
            case 1 -> stack.method_31574((class_1792)this.item.get());
            case 2 -> stack.method_31573(class_3489.field_42614) || stack.method_31574((class_1792)this.item.get());
        };
    }

    private boolean renderWater(class_1799 stack, boolean first, boolean left, boolean throwing, class_4587 matrices, class_11659 queue, int light) {
        WaterModels.Kind kind = WaterModels.kind(CustomModels.itemId(stack));
        if (kind == null || !WaterModels.ready()) {
            return false;
        }
        boolean totem = kind == WaterModels.Kind.TOTEM;
        float s = (float)this.size.get().intValue() / 75.0f;
        matrices.method_22903();
        if (kind == WaterModels.Kind.TRIDENT) {
            if (first) {
                matrices.method_46416((float)this.offsetX.get().intValue() / 100.0f, (float)this.offsetY.get().intValue() / 100.0f, (float)this.offsetZ.get().intValue() / 100.0f);
                if (left) {
                    CustomModels.vanilla(matrices, true, 13.0f, 17.0f, 1.0f, 0.0f, 90.0f, -25.0f);
                } else {
                    CustomModels.vanilla(matrices, false, -3.0f, 17.0f, 1.0f, 0.0f, -90.0f, 25.0f);
                }
            } else if (throwing) {
                CustomModels.vanilla(matrices, left, 8.0f, -17.0f, left ? -7.0f : 9.0f, 0.0f, 90.0f, 180.0f);
            } else {
                CustomModels.vanilla(matrices, left, left ? 3.0f : 11.0f, 17.0f, left ? 12.0f : -2.0f, 0.0f, 60.0f, 0.0f);
            }
            float k = 1.41f * s;
            matrices.method_46416(0.0f, -1.6875f + k * 0.035f, 0.0f);
            matrices.method_22905(k, k, k);
            matrices.method_22907((Quaternionfc)new Quaternionf().rotationZ((float)Math.toRadians(45.0)));
            matrices.method_46416(-0.02f, -0.02f, -0.5f);
        } else if (first) {
            matrices.method_46416((float)this.offsetX.get().intValue() / 100.0f, (float)this.offsetY.get().intValue() / 100.0f, (float)this.offsetZ.get().intValue() / 100.0f);
            if (totem) {
                CustomModels.pose(matrices, left, -2.6f, 4.0f, -2.1f, 0.0f, -62.0f, 0.0f, 0.41f * s);
            } else {
                CustomModels.pose(matrices, left, -3.2f, 4.1f, -2.1f, 0.0f, 125.0f, 43.0f, (kind == WaterModels.Kind.TRIDENT ? 0.4f : 0.44f) * s);
            }
        } else if (totem) {
            CustomModels.pose(matrices, left, 0.0f, 3.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.55f * s);
        } else {
            CustomModels.pose(matrices, left, 0.0f, 4.0f, 0.5f, 0.0f, -90.0f, 55.0f, 0.85f * s);
        }
        int color = 0xFF000000 | this.waterColor.get().packed() & 0xFFFFFF;
        WaterModels.render(matrices, queue, kind, color, light, this.waterGlow.get());
        matrices.method_22909();
        return true;
    }

    private static void vanilla(class_4587 matrices, boolean left, float tx, float ty, float tz, float rx, float ry, float rz) {
        if (left) {
            tx = -tx;
            ry = -ry;
            rz = -rz;
        }
        matrices.method_46416(tx / 16.0f, ty / 16.0f, tz / 16.0f);
        matrices.method_22907((Quaternionfc)new Quaternionf().rotationXYZ((float)Math.toRadians(rx), (float)Math.toRadians(ry), (float)Math.toRadians(rz)));
        matrices.method_46416(-0.5f, -0.5f, -0.5f);
    }

    private static void pose(class_4587 matrices, boolean left, float tx, float ty, float tz, float rx, float ry, float rz, float scale) {
        matrices.method_46416((left ? -tx : tx) / 16.0f, ty / 16.0f, tz / 16.0f);
        matrices.method_22907((Quaternionfc)new Quaternionf().rotationXYZ((float)Math.toRadians(rx), (float)Math.toRadians(left ? (double)(-ry) : (double)ry), (float)Math.toRadians(left ? (double)(-rz) : (double)rz)));
        matrices.method_22905(scale, scale, scale);
        matrices.method_46416(-0.5f, -0.5f, -0.5f);
    }

    public boolean render(class_1799 stack, class_811 context, class_4587 matrices, class_11659 queue, int light) {
        return this.render(stack, context, matrices, queue, light, false);
    }

    public boolean render(class_1799 stack, class_811 context, class_4587 matrices, class_11659 queue, int light, boolean throwing) {
        ModelMesh mesh;
        boolean leftHand;
        boolean third;
        boolean first = context == class_811.field_4322 || context == class_811.field_4321;
        boolean bl = third = context == class_811.field_4320 || context == class_811.field_4323;
        if (!first && !third) {
            return false;
        }
        boolean bl2 = leftHand = context == class_811.field_4321 || context == class_811.field_4323;
        if (!(first && this.firstPerson.get().booleanValue() || third && this.thirdPerson.get().booleanValue())) {
            return false;
        }
        if (!this.matches(stack)) {
            return false;
        }
        if (ModelManager.isWatery(this.model.get())) {
            return this.renderWater(stack, first, leftHand, throwing, matrices, queue, light);
        }
        SpriteModel sprite = this.sprite(stack);
        ModelMesh modelMesh = mesh = sprite == null ? this.mesh() : null;
        if (sprite == null && mesh == null) {
            return false;
        }
        boolean left = context == class_811.field_4321 || context == class_811.field_4323;
        matrices.method_22903();
        if (first) {
            matrices.method_46416((float)this.offsetX.get().intValue() / 100.0f, (float)this.offsetY.get().intValue() / 100.0f, (float)this.offsetZ.get().intValue() / 100.0f);
        }
        if (first) {
            CustomModels.display(matrices, left, 1.13f, 3.2f, 1.13f, -90.0f, 25.0f, 0.68f);
        } else {
            CustomModels.display(matrices, left, 0.0f, 4.0f, 0.5f, -90.0f, 55.0f, 0.85f);
        }
        float s = (float)this.size.get().intValue() / 100.0f;
        matrices.method_46416(0.5f, 0.5f, 0.5f);
        matrices.method_22905(this.flip.get() != false ? -s : s, s, s);
        matrices.method_46416(-0.5f, -0.5f, -0.5f);
        if (sprite != null) {
            queue.method_73483(matrices, class_12249.method_75994((class_2960)sprite.texture), (entry, consumer) -> {
                float[] p = sprite.positions;
                float[] uv = sprite.uvs;
                float[] n = sprite.normals;
                int[] c = sprite.colors;
                for (int q = 0; q < sprite.quads; ++q) {
                    for (int k = 0; k < 4; ++k) {
                        int v = q * 4 + k;
                        consumer.method_56824(entry, p[v * 3], p[v * 3 + 1], p[v * 3 + 2]).method_39415(c[q]).method_22913(uv[v * 2], uv[v * 2 + 1]).method_22922(class_4608.field_21444).method_60803(light).method_60831(entry, n[v * 3], n[v * 3 + 1], n[v * 3 + 2]);
                    }
                }
            });
            matrices.method_22909();
            return true;
        }
        int[] colors = this.colors(mesh, this.shine.get());
        queue.method_73483(matrices, LAYER, (entry, consumer) -> {
            float[] p = mesh.positions;
            float[] n = mesh.normals;
            for (int t = 0; t < mesh.triangles; ++t) {
                float nx = n[t * 3];
                float ny = n[t * 3 + 1];
                float nz = n[t * 3 + 2];
                for (int k = 0; k < 4; ++k) {
                    int v = t * 3 + Math.min(k, 2);
                    consumer.method_56824(entry, p[v * 3], p[v * 3 + 1], p[v * 3 + 2]).method_39415(colors[v]).method_22913(0.5f, 0.5f).method_22922(class_4608.field_21444).method_60803(light).method_60831(entry, nx, ny, nz);
                }
            }
        });
        matrices.method_22909();
        return true;
    }

    private static void display(class_4587 matrices, boolean left, float tx, float ty, float tz, float ry, float rz, float scale) {
        matrices.method_46416((left ? -tx : tx) / 16.0f, ty / 16.0f, tz / 16.0f);
        matrices.method_22907((Quaternionfc)new Quaternionf().rotationXYZ(0.0f, (float)Math.toRadians(ry), (float)Math.toRadians(rz)));
        matrices.method_22905(scale, scale, scale);
        matrices.method_46416(-0.5f, -0.5f, -0.5f);
    }

    public class_2960 icon(class_1799 stack) {
        if (!this.inventoryIcon.get().booleanValue() || !this.matches(stack)) {
            return null;
        }
        if (ModelManager.isWatery(this.model.get())) {
            WaterModels.Kind kind = WaterModels.kind(CustomModels.itemId(stack));
            return kind == null ? null : WaterIcon.get(kind, this.waterColor.get().packed() & 0xFFFFFF);
        }
        SpriteModel sprite = this.sprite(stack);
        if (sprite != null) {
            return sprite.icon;
        }
        if (ModelManager.isPack(this.model.get()) || ModelManager.isSprite(this.model.get())) {
            return null;
        }
        ModelMesh mesh = this.mesh();
        if (mesh == null) {
            return null;
        }
        boolean animated = this.colorMode.get() == ColorMode.Rainbow || this.colorMode.get() == ColorMode.Sky;
        int key = Objects.hash(new Object[]{this.model.get(), this.colorMode.get(), this.headColor.get().packed(), this.handleColor.get().packed(), this.headSize.get(), this.flip.get(), animated ? System.currentTimeMillis() / 400L : 0L});
        return ModelIcon.get(mesh, () -> this.colors(mesh, false), this.flip.get(), key);
    }

    public class_2960 previewIcon(int index){
      if(index<=0)return null;var files=ModelManager.files();if(index>files.size())return null;String name=files.get(index-1);
      if(ModelManager.isWatery(name))return WaterIcon.get(WaterModels.Kind.PICKAXE,this.waterColor.get().packed()&0xffffff);
      if(ModelManager.isPack(name)||ModelManager.isSprite(name)){var sprite=ModelManager.isPack(name)?ModelManager.packSprite(name,"diamond_pickaxe"):ModelManager.sprite(name);return sprite==null?null:sprite.icon;}
      var mesh=ModelManager.get(name);return mesh==null?null:ModelIcon.get(mesh,()->this.colors(mesh,false),this.flip.get(),Objects.hash(name,this.headColor.get().packed(),this.handleColor.get().packed(),this.colorMode.get(),this.flip.get()));
    }

    private int[] colors(ModelMesh mesh, boolean withShine) {
        int[] colors = new int[mesh.triangles * 3];
        ColorMode mode = this.colorMode.get();
        boolean colored = mesh.hasColors();
        if (mode == ColorMode.Model && !colored) {
            mode = ColorMode.TwoTone;
        }
        if (mode == ColorMode.FNCS && colored) {
            mode = ColorMode.Model;
        }
        float split = 1.0f - (float)this.headSize.get().intValue() / 100.0f;
        double time = (double)System.currentTimeMillis() / 1000.0;
        int head = this.headColor.get().packed() & 0xFFFFFF;
        int handle = this.handleColor.get().packed() & 0xFFFFFF;
        if (mode == ColorMode.Sky) {
            int sky = SoberClient.MODULES.get(Sky.class).accentColor();
            head = sky >= 0 ? sky : Theme.accent() & 0xFFFFFF;
            handle = CustomModels.mix(head, 0x202028, 0.55f);
        }
        for (int v = 0; v < colors.length; ++v) {
            float band;
            float glow;
            int rgb;
            float along = mesh.along[v];
            rgb=switch(mode){
                case Single -> head;
                case Rainbow -> Color.fromHsv((float)(((double)along*.6+time*.25)%1),.75f,1,255).packed()&0xffffff;
                case Model -> mesh.colors[v/3]==-1?head:mesh.colors[v/3];
                case FNCS -> CustomModels.fncs(mesh,v,along,split);
                default -> CustomModels.mix(handle,head,CustomModels.smooth((along-split)/.04f+.5f));
            };
            if (withShine && (glow = 1.0f - Math.min(1.0f, Math.abs(along - (band = (float)(time * 0.45 % 1.6) - 0.3f)) / 0.07f)) > 0.0f) {
                rgb = CustomModels.mix(rgb, 0xFFFFFF, glow * 0.55f);
            }
            colors[v] = 0xFF000000 | rgb;
        }
        return colors;
    }

    private static int fncs(ModelMesh mesh, int v, float along, float split) {
        int t = v / 3;
        float nz = Math.abs(mesh.normals[t * 3 + 2]);
        float raised = Math.abs(mesh.positions[v * 3 + 2] - 0.5f) / mesh.depth;
        if (along > split) {
            boolean frame;
            boolean bl = frame = nz < 0.55f || raised > 0.72f;
            if (frame) {
                return CustomModels.mix(0xE9E4EE, 16103624, 0.25f + 0.25f * (1.0f - nz));
            }
            return CustomModels.mix(13116988, 15222874, along);
        }
        if (along < 0.045f) {
            return 0xE0303E;
        }
        if (along < 0.075f) {
            return 15909466;
        }
        float stripe = nz < 0.55f ? 0.35f : 0.0f;
        return CustomModels.mix(CustomModels.mix(12605486, 15768158, along / Math.max(0.01f, split)), 16769224, stripe);
    }

    private static float smooth(float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        return t * t * (3.0f - 2.0f * t);
    }

    private static int mix(int a, int b, float t) {
        int ar = a >> 16 & 0xFF;
        int ag = a >> 8 & 0xFF;
        int ab = a & 0xFF;
        int br = b >> 16 & 0xFF;
        int bg = b >> 8 & 0xFF;
        int bb = b & 0xFF;
        return (int)((float)ar + (float)(br - ar) * t) << 16 | (int)((float)ag + (float)(bg - ag) * t) << 8 | (int)((float)ab + (float)(bb - ab) * t);
    }

    @Override
    public int presetCount() {
        return 1 + ModelManager.files().size();
    }

    @Override
    public String presetName(int index) {
        if (index == 0) {
            return "None";
        }
        List<String> files = ModelManager.files();
        return index - 1 < files.size() ? ModelManager.title(files.get(index - 1)) : "";
    }

    @Override
    public int presetColor(int index) {
        if (index == 0) {
            return -9801856;
        }
        List<String> files = ModelManager.files();
        if (index - 1 < files.size() && ModelManager.isWatery(files.get(index - 1))) {
            return 0xFF000000 | this.waterColor.get().packed() & 0xFFFFFF;
        }
        int swatch = index - 1 < files.size() ? ModelLibrary.swatch(files.get(index - 1)) : -1;
        return 0xFF000000 | (swatch >= 0 ? swatch : this.headColor.get().packed() & 0xFFFFFF);
    }

    @Override
    public boolean presetActive(int index) {
        if (index == 0) {
            return this.model.get().isEmpty();
        }
        List<String> files = ModelManager.files();
        return index - 1 < files.size() && files.get(index - 1).equals(this.model.get());
    }

    @Override
    public void selectPreset(int index) {
        if (index == 0) {
            this.model.set("");
            this.lastModel.set("");
            return;
        }
        List<String> files = ModelManager.files();
        if (index - 1 >= files.size()) {
            return;
        }
        String file = files.get(index - 1);
        this.model.set(file);
        if (!this.isActive()) {
            this.setEnabled(true);
        }
        if (ModelManager.isPack(file) || ModelManager.isSprite(file)) {
            return;
        }
        if (ModelManager.get(file) == null) {
            Notifications.push("Custom Models", "Could not load " + file + ": " + ModelManager.error(file), Notifications.Type.ERROR);
        }
    }

    @Override
    public String presetDescription() {
        return "Uses this model. Folders are item packs: every item gets the picture with its name, for example diamond_pickaxe.png. Watery turns swords, pickaxes, shovels, tridents and totems into deep blue water glass. Your own .stl, .obj or .png files go into config/albız/models.";
    }

    @Override
    public boolean showInHud() {
        return false;
    }

    @Override
    public boolean bindable() {
        return false;
    }

    public static enum Replace {
        Pickaxes,
        Item,
        Both;

    }

    public static enum ColorMode {
        TwoTone,
        Single,
        Sky,
        Rainbow,
        Model,
        FNCS;

    }
}

