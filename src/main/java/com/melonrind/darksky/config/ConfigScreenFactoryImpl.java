package com.melonrind.darksky.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerSliderEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

import static com.melonrind.darksky.DarkSky.config;
import static com.melonrind.darksky.config.Config.DEFAULT;

public class ConfigScreenFactoryImpl implements ConfigScreenFactory<Screen> {

    private static final Text TEXT_NONE = Text.of("None");

    @Override
    public Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .transparentBackground()
                .setTitle(Text.translatable("darkSky.config.title"));

        ConfigCategory category = builder.getOrCreateCategory(Text.empty());
        ConfigEntries entries = new ConfigEntries(builder.entryBuilder(), category);

        builder.setSavingRunnable(() -> {
            config = entries.createConfig();
            config.write();
        });

        return builder.build();
    }

    @Contract(value = "_ -> new", pure = true)
    private static @NotNull Text translateField(String id) {
        return Text.translatable("darkSky.config.entry." + id);
    }

    private static class ConfigEntries {

        private final ConfigEntryBuilder builder;
        private final ConfigCategory category;
        private final BooleanListEntry enabledField;
        private final IntegerSliderEntry
                skySatFactorField, fogSatFactorField, bgSatFactorField,
                skyBriFactorField, fogBriFactorField, bgBriFactorField;

        public ConfigEntries(@NotNull ConfigEntryBuilder builder, @NotNull ConfigCategory category) {
            this.builder = builder;
            this.category = category;

            enabledField = builder.startBooleanToggle(translateField("enabled"), config.enabled)
                    .setDefaultValue(DEFAULT.enabled)
                    .setErrorSupplier(v -> {
                        config.enabled = v;
                        return Optional.empty();
                    })
                    .build();
            category.addEntry(enabledField);

            skySatFactorField = createSliderField("skySatFactor", config.skySat, DEFAULT.skySat, v -> config.skySatFactor = (float) v / 100.0f);
            fogSatFactorField = createSliderField("fogSatFactor", config.fogSat, DEFAULT.fogSat, v -> config.fogSatFactor = (float) v / 100.0f);
            bgSatFactorField  = createSliderField("bgSatFactor",  config.bgSat,  DEFAULT.bgSat,  v -> config.bgSatFactor  = (float) v / 100.0f);
            skyBriFactorField = createSliderField("skyBriFactor", config.skyBri, DEFAULT.skyBri, v -> config.skyBriFactor = (float) v / 100.0f);
            fogBriFactorField = createSliderField("fogBriFactor", config.fogBri, DEFAULT.fogBri, v -> config.fogBriFactor = (float) v / 100.0f);
            bgBriFactorField  = createSliderField("bgBriFactor",  config.bgBri,  DEFAULT.bgBri,  v -> config.bgBriFactor  = (float) v / 100.0f);
        }

        @Contract(" -> new")
        public @NotNull Config createConfig() {
            return new Config(
                    enabledField.getValue(),
                    skySatFactorField.getValue(), fogSatFactorField.getValue(), bgSatFactorField.getValue(),
                    skyBriFactorField.getValue(), fogBriFactorField.getValue(), bgBriFactorField.getValue()
            );
        }

        private @NotNull IntegerSliderEntry createSliderField(String id, int value, int defaultValue, @Nullable Consumer<Integer> changeCallback) {
            IntegerSliderEntry entry = builder.startIntSlider(translateField(id), value, -100, 200)
                    .setDefaultValue(defaultValue)
                    .setTextGetter(v -> {
                        if (changeCallback != null) changeCallback.accept(v);
                        if (v == 0) return TEXT_NONE;
                        return Text.of((v > 0 ? "+" : "") + v + "%");
                    })
                    .build();
            category.addEntry(entry);
            return entry;
        }

    }

}
