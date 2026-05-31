/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.platform;

import dev.yumi.mc.core.api.ModContainer;

/**
 * Represents platform-specific operations.
 *
 * @author LambdAurora
 * @version 2.0.4
 * @since 2.0.4
 */
public interface Platform {
	void registerBuiltinResourcePacks(ModContainer mod);

	void registerModelLoadingPlugin();
}
