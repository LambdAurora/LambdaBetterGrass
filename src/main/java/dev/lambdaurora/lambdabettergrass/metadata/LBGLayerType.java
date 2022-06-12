/*
 * Copyright © 2021-2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata;

import com.google.gson.JsonParser;
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.spruceui.util.Nameable;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents the layer types.
 *
 * @author LambdAurora
 * @version 1.2.3
 * @since 1.0.0
 */
public class LBGLayerType implements Nameable {
	private static final List<LBGLayerType> LAYER_TYPES = new ArrayList<>();

	public final Identifier id;
	public final Block block;
	public final Identifier modelId;
	private final String name;

	public LBGLayerType(Identifier id, Block block, Identifier modelId) {
		this.id = id;
		this.block = block;
		this.modelId = modelId;
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
	 * Resets the registered layer types.
	 */
	public static void reset() {
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

	public static void load(Identifier resourceId, ResourceManager resourceManager) {
		var id = new Identifier(resourceId.getNamespace(), resourceId.getPath().replace(".json", ""));
		try {
			var stream = resourceManager.open(resourceId);
			var json = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

			var affectId = new Identifier(json.get("block").getAsString());
			var block = Registry.BLOCK.get(affectId);

			if (block == Blocks.AIR)
				return;

			var modelId = new Identifier(json.get("model").getAsString());

			stream.close();

			LAYER_TYPES.add(new LBGLayerType(id, block, modelId));
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
