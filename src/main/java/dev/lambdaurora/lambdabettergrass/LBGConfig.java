/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

/**
 * Represents the mod configuration.
 *
 * @author LambdAurora
 * @version 1.6.0
 * @since 1.0.0
 */
public class LBGConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger("LambdaBetterGrass|Config");

	private static final LBGMode DEFAULT_MODE = LBGMode.FANCY;
	private static final boolean DEFAULT_BETTER_LAYER = true;
	private static final boolean DEFAULT_DEBUG = false;

	public static final Path CONFIG_FILE_PATH = FabricLoader.getInstance().getConfigDir()
			.resolve(LambdaBetterGrass.NAMESPACE + ".toml")
			.normalize();

	private final CommentedConfig config;
	private final LambdaBetterGrass mod;
	private LBGMode mode;
	private boolean betterLayer;

	public LBGConfig(@NotNull LambdaBetterGrass mod) {
		this.mod = mod;
		this.config = CommentedConfig.inMemory();
	}

	/**
	 * Loads the configuration.
	 */
	public void load() {
		try {
			this.loadFromFile(true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		this.mode = LBGMode.byId(this.config.getOrElse("mode", DEFAULT_MODE.getName())).orElse(DEFAULT_MODE);
		this.betterLayer = this.config.getOrElse("better_layer", DEFAULT_BETTER_LAYER);

		LambdaBetterGrass.log(LOGGER, "Configuration loaded.");
	}

	private void loadFromFile(boolean firstAttempt) throws IOException {
		try (var reader = Files.newBufferedReader(CONFIG_FILE_PATH)) {
			new TomlParser().parse(reader, this.config, ParsingMode.REPLACE);
		} catch (NoSuchFileException | FileNotFoundException e) {
			if (!firstAttempt) {
				throw e;
			}

			this.copyDefaultFile();
			this.loadFromFile(false);
		} catch (ParsingException e) {
			if (!firstAttempt) {
				throw e;
			}

			var backupPath = CONFIG_FILE_PATH.resolveSibling(LambdaBetterGrass.NAMESPACE + ".toml.old").toAbsolutePath().normalize();

			LambdaBetterGrass.error(LOGGER, "Failed to parse configuration file, THIS IS BAD.", e);
			LambdaBetterGrass.error(LOGGER, "Copying the corrupt file to \"{}\".", backupPath);
			Files.copy(CONFIG_FILE_PATH, backupPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			this.copyDefaultFile();
			this.loadFromFile(false);
		}
	}

	/**
	 * Copies the default configuration file from this mod's JAR.
	 *
	 * @throws IOException if copying the file fails
	 */
	private void copyDefaultFile() throws IOException {
		Files.createDirectories(CONFIG_FILE_PATH.getParent());

		try (var defaultStream = LBGConfig.class.getResourceAsStream("/" + LambdaBetterGrass.NAMESPACE + ".toml")) {
			Files.copy(
					Objects.requireNonNull(
							defaultStream,
							"This distribution of LambdaBetterGrass is broken: "
									+ "cannot find the default configuration file inside of the mod's JAR."
					),
					CONFIG_FILE_PATH,
					StandardCopyOption.REPLACE_EXISTING
			);
			LambdaBetterGrass.log(LOGGER, "Copied default configuration file.");
		}
	}

	/**
	 * Saves the configuration.
	 */
	public void save() {
		var toml = new TomlWriter().writeToString(this.config);

		try {
			Files.createDirectories(CONFIG_FILE_PATH.getParent());
			Files.writeString(CONFIG_FILE_PATH, toml,
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.DSYNC
			);
		} catch (IOException e) {
			LambdaBetterGrass.error(LOGGER, "Failed to save configuration file.", e);
			return;
		}

		LambdaBetterGrass.log(LOGGER, "Configuration saved.");
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
