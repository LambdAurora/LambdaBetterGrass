/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata.grass;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import net.minecraft.resources.Identifier;

/**
 * Represents the masks to use for a better grass layer.
 *
 * @param connect the connecting mask identifier
 * @param blendUp the blend-up mask identifier
 * @param arch the arch mask identifier
 *
 * @author LambdAurora
 * @version 2.0.0
 * @since 2.0.0
 */
public record LBGGrassMasks(
		Identifier connect,
		Identifier blendUp,
		Identifier arch
) {
	public static final Identifier DEFAULT_CONNECT_MASK = LambdaBetterGrass.id("bettergrass/mask/standard_block_side_connect");
	public static final Identifier DEFAULT_BLEND_UP_MASK = LambdaBetterGrass.id("bettergrass/mask/grass_block_side_blend_up");
	public static final Identifier DEFAULT_ARCH_MASK = LambdaBetterGrass.id("bettergrass/mask/grass_block_side_arch_blend");

	public static Codec<LBGGrassMasks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.optionalFieldOf("connect", DEFAULT_CONNECT_MASK).forGetter(LBGGrassMasks::connect),
			Identifier.CODEC.optionalFieldOf("blend_up", DEFAULT_BLEND_UP_MASK).forGetter(LBGGrassMasks::blendUp),
			Identifier.CODEC.optionalFieldOf("arch", DEFAULT_ARCH_MASK).forGetter(LBGGrassMasks::arch)
	).apply(instance, LBGGrassMasks::new));

	public static final LBGGrassMasks DEFAULT = new LBGGrassMasks(DEFAULT_CONNECT_MASK, DEFAULT_BLEND_UP_MASK, DEFAULT_ARCH_MASK);

	/**
	 * {@return the masks identifiers suffixed with the {@code .png} extension}
	 */
	public LBGGrassMasks withPngSuffix() {
		return new LBGGrassMasks(
				this.connect.withSuffix(".png"),
				this.blendUp.withSuffix(".png"),
				this.arch.withSuffix(".png")
		);
	}
}
