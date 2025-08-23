/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.gui;

import dev.lambdaurora.lambdabettergrass.LBGConfig;
import dev.lambdaurora.lambdabettergrass.LBGMode;
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.SpruceTexts;
import dev.lambdaurora.spruceui.option.SpruceBooleanOption;
import dev.lambdaurora.spruceui.option.SpruceCyclingOption;
import dev.lambdaurora.spruceui.option.SpruceOption;
import dev.lambdaurora.spruceui.option.SpruceSimpleActionOption;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.SpruceLabelWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.TextFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Text;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the LambdaBetterGrass settings screen.
 *
 * @author LambdAurora
 * @version 2.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class SettingsScreen extends SpruceScreen {
	public static final Text MOD_NAME = Text.translatable(LambdaBetterGrass.NAMESPACE);
	private static final String API_URL = "https://lambdaurora.dev/projects/lambdabettergrass/documentation/";
	private static final Text VERSION;

	private final LBGConfig config;
	private final Screen parent;

	private final SpruceOption modeOption;
	private final SpruceOption betterSnowOption;
	private final SpruceOption resetOption;

	static {
		String rawVersion = LambdaBetterGrass.VERSION;

		if (rawVersion.endsWith("-local")) {
			rawVersion = rawVersion.substring(0, rawVersion.length() - "-local".length());
		}

		var version = Text.literal('v' + rawVersion).withStyle(TextFormatting.GRAY);

		if (rawVersion.matches("^.+-rc\\.\\d+\\+.+$")) {
			version = version.append(Text.literal(" (Release Candidate)").withStyle(TextFormatting.GOLD));
		}

		/*if (LambdaBetterGrass.isDevMode()) {
			version = version.append(Text.literal(" (dev)").withStyle(TextFormatting.RED));
		}*/

		VERSION = version;
	}

	public SettingsScreen(@Nullable Screen parent) {
		super(Text.translatable("lambdabettergrass.menu.title", MOD_NAME));
		this.config = LambdaBetterGrass.get().config;
		this.parent = parent;

		this.modeOption = new SpruceCyclingOption("lambdabettergrass.option.mode",
				amount -> {
					this.config.setMode(this.config.getMode().next());
					if (this.client != null && this.client.levelRenderer != null)
						this.client.levelRenderer.allChanged();
				},
				option -> option.getDisplayText(this.config.getMode().getTranslatedText()),
				Text.translatable("lambdabettergrass.tooltip.mode",
						LBGMode.OFF.getTranslatedText(),
						LBGMode.FASTEST.getTranslatedText(),
						LBGMode.FAST.getTranslatedText(),
						LBGMode.FANCY.getTranslatedText()));

		this.betterSnowOption = new SpruceBooleanOption("lambdabettergrass.option.better_snow",
				this.config::hasBetterLayer,
				betterSnow -> {
					this.config.setBetterLayer(betterSnow);
					if (this.client != null && this.client.levelRenderer != null)
						this.client.levelRenderer.allChanged();
				},
				Text.translatable("lambdabettergrass.tooltip.better_snow"),
				true);

		this.resetOption = SpruceSimpleActionOption.reset(btn -> {
			this.config.reset();
			var client = Minecraft.getInstance();
			this.init(client, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
		});
	}

	@Override
	public void removed() {
		super.removed();
		this.config.save();
	}

	@Override
	public void onClose() {
		this.client.setScreen(this.parent);
	}

	@Override
	protected void init() {
		super.init();
		int buttonHeight = 20;

		this.addRenderableWidget(this.modeOption.createWidget(
				Position.of(this, this.width / 2 - 205, this.height / 4 - buttonHeight),
				200
		));
		this.addRenderableWidget(this.betterSnowOption.createWidget(
				Position.of(this.width / 2 + 5, this.height / 4 - buttonHeight), 200
		));

		this.buildLabels();

		this.addRenderableWidget(this.resetOption.createWidget(
				Position.of(this, this.width / 2 - 155, this.height - 29), 150
		));
		this.addRenderableWidget(new SpruceButtonWidget(
				Position.of(this, this.width / 2 - 155 + 160, this.height - 29), 150,
				buttonHeight, SpruceTexts.GUI_DONE,
				(buttonWidget) -> this.onClose()
		));
	}

	private void buildLabels() {
		this.addRenderableWidget(new SpruceLabelWidget(
				Position.of(0, 8), this.title.copy().withStyle(TextFormatting.WHITE),
				this.width, true
		));
		this.addRenderableWidget(new SpruceLabelWidget(
				Position.of(this.width - 4 - this.font.width(VERSION), 8), VERSION,
				this.width - 4
		));

		int y = this.height / 2;

		var text = Text.literal("");
		text.append(Text.translatable("lambdabettergrass.menu.title.info").withStyle(TextFormatting.GOLD, TextFormatting.BOLD));
		text.append("\n");
		text.append(Text.translatable("lambdabettergrass.menu.info.1").withStyle(TextFormatting.WHITE)).append("\n");
		text.append(Text.translatable("lambdabettergrass.menu.info.2").withStyle(TextFormatting.WHITE)).append(" ");
		text.append(Text.translatable("lambdabettergrass.menu.info.3").withStyle(TextFormatting.WHITE)).append("\n");
		var widget = this.addRenderableWidget(new SpruceLabelWidget(Position.of(this, 10, y),
				text, this.width - 20, true));
		var readMore = new SpruceLabelWidget(Position.of(this, 0, y + 5 + widget.getHeight()),
				Text.translatable("lambdabettergrass.menu.info.read_more", "[lambdaurora.dev]")
						.withStyle(TextFormatting.GREEN),
				this.width,
				label -> Util.getPlatform().openUri(API_URL), true);
		readMore.setTooltip(Text.translatable("chat.link.open"));
		this.addRenderableWidget(readMore);
	}

	@Override
	public void renderTitle(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
	}
}
