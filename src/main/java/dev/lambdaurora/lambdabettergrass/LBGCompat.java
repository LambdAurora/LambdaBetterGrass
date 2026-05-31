/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass;

import dev.yumi.mc.core.api.YumiMods;

/**
 * Represents a utility class for compatibility.
 *
 * @author LambdAurora
 * @version 2.0.0
 * @since 2.0.0
 */
public final class LBGCompat {
	/**
	 * {@return the type of Sodium version}
	 */
	public static boolean isSodiumInstalled() {
		return YumiMods.get().isModLoaded("sodium");
	}
}
