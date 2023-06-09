/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.mixin;

import dev.lambdaurora.lambdabettergrass.metadata.LBGState;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;

@Mixin(ModelLoader.class)
public abstract class ModelLoaderMixin {
	@Shadow
	@Final
	private Map<Identifier, UnbakedModel> unbakedModels;

	@Shadow
	public abstract UnbakedModel getOrLoadModel(Identifier id);

	@Shadow
	@Final
	private Set<Identifier> modelsToLoad;

	@Inject(method = "putModel", at = @At("HEAD"), cancellable = true)
	private void onPutModel(Identifier id, UnbakedModel unbakedModel, CallbackInfo ci) {
		if (id instanceof ModelIdentifier modelId) {
			if (!modelId.getVariant().equals("inventory")) {
				var stateId = new Identifier(modelId.getNamespace(), modelId.getPath());

				// Get cached states metadata.
				var state = LBGState.getMetadataState(stateId);

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
