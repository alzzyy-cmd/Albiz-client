// Adapted from SoberClient (Violet), GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.mixin;

import net.minecraft.class_11228;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={class_11228.class})
public interface GuiRendererAccessor {
    @Invoker(value="method_70896")
    public void albizGlass$resetItemAtlas();

    @Invoker(value="method_72109")
    public void albizGlass$clearOversizedItems();
}

