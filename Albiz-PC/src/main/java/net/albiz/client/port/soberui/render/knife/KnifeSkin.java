// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.render.knife;

import net.albiz.client.port.soberui.render.knife.InspectClip;
import net.albiz.client.port.soberui.render.knife.KnifeMaterial;
import net.albiz.client.port.soberui.render.knife.KnifeMesh;
import net.albiz.client.port.soberui.render.knife.KnifeModels;
import net.albiz.client.port.soberui.render.knife.SlashTrail;
import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class KnifeSkin {
    public static final String[] NAMES = new String[]{"Butterfly", "Karambit", "Katana"};
    public static final int MAX_STREAKS = 2;
    public static final float[] REST = new float[16];
    public static final float SPARKLE_SIZE = 0.07f;
    private static final class_2960 WHITE = class_2960.method_60655((String)"albiz", (String)"textures/models/white.png");
    private static final Vector3f SCABBARD_FALL = new Vector3f(-0.3f, -0.9f, 0.25f);
    private static final float SCABBARD_TURN = 0.6981317f;
    private static final float[][] BUTTERFLY_INSPECT = new float[][]{{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}, {0.08f, -0.07f, 0.09f, 0.05f, -8.0f, 8.0f, 10.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 8.0f}, {0.14f, -0.09f, 0.12f, 0.06f, -12.0f, 8.0f, 14.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 12.0f}, {0.19f, -0.09f, 0.12f, 0.06f, -10.0f, 10.0f, 2.0f, -18.0f, 0.0f, 90.0f, 180.0f, 0.0f, 12.0f}, {0.24f, -0.09f, 0.13f, 0.06f, -12.0f, 8.0f, 10.0f, 0.0f, 0.0f, 180.0f, 360.0f, 0.0f, 12.0f}, {0.29f, -0.09f, 0.12f, 0.06f, -10.0f, 10.0f, 2.0f, 18.0f, 0.0f, 270.0f, 540.0f, 0.0f, 12.0f}, {0.34f, -0.09f, 0.12f, 0.06f, -12.0f, 8.0f, 14.0f, 0.0f, 0.0f, 360.0f, 720.0f, 0.0f, 12.0f}, {0.39f, -0.09f, 0.12f, 0.06f, -10.0f, 10.0f, 2.0f, -18.0f, 0.0f, 450.0f, 900.0f, 0.0f, 12.0f}, {0.44f, -0.09f, 0.13f, 0.06f, -12.0f, 8.0f, 10.0f, 0.0f, 0.0f, 540.0f, 1080.0f, 0.0f, 12.0f}, {0.49f, -0.09f, 0.12f, 0.06f, -10.0f, 10.0f, 2.0f, 18.0f, 0.0f, 630.0f, 1260.0f, 0.0f, 12.0f}, {0.54f, -0.09f, 0.12f, 0.06f, -12.0f, 8.0f, 14.0f, 0.0f, 0.0f, 720.0f, 1440.0f, 0.0f, 12.0f}, {0.62f, -0.1f, 0.12f, 0.07f, -35.0f, 6.0f, 20.0f, 0.0f, 0.0f, 720.0f, 1440.0f, 0.0f, 0.0f}, {0.76f, -0.1f, 0.13f, 0.07f, 25.0f, 6.0f, 16.0f, 0.0f, 0.0f, 720.0f, 1440.0f, 1.0f, 0.0f}, {0.88f, -0.05f, 0.06f, 0.03f, 8.0f, 3.0f, 6.0f, 0.0f, 0.0f, 720.0f, 1440.0f, 1.0f, 0.0f}, {1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 720.0f, 1440.0f, 1.0f, 0.0f}};
    private static final float[][] BUTTERFLY_DEPLOY = new float[][]{{0.0f, 0.02f, -0.14f, 0.02f, 0.0f, -30.0f, -20.0f, 0.0f, 0.0f, 180.0f, 0.0f, 0.0f, 10.0f}, {0.3f, 0.01f, -0.04f, 0.01f, 0.0f, -10.0f, -10.0f, 0.0f, 0.0f, 220.0f, 100.0f, 0.0f, 10.0f}, {0.62f, 0.0f, 0.01f, 0.0f, 0.0f, 4.0f, 6.0f, 0.0f, 0.0f, 350.0f, 340.0f, 0.0f, 3.0f}, {0.8f, 0.0f, 0.0f, 0.0f, 0.0f, -2.0f, -2.0f, 0.0f, 0.0f, 365.0f, 368.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 360.0f, 360.0f, 0.0f, 0.0f}};
    private static final float[][] KARAMBIT_INSPECT = new float[][]{{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}, {0.1f, -0.12f, 0.14f, 0.06f, -10.0f, 14.0f, 10.0f, 0.0f, 0.0f, -25.0f, 0.0f, 0.0f}, {0.14f, -0.14f, 0.16f, 0.06f, -10.0f, 14.0f, 10.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}, {0.205f, -0.14f, 0.17f, 0.06f, -10.0f, 14.0f, 10.0f, 0.0f, 0.0f, 180.0f, 0.0f, 0.0f}, {0.27f, -0.14f, 0.16f, 0.06f, -10.0f, 14.0f, 10.0f, 0.0f, 0.0f, 360.0f, 0.0f, 0.0f}, {0.335f, -0.14f, 0.17f, 0.06f, -10.0f, 14.0f, 10.0f, 0.0f, 0.0f, 540.0f, 0.0f, 0.0f}, {0.4f, -0.14f, 0.16f, 0.06f, -10.0f, 14.0f, 10.0f, 0.0f, 0.0f, 720.0f, 0.0f, 0.0f}, {0.48f, -0.13f, 0.16f, 0.06f, -10.0f, 12.0f, 10.0f, 0.0f, 0.0f, 750.0f, 0.0f, 0.0f}, {0.56f, -0.1f, 0.12f, 0.06f, 20.0f, 10.0f, -70.0f, 0.0f, 0.0f, 720.0f, 0.0f, 0.0f}, {0.72f, -0.1f, 0.13f, 0.06f, -10.0f, 10.0f, -60.0f, 0.0f, 0.0f, 720.0f, 0.0f, 1.0f}, {0.86f, -0.06f, 0.08f, 0.05f, -6.0f, 4.0f, 8.0f, 0.0f, 0.0f, 360.0f, 0.0f, 1.0f}, {1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 360.0f, 0.0f, 1.0f}};
    private static final float[][] KARAMBIT_DEPLOY = new float[][]{{0.0f, 0.02f, -0.14f, 0.02f, 0.0f, -30.0f, -20.0f, 0.0f, 0.0f, -540.0f, 0.0f, 0.0f}, {0.55f, 0.0f, 0.0f, 0.0f, 0.0f, 4.0f, 4.0f, 0.0f, 0.0f, -60.0f, 0.0f, 0.0f}, {0.78f, 0.0f, 0.0f, 0.0f, 0.0f, -2.0f, -2.0f, 0.0f, 0.0f, 12.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}};
    private static final float[][] KATANA_INSPECT = new float[][]{{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 4.0f}, {0.1f, -0.06f, 0.12f, 0.06f, 0.0f, 0.0f, 60.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.6f, 4.0f}, {0.15f, -0.07f, 0.14f, 0.08f, -2.0f, 0.0f, 60.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 4.0f}, {0.2f, -0.07f, 0.14f, 0.08f, -2.0f, 0.0f, 60.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 12.0f}, {0.36f, -0.07f, 0.14f, 0.08f, 0.0f, 0.0f, 58.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 44.0f}, {0.4f, -0.07f, 0.14f, 0.08f, 0.0f, 0.0f, 58.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 53.0f}, {0.44f, -0.06f, 0.13f, 0.06f, 2.0f, 1.0f, 54.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.6f, 58.0f}, {0.48f, -0.05f, 0.11f, 0.03f, 4.0f, 2.0f, 42.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 62.0f}, {0.52f, -0.04f, 0.1f, 0.02f, 0.0f, 4.0f, 20.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 62.0f}, {0.6f, -0.04f, 0.12f, 0.02f, 0.0f, 4.0f, -160.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 62.0f}, {0.68f, -0.04f, 0.1f, 0.02f, 0.0f, 4.0f, -340.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 62.0f}, {0.78f, 0.04f, -0.03f, 0.0f, 0.0f, -6.0f, -400.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 62.0f}, {0.88f, 0.0f, 0.02f, 0.0f, 0.0f, 2.0f, -365.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 62.0f}, {1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -360.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 62.0f}};
    private static final float[][] KATANA_DEPLOY = new float[][]{{0.0f, 0.06f, 0.0f, 0.02f, -10.0f, 0.0f, 45.0f, 180.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f}, {0.05f, 0.05f, 0.01f, 0.01f, -12.0f, 0.0f, 47.0f, 180.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 2.0f, 1.0f, 0.0f}, {0.18f, 0.15f, 0.0f, 0.05f, -10.0f, 0.0f, 45.0f, 180.0f, 0.0f, 0.0f, 0.0f, 0.3f, 0.0f, 16.0f, 1.0f, 0.0f}, {0.3f, 0.3f, -0.02f, 0.1f, -8.0f, 0.0f, 43.0f, 180.0f, 0.0f, 0.0f, 0.0f, 0.9f, 0.0f, 50.0f, 1.0f, 0.0f}, {0.33f, 0.3f, 0.0f, 0.1f, -4.0f, 4.0f, 36.0f, 180.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 52.0f, 1.0f, 0.0f}, {0.41f, 0.08f, 0.12f, 0.04f, 0.0f, 10.0f, -40.0f, 180.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 52.0f, 0.25f, 0.0f}, {0.47f, -0.02f, 0.1f, 0.02f, 5.0f, 8.0f, -100.0f, 180.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 52.0f, 0.0f, 0.0f}, {0.62f, -0.02f, 0.08f, 0.02f, 0.0f, 6.0f, -460.0f, 180.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 52.0f, 0.0f, 0.0f}, {0.71f, 0.04f, -0.04f, 0.0f, 0.0f, -6.0f, -520.0f, 180.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 52.0f, 0.0f, 0.0f}, {0.82f, 0.0f, 0.02f, 0.0f, 0.0f, 2.0f, -370.0f, 300.0f, 0.0f, 0.0f, 0.0f, 2.0f, 0.0f, 52.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -360.0f, 360.0f, 0.0f, 0.0f, 0.0f, 3.0f, 0.0f, 52.0f, 0.0f, 0.0f}};
    private static KnifeSkin butterfly;
    private static KnifeSkin karambit;
    private static KnifeSkin katana;
    public final String name;
    public final InspectClip inspect;
    public final InspectClip deploy;
    private final Part[] parts;
    private final Streak[] streaks;
    private final float voxel;
    private final float gripX;
    private final float gripY;
    private final Vector3f fist;
    private final Quaternionf rest;
    private KnifeMesh scabbard;
    private float saddleX;
    private float saddleY;
    private float[] letGo;
    private Centre centre;
    private float bladeFrom;
    private float bladeTo;

    private KnifeSkin(String name, Part[] parts, Streak[] streaks, float voxel, float gripX, float gripY, Vector3f fist, Vector3f blade, Vector3f spine, InspectClip inspect, InspectClip deploy) {
        this.name = name;
        this.parts = parts;
        this.streaks = streaks;
        this.voxel = voxel;
        this.gripX = gripX;
        this.gripY = gripY;
        this.fist = fist;
        this.inspect = inspect;
        this.deploy = deploy;
        Vector3f x = new Vector3f((Vector3fc)blade).normalize();
        Vector3f y = new Vector3f((Vector3fc)spine).fma(-spine.dot((Vector3fc)x), (Vector3fc)x).normalize();
        this.rest = new Quaternionf().setFromNormalized((Matrix3fc)new Matrix3f((Vector3fc)x, (Vector3fc)y, (Vector3fc)new Vector3f((Vector3fc)x).cross((Vector3fc)y)));
    }

    public static KnifeSkin of(String name) {
        return switch (name) {
            case "Karambit" -> {
                if (karambit != null) {
                    yield karambit;
                }
                yield karambit = KnifeSkin.karambit();
            }
            case "Katana" -> {
                if (katana != null) {
                    yield katana;
                }
                yield katana = KnifeSkin.katana();
            }
            default -> butterfly != null ? butterfly : (butterfly = KnifeSkin.butterfly());
        };
    }

    private static KnifeSkin butterfly() {
        float pivotX = -1.5f;
        float pivotY = 0.5f;
        Part[] parts = new Part[]{new Part(KnifeModels.butterflyHandle(1), -1, pivotX, pivotY), new Part(KnifeModels.butterflyBlade(), 8, pivotX, pivotY), new Part(KnifeModels.butterflyHandle(-1), 9, pivotX, pivotY)};
        Streak[] streaks = new Streak[]{new Streak(1, 2.5f, 0.5f, 21.5f, -0.5f, 1.0f), new Streak(2, -4.0f, 0.5f, -27.5f, 0.5f, 0.8f)};
        return new KnifeSkin("Butterfly", parts, streaks, 0.021f, -10.0f, 0.5f, new Vector3f(-0.06f, 0.06f, 0.02f), new Vector3f(-0.15f, 1.0f, -0.1f), new Vector3f(1.0f, 0.15f, 0.3f), new InspectClip(BUTTERFLY_INSPECT, 3.8f, 0.14f, 0.54f), new InspectClip(BUTTERFLY_DEPLOY, 0.9f));
    }

    private static KnifeSkin karambit() {
        float ringX = -16.0f;
        float ringY = -1.0f;
        Part[] parts = new Part[]{new Part(KnifeModels.karambit(), 8, ringX, ringY)};
        float[] tip = KnifeModels.KARAMBIT_TIP;
        int k = 2;
        Streak[] streaks = new Streak[]{new Streak(0, 6.0f * (float)k, 0.5f * (float)k, tip[0], tip[1], 1.0f)};
        return new KnifeSkin("Karambit", parts, streaks, 0.022f / (float)k, ringX + 2.0f, ringY, new Vector3f(-0.06f, 0.1f, 0.02f), new Vector3f(-0.5f, -0.8f, -0.3f), new Vector3f(0.8f, -0.5f, 0.35f), new InspectClip(KARAMBIT_INSPECT, 3.2f, 0.14f, 0.4f), new InspectClip(KARAMBIT_DEPLOY, 0.8f));
    }

    private static KnifeSkin katana() {
        Part[] parts = new Part[]{new Part(KnifeModels.katana(), -1, 0.0f, 0.0f)};
        Streak[] streaks = new Streak[]{new Streak(0, 6.0f, (float)KnifeModels.katanaBow(6) + 0.5f, 48.5f, (float)KnifeModels.katanaBow(48) + 1.5f, 1.0f)};
        KnifeSkin skin = new KnifeSkin("Katana", parts, streaks, 0.028f, -5.0f, 0.5f, new Vector3f(-0.06f, 0.02f, 0.02f), new Vector3f(-0.6f, 0.8f, -0.2f), new Vector3f(0.6f, 0.45f, 0.1f), new InspectClip(KATANA_INSPECT, 4.4f, 0.52f, 0.68f), new InspectClip(KATANA_DEPLOY, 2.0f));
        skin.scabbard = KnifeModels.katanaScabbard();
        skin.saddleX = 20.0f;
        skin.saddleY = 0.0f;
        skin.centre = x -> x < 3.0f ? 0.5f : KnifeModels.katanaCentre(x);
        skin.bladeFrom = 3.0f;
        skin.bladeTo = 49.0f;
        skin.letGo = skin.findLetGo();
        return skin;
    }

    private float[] findLetGo() {
        float[] at = new float[16];
        float[] held = new float[16];
        for (int i = 0; i <= 2000; ++i) {
            this.deploy.sample((float)i / 2000.0f, at);
            if (at[13] < 1.0f) break;
            System.arraycopy(at, 0, held, 0, 16);
        }
        return held;
    }

    public int streakCount() {
        return this.streaks.length;
    }

    public float streakStrength(int streak) {
        return this.streaks[streak].strength;
    }

    public static Sink sink(final class_11659 queue, final int light, final KnifeMaterial material, final boolean foil) {
        final class_1921 solid = class_12249.method_75994((class_2960)WHITE);
        final int[] palette = material.palette;
        return new Sink(){

            @Override
            public void part(class_4587 part, KnifeMesh mesh, KnifeMesh.Shine shine) {
                queue.method_73483(part, solid, (entry, out) -> mesh.emit(entry, out, palette, light, shine));
                if (foil) {
                    queue.method_73529(1).method_73483(part, class_12249.method_75997(), (entry, out) -> mesh.emit(entry, out, palette, light, KnifeMesh.Shine.NONE));
                }
            }

            @Override
            public void glow(class_4587 part, KnifeMesh mesh, KnifeMesh.Shine shine) {
                queue.method_73529(1).method_73483(part, class_12249.method_76014((class_2960)WHITE), (entry, out) -> mesh.emitGlow(entry, out, material.glow, shine));
            }
        };
    }

    public void draw(class_4587 pose, float[] at, float weight, float size, Sink sink, float[] trail) {
        KnifeMesh.Shine wipe;
        Matrix4f knife = this.knifeMatrix(at, weight, size, new Matrix4f());
        Matrix4f sheath = this.scabbard != null && weight >= 0.999f && at[13] > 0.01f ? this.scabbardMatrix(at, size) : null;
        KnifeMesh.Shine shine = wipe = this.wipe(at, weight);
        if (wipe == null) {
            float sweep;
            sweep = KnifeSkin.shinePosition(at[10]);
            shine = new KnifeMesh.Shine(sweep, sweep > 0.0f ? class_3532.method_15374((double)((float)Math.PI * sweep)) * weight : 0.0f, 0.0f);
        }
        pose.method_22903();
        pose.method_34425((Matrix4fc)knife);
        for (int i = 0; i < this.parts.length; ++i) {
            float angle;
            Part part = this.parts[i];
            pose.method_22903();
            if (part.channel >= 0 && (angle = class_3532.method_15393((float)at[part.channel]) * weight) != 0.0f) {
                pose.method_49278((Quaternionfc)class_7833.field_40718.rotationDegrees(angle), part.pivotX, part.pivotY, 0.5f);
            }
            sink.part(pose, part.mesh, shine);
            if (wipe != null) {
                sink.glow(pose, part.mesh, wipe);
            }
            if (trail != null) {
                this.streaks(i, pose.method_23760().method_23761(), trail);
            }
            pose.method_22909();
        }
        pose.method_22909();
        if (sheath != null) {
            pose.method_22903();
            pose.method_34425((Matrix4fc)sheath);
            sink.part(pose, this.scabbard, KnifeMesh.Shine.NONE);
            pose.method_22909();
        }
    }

    public void drawHeld(class_4587 pose, float size, Sink sink) {
        float scale = this.voxel * 1.15f * size;
        pose.method_22903();
        pose.method_22905(scale, scale, scale);
        pose.method_46416(-this.gripX, -this.gripY, -0.5f);
        for (Part part : this.parts) {
            sink.part(pose, part.mesh, KnifeMesh.Shine.NONE);
        }
        pose.method_22909();
    }

    private void streaks(int part, Matrix4f m, float[] trail) {
        Vector3f p = new Vector3f();
        for (int s = 0; s < this.streaks.length; ++s) {
            Streak streak = this.streaks[s];
            if (streak.part != part) continue;
            m.transformPosition(streak.baseX, streak.baseY, 0.5f, p);
            trail[s * 6] = p.x;
            trail[s * 6 + 1] = p.y;
            trail[s * 6 + 2] = p.z;
            m.transformPosition(streak.tipX, streak.tipY, 0.5f, p);
            trail[s * 6 + 3] = p.x;
            trail[s * 6 + 4] = p.y;
            trail[s * 6 + 5] = p.z;
        }
    }

    private Matrix4f knifeMatrix(float[] at, float weight, float size, Matrix4f dest) {
        float turn = (float)Math.PI / 180 * weight;
        float scale = this.voxel * size;
        dest.translation(this.fist.x + at[0] * weight, this.fist.y + at[1] * weight, this.fist.z + at[2] * weight).rotate((Quaternionfc)new Quaternionf().rotateY(class_3532.method_15393((float)at[3]) * turn).rotateX(class_3532.method_15393((float)at[4]) * turn).rotateZ(class_3532.method_15393((float)at[5]) * turn)).rotate((Quaternionfc)this.rest);
        float flip = class_3532.method_15393((float)at[6]) * turn;
        if (flip != 0.0f) {
            dest.rotateX(flip);
        }
        return dest.scale(scale).translate(at[11] * weight - this.gripX, -this.gripY, -0.5f);
    }

    private Matrix4f scabbardMatrix(float[] at, float size) {
        boolean held = at[13] >= 1.0f;
        float[] state = held ? at : this.letGo;
        float radius = 312.5f;
        Matrix4f m = this.knifeMatrix(state, 1.0f, size, new Matrix4f()).rotateAround((Quaternionfc)new Quaternionf().rotationZ(state[12] / radius), 3.0f, radius, 0.5f).translate(0.05f, 0.0f, 0.0f);
        if (!held) {
            float fall = 1.0f - Math.max(at[13], 0.0f);
            fall *= fall;
            Vector3f hold = m.transformPosition(this.saddleX, this.saddleY, 0.5f, new Vector3f());
            m.translateLocal(-hold.x, -hold.y, -hold.z).rotateLocalZ(0.6981317f * fall).translateLocal(hold.x + KnifeSkin.SCABBARD_FALL.x * fall, hold.y + KnifeSkin.SCABBARD_FALL.y * fall, hold.z + KnifeSkin.SCABBARD_FALL.z * fall);
        }
        return m;
    }

    private KnifeMesh.Shine wipe(float[] at, float weight) {
        float touch;
        float f = touch = this.centre != null ? class_3532.method_15363((float)((at[14] * weight - 0.7f) / 0.3f), (float)0.0f, (float)1.0f) : 0.0f;
        if (touch <= 0.01f || at[15] < this.bladeFrom) {
            return null;
        }
        float along = (at[15] - this.bladeFrom) / (this.bladeTo - this.bladeFrom);
        return new KnifeMesh.Shine(along, touch * touch * (3.0f - 2.0f * touch), 16.0f);
    }

    public float sparkle(float[] at, float weight) {
        float flash = KnifeSkin.sparkle(at[10]);
        if (this.centre != null && at[15] >= this.bladeFrom) {
            float along = (at[15] - this.bladeFrom) / (this.bladeTo - this.bladeFrom);
            float leaving = class_3532.method_15363((float)((along - 0.96f) / 0.06f), (float)0.0f, (float)1.0f) * class_3532.method_15363((float)((1.3f - along) / 0.15f), (float)0.0f, (float)1.0f);
            flash = Math.max(flash, leaving * class_3532.method_15363((float)(at[14] * 1.6f), (float)0.0f, (float)1.0f));
        }
        return flash * weight;
    }

    static float shinePosition(float channel) {
        if (channel <= 0.0f) {
            return 0.0f;
        }
        float f = channel - 2.0f * (float)class_3532.method_15375((float)(channel / 2.0f));
        return f < 1.0f ? f : 0.0f;
    }

    static float sparkle(float channel) {
        float f = KnifeSkin.shinePosition(channel);
        return class_3532.method_15363((float)((f - 0.72f) / 0.16f), (float)0.0f, (float)1.0f) * class_3532.method_15363((float)((1.0f - f) / 0.07f), (float)0.0f, (float)1.0f);
    }

    public void glow(class_11659 queue, Matrix4f base, Matrix3f baseNormal, SlashTrail[] trails, long now, long lifetime, int rgb, float flash, float[] tip) {
        class_4587 hand = new class_4587();
        hand.method_23760().method_23761().set((Matrix4fc)base);
        hand.method_23760().method_23762().set((Matrix3fc)baseNormal);
        float tipX = tip[3];
        float tipY = tip[4];
        float tipZ = tip[5];
        float turn = (float)(now % 6283185307L) / 1.0E9f * 1.5f;
        int count = this.streakCount();
        queue.method_73529(1).method_73483(hand, class_12249.method_76014((class_2960)WHITE), (entry, out) -> {
            for (int s = 0; s < count; ++s) {
                trails[s].emit(entry, out, now, lifetime, rgb, this.streakStrength(s));
            }
            if (flash > 0.0f) {
                SlashTrail.sparkle(entry, out, tipX, tipY, tipZ, 0.07f * flash, turn, rgb, flash);
            }
        });
    }

    private record Part(KnifeMesh mesh, int channel, float pivotX, float pivotY) {
    }

    private record Streak(int part, float baseX, float baseY, float tipX, float tipY, float strength) {
    }

    private static interface Centre {
        public float y(float var1);
    }

    public static interface Sink {
        public void part(class_4587 var1, KnifeMesh var2, KnifeMesh.Shine var3);

        public void glow(class_4587 var1, KnifeMesh var2, KnifeMesh.Shine var3);
    }
}

