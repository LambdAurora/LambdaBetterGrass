/*
 * Copyright © 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdabettergrass.gui;

import me.lambdaurora.lambdabettergrass.LBGConfig;
import me.lambdaurora.lambdabettergrass.LBGMode;
import me.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import me.lambdaurora.spruceui.Position;
import me.lambdaurora.spruceui.SpruceTexts;
import me.lambdaurora.spruceui.option.SpruceBooleanOption;
import me.lambdaurora.spruceui.option.SpruceCyclingOption;
import me.lambdaurora.spruceui.option.SpruceOption;
import me.lambdaurora.spruceui.option.SpruceSimpleActionOption;
import me.lambdaurora.spruceui.screen.SpruceScreen;
import me.lambdaurora.spruceui.widget.SpruceButtonWidget;
import me.lambdaurora.spruceui.widget.SpruceLabelWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the LambdaBetterGrass settings screen.
 *
 * @author LambdAurora
 * @version 1.0.2
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class SettingsScreen extends SpruceScreen {
    private static final String API_URL = "https://github.com/LambdAurora/LambdaBetterGrass/blob/1.17/documentation/API.md";

    private final LBGConfig config;
    private final Screen parent;

    private final SpruceOption modeOption;
    private final SpruceOption betterSnowOption;
    private final SpruceOption resetOption;

    public SettingsScreen(@Nullable Screen parent) {
        super(new TranslatableText("lambdabettergrass.menu.title"));
        this.config = LambdaBetterGrass.get().config;
        this.parent = parent;

        this.modeOption = new SpruceCyclingOption("lambdabettergrass.option.mode",
                amount -> {
                    this.config.setMode(this.config.getMode().next());
                    if (this.client != null && this.client.worldRenderer != null)
                        this.client.worldRenderer.reload();
                },
                option -> option.getDisplayText(this.config.getMode().getTranslatedText()),
                new TranslatableText("lambdabettergrass.tooltip.mode",
                        LBGMode.OFF.getTranslatedText(),
                        LBGMode.FASTEST.getTranslatedText(),
                        LBGMode.FAST.getTranslatedText(),
                        LBGMode.FANCY.getTranslatedText()));

        this.betterSnowOption = new SpruceBooleanOption("lambdabettergrass.option.better_snow",
                this.config::hasBetterLayer,
                betterSnow -> {
                    this.config.setBetterLayer(betterSnow);
                    if (this.client != null && this.client.worldRenderer != null)
                        this.client.worldRenderer.reload();
                },
                new TranslatableText("lambdabettergrass.tooltip.better_snow"),
                true);

        this.resetOption = SpruceSimpleActionOption.reset(btn -> {
            this.config.reset();
            MinecraftClient client = MinecraftClient.getInstance();
            this.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        });
    }

    @Override
    public void onClose() {
        this.client.openScreen(this.parent);
    }

    @Override
    protected void init() {
        super.init();
        int buttonHeight = 20;

        this.addChild(this.modeOption.createWidget(Position.of(this, this.width / 2 - 205, this.height / 4 - buttonHeight), 200));
        this.addChild(this.betterSnowOption.createWidget(Position.of(this.width / 2 + 5, this.height / 4 - buttonHeight), 200));

        this.buildLabels();

        this.addChild(this.resetOption.createWidget(Position.of(this, this.width / 2 - 155, this.height - 29), 150));
        this.addChild(new SpruceButtonWidget(Position.of(this, this.width / 2 - 155 + 160, this.height - 29), 150,
                buttonHeight, SpruceTexts.GUI_DONE,
                (buttonWidget) -> this.onClose()));
    }

    private void buildLabels() {
        int y = this.height / 2;

        MutableText text = new LiteralText("");
        text.append(new TranslatableText("lambdabettergrass.menu.title.info").formatted(Formatting.GOLD, Formatting.BOLD));
        text.append("\n");
        text.append(new TranslatableText("lambdabettergrass.menu.info.1")).append("\n");
        text.append(new TranslatableText("lambdabettergrass.menu.info.2")).append(" ");
        text.append(new TranslatableText("lambdabettergrass.menu.info.3")).append("\n");
        SpruceLabelWidget widget = this.addChild(new SpruceLabelWidget(Position.of(this, this.width / 2, y),
                text, this.width, true));
        SpruceLabelWidget readMore = new SpruceLabelWidget(Position.of(this, this.width / 2, y + 5 + widget.getHeight()),
                new TranslatableText("lambdabettergrass.menu.info.read_more", "[GitHub]").formatted(Formatting.GREEN),
                this.width,
                label -> Util.getOperatingSystem().open(API_URL), true);
        readMore.setTooltip(new TranslatableText("chat.link.open"));
        this.addChild(readMore);
    }

    @Override
    public void renderTitle(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 16777215);
    }
}
