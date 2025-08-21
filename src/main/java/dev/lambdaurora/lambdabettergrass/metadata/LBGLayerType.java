/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
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
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.io.Resource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

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
	private static final Map<String, RenderType> NAMED_RENDER_LAYERS = new ImmutableMap.Builder<String, RenderType>()
			.put("solid", RenderType.solid())
			.put("cutout", RenderType.cutout())
			.put("cutout_mipped", RenderType.cutoutMipped())
			.put("translucent", RenderType.translucent())
			.put("tripwire", RenderType.tripwire())
			.build();
	private static final List<LBGLayerType> LAYER_TYPES = new ArrayList<>();

	public final Identifier id;
	public final Block block;
	public final Identifier modelId;
	private final String name;
	private final List<RenderType> acceptedRenderLayers;
	private final RenderType defaultRenderLayer;
	private final Reference2ReferenceMap<Block, RenderType> oldRenderLayers = new Reference2ReferenceOpenHashMap<>();

	public LBGLayerType(Identifier id, Block block, Identifier modelId, List<RenderType> acceptedRenderLayers, RenderType defaultRenderLayer) {
		this.id = id;
		this.block = block;
		this.modelId = modelId;
		this.acceptedRenderLayers = acceptedRenderLayers;
		this.defaultRenderLayer = defaultRenderLayer;
		String[] path = this.id.path().split("/");
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

		var currentLayer = ItemBlockRenderTypes.getChunkRenderType(block.defaultState());

		if (currentLayer != this.defaultRenderLayer && !this.acceptedRenderLayers.contains(currentLayer)) {
			this.oldRenderLayers.putIfAbsent(block, currentLayer);

			BlockRenderLayerMap.INSTANCE.putBlock(block, this.defaultRenderLayer);
		}
	}

	private void resetSelf() {
		this.oldRenderLayers.forEach(BlockRenderLayerMap.INSTANCE::putBlock);
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
		var id = new Identifier(resourceId.namespace(), resourceId.path().replace(".json", ""));
		try (var reader = new InputStreamReader(resource.open())) {
			var json = JsonParser.parseReader(reader).getAsJsonObject();

			var affectId = new Identifier(json.get("block").getAsString());
			var block = BuiltInRegistries.BLOCK.get(affectId);

			if (block == Blocks.AIR)
				return;

			var modelId = new Identifier(json.get("model").getAsString());

			var acceptedRenderLayers = new ReferenceArrayList<RenderType>();
			RenderType defaultRenderLayer = null;

			if (json.has("render_layer")) {
				var renderLayerData = json.getAsJsonObject("render_layer");

				JsonArray accepted = renderLayerData.getAsJsonArray("accepted");
				String defaultLayer = renderLayerData.get("default").getAsString();

				for (var el : accepted) {
					String name = el.getAsString();
					RenderType layer = NAMED_RENDER_LAYERS.get(name);

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
