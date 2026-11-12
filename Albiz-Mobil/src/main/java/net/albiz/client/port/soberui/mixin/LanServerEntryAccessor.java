// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.mixin;

import net.minecraft.class_1131;
import net.minecraft.class_4267;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_4267.class_4269.class})
public interface LanServerEntryAccessor {
    @Accessor(value="field_19114")
    public class_1131 sober$server();
}

