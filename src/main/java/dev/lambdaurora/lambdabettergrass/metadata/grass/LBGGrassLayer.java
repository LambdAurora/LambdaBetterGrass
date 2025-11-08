/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata.grass;

import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.metadata.LBGMetadata;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Represents a grass layer.
 *
 * @author LambdAurora
 * @version 2.2.0
 * @since 1.0.0
 */
public class LBGGrassLayer implements ModelDebugName {
	private static final Logger LOGGER = LoggerFactory.getLogger("LambdaBetterGrass|LBGGrassLayer");
	/**
	 * Parent metadata.
	 */
	private final LBGMetadata parentMetadata;

	public final int colorIndex;

	/* Generated textures */
	private final Material connectTexture;
	private final Material blendUpTexture;
	private final Material blendUpMirroredTexture;
	private final Material archTexture;

	private final Map<String, TextureAtlasSprite> bakedSprites = new Object2ObjectOpenHashMap<>();

	@SuppressWarnings("deprecation")
	public LBGGrassLayer(ResourceManager resourceManager, LBGMetadata metadata, List<LBGLoadingGrassLayer> layers) {
		this.parentMetadata = metadata;
		var first = layers.getFirst();

		this.colorIndex = first.colorIndex();

		var textures = layers.stream()
				.map(layer -> LBGGrassLayerTextures.generate(resourceManager, metadata.id, layer))
				.toList();

		var parentTextures = textures.getFirst();
		for (int i = 1; i < textures.size(); i++) {
			var texture = textures.get(i);
			var oldTextures = parentTextures;
			parentTextures = parentTextures.merge(texture);

			oldTextures.close();
			texture.close();
		}

		this.connectTexture = new Material(TextureAtlas.LOCATION_BLOCKS, parentTextures.resolveConnect());
		this.blendUpTexture = new Material(TextureAtlas.LOCATION_BLOCKS, parentTextures.resolveBlendUp());
		this.blendUpMirroredTexture = new Material(TextureAtlas.LOCATION_BLOCKS, parentTextures.resolveBlendUpMirrored());
		this.archTexture = new Material(TextureAtlas.LOCATION_BLOCKS, parentTextures.resolveArch());

		this.parentMetadata.getTextures().add(this.connectTexture);
		this.parentMetadata.getTextures().add(this.blendUpTexture);
		this.parentMetadata.getTextures().add(this.blendUpMirroredTexture);
		this.parentMetadata.getTextures().add(this.archTexture);
	}

	/**
	 * Gets the baked texture by its name.
	 *
	 * @param name the name of the baked texture
	 * @return the baked texture if found, or {@code null} otherwise
	 */
	public @Nullable TextureAtlasSprite getBakedTexture(String name) {
		return this.bakedSprites.get(name);
	}

	/**
	 * Bakes the textures of this layer.
	 *
	 * @param textureGetter the texture getter
	 */
	public void bakeTextures(SpriteGetter textureGetter) {
		this.tryBakeSprite("connect", this.connectTexture, textureGetter);
		this.tryBakeSprite("blend_up", this.blendUpTexture, textureGetter);
		this.tryBakeSprite("blend_up_m", this.blendUpMirroredTexture, textureGetter);
		this.tryBakeSprite("arch", this.archTexture, textureGetter);
	}

	@SuppressWarnings("deprecation")
	private void tryBakeSprite(String name, @Nullable Material id, SpriteGetter textureGetter) {
		if (id == null)
			id = new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation());

		try {
			this.bakedSprites.put(name, textureGetter.get(id, this));
		} catch (NullPointerException e) {
			LambdaBetterGrass.warn(LOGGER, "Could not bake sprite `{}` with id `{}`!", name, id);

			this.bakedSprites.put(
					name,
					textureGetter.get(
							new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation()),
							this
					)
			);
		}
	}

	@Override
	public String toString() {
		return "LBGGrassLayer{" +
				"id=" + this.parentMetadata.id +
				", colorIndex=" + this.colorIndex +
				'}';
	}

	@Override
	public String debugName() {
		return "%s (Better Grass Layer %s)".formatted(this.parentMetadata.id, this.colorIndex);
	}
}
