/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.platform.neoforge;

import dev.lambdaurora.lambdabettergrass.platform.Platform;
import dev.yumi.mc.core.api.ModContainer;

/**
 * Provides NeoForge-specific platform operations.
 *
 * @author LambdAurora
 * @version 2.0.4
 * @since 2.0.4
 */
public final class NeoForgePlatform implements Platform {
	public static final NeoForgePlatform INSTANCE = new NeoForgePlatform();

	private NeoForgePlatform() {}

	@Override
	public void registerBuiltinResourcePacks(ModContainer mod) {
	}

	@Override
	public void registerModelLoadingPlugin() {
	}
}
