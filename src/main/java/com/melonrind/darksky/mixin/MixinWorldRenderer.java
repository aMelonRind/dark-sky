package com.melonrind.darksky.mixin;

import com.melonrind.darksky.ColorDimmer;
import com.melonrind.darksky.config.Config;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @Inject(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At("HEAD"))
    public void mixinRenderSky(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean bl, Runnable runnable, CallbackInfo ci) {
        if (!Config.enabled_) return;
        ColorDimmer.dimBackgroundColor(
                MixinBackgroundRenderer.getRed(),
                MixinBackgroundRenderer.getGreen(),
                MixinBackgroundRenderer.getBlue()
        );
        RenderSystem.clear(16640, MinecraftClient.IS_SYSTEM_MAC);
    }

}
