package com.melonrind.darksky.mixin;

import com.melonrind.darksky.ColorDimmer;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import static com.melonrind.darksky.DarkSky.config;

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
        return config.enabled ? ColorDimmer.dimFogColor(shaderFogColor) : shaderFogColor;
    }

}