/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata.grass;

import dev.lambdaurora.lambdabettergrass.metadata.LBGMetadata;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
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
 * @version 2.7.0
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

	private final Map<String, Material.Baked> bakedSprites = new Object2ObjectOpenHashMap<>();

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

		this.connectTexture = new Material(parentTextures.resolveConnect());
		this.blendUpTexture = new Material(parentTextures.resolveBlendUp());
		this.blendUpMirroredTexture = new Material(parentTextures.resolveBlendUpMirrored());
		this.archTexture = new Material(parentTextures.resolveArch());

		this.parentMetadata.getTextures().add(this.connectTexture);
		this.parentMetadata.getTextures().add(this.blendUpTexture);
		this.parentMetadata.getTextures().add(this.blendUpMirroredTexture);
		this.parentMetadata.getTextures().add(this.archTexture);
	}

	/**
	 * Gets the baked material by its name.
	 *
	 * @param name the name of the baked material
	 * @return the baked material if found, or {@code null} otherwise
	 */
	public Material.@Nullable Baked getBakedMaterial(String name) {
		return this.bakedSprites.get(name);
	}

	/**
	 * Bakes the textures of this layer.
	 *
	 * @param materialBaker the texture baker
	 */
	public void bakeMaterials(MaterialBaker materialBaker) {
		this.bakeMaterial("connect", this.connectTexture, materialBaker);
		this.bakeMaterial("blend_up", this.blendUpTexture, materialBaker);
		this.bakeMaterial("blend_up_m", this.blendUpMirroredTexture, materialBaker);
		this.bakeMaterial("arch", this.archTexture, materialBaker);
	}

	private void bakeMaterial(String name, @Nullable Material material, MaterialBaker materialBaker) {
		if (material != null) {
			this.bakedSprites.put(name, materialBaker.get(material, this));
		} else {
			this.doMissingBake(name, materialBaker);
		}
	}

	private void doMissingBake(String name, MaterialBaker materialBaker) {
		this.bakedSprites.put(
				name,
				materialBaker.reportMissingReference(name, this)
		);
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
