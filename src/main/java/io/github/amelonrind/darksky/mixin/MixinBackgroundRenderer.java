package io.github.amelonrind.darksky.mixin;

import net.minecraft.client.render.BackgroundRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BackgroundRenderer.class)
public interface MixinBackgroundRenderer {

    @Accessor
    static float getRed() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static float getGreen() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static float getBlue() {
        throw new UnsupportedOperationException();
    }

}
