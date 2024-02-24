package io.github.amelonrind.darksky.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import io.github.amelonrind.darksky.ColorDimmer;
import io.github.amelonrind.darksky.DarkSky;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import static io.github.amelonrind.darksky.DarkSky.LOGGER;

public class Config {
    public static final ConfigClassHandler<Config> HANDLER = ConfigClassHandler.createBuilder(Config.class)
            .id(new Identifier(DarkSky.MOD_ID, "main"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve(DarkSky.MOD_ID + ".json"))
                    .build())
            .build();

    public static Config get() {
        return HANDLER.instance();
    }

    @SerialEntry
    public boolean enabled = true;
    @SerialEntry
    public int skySat = 100;
    @SerialEntry
    public int fogSat = 100;
    @SerialEntry
    public int skyBri = -60;
    @SerialEntry
    public int fogBri = -60;

    public void apply() {
        skySat = MathHelper.clamp(skySat, -100, 200);
        fogSat = MathHelper.clamp(fogSat, -100, 200);
        skyBri = MathHelper.clamp(skyBri, -100, 200);
        fogBri = MathHelper.clamp(fogBri, -100, 200);
        ColorDimmer.skySatFactor = skySat / 100.0f;
        ColorDimmer.fogSatFactor = fogSat / 100.0f;
        ColorDimmer.skyBriFactor = skyBri / 100.0f;
        ColorDimmer.fogBriFactor = fogBri / 100.0f;

        DarkSky.enabled = enabled;
        LOGGER.info("[Dark Sky] applied config");
    }

    public Config write(@NotNull Config other) {
        other.enabled = enabled;
        other.skySat = skySat;
        other.fogSat = fogSat;
        other.skyBri = skyBri;
        other.fogBri = fogBri;
        return this;
    }

}
