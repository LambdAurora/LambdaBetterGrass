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
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.io.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a metadata.
 *
 * @author LambdAurora
 * @version 1.1.2
 * @since 1.0.0
 */
public class LBGMetadata {
	/**
	 * Represents the identifier of the metadata.
	 */
	public final Identifier id;

	protected final ResourceManager resourceManager;
	protected final List<Material> textures = new ArrayList<>();

	private final List<LBGGrassLayer> layers;

	private int lastLayerIndex = 0;

	protected UnbakedModel snowyVariant = null;
	protected Consumer<BakedModel> snowyModelVariantProvider = null;
	protected BakedModel snowyModelVariant = null;

	public LBGMetadata(ResourceManager resourceManager, Identifier id, JsonObject json) {
		this.id = id;
		this.resourceManager = resourceManager;

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
	 * Returns the next layer index to assign and increments the internal layer index counter.
	 *
	 * @return the next layer index
	 */
	protected int nextLayerIndex() {
		return this.lastLayerIndex++;
	}

	/**
	 * Bakes the textures.
	 *
	 * @param textureGetter the texture getter
	 */
	public void bakeTextures(Function<Material, TextureAtlasSprite> textureGetter) {
		for (var layer : this.layers) {
			layer.bakeTextures(textureGetter);
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
	 * {@return the snowy variant of this}
	 */
	public @Nullable UnbakedModel getSnowyVariant() {
		return this.snowyVariant;
	}

	/**
	 * {@return the snowy model variant}
	 */
	public @Nullable BakedModel getSnowyModelVariant() {
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
				", snowyVariant=" + this.snowyVariant +
				'}';
	}
}
