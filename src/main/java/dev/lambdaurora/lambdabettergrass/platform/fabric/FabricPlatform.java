/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.platform.fabric;

import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.metadata.LBGState;
import dev.lambdaurora.lambdabettergrass.platform.Platform;
import dev.yumi.mc.core.api.ModContainer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Text;

/**
 * Provides Fabric-specific platform operations.
 *
 * @author LambdAurora
 * @version 2.0.4
 * @since 2.0.4
 */
public final class FabricPlatform implements Platform {
	private static boolean modelLoadingPluginRegistered;

	@Override
	public void registerBuiltinResourcePacks(ModContainer mod) {
		var fabricMod = FabricLoader.getInstance().getModContainer(mod.id()).orElseThrow();

		ResourceManagerHelper.registerBuiltinResourcePack(
				LambdaBetterGrass.id("default"), fabricMod,
				Text.translatable("lambdabettergrass.resourcepack.default", Text.translatable(LambdaBetterGrass.NAMESPACE)),
				ResourcePackActivationType.DEFAULT_ENABLED
		);
		ResourceManagerHelper.registerBuiltinResourcePack(
				LambdaBetterGrass.id("x32"), fabricMod, ResourcePackActivationType.NORMAL
		);
	}

	@Override
	public void registerModelLoadingPlugin() {
		synchronized (FabricPlatform.class) {
			if (modelLoadingPluginRegistered) {
				return;
			}

			modelLoadingPluginRegistered = true;
		}

		ModelLoadingPlugin.register(pluginCtx -> {
			pluginCtx.modifyModelOnLoad().register(ModelModifier.WRAP_PHASE, (model, context) -> {
				final var modelId = context.topLevelId();
				if (modelId != null && !modelId.variant().equals("inventory")) {
					var state = LBGState.getMetadataState(modelId.id());

					if (state != null) {
						var newModel = state.getCustomUnbakedModel(modelId, model, context::getOrLoadModel);

						if (newModel != null) {
							return newModel;
						}
					}
				}

				return model;
			});
		});
	}
}
