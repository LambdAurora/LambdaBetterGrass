/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.mixin;

import dev.lambdaurora.lambdabettergrass.metadata.LBGState;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelIdentifier;
import net.minecraft.client.resources.model.UnbakedModel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {
	@Shadow
	@Final
	private Map<ModelIdentifier, UnbakedModel> topLevelModels;

	@Inject(method = "registerModel", at = @At("TAIL"))
	private void lbg$wrapTopLevelModel(ModelIdentifier modelLocation, UnbakedModel model, CallbackInfo ci) {
		if (ModelIdentifier.INVENTORY_VARIANT.equals(modelLocation.variant())) {
			return;
		}

		var state = LBGState.getMetadataState(modelLocation.id());
		if (state == null) {
			return;
		}

		var replacement = state.getCustomUnbakedModel(modelLocation, model, ignored -> model);
		if (replacement != null) {
			this.topLevelModels.put(modelLocation, replacement);
		}
	}
}
