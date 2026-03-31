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
import net.minecraft.client.resources.model.sprite.Material;

import java.util.Optional;

/**
 * Represents a grass layer that is being loaded.
 *
 * @param colorIndex the color index targeted by this grass layer
 * @param textures the textures used for this grass layer
 * @param masks the masks used for this grass layer
 *
 * @author LambdAurora
 * @version 2.7.0
 * @since 2.0.0
 */
public record LBGLoadingGrassLayer(int colorIndex, Textures textures, LBGGrassMasks masks) {
	public static final Codec<LBGLoadingGrassLayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("color_index", -1).forGetter(LBGLoadingGrassLayer::colorIndex),
			Textures.CODEC.fieldOf("textures").forGetter(LBGLoadingGrassLayer::textures),
			LBGGrassMasks.CODEC.optionalFieldOf("masks", LBGGrassMasks.DEFAULT).forGetter(LBGLoadingGrassLayer::masks)
	).apply(instance, LBGLoadingGrassLayer::new));

	public record Textures(
			Material top,
			Material side,
			Overrides overrides
	) {
		public static final Codec<Textures> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Material.CODEC.fieldOf("top").forGetter(Textures::top),
				Material.CODEC.fieldOf("side").forGetter(Textures::side),
				Overrides.CODEC.optionalFieldOf("overrides")
						.xmap(
								overrides -> overrides.orElse(Overrides.EMPTY),
								overrides -> overrides.isEmpty() ? Optional.empty() : Optional.of(overrides)
						)
						.forGetter(Textures::overrides)
		).apply(instance, Textures::new));

		public record Overrides(
				Optional<Material> connect,
				Optional<Material> blendUp,
				Optional<Material> blendUpMirrored,
				Optional<Material> arch
		) {
			public static final Codec<Overrides> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Material.CODEC.optionalFieldOf("connect").forGetter(Overrides::connect),
					Material.CODEC.optionalFieldOf("blend_up").forGetter(Overrides::blendUp),
					Material.CODEC.optionalFieldOf("blend_up_m").forGetter(Overrides::blendUpMirrored),
					Material.CODEC.optionalFieldOf("arch").forGetter(Overrides::arch)
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
