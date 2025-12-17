/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass;

import dev.lambdaurora.lambdabettergrass.gui.SettingsScreen;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.option.OptionBinding;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;

public class LambdaBetterGrassSodiumConfig implements ConfigEntryPoint {
	@Override
	public void registerConfigLate(ConfigBuilder builder) {
		var mod = LambdaBetterGrass.get();

		builder.registerOwnModOptions()
				.setColorTheme(builder.createColorTheme()
						.setBaseThemeRGB(0xff00ed76)
				)
				.addPage(builder.createOptionPage()
						.setName(SettingsScreen.MOD_NAME)
						.addOption(builder.createEnumOption(LambdaBetterGrass.id("mode"), LBGMode.class)
								.setName(Component.translatable("lambdabettergrass.option.mode"))
								.setTooltip(SettingsScreen.MODE_TOOLTIP)
								.setElementNameProvider(LBGMode::getTranslatedText)
								.setDefaultValue(LBGConfig.DEFAULT_MODE)
								.setBinding(new OptionBinding<>() {
									@Override
									public void save(LBGMode value) {
										mod.config.setMode(value);
									}

									@Override
									public LBGMode load() {
										return mod.config.getMode();
									}
								})
								.setStorageHandler(mod.config::save)
						)
						.addOption(builder.createBooleanOption(LambdaBetterGrass.id("better_layer"))
								.setName(Component.translatable("lambdabettergrass.option.better_snow"))
								.setTooltip(Component.translatable("lambdabettergrass.tooltip.better_snow"))
								.setDefaultValue(LBGConfig.DEFAULT_BETTER_LAYER)
								.setBinding(new OptionBinding<>() {
									@Override
									public void save(Boolean value) {
										mod.config.setBetterLayer(value);
									}

									@Override
									public Boolean load() {
										return mod.config.hasBetterLayer();
									}
								})
								.setStorageHandler(mod.config::save)
						)
				);
	}
}
