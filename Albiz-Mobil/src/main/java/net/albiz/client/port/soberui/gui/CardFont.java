// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.gui;

import net.minecraft.class_11719;
import net.minecraft.class_2583;
import net.minecraft.class_2960;

public record CardFont(class_2583 style, float ascent, float capTop) {
    public static final CardFont TEXT = new CardFont(class_2583.field_24360.method_27704((class_11719)new class_11719.class_11721(class_2960.method_60655((String)"albiz", (String)"geist_text"))), 0.8f, 0.2f);
    public static final CardFont LABEL = new CardFont(class_2583.field_24360.method_27704((class_11719)new class_11719.class_11721(class_2960.method_60655((String)"albiz", (String)"geist_label"))), 0.8f, 0.2f);
}

