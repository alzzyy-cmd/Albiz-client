// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.mixin;

import net.minecraft.class_4267;
import net.minecraft.class_8573;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_4267.class_4270.class})
public interface ServerEntryAccessor {
    @Accessor(value="field_19123")
    public class_8573 sober$icon();

    @Accessor(value="field_42874")
    public byte[] sober$favicon();
}

