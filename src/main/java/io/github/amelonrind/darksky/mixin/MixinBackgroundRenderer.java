package io.github.amelonrind.darksky.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.amelonrind.darksky.ColorDimmer;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer {
    @Shadow private static float red;
    @Shadow private static float green;
    @Shadow private static float blue;

    @Inject(method = "render", at = @At(value = "TAIL"))
    private static void mutateBackgroundColor(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness, CallbackInfo ci) {
        ColorDimmer.dimBackgroundColor(red, green, blue, (r, g, b) -> RenderSystem.clearColor(r, g, b, 0.0F));
    }

}
