/*
 * Copyright © 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.mixin;

import com.google.gson.JsonObject;
import dev.lambdaurora.lambdabettergrass.metadata.LBGLayerType;
import dev.lambdaurora.lambdabettergrass.metadata.LBGState;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelVariantMap;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.aperlambda.lambdacommon.LambdaConstants;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    private boolean lbg_firstLoad = true;

    @Inject(method = "putModel", at = @At("HEAD"), cancellable = true)
    private void onPutModel(Identifier id, UnbakedModel unbakedModel, CallbackInfo ci) {
        if (id instanceof ModelIdentifier modelId) {
            if (!modelId.getVariant().equals("inventory")) {
                if (this.lbg_firstLoad) {
                    LBGState.reset();
                    LBGLayerType.reset();
                    var layerTypes = this.resourceManager.findResources("bettergrass/layer_types",
                            path -> path.endsWith(".json"));
                    for (var layerTypeId : layerTypes) {
                        LBGLayerType.load(layerTypeId, this.resourceManager);
                    }
                    this.lbg_firstLoad = false;
                }

                var stateId = new Identifier(modelId.getNamespace(), "bettergrass/states/" + modelId.getPath());

                // Get cached states metadata.
                var state = LBGState.getMetadataState(stateId);

                // Find and load states metadata if not cached.
                if (state == null) {
                    var stateResourceId = new Identifier(stateId.getNamespace(), stateId.getPath() + ".json");
                    if (this.resourceManager.containsResource(stateResourceId)) {
                        try {
                            var json = (JsonObject) LambdaConstants.JSON_PARSER.parse(
                                    new InputStreamReader(this.resourceManager.getResource(stateResourceId).getInputStream())
                            );
                            state = LBGState.getOrLoadMetadataState(stateId, this.resourceManager, json, this.variantMapDeserializationContext);
                        } catch (IOException e) {
                            // Ignore.
                        }
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
