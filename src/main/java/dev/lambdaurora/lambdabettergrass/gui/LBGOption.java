/*
 * Copyright © 2021, 2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.gui;

import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Option;
import net.minecraft.text.LiteralText;

/**
 * A dummy option to add a button leading to LambdaBetterGrass' settings.
 *
 * @author LambdAurora
 * @version 1.1.2
 * @since 1.1.2
 */
public class LBGOption extends Option {
	private final Screen parent;

	public LBGOption(Screen parent) {
		super("lambdabettergrass");
		this.parent = parent;
	}

	@Override
	public ClickableWidget createButton(GameOptions options, int x, int y, int width) {
		return new SpruceButtonWidget(Position.of(x, y), width, 20, new LiteralText("LambdaBetterGrass"),
				btn -> MinecraftClient.getInstance().setScreen(new SettingsScreen(this.parent)))
				.asVanilla();
	}
}
