// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.minecraft.class_11719;
import net.minecraft.class_2960;

public final class Fonts {
    public static final class_11719.class_11721 VANILLA = new class_11719.class_11721(class_2960.method_60655((String)"albiz", (String)"vanilla"));
    public static final class_11719.class_11721 UNIFONT = new class_11719.class_11721(class_2960.method_60656((String)"uniform"));

    private Fonts() {
    }

    public static enum Weight {
        Regular,
        Medium,
        Bold,
        Black;


        public Weight heavier() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0, 1 -> Bold;
                case 2, 3 -> Black;
            };
        }
    }

    public static enum Family {
        Geist("Geist", 0.98f, "geist", "medium", "medium", "semibold", "semibold"),
        Ubuntu("Ubuntu", 1.0f, "ubuntu", "regular", "medium", "bold", "bold"),
        Xuong("Xuong", 1.0f, "xuong", "regular", "regular", "regular", "regular"),
        Inter("Inter", 0.96f, "inter", "regular", "regular", "bold", "bold"),
        Roboto("Roboto", 0.985f, "roboto", "regular", "medium", "bold", "black"),
        Montserrat("Montserrat", 0.98f, "montserrat", "regular", "medium", "bold", "black"),
        SourceSans("Source Sans", 1.06f, "sourcesans", "regular", "semibold", "bold", "bold"),
        Nunito("Nunito", 1.015f, "nunito", "regular", "regular", "regular", "regular"),
        Isometra("Isometra", 0.87f, "isometra", "regular", "regular", "regular", "regular"),
        RobotoMono("Roboto Mono", 0.985f, "robotomono", "regular", "medium", "bold", "bold"),
        Consolas("Consolas (Roboto Mono)", 1.075f, "robotomono", "regular", "regular", "bold", "bold"),
        EduQLDHand("Edu QLD Hand", 1.02f, "eduqldhand", "regular", "regular", "bold", "bold"),
        BlackOpsOne("Black Ops One", 1.03f, "blackopsone", "regular", "regular", "regular", "regular"),
        Caveat("Caveat", 1.25f, "caveat", "regular", "regular", "bold", "bold"),
        Minecraft("Minecraft", 1.0f, null, new String[0]),
        Unifont("Unifont", 1.0f, null, new String[0]);

        private final String title;
        private final float scale;
        private final String key;
        private final String[] files;
        private final class_11719.class_11721[] weights = new class_11719.class_11721[Weight.values().length];
        private final class_11719.class_11721[] large = new class_11719.class_11721[Weight.values().length];

        private Family(String title, float scale, String key, String ... files) {
            this.title = title;
            this.scale = scale;
            this.key = key;
            this.files = files;
            if (key == null) {
                return;
            }
            for (int i = 0; i < this.weights.length; ++i) {
                this.weights[i] = new class_11719.class_11721(class_2960.method_60655((String)"albiz", (String)(key + "_" + files[i])));
                this.large[i] = new class_11719.class_11721(class_2960.method_60655((String)"albiz", (String)(key + "_" + files[i] + "_hd")));
            }
        }

        public float scale() {
            return this.scale;
        }

        public class_11719.class_11721 large(Weight weight) {
            return this.large[weight.ordinal()];
        }

        public boolean custom() {
            return this.weights[0] != null;
        }

        public boolean hasWeights() {
            return this.custom() && !this.weights[0].equals((Object)this.weights[this.weights.length - 1]);
        }

        public class_11719.class_11721 font(Weight weight) {
            return this.weights[weight.ordinal()];
        }

        public String file(Weight weight) {
            return this.key == null ? null : this.key + "-" + this.files[weight.ordinal()] + ".ttf";
        }

        public String toString() {
            return this.title;
        }
    }
}

