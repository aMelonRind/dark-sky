package com.melonrind.darksky.mixin;

import com.melonrind.darksky.ColorDimmer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.melonrind.darksky.DarkSky.config;

@Mixin(ClientWorld.class)
public class MixinClientWorld {

    @Inject(method = "getSkyColor", at = @At("TAIL"), cancellable = true)
    public void mixinGetSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        if (config.enabled) ColorDimmer.dimSkyColor(cir);
    }

}
