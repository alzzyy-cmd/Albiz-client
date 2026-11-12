// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.mixin;

import net.minecraft.class_437;
import net.minecraft.class_500;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={class_500.class})
public interface MultiplayerScreenAccessor {
    @Accessor(value="field_3049")
    public class_437 sober$parent();

    @Invoker(value="method_2534")
    public void sober$refresh();
}

