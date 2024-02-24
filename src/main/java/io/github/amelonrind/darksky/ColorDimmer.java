package io.github.amelonrind.darksky;

import io.github.amelonrind.darksky.config.Config;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class ColorDimmer {

    public static void dimBackgroundColor(float red, float green, float blue, @NotNull ColorConsumer consumer) {
        if (Config.enabled_) dimColor(red, green, blue, Config.bgBriFactor, Config.bgSatFactor, consumer);
    }

    public static void dimSkyColor(@NotNull CallbackInfoReturnable<Vec3d> cir) {
        Vec3d ret = cir.getReturnValue();
        if (Config.enabled_) dimColor((float) ret.x, (float) ret.y, (float) ret.z, Config.skyBriFactor, Config.skySatFactor, (r, g, b) -> cir.setReturnValue(new Vec3d(r, g, b)));
    }

    public static void dimFogColor(@NotNull CallbackInfoReturnable<float[]> cir) {
        float[] color = cir.getReturnValue();
        if (Config.enabled_) dimColor(color[0], color[1], color[2], Config.fogBriFactor, Config.fogSatFactor, (r, g, b) -> cir.setReturnValue(new float[]{r, g, b, color[3]}));
    }

    private static void dimColor(float r, float g, float b, float briFactor, float satFactor, ColorConsumer consumer) {
        float max = Math.max(r, Math.max(g, b));
        if (max == 0) return;
        float min = Math.min(r, Math.min(g, b));

        float origSat = 1 - min / max;
        if (origSat > 0) {
            float satMul = calculateMultiplier(origSat, satFactor, max);
            r = max - (max - r) * satMul;
            g = max - (max - g) * satMul;
            b = max - (max - b) * satMul;
        }

        float briMul = calculateMultiplier(max, briFactor, max);
        consumer.accept(r * briMul, g * briMul, b * briMul);
    }

    private static float calculateMultiplier(float value, float factor, float impact) {
        return MathHelper.clamp(value * (1.0f + factor * impact), 0.0f, 1.0f) / value;
    }

    @FunctionalInterface
    public interface ColorConsumer {
        void accept(float r, float g, float b);
    }

}
