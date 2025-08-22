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
import net.minecraft.network.chat.Text;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A dummy option to add a button leading to LambdaBetterGrass' settings.
 *
 * @author LambdAurora
 * @version 1.6.0
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
		public Function<OptionInstance<Unit>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<Unit> tooltipSupplier, Options options,
				int x, int y, int width, Consumer<Unit> consumer) {
			return option -> Button.builder(
							Text.translatable(KEY), btn -> Minecraft.getInstance().setScreen(new SettingsScreen(this.parent))
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
