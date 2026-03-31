/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.lambdaurora.lambdabettergrass.metadata.grass.LBGGrassLayer;
import dev.lambdaurora.lambdabettergrass.metadata.grass.LBGLoadingGrassLayer;
import dev.lambdaurora.lambdabettergrass.model.LBGBakedModel;
import dev.lambdaurora.lambdabettergrass.resource.LBGContext;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Represents a metadata.
 *
 * @author LambdAurora
 * @version 2.7.0
 * @since 1.0.0
 */
public class LBGMetadata {
	/**
	 * Represents the identifier of the metadata.
	 */
	public final Identifier id;

	private final LBGContext context;
	protected final List<Material> textures = new ArrayList<>();

	private final List<LBGGrassLayer> layers;

	protected @Nullable Consumer<BlockStateModel> snowyModelVariantProvider = null;
	protected @Nullable BlockStateModel snowyModelVariant = null;

	public LBGMetadata(
			ResourceManager resourceManager, LBGContext context, Identifier id, JsonObject json
	) {
		this.id = id;
		this.context = context;

		/* JSON read */
		var loadingLayers = new ArrayList<LBGLoadingGrassLayer>();
		if (json.has("layers")) {
			json.getAsJsonArray("layers").forEach(
					layer -> loadingLayers.add(LBGLoadingGrassLayer.CODEC.parse(JsonOps.INSTANCE, layer)
							.result().orElseThrow()
					));
		}

		var layers = new Int2ObjectArrayMap<List<LBGLoadingGrassLayer>>();
		for (var loadingLayer : loadingLayers) {
			var list = layers.computeIfAbsent(loadingLayer.colorIndex(), ignored -> new ArrayList<>());
			list.add(loadingLayer);
		}

		this.layers = layers.values().stream()
				.map(layer -> new LBGGrassLayer(resourceManager, this, layer))
				.toList();
	}

	/**
	 * {@return the LambdaBetterGrass context}
	 */
	public LBGContext context() {
		return this.context;
	}

	/**
	 * Bakes the materials.
	 *
	 * @param materialBaker the material baker
	 */
	public void bakeMaterials(MaterialBaker materialBaker) {
		for (var layer : this.layers) {
			layer.bakeMaterials(materialBaker);
		}
	}

	/**
	 * Returns the layer assigned to the specified color index.
	 *
	 * @param colorIndex the color index
	 * @return the optional layer
	 */
	public Optional<LBGGrassLayer> getLayer(int colorIndex) {
		for (var layer : this.layers) {
			if (layer.colorIndex == colorIndex)
				return Optional.of(layer);
		}
		return Optional.empty();
	}

	/**
	 * {@return the textures}
	 */
	public Collection<Material> getTextures() {
		return this.textures;
	}

	/**
	 * {@return the snowy model variant}
	 */
	public @Nullable BlockStateModel getSnowyModelVariant() {
		return this.snowyModelVariant;
	}

	/**
	 * Propagates the baked model to other variants if applicable.
	 *
	 * @param model the model to propagate
	 */
	public void propagate(LBGBakedModel model) {
		if (this.snowyModelVariantProvider != null)
			this.snowyModelVariantProvider.accept(model);
	}

	@Override
	public String toString() {
		return "LBGMetadata{" +
				"id=" + this.id +
				", layers=" + this.layers +
				'}';
	}
}
