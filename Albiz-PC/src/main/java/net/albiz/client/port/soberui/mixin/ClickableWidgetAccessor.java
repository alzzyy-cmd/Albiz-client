// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.mixin;

import net.minecraft.class_339;
import net.minecraft.class_9110;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_339.class})
public interface ClickableWidgetAccessor {
    @Accessor(value="field_41095")
    public class_9110 sober$tooltip();
}

