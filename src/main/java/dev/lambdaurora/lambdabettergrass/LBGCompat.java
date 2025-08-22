/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

/**
 * Represents a utility class for compatibility.
 *
 * @author LambdAurora
 * @version 1.6.0
 * @since 1.6.0
 */
public final class LBGCompat {
	private static final SodiumInstallation SODIUM_INSTALLATION;

	/**
	 * {@return the type of Sodium version}
	 */
	public static SodiumInstallation isSodiumInstalled() {
		return SODIUM_INSTALLATION;
	}

	static {
		SODIUM_INSTALLATION = FabricLoader.getInstance().getModContainer("sodium").map(mod -> {
			try {
				if (mod.getMetadata().getVersion().compareTo(Version.parse("0.6.0")) >= 0) {
					return SodiumInstallation.V06X;
				} else if (mod.getMetadata().getVersion().compareTo(Version.parse("0.5.0")) >= 0) {
					return SodiumInstallation.V05X;
				} else {
					return SodiumInstallation.OLDER;
				}
			} catch (VersionParsingException e) {
				throw new RuntimeException(e);
			}
		}).orElse(SodiumInstallation.NONE);
	}

	public enum SodiumInstallation {
		NONE,
		V06X,
		V05X,
		OLDER
	}
}
