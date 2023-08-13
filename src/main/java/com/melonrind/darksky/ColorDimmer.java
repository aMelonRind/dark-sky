package com.melonrind.darksky;

import com.melonrind.darksky.config.Config;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class ColorDimmer {

    public static void dimSkyColor(@NotNull CallbackInfoReturnable<Vec3d> cir) {
        Vec3d ret = cir.getReturnValue();
        float[] dimmed = dimColor(new float[]{(float) ret.x, (float) ret.y, (float) ret.z}, Config.skyBriFactor, Config.skySatFactor);
        cir.setReturnValue(new Vec3d(dimmed[0], dimmed[1], dimmed[2]));
    }

    public static void dimBackgroundColor(float red, float green, float blue) {
        float[] dimmed = dimColor(new float[]{red, green, blue}, Config.bgBriFactor, Config.bgSatFactor);
        RenderSystem.clearColor(dimmed[0], dimmed[1], dimmed[2], 0.0f);
    }

    @Contract("_ -> param1")
    public static float @NotNull [] dimFogColor(float @NotNull [] color) {
        return dimColor(color, Config.fogBriFactor, Config.fogSatFactor);
    }

    @Contract("_, _, _ -> param1")
    private static float @NotNull [] dimColor(float @NotNull [] color, float briFactor, float satFactor) {
        float max = Math.max(color[0], Math.max(color[1], color[2]));
        if (max == 0) return color;
        float min = Math.min(color[0], Math.min(color[1], color[2]));

        float origSat = 1 - min / max;
        float satMultiplier = MathHelper.clamp(origSat * (satFactor * max + 1.0f), 0.0f, 1.0f) / origSat;
        color[0] = max - (max - color[0]) * satMultiplier;
        color[1] = max - (max - color[1]) * satMultiplier;
        color[2] = max - (max - color[2]) * satMultiplier;

        float briMultiplier = MathHelper.clamp(max * (briFactor * max + 1.0f), 0.0f, 1.0f) / max;
        color[0] *= briMultiplier;
        color[1] *= briMultiplier;
        color[2] *= briMultiplier;

        return color;

        /* this code used java.awt.Color, which is not recommended in fabric
        float[] color = Color.RGBtoHSB((int) (red * 255), (int) (green * 255), (int) (blue * 255), null);
        float multiplier = color[2] * color[2];
        color[1] = MathHelper.clamp(color[1] * (satFactor * multiplier + 1.0f), 0.0f, 1.0f);
        color[2] = MathHelper.clamp(color[2] * (briFactor * multiplier + 1.0f), 0.0f, 1.0f);
        Color res = new Color(Color.HSBtoRGB(color[0], color[1], color[2]));
        color[0] = ((float) res.getRed()) / 255.0f;
        color[1] = ((float) res.getGreen()) / 255.0f;
        color[2] = ((float) res.getBlue()) / 255.0f;
        return color;
        */
    }

}
