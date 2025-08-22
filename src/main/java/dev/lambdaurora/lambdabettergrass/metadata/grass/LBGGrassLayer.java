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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.io.ResourceManager;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Represents a grass layer.
 *
 * @author LambdAurora
 * @version 1.6.0
 * @since 1.0.0
 */
public class LBGGrassLayer {
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

	public LBGGrassLayer(ResourceManager resourceManager, LBGMetadata metadata, List<LBGLoadingGrassLayer> layers) {
		this.parentMetadata = metadata;
		var first = layers.get(0);

		this.colorIndex = first.colorIndex();

		var textures = layers.stream()
				.map(layer -> LBGGrassLayerTextures.generate(resourceManager, metadata.id, layer))
				.toList();

		var parentTextures = textures.get(0);
		for (int i = 1; i < textures.size(); i++) {
			var texture = textures.get(i);
			var oldTextures = parentTextures;
			parentTextures = parentTextures.merge(texture);

			oldTextures.close();
			texture.close();
		}

		this.connectTexture = new Material(InventoryMenu.BLOCK_ATLAS, parentTextures.resolveConnect());
		this.blendUpTexture = new Material(InventoryMenu.BLOCK_ATLAS, parentTextures.resolveBlendUp());
		this.blendUpMirroredTexture = new Material(InventoryMenu.BLOCK_ATLAS, parentTextures.resolveBlendUpMirrored());
		this.archTexture = new Material(InventoryMenu.BLOCK_ATLAS, parentTextures.resolveArch());

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
	public void bakeTextures(Function<Material, TextureAtlasSprite> textureGetter) {
		this.tryBakeSprite("connect", this.connectTexture, textureGetter);
		this.tryBakeSprite("blend_up", this.blendUpTexture, textureGetter);
		this.tryBakeSprite("blend_up_m", this.blendUpMirroredTexture, textureGetter);
		this.tryBakeSprite("arch", this.archTexture, textureGetter);
	}

	private void tryBakeSprite(String name, @Nullable Material id, Function<Material, TextureAtlasSprite> textureGetter) {
		if (id == null)
			id = new Material(InventoryMenu.BLOCK_ATLAS, ModelBakery.MISSING_MODEL_ID);

		try {
			this.bakedSprites.put(name, textureGetter.apply(id));
		} catch (NullPointerException e) {
			LambdaBetterGrass.warn(LOGGER, "Could not bake sprite `{}` with id `{}`!", name, id);

			this.bakedSprites.put(name, textureGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, ModelBakery.MISSING_MODEL_ID)));
		}
	}

	@Override
	public String toString() {
		return "LBGGrassLayer{" +
				"id=" + this.parentMetadata.id +
				", colorIndex=" + this.colorIndex +
				'}';
	}
}
