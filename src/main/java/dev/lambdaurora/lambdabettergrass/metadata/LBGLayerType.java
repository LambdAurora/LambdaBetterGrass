/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.spruceui.util.Nameable;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.block.extensions.api.client.BlockRenderLayerMap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents the layer types.
 *
 * @author LambdAurora
 * @version 1.4.0
 * @since 1.0.0
 */
public class LBGLayerType implements Nameable {
	private static final Map<String, RenderLayer> NAMED_RENDER_LAYERS = new ImmutableMap.Builder<String, RenderLayer>()
			.put("solid", RenderLayer.getSolid())
			.put("cutout", RenderLayer.getCutout())
			.put("cutout_mipped", RenderLayer.getCutoutMipped())
			.put("translucent", RenderLayer.getTranslucent())
			.put("tripwire", RenderLayer.getTripwire())
			.build();
	private static final List<LBGLayerType> LAYER_TYPES = new ArrayList<>();

	public final Identifier id;
	public final Block block;
	public final Identifier modelId;
	private final String name;
	private final List<RenderLayer> acceptedRenderLayers;
	private final RenderLayer defaultRenderLayer;
	private final Reference2ReferenceMap<Block, RenderLayer> oldRenderLayers = new Reference2ReferenceOpenHashMap<>();

	public LBGLayerType(Identifier id, Block block, Identifier modelId, List<RenderLayer> acceptedRenderLayers, RenderLayer defaultRenderLayer) {
		this.id = id;
		this.block = block;
		this.modelId = modelId;
		this.acceptedRenderLayers = acceptedRenderLayers;
		this.defaultRenderLayer = defaultRenderLayer;
		String[] path = this.id.getPath().split("/");
		this.name = path[path.length - 1];
	}

	/**
	 * Returns the unbaked layer model.
	 *
	 * @param modelGetter The model getter.
	 * @return The unbaked model.
	 */
	public UnbakedModel getLayerModel(Function<Identifier, UnbakedModel> modelGetter) {
		return modelGetter.apply(this.modelId);
	}

	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * {@return {@code true} if this layer type requires a special render layer, otherwise {@code false}}
	 */
	public boolean hasSpecialRenderLayer() {
		return this.defaultRenderLayer != null;
	}

	/**
	 * Applies a new render layer on the block.
	 *
	 * @param block the block
	 */
	public void apply(Block block) {
		if (!this.hasSpecialRenderLayer()) {
			return;
		}

		var currentLayer = RenderLayers.getBlockLayer(block.getDefaultState());

		if (currentLayer != this.defaultRenderLayer && !this.acceptedRenderLayers.contains(currentLayer)) {
			this.oldRenderLayers.putIfAbsent(block, currentLayer);

			BlockRenderLayerMap.put(this.defaultRenderLayer, block);
		}
	}

	private void resetSelf() {
		this.oldRenderLayers.forEach((block, renderLayer) -> BlockRenderLayerMap.put(renderLayer, block));
	}

	/**
	 * Resets the registered layer types.
	 */
	public static void reset() {
		LAYER_TYPES.forEach(LBGLayerType::resetSelf);
		LAYER_TYPES.clear();
	}

	public static void forEach(Consumer<LBGLayerType> consumer) {
		LAYER_TYPES.forEach(consumer);
	}

	public static @Nullable LBGLayerType fromName(String name) {
		for (var type : LAYER_TYPES) {
			if (type.getName().equals(name))
				return type;
		}
		return null;
	}

	public static void load(Identifier resourceId, Resource resource) {
		var id = new Identifier(resourceId.getNamespace(), resourceId.getPath().replace(".json", ""));
		try (var reader = new InputStreamReader(resource.open())) {
			var json = JsonParser.parseReader(reader).getAsJsonObject();

			var affectId = new Identifier(json.get("block").getAsString());
			var block = Registries.BLOCK.get(affectId);

			if (block == Blocks.AIR)
				return;

			var modelId = new Identifier(json.get("model").getAsString());

			var acceptedRenderLayers = new ReferenceArrayList<RenderLayer>();
			RenderLayer defaultRenderLayer = null;

			if (json.has("render_layer")) {
				var renderLayerData = json.getAsJsonObject("render_layer");

				JsonArray accepted = renderLayerData.getAsJsonArray("accepted");
				String defaultLayer = renderLayerData.get("default").getAsString();

				for (var el : accepted) {
					String name = el.getAsString();
					RenderLayer layer = NAMED_RENDER_LAYERS.get(name);

					if (layer != null) {
						acceptedRenderLayers.add(layer);
					} else {
						LambdaBetterGrass.get().warn("Failed to find accepted render layer \"" + name + "\" for LBG layer type \"" + id + "\".");
					}
				}

				defaultRenderLayer = NAMED_RENDER_LAYERS.get(defaultLayer);

				if (defaultRenderLayer == null) {
					LambdaBetterGrass.get().warn("Failed to find default render layer \"" + defaultLayer + "\" for LBG layer type \"" + id + "\".");
				}
			}

			LAYER_TYPES.add(new LBGLayerType(id, block, modelId, acceptedRenderLayers, defaultRenderLayer));
		} catch (IOException | IllegalStateException e) {
			LambdaBetterGrass.get().warn("Failed to load layer type \"" + id + "\".");
		}
	}

	@Override
	public String toString() {
		return "LBGLayerType{" +
				"id=" + id +
				", block=" + block +
				", modelId=" + modelId +
				'}';
	}
}
