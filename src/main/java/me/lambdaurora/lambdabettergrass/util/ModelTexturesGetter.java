/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdabettergrass.util;

import com.google.gson.JsonObject;
import me.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import org.aperlambda.lambdacommon.LambdaConstants;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public enum ModelTexturesGetter
{
    ; // No instantiation possible <3

    public static @NotNull Collection<SpriteIdentifier> getOriginalModelTextures(@NotNull ResourceManager resourceManager, @NotNull Identifier modelId)
    {
        List<SpriteIdentifier> textures = new ArrayList<>();

        Identifier modelMetadataId = new Identifier(modelId.getNamespace(), "models/block/" + modelId.getPath() + ".json");
        if (resourceManager.containsResource(modelMetadataId)) {
            try {
                JsonObject json = (JsonObject) LambdaConstants.JSON_PARSER.parse(new InputStreamReader(resourceManager.getResource(modelMetadataId).getInputStream()));
                textures.addAll(getOriginalModelTextures(resourceManager, modelId, json));
            } catch (IOException e) {
                LambdaBetterGrass.get().warn("Error while loading original model textures from model " + modelId.toString());
            }
        }

        return textures;
    }

    public static @NotNull Collection<SpriteIdentifier> getOriginalModelTextures(@NotNull ResourceManager resourceManager, @NotNull Identifier modelId, @NotNull JsonObject json)
    {
        List<SpriteIdentifier> textures = new ArrayList<>();

        if (json.has("parent")) {
            // Get parent textures.
            Identifier parentId = new Identifier(json.get("parent").getAsString());
            textures.addAll(getOriginalModelTextures(resourceManager, parentId));
        }

        if (json.has("textures")) {
            JsonObject texturesField = json.getAsJsonObject("textures");
            texturesField.entrySet().forEach(entry -> textures.add(new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier(entry.getValue().getAsString()))));
        }

        return textures;
    }
}
