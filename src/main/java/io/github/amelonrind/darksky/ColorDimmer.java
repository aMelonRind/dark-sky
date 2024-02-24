package io.github.amelonrind.darksky;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class ColorDimmer {
    public static float skySatFactor = 1.0f;
    public static float fogSatFactor = 1.0f;
    public static float skyBriFactor = -0.6f;
    public static float fogBriFactor = -0.6f;

    public static final Color<Color<Void>> lastBg = new Color<>(new Color<>(null));
    public static final Color<Vec3d> lastSky = new Color<>(new Vec3d(0, 0, 0));
    public static final Color<float[]> lastFog = new Color<>(new float[]{0, 0, 0, 0});

    public static void dimBackgroundColor(float red, float green, float blue, @NotNull ColorConsumer consumer) {
        boolean equals = lastBg.checkEquality(red, green, blue);
        if (!DarkSky.enabled) return;
        if (equals) {
            consumer.accept(lastBg.t.r, lastBg.t.g, lastBg.t.b);
            return;
        }
        lastBg.t.setColor(red, green, blue);
        dimColor(red, green, blue, fogBriFactor, fogSatFactor, (r, g, b) -> {
            lastBg.t.setColor(r, g, b);
            consumer.accept(r, g, b);
        });
    }

    public static void dimSkyColor(@NotNull CallbackInfoReturnable<Vec3d> cir) {
        Vec3d vec = cir.getReturnValue();
        boolean equals = lastSky.checkEquality((float) vec.x, (float) vec.y, (float) vec.z);
        if (!DarkSky.enabled) return;
        if (equals) {
            cir.setReturnValue(lastSky.t);
            return;
        }
        lastSky.t = vec;
        dimColor(lastSky.r, lastSky.g, lastSky.b, skyBriFactor, skySatFactor, (r, g, b) -> {
            lastSky.t = new Vec3d(r, g, b);
            cir.setReturnValue(lastSky.t);
        });
    }

    public static void dimFogColor(@NotNull CallbackInfoReturnable<float[]> cir) {
        if (!DarkSky.enabled) return;
        float[] color = cir.getReturnValue();
        if (lastFog.checkEquality(color[0], color[1], color[2]) && lastFog.t[3] == color[3]) {
            cir.setReturnValue(lastFog.t);
            return;
        }
        lastFog.t[0] = color[0];
        lastFog.t[1] = color[1];
        lastFog.t[2] = color[2];
        lastFog.t[3] = color[3];
        dimColor(color[0], color[1], color[2], fogBriFactor, fogSatFactor, (r, g, b) -> {
            lastFog.t[0] = r;
            lastFog.t[1] = g;
            lastFog.t[2] = b;
            cir.setReturnValue(lastFog.t);
        });
    }

    public static void dimColor(float r, float g, float b, float briFactor, float satFactor, ColorConsumer consumer) {
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

    public static class Color<T> {
        public boolean dirty = false;
        public float r = 0.0f;
        public float g = 0.0f;
        public float b = 0.0f;
        T t;

        Color(T t) {
            this.t = t;
        }

        boolean checkEquality(float r, float g, float b) {
            if (dirty) dirty = false;
            else if (this.r == r && this.g == g && this.b == b) return true;
            setColor(r, g, b);
            return false;
        }

        void setColor(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

    }

}
