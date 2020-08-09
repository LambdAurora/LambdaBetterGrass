/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
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
import me.lambdaurora.spruceui.SpruceLabelWidget;
import me.lambdaurora.spruceui.SpruceTexts;
import me.lambdaurora.spruceui.Tooltip;
import me.lambdaurora.spruceui.option.SpruceBooleanOption;
import me.lambdaurora.spruceui.option.SpruceCyclingOption;
import me.lambdaurora.spruceui.option.SpruceResetOption;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.options.Option;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the LambdaBetterGrass settings screen.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class SettingsScreen extends Screen
{
    private static final String API_URL = "https://github.com/LambdAurora/LambdaBetterGrass/blob/mc1.16/API.md";

    private final LBGConfig config;
    private final Screen    parent;

    private final Option modeOption;
    private final Option betterSnowOption;
    private final Option resetOption;

    private final List<SpruceLabelWidget> labels = new ArrayList<>();

    public SettingsScreen(@Nullable Screen parent)
    {
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

        this.betterSnowOption = new SpruceBooleanOption("lambdabettergrass.option.better_layer",
                this.config::hasBetterLayer,
                betterSnow -> {
                    this.config.setBetterLayer(betterSnow);
                    if (this.client != null && this.client.worldRenderer != null)
                        this.client.worldRenderer.reload();
                },
                new TranslatableText("lambdabettergrass.tooltip.better_layer"));

        this.resetOption = new SpruceResetOption(btn -> {
            this.config.reset();
            MinecraftClient client = MinecraftClient.getInstance();
            this.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        });
    }

    @Override
    protected void init()
    {
        super.init();
        int buttonHeight = 20;

        this.addButton(this.modeOption.createButton(this.client.options, this.width / 2 - 155, this.height / 4 - buttonHeight, 150));
        this.addButton(this.betterSnowOption.createButton(this.client.options, this.width / 2 + 5, this.height / 4 - buttonHeight, 150));

        this.buildLabels();

        this.addButton(this.resetOption.createButton(this.client.options, this.width / 2 - 155, this.height - 29, 150));
        this.addButton(new ButtonWidget(this.width / 2 - 155 + 160, this.height - 29, 150, buttonHeight, SpruceTexts.GUI_DONE,
                (buttonWidget) -> this.client.openScreen(this.parent)));
    }

    private void buildLabels()
    {
        this.labels.clear();

        int y = this.height / 2;

        this.labels.add(new SpruceLabelWidget(this.width / 2, y, new TranslatableText("lambdabettergrass.menu.title.info").formatted(Formatting.GOLD, Formatting.BOLD), this.width, true));
        this.labels.add(new SpruceLabelWidget(this.width / 2, y += 5 + this.textRenderer.fontHeight, new TranslatableText("lambdabettergrass.menu.info.1"), this.width, true));
        this.labels.add(new SpruceLabelWidget(this.width / 2, y += 5 + this.textRenderer.fontHeight, new TranslatableText("lambdabettergrass.menu.info.2"), this.width, true));
        this.labels.add(new SpruceLabelWidget(this.width / 2, y += 5 + this.textRenderer.fontHeight, new TranslatableText("lambdabettergrass.menu.info.3"), this.width, true));
        SpruceLabelWidget readMore = new SpruceLabelWidget(this.width / 2, y + 5 + this.textRenderer.fontHeight,
                new TranslatableText("lambdabettergrass.menu.info.read_more", API_URL).formatted(Formatting.GREEN),
                this.width,
                label -> Util.getOperatingSystem().open(API_URL), true);
        readMore.setTooltip(new TranslatableText("chat.link.open"));
        this.labels.add(readMore);

        this.children.addAll(this.labels);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
    {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 16777215);

        for (SpruceLabelWidget label : this.labels)
            label.render(matrices, mouseX, mouseY, delta);

        Tooltip.renderAll(matrices);
    }
}
