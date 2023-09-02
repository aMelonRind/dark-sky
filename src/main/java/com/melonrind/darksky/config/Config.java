package com.melonrind.darksky.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonWriter;
import com.melonrind.darksky.DarkSky;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {

    private static final Logger LOGGER = LogManager.getLogger(DarkSky.class);
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(DarkSky.MOD_ID + ".json").toFile();

    public final boolean enabled;
    public static boolean enabled_;
    public final int
            skySat, fogSat, bgSat,
            skyBri, fogBri, bgBri;
    // converted factors
    public static float
            skySatFactor, fogSatFactor, bgSatFactor,
            skyBriFactor, fogBriFactor, bgBriFactor;

    public static final Config DEFAULT = new Config(
            true,
            100, 100, 100,
            -60, -60, -60
    ).apply();

    public Config(
            boolean enabled,
            int skySat, int fogSat, int bgSat,
            int skyBri, int fogBri, int bgBri
    ) {
        this.enabled = enabled;

        this.skySat = MathHelper.clamp(skySat, -100, 200);
        this.fogSat = MathHelper.clamp(fogSat, -100, 200);
        this.bgSat  = MathHelper.clamp(bgSat,  -100, 200);
        this.skyBri = MathHelper.clamp(skyBri, -100, 200);
        this.fogBri = MathHelper.clamp(fogBri, -100, 200);
        this.bgBri  = MathHelper.clamp(bgBri,  -100, 200);
    }

    public Config apply() {
        skySatFactor = (float) skySat / 100.0f;
        fogSatFactor = (float) fogSat / 100.0f;
        bgSatFactor  = (float) bgSat  / 100.0f;
        skyBriFactor = (float) skyBri / 100.0f;
        fogBriFactor = (float) fogBri / 100.0f;
        bgBriFactor  = (float) bgBri  / 100.0f;

        enabled_ = enabled;
        LOGGER.info("[Dark Sky] applied config");

        return this;
    }

    public static Config read() {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonElement el = JsonParser.parseReader(reader);
            if (!el.isJsonObject()) return DEFAULT.apply();

            JsonObject o = el.getAsJsonObject();
            return new Config(
                    readBoolean(o, "enabled", DEFAULT.enabled),
                    readInt(o, "skySat", DEFAULT.skySat),
                    readInt(o, "fogSat", DEFAULT.fogSat),
                    readInt(o, "bgSat",  DEFAULT.bgSat),
                    readInt(o, "skyBri", DEFAULT.skyBri),
                    readInt(o, "fogBri", DEFAULT.fogBri),
                    readInt(o, "bgBri",  DEFAULT.bgBri)
            );
        } catch (FileNotFoundException e) {
            DEFAULT.write();
            return DEFAULT;
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.error("[Dark Sky] Couldn't read " + CONFIG_FILE + ", loading default config");
            LOGGER.error(e.toString());
            return DEFAULT;
        }
    }

    private static boolean readBoolean(@NotNull JsonObject o, String key, boolean fallback) {
        JsonElement el = o.get(key);
        if (el == null) return fallback;

        try {
            return el.getAsBoolean();
        } catch (ClassCastException | IllegalStateException e) {
            LOGGER.warn("[Dark Sky] Invalid boolean '{}' for option '{}'", el, key);
            return fallback;
        }
    }

    private static int readInt(@NotNull JsonObject o, String key, int fallback) {
        JsonElement el = o.get(key);
        if (el == null) return fallback;

        try {
            return el.getAsInt();
        } catch (ClassCastException | IllegalStateException e) {
            LOGGER.warn("[Dark Sky] Invalid int '{}' for option '{}'", el, key);
            return fallback;
        }
    }

    public void write() {
        try (
                FileWriter fileWriter = new FileWriter(CONFIG_FILE);
                JsonWriter jsonWriter = new JsonWriter(fileWriter)
        ) {
            jsonWriter.setIndent("  ");
            jsonWriter.beginObject()
                    .name("enabled").value(enabled)
                    .name("skySat").value(skySat)
                    .name("fogSat").value(fogSat)
                    .name("bgSat").value(bgSat)
                    .name("skyBri").value(skyBri)
                    .name("fogBri").value(fogBri)
                    .name("bgBri").value(bgBri)
                    .endObject();
        } catch (IOException e) {
            LOGGER.error("[Dark Sky] Couldn't write settings to " + CONFIG_FILE);
            LOGGER.error(e.toString());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Config config)) return false;

        return enabled == config.enabled
                && skySat == config.skySat
                && fogSat == config.fogSat
                && bgSat == config.bgSat
                && skyBri == config.skyBri
                && fogBri == config.fogBri
                && bgBri == config.bgBri;
    }

}
