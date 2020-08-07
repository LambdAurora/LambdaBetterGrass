/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdabettergrass.mixin;

import com.google.gson.JsonObject;
import me.lambdaurora.lambdabettergrass.metadata.LBGMetadata;
import me.lambdaurora.lambdabettergrass.metadata.LBGState;
import me.lambdaurora.lambdabettergrass.model.LBGUnbakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
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
public abstract class ModelLoaderMixin
{
    @Shadow
    @Final
    private Map<Identifier, UnbakedModel> unbakedModels;

    @Shadow
    @Final
    private Set<Identifier> modelsToLoad;

    @Shadow
    @Final
    private ResourceManager resourceManager;

    @Shadow
    public abstract UnbakedModel getOrLoadModel(Identifier id);

    @Inject(method = "putModel", at = @At("HEAD"), cancellable = true)
    private void onPutModel(Identifier id, UnbakedModel unbakedModel, CallbackInfo ci)
    {
        if (id.getPath().contains("snow"))
            System.out.println(id + " ;; " + unbakedModel);
        if (id instanceof ModelIdentifier) {
            ModelIdentifier modelId = (ModelIdentifier) id;
            if (!modelId.getVariant().equals("inventory")) {
                Identifier stateId = new Identifier(modelId.getNamespace(), "bettergrass/states/" + modelId.getPath());

                // Get cached states metadata.
                LBGState state = LBGState.getMetadataState(stateId);

                // Find and load states metadata if not cached.
                if (state == null) {
                    Identifier stateResourceId = new Identifier(stateId.getNamespace(), stateId.getPath() + ".json");
                    if (this.resourceManager.containsResource(stateResourceId)) {
                        try {
                            JsonObject json = (JsonObject) LambdaConstants.JSON_PARSER.parse(new InputStreamReader(this.resourceManager.getResource(stateResourceId).getInputStream()));
                            state = LBGState.getOrLoadMetadataState(stateId, this.resourceManager, json);
                        } catch (IOException e) {
                            // Ignore.
                        }
                    }
                }

                // If states metadata found, search for corresponding metadata and if exists replace the model.
                if (state != null) {
                    UnbakedModel newModel = state.getCustomUnbakedModel(modelId, unbakedModel, this::getOrLoadModel);
                    if (newModel != null) {
                        this.unbakedModels.put(modelId, newModel);
                        this.modelsToLoad.addAll(unbakedModel.getModelDependencies());
                        ci.cancel();
                    }
                }
            }
        }
    }
}
