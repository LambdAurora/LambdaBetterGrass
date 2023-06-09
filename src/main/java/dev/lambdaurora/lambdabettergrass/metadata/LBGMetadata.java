/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata;

import com.google.gson.JsonObject;
import dev.lambdaurora.lambdabettergrass.model.LBGBakedModel;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.resource.Material;
import net.minecraft.client.texture.Sprite;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
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

	private final List<LBGLayer> layers = new ArrayList<>();

	private int lastLayerIndex = 0;

	protected UnbakedModel snowyVariant = null;
	protected Consumer<BakedModel> snowyModelVariantProvider = null;
	protected BakedModel snowyModelVariant = null;

	public LBGMetadata(ResourceManager resourceManager, Identifier id, JsonObject json) {
		this.id = id;
		this.resourceManager = resourceManager;

		/* JSON read */
		if (json.has("layers")) {
			json.getAsJsonArray("layers").forEach(layer -> this.layers.add(new LBGLayer(this, layer.getAsJsonObject())));
		}

		this.buildTextures();

		/* Merge layers */
		var parentLayers = new Int2ObjectArrayMap<LBGLayer>();
		for (var layer : this.layers) {
			if (!parentLayers.containsKey(layer.colorIndex)) {
				parentLayers.put(layer.colorIndex, layer);
			} else {
				// Merge layer
				LBGLayer.mergeLayers(parentLayers.get(layer.colorIndex), layer);
			}
		}

		this.layers.clear();
		this.layers.addAll(parentLayers.values());
	}

	/**
	 * Returns the next layer index to assign and increments the internal layer index counter.
	 *
	 * @return the next layer index
	 */
	protected int nextLayerIndex() {
		return this.lastLayerIndex++;
	}

	private void buildTextures() {
		for (var layer : this.layers)
			layer.buildTextures();
	}

	/**
	 * Bakes the textures.
	 *
	 * @param textureGetter the texture getter
	 */
	public void bakeTextures(Function<Material, Sprite> textureGetter) {
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
	public Optional<LBGLayer> getLayer(int colorIndex) {
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
