/*
 * Copyright © 2021-2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the LambdaBetterGrass settings screen.
 *
 * @author LambdAurora
 * @version 1.3.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class SettingsScreen extends SpruceScreen {
	private static final String API_URL = "https://github.com/LambdAurora/LambdaBetterGrass/blob/1.18/documentation/API.md";

	private final LBGConfig config;
	private final Screen parent;

	private final SpruceOption modeOption;
	private final SpruceOption betterSnowOption;
	private final SpruceOption resetOption;

	public SettingsScreen(@Nullable Screen parent) {
		super(Text.translatable("lambdabettergrass.menu.title"));
		this.config = LambdaBetterGrass.get().config;
		this.parent = parent;

		this.modeOption = new SpruceCyclingOption("lambdabettergrass.option.mode",
				amount -> {
					this.config.setMode(this.config.getMode().next());
					if (this.client != null && this.client.worldRenderer != null)
						this.client.worldRenderer.reload();
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
					if (this.client != null && this.client.worldRenderer != null)
						this.client.worldRenderer.reload();
				},
				Text.translatable("lambdabettergrass.tooltip.better_snow"),
				true);

		this.resetOption = SpruceSimpleActionOption.reset(btn -> {
			this.config.reset();
			var client = MinecraftClient.getInstance();
			this.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
		});
	}

	@Override
	public void closeScreen() {
		this.client.setScreen(this.parent);
	}

	@Override
	protected void init() {
		super.init();
		int buttonHeight = 20;

		this.addDrawableChild(this.modeOption.createWidget(Position.of(this, this.width / 2 - 205, this.height / 4 - buttonHeight),
				200));
		this.addDrawableChild(this.betterSnowOption.createWidget(Position.of(this.width / 2 + 5, this.height / 4 - buttonHeight), 200));

		this.buildLabels();

		this.addDrawableChild(this.resetOption.createWidget(Position.of(this, this.width / 2 - 155, this.height - 29), 150));
		this.addDrawableChild(new SpruceButtonWidget(Position.of(this, this.width / 2 - 155 + 160, this.height - 29), 150,
				buttonHeight, SpruceTexts.GUI_DONE,
				(buttonWidget) -> this.closeScreen()));
	}

	private void buildLabels() {
		int y = this.height / 2;

		var text = Text.literal("");
		text.append(Text.translatable("lambdabettergrass.menu.title.info").formatted(Formatting.GOLD, Formatting.BOLD));
		text.append("\n");
		text.append(Text.translatable("lambdabettergrass.menu.info.1")).append("\n");
		text.append(Text.translatable("lambdabettergrass.menu.info.2")).append(" ");
		text.append(Text.translatable("lambdabettergrass.menu.info.3")).append("\n");
		var widget = this.addDrawableChild(new SpruceLabelWidget(Position.of(this, 0, y),
				text, this.width, true));
		var readMore = new SpruceLabelWidget(Position.of(this, 0, y + 5 + widget.getHeight()),
				Text.translatable("lambdabettergrass.menu.info.read_more", "[GitHub]").formatted(Formatting.GREEN),
				this.width,
				label -> Util.getOperatingSystem().open(API_URL), true);
		readMore.setTooltip(Text.translatable("chat.link.open"));
		this.addDrawableChild(readMore);
	}

	@Override
	public void renderTitle(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 16777215);
	}
}
