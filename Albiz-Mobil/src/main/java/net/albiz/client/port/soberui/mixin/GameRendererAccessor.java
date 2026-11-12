// Adapted from SoberClient (Violet), GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.mixin;

import net.minecraft.class_11228;
import net.minecraft.class_4184;
import net.minecraft.class_757;
import net.minecraft.class_758;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={class_757.class})
public interface GameRendererAccessor {
    @Invoker(value="method_3196")
    public float albizGlass$getFov(class_4184 var1, float var2, boolean var3);

    @Accessor(value="field_60793")
    public class_758 albizGlass$getFogRenderer();

    @Invoker(value="method_3202")
    public boolean albizGlass$shouldRenderBlockOutline();

    @Accessor(value="field_59965")
    public class_11228 albizGlass$getGuiRenderer();
}

