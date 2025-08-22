/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.metadata.layer.LBGLayerTypeData;
import dev.lambdaurora.spruceui.util.Nameable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.io.Resource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents the layer types.
 *
 * @author LambdAurora
 * @version 1.6.0
 * @since 1.0.0
 */
public class LBGLayerType implements Nameable {
	private static final Logger LOGGER = LoggerFactory.getLogger("LambdaBetterGrass|LBGLayerType");
	private static final List<LBGLayerType> LAYER_TYPES = new ArrayList<>();

	public final Identifier id;
	public final LBGLayerTypeData data;
	public final RenderType renderType;
	private final String name;

	public LBGLayerType(Identifier id, LBGLayerTypeData data) {
		this.id = id;
		String[] path = this.id.path().split("/");
		this.name = path[path.length - 1];
		this.data = data;
		this.renderType = ItemBlockRenderTypes.getChunkRenderType(data.state());
	}

	/**
	 * {@return the baked layer model}
	 */
	public BakedModel getLayerModel() {
		return Minecraft.getInstance().getBlockRenderer().getBlockModel(this.data.state());
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

	public static void load(Identifier resourceId, Resource resource) {
		var id = new Identifier(resourceId.namespace(), resourceId.path().replace(".json", ""));
		try (var reader = new InputStreamReader(resource.open())) {
			var rawJson = JsonParser.parseReader(reader);
			var loaded = LBGLayerTypeData.CODEC.parse(JsonOps.INSTANCE, rawJson);

			loaded.result().ifPresentOrElse(
					data -> LAYER_TYPES.add(new LBGLayerType(id, data)),
					() -> {
						LambdaBetterGrass.warn(LOGGER, "Failed to load layer type \"{}\" due to error: {}",
								id, loaded.error().orElseThrow().message()
						);
					}
			);
		} catch (Exception e) {
			LambdaBetterGrass.warn(LOGGER, "Failed to load layer type \"{}\".", id);
		}
	}

	@Override
	public String toString() {
		return "LBGLayerType{" +
				"id=" + this.id +
				", data=" + this.data +
				'}';
	}
}
