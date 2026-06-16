/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.gui;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.function.Function;

/**
 * A dummy option to add a button leading to LambdaBetterGrass' settings.
 *
 * @author LambdAurora
 * @version 2.0.0
 * @since 1.1.2
 */
public final class LBGOption {
	private static final String KEY = LambdaBetterGrass.NAMESPACE;

	public static OptionInstance<Unit> getOption(Screen parent) {
		return new OptionInstance<>(
				KEY, OptionInstance.noTooltip(),
				(title, object) -> title,
				new DummyValueSet(parent),
				Unit.INSTANCE,
				unit -> {
				}
		);
	}

	private record DummyValueSet(Screen parent) implements OptionInstance.ValueSet<Unit> {
		@Override
		public Function<OptionInstance<Unit>, AbstractWidget> createButton(
				OptionInstance.TooltipSupplier<Unit> tooltipSupplier, Options options,
				int x, int y, int width, OptionInstance.ValueUpdateListener<? super Unit> onValueChanged
		) {
			return option -> Button.builder(
							Component.translatable(KEY), btn -> Minecraft.getInstance().setScreenAndShow(new SettingsScreen(this.parent))
					)
					.pos(x, y)
					.size(width, 20)
					.build();
		}

		@Override
		public Optional<Unit> validateValue(Unit value) {
			return Optional.of(Unit.INSTANCE);
		}

		@Override
		public Codec<Unit> codec() {
			return Codec.EMPTY.codec();
		}
	}
}
