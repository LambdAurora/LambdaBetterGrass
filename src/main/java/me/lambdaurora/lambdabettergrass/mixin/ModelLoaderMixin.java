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

@Mixin(ModelLoader.class)
public class ModelLoaderMixin
{
    @Shadow
    @Final
    private Map<Identifier, UnbakedModel> unbakedModels;

    @Shadow
    @Final
    private Map<Identifier, UnbakedModel> modelsToBake;

    @Shadow
    @Final
    private ResourceManager resourceManager;

    @Inject(method = "putModel", at = @At("HEAD"), cancellable = true)
    private void onPutModel(Identifier id, UnbakedModel unbakedModel, CallbackInfo ci)
    {
        if (id instanceof ModelIdentifier) {
            ModelIdentifier modelId = (ModelIdentifier) id;
            if (!modelId.getVariant().equals("inventory")) {
                // Find and load metadata.
                Identifier metadataId = new Identifier(modelId.getNamespace(), "bettergrass/data/" + modelId.getPath());
                Identifier metadataResourceId = new Identifier(metadataId.getNamespace(), metadataId.getPath() + ".json");
                if (this.resourceManager.containsResource(metadataResourceId)) {
                    try {
                        JsonObject json = (JsonObject) LambdaConstants.JSON_PARSER.parse(new InputStreamReader(resourceManager.getResource(metadataResourceId).getInputStream()));
                        LBGMetadata metadata = new LBGMetadata(resourceManager, metadataId, json, modelId);

                        UnbakedModel model = new LBGUnbakedModel(unbakedModel, metadata);
                        this.unbakedModels.put(modelId, model);
                        this.modelsToBake.put(modelId, model);
                        ci.cancel();
                    } catch (IOException e) {
                        // Ignore.
                    }
                }
            }
        }
    }
}
