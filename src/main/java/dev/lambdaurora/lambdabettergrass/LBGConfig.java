/*
 * Copyright © 2021-2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Represents the mod configuration.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LBGConfig {
	private static final LBGMode DEFAULT_MODE = LBGMode.FANCY;
	private static final boolean DEFAULT_BETTER_LAYER = true;
	private static final boolean DEFAULT_DEBUG = false;

	public static final Path CONFIG_FILE_PATH = Paths.get("config/lambdabettergrass.toml");

	protected final FileConfig config;
	private final LambdaBetterGrass mod;
	private LBGMode mode;
	private boolean betterLayer;

	public LBGConfig(@NotNull LambdaBetterGrass mod) {
		this.mod = mod;
		this.config = FileConfig.builder(CONFIG_FILE_PATH)
				.concurrent()
				.defaultResource("/lambdabettergrass.toml")
				.autoreload()
				.autosave()
				.build();
	}

	/**
	 * Loads the configuration.
	 */
	public void load() {
		this.config.load();

		this.mode = LBGMode.byId(this.config.getOrElse("mode", DEFAULT_MODE.getName())).orElse(DEFAULT_MODE);
		this.betterLayer = this.config.getOrElse("better_layer", DEFAULT_BETTER_LAYER);

		this.mod.log("Configuration loaded.");
	}

	/**
	 * Resets the configuration.
	 */
	public void reset() {
		this.setMode(DEFAULT_MODE);
		this.setBetterLayer(DEFAULT_BETTER_LAYER);
		this.setDebug(DEFAULT_DEBUG);
	}

	/**
	 * {@return the better grass mode}
	 */
	public LBGMode getMode() {
		return this.mode;
	}

	/**
	 * Sets the better grass mode.
	 *
	 * @param mode the better grass mode
	 */
	public void setMode(@NotNull LBGMode mode) {
		this.mode = mode;
		this.config.set("mode", mode.getName());
	}

	/**
	 * Returns whether better snow is enabled or not.
	 *
	 * @return {@code true} if better snow is enabled, otherwise {@code false}
	 */
	public boolean hasBetterLayer() {
		return this.betterLayer;
	}

	/**
	 * Sets whether better snow is enabled or not.
	 *
	 * @param betterSnow {@code true} if better snow is enabled, otherwise {@code false}
	 */
	public void setBetterLayer(boolean betterSnow) {
		this.betterLayer = betterSnow;
		this.config.set("better_layer", betterSnow);
	}

	/**
	 * Returns whether this mod is in debug mode.
	 *
	 * @return {@code true} if this mod is in debug mode, otherwise {@code false}
	 */
	public boolean isDebug() {
		return this.config.getOrElse("debug", DEFAULT_DEBUG);
	}

	/**
	 * Sets whether this mod is in debug mode.
	 *
	 * @param debug {@code true} if this mod is in debug mode, otherwise {@code false}
	 */
	public void setDebug(boolean debug) {
		this.config.set("debug", debug);
	}
}
