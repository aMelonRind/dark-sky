package io.github.amelonrind.darksky.mixin;

import io.github.amelonrind.darksky.ColorDimmer;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderSystem.class)
public abstract class MixinRenderSystem {

    @Inject(method = "getShaderFogColor", remap = false, at = @At("RETURN"), cancellable = true)
    private static void mutateFogColor(CallbackInfoReturnable<float[]> cir) {
        ColorDimmer.dimFogColor(cir);
    }

}
