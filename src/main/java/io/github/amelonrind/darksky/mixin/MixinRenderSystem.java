package io.github.amelonrind.darksky.mixin;

import io.github.amelonrind.darksky.ColorDimmer;
import io.github.amelonrind.darksky.config.Config;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderSystem.class)
public abstract class MixinRenderSystem {

    @Final
    @Shadow(remap = false)
    private static final float[] shaderFogColor = new float[]{0.0f, 0.0f, 0.0f, 0.0f};

    @Shadow(remap = false)
    public static void assertOnRenderThread() {}

    /**
     * @author MelonRind
     * @reason it's public static and i need to modify the return value
     */
    @Overwrite(remap = false)
    public static float[] getShaderFogColor() {
        assertOnRenderThread();
        return Config.enabled_ ? ColorDimmer.dimFogColor(shaderFogColor.clone()) : shaderFogColor;
    }

}
