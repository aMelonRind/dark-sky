package io.github.amelonrind.darksky.config;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import io.github.amelonrind.darksky.ColorDimmer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static io.github.amelonrind.darksky.ColorDimmer.*;

public class ModMenuApiImpl implements ModMenuApi {
    private static final Text TITLE = Text.translatable("darkSky.config.title");
    private static final Text TEXT_NONE = Text.of("None");
    private static final CompletableFuture<Optional<ImageRenderer>> skyImage = new CompletableFuture<>();
    private static final CompletableFuture<Optional<ImageRenderer>> fogImage = new CompletableFuture<>();
    private static final ColorPreview skyColorPreview = new ColorPreview();
    private static final ColorPreview fogColorPreview = new ColorPreview();
    private static final Config cfg4preview = new Config();

    private static final Config def = Config.HANDLER.defaults();
    private static Config cfg = Config.get();

    static {
        skyImage.complete(Optional.of(skyColorPreview));
        fogImage.complete(Optional.of(fogColorPreview));
    }

    @Contract(value = "_ -> new", pure = true)
    private static @NotNull Text translate(String key) {
        return Text.translatable("darkSky.config.entry." + key);
    }

    private static Option<Integer> optionOf(String nameKey, Function<Config, Integer> getter, BiConsumer<Config, Integer> setter, boolean isSky) {
        return Option.<Integer>createBuilder()
                .name(translate(nameKey))
                .binding(getter.apply(def), () -> getter.apply(cfg), val -> setter.accept(cfg, val))
                .description(val -> {
                    setter.accept(cfg4preview, val);
                    if (isSky) {
                        skyColorPreview.setColor(lastSky.r, lastSky.g, lastSky.b);
                        ColorDimmer.dimColor(lastSky.r, lastSky.g, lastSky.b, cfg4preview.skyBri / 100.0f, cfg4preview.skySat / 100.0f, skyColorPreview::setColor);
                        return OptionDescription.createBuilder()
                                .text(formatColor(lastSky.getColor(), skyColorPreview.getColor()))
                                .customImage(skyImage)
                                .build();
                    } else {
                        fogColorPreview.setColor(lastBg.r, lastBg.g, lastBg.b);
                        ColorDimmer.dimColor(lastBg.r, lastBg.g, lastBg.b, cfg4preview.fogBri / 100.0f, cfg4preview.fogSat / 100.0f, fogColorPreview::setColor);
                        return OptionDescription.createBuilder()
                                .text(formatColor(lastBg.getColor(), fogColorPreview.getColor()))
                                .customImage(fogImage)
                                .build();
                    }
                })
                .controller(option -> IntegerSliderControllerBuilder.create(option)
                        .range(-100, 200)
                        .step(1)
                        .formatValue(v -> {
                            if (v == 0) return TEXT_NONE;
                            return Text.of((v > 0 ? "+" : "") + v + "%");
                        }))
                .build();
    }

    @Contract("_, _ -> new")
    private static @NotNull Text formatColor(int from, int to) {
        return Text.literal(String.format("#%06X -> #%06X", from, to));
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return p -> {
            cfg = Config.get().write(cfg4preview);
            return YetAnotherConfigLib.createBuilder()
                    .title(TITLE)
                    .category(ConfigCategory.createBuilder()
                            .name(TITLE)
                            .option(Option.<Boolean>createBuilder()
                                    .name(translate("enabled"))
                                    .binding(def.enabled, () -> cfg.enabled, val -> cfg.enabled = val)
                                    .controller(TickBoxControllerBuilder::create)
                                    .build())
                            .option(optionOf("skySatFactor", cfg -> cfg.skySat, (cfg, val) -> cfg.skySat = val, true))
                            .option(optionOf("fogSatFactor", cfg -> cfg.fogSat, (cfg, val) -> cfg.fogSat = val, false))
                            .option(optionOf("skyBriFactor", cfg -> cfg.skyBri, (cfg, val) -> cfg.skyBri = val, true))
                            .option(optionOf("fogBriFactor", cfg -> cfg.fogBri, (cfg, val) -> cfg.fogBri = val, false))
                            .build())
                    .save(() -> {
                        cfg.apply();
                        Config.HANDLER.save();
                        lastBg.dirty = true;
                        lastSky.dirty = true;
                        lastFog.dirty = true;
                    })
                    .build()
                    .generateScreen(p);
        };
    }

    public static class ColorPreview implements ImageRenderer {
        public int r = 0;
        public int g = 0;
        public int b = 0;

        public void setColor(float r, float g, float b) {
            this.r = (int) (r * 255);
            this.g = (int) (g * 255);
            this.b = (int) (b * 255);
        }

        public int getColor() {
            return (r << 16) + (g << 8) + b;
        }

        @Override
        public int render(@NotNull DrawContext context, int x1, int y1, int renderWidth, float tickDelta) {
            MatrixStack matrices = context.getMatrices();
            matrices.push();

            Tessellator tess = Tessellator.getInstance();
//            BufferBuilder buf = tess.getBuffer();

            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);

            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            Matrix4f matrix = matrices.peek().getPositionMatrix();

            int x2 = x1 + renderWidth;
            int y2 = y1 + renderWidth;

            buf.vertex(matrix, x1, y2, 0).color(r, g, b, 255);
            buf.vertex(matrix, x2, y2, 0).color(r, g, b, 255);
            buf.vertex(matrix, x1, y1, 0).color(r, g, b, 255);
            buf.vertex(matrix, x2, y1, 0).color(r, g, b, 255);
            BufferRenderer.drawWithGlobalProgram(buf.end());

            matrices.pop();
            return renderWidth;
        }

        @Override
        public void close() {}

    }

}