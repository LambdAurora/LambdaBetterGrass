/*
 * Copyright © 2021-2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.mixin;

import com.google.gson.JsonParser;
import dev.lambdaurora.lambdabettergrass.metadata.LBGLayerType;
import dev.lambdaurora.lambdabettergrass.metadata.LBGState;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;

@Mixin(ModelLoader.class)
public abstract class ModelLoaderMixin {
	@Shadow
	@Final
	private Map<Identifier, UnbakedModel> unbakedModels;

	@Shadow
	@Final
	private ResourceManager resourceManager;

	@Shadow
	public abstract UnbakedModel getOrLoadModel(Identifier id);

	@Shadow
	@Final
	private ModelVariantMap.DeserializationContext variantMapDeserializationContext;

	@Shadow
	@Final
	private Set<Identifier> modelsToLoad;

	@Unique
	private boolean lbg$firstLoad = true;

	@Inject(method = "putModel", at = @At("HEAD"), cancellable = true)
	private void onPutModel(Identifier id, UnbakedModel unbakedModel, CallbackInfo ci) {
		if (id instanceof ModelIdentifier modelId) {
			if (!modelId.getVariant().equals("inventory")) {
				if (this.lbg$firstLoad) {
					LBGState.reset();
					LBGLayerType.reset();
					var layerTypes = this.resourceManager.findResources("bettergrass/layer_types",
							path -> path.toString().endsWith(".json"));
					for (var layerTypeId : layerTypes.keySet()) {
						LBGLayerType.load(layerTypeId, this.resourceManager);
					}
					this.lbg$firstLoad = false;
				}

				var stateId = new Identifier(modelId.getNamespace(), "bettergrass/states/" + modelId.getPath());

				// Get cached states metadata.
				var state = LBGState.getMetadataState(stateId);

				// Find and load states metadata if not cached.
				if (state == null) {
					var stateResourceId = new Identifier(stateId.getNamespace(), stateId.getPath() + ".json");
					try (var reader = new InputStreamReader(resourceManager.getResourceOrThrow(stateResourceId).open())) {
						var json = JsonParser.parseReader(reader).getAsJsonObject();
						state = LBGState.getOrLoadMetadataState(stateId, this.resourceManager, json, this.variantMapDeserializationContext);
					} catch (IOException e) {
						// Ignore.
					}
				}

				// If states metadata found, search for corresponding metadata and if exists replace the model.
				if (state != null) {
					var newModel = state.getCustomUnbakedModel(modelId, unbakedModel, this::getOrLoadModel);
					if (newModel != null) {
						this.unbakedModels.put(modelId, newModel);
						this.modelsToLoad.addAll(newModel.getModelDependencies());
						ci.cancel();
					}
				}
			}
		}
	}
}
