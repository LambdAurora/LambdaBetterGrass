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
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Represents a grass layer that is being loaded.
 *
 * @param colorIndex the color index targeted by this grass layer
 * @param textures the textures used for this grass layer
 * @param masks the masks used for this grass layer
 *
 * @version 2.0.0
 * @since 2.0.0
 * @author LambdAurora
 */
public record LBGLoadingGrassLayer(int colorIndex, @NotNull Textures textures, @NotNull LBGGrassMasks masks) {
	public static final Codec<LBGLoadingGrassLayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("color_index", -1).forGetter(LBGLoadingGrassLayer::colorIndex),
			Textures.CODEC.fieldOf("textures").forGetter(LBGLoadingGrassLayer::textures),
			LBGGrassMasks.CODEC.optionalFieldOf("masks", LBGGrassMasks.DEFAULT).forGetter(LBGLoadingGrassLayer::masks)
	).apply(instance, LBGLoadingGrassLayer::new));

	public record Textures(
			@NotNull Identifier top,
			@NotNull Identifier side,
			@NotNull Overrides overrides
	) {
		public static final Codec<Textures> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Identifier.CODEC.fieldOf("top").forGetter(Textures::top),
				Identifier.CODEC.fieldOf("side").forGetter(Textures::side),
				Overrides.CODEC.optionalFieldOf("overrides")
						.xmap(
								overrides -> overrides.orElse(Overrides.EMPTY),
								overrides -> overrides.isEmpty() ? Optional.empty() : Optional.of(overrides)
						)
						.forGetter(Textures::overrides)
		).apply(instance, Textures::new));

		public record Overrides(
				Optional<Identifier> connect,
				Optional<Identifier> blendUp,
				Optional<Identifier> blendUpMirrored,
				Optional<Identifier> arch
		) {
			public static final Codec<Overrides> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Identifier.CODEC.optionalFieldOf("connect").forGetter(Overrides::connect),
					Identifier.CODEC.optionalFieldOf("blend_up").forGetter(Overrides::blendUp),
					Identifier.CODEC.optionalFieldOf("blend_up_m").forGetter(Overrides::blendUpMirrored),
					Identifier.CODEC.optionalFieldOf("arch").forGetter(Overrides::arch)
			).apply(instance, Overrides::new));
			public static final Overrides EMPTY = new Overrides(
					Optional.empty(),
					Optional.empty(),
					Optional.empty(),
					Optional.empty()
			);

			public boolean isEmpty() {
				return this.connect.isEmpty() && this.blendUp.isEmpty() && this.blendUpMirrored.isEmpty() && this.arch.isEmpty();
			}
		}
	}
}
