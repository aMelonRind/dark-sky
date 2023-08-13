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
        config.apply();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .transparentBackground()
                .setTitle(Text.translatable("darkSky.config.title"));

        ConfigCategory category = builder.getOrCreateCategory(Text.empty());
        ConfigEntries entries = new ConfigEntries(builder.entryBuilder(), category);

        builder.setSavingRunnable(() -> {
            config = entries.createConfig().apply();
            config.write();
        });

        // why tf this isn't a thing
//        builder.setDiscardRunnable(() -> {
//            config.apply();
//        });

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
        private BooleanListEntry immediateModeField;
        private IntegerSliderEntry
                skySatFactorField, fogSatFactorField, bgSatFactorField,
                skyBriFactorField, fogBriFactorField, bgBriFactorField;

        public ConfigEntries(@NotNull ConfigEntryBuilder builder, @NotNull ConfigCategory category) {
            this.builder = builder;
            this.category = category;

            enabledField = builder.startBooleanToggle(translateField("enabled"), config.enabled)
                    .setDefaultValue(DEFAULT.enabled)
                    .setErrorSupplier(v -> {
                        if (immediateModeField != null && immediateModeField.getValue()) Config.enabled_ = v;
                        return Optional.empty();
                    })
                    .build();
            category.addEntry(enabledField);

            immediateModeField = builder.startBooleanToggle(translateField("immediateMode"), false)
                    .setTooltip(
                            translateField("immediateMode.tooltip1"),
                            translateField("immediateMode.tooltip2")
                    )
                    .setErrorSupplier(v -> {
                        if (v) {
                            Config.enabled_ = enabledField.getValue();
                            if (bgBriFactorField == null) return Optional.empty();
                            Config.skySatFactor = (float) skySatFactorField.getValue() / 100.0f;
                            Config.fogSatFactor = (float) fogSatFactorField.getValue() / 100.0f;
                            Config.bgSatFactor  = (float) bgSatFactorField.getValue()  / 100.0f;
                            Config.skyBriFactor = (float) skyBriFactorField.getValue() / 100.0f;
                            Config.fogBriFactor = (float) fogBriFactorField.getValue() / 100.0f;
                            Config.bgBriFactor  = (float) bgBriFactorField.getValue()  / 100.0f;
                        } else config.apply();
                        return Optional.empty();
                    })
                    .build();
            category.addEntry(immediateModeField);

            skySatFactorField = createSliderField("skySatFactor", config.skySat, DEFAULT.skySat, v -> Config.skySatFactor = (float) v / 100.0f);
            fogSatFactorField = createSliderField("fogSatFactor", config.fogSat, DEFAULT.fogSat, v -> Config.fogSatFactor = (float) v / 100.0f);
            bgSatFactorField  = createSliderField("bgSatFactor",  config.bgSat,  DEFAULT.bgSat,  v -> Config.bgSatFactor  = (float) v / 100.0f);
            skyBriFactorField = createSliderField("skyBriFactor", config.skyBri, DEFAULT.skyBri, v -> Config.skyBriFactor = (float) v / 100.0f);
            fogBriFactorField = createSliderField("fogBriFactor", config.fogBri, DEFAULT.fogBri, v -> Config.fogBriFactor = (float) v / 100.0f);
            bgBriFactorField  = createSliderField("bgBriFactor",  config.bgBri,  DEFAULT.bgBri,  v -> Config.bgBriFactor  = (float) v / 100.0f);
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
                        if (immediateModeField.getValue() && changeCallback != null) changeCallback.accept(v);
                        if (v == 0) return TEXT_NONE;
                        return Text.of((v > 0 ? "+" : "") + v + "%");
                    })
                    .build();
            category.addEntry(entry);
            return entry;
        }

    }

}
