/*
 * Copyright © 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.model.LBGUnbakedModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.aperlambda.lambdacommon.LambdaConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.function.Function;

/**
 * Represents grass model states with its different {@link LBGMetadata}.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LBGGrassState extends LBGState {
    private final LBGMetadata metadata;
    private final Object2ObjectMap<String, LBGMetadata> metadatas = new Object2ObjectOpenHashMap<>();

    public LBGGrassState(@NotNull Identifier id, @NotNull ResourceManager resourceManager, @NotNull JsonObject json) {
        super(id);

        // Look for variants.
        if (json.has("variants")) {
            JsonObject variants = json.getAsJsonObject("variants");
            variants.entrySet().forEach(entry -> {
                JsonObject variant = entry.getValue().getAsJsonObject();
                if (variant.has("data")) {
                    Identifier metadataId = new Identifier(variant.get("data").getAsString());

                    this.metadatas.put(entry.getKey(), this.loadMetadata(resourceManager, metadataId));
                }
            });

            {
                LBGMetadata normalMetadata = this.metadatas.get("snowy=false");
                LBGMetadata snowyMetadata = this.metadatas.get("snowy=true");

                if (normalMetadata != null && snowyMetadata != null) {
                    snowyMetadata.snowyModelVariantProvider = bakedModel -> normalMetadata.snowyModelVariant = bakedModel;
                }
            }

            this.metadata = null;
        } else if (json.has("data")) { // Look for a common metadata if no variants are specified.
            Identifier metadataId = new Identifier(json.get("data").getAsString());
            this.metadata = this.loadMetadata(resourceManager, metadataId);
        } else // The state file is invalid, cannot find any metadata.
            this.metadata = null;
    }

    /**
     * Loads the metadata from the resource manager.
     *
     * @param resourceManager The resource manager.
     * @param metadataId The metadata identifier.
     * @return The metadata if loaded successfully, else null
     */
    private @Nullable LBGMetadata loadMetadata(@NotNull ResourceManager resourceManager, @NotNull Identifier metadataId) {
        Identifier metadataResourceId = new Identifier(metadataId.getNamespace(), metadataId.getPath() + ".json");
        if (resourceManager.containsResource(metadataResourceId)) {
            try {
                JsonObject metadataJson = (JsonObject) LambdaConstants.JSON_PARSER.parse(new InputStreamReader(resourceManager.getResource(metadataResourceId).getInputStream()));
                LBGMetadata metadata = new LBGMetadata(resourceManager, metadataId, metadataJson);

                return metadata;
            } catch (IOException e) {
                // Ignore.
            }
        }
        LambdaBetterGrass.get().warn("Could not load metadata `" + metadataId + "`.");
        return null;
    }

    /**
     * Returns the metadata corresponding to the specified model identifier.
     *
     * @param modelId The model identifier.
     * @return A metadata if it exists for the given model id, else null.
     */
    public @Nullable LBGMetadata getMetadata(@NotNull ModelIdentifier modelId) {
        if (this.metadata != null)
            return this.metadata;
        for (Map.Entry<String, LBGMetadata> variant : this.metadatas.entrySet()) {
            if (variant.getKey().equals(modelId.getVariant()))
                return variant.getValue();
        }
        return null;
    }

    @Override
    public @Nullable UnbakedModel getCustomUnbakedModel(@NotNull ModelIdentifier modelId, @NotNull UnbakedModel originalModel, @NotNull Function<Identifier, UnbakedModel> modelGetter) {
        LBGMetadata metadata = this.getMetadata(modelId);
        if (metadata != null) {
            UnbakedModel model = new LBGUnbakedModel(originalModel, metadata);
            /*if (this.metadata == null && modelId.getVariant().equals("snowy=true")) {
                LBGMetadata nonSnowyMetadata = this.getMetadata(new ModelIdentifier(new Identifier(modelId.getNamespace(), modelId.getPath()), "snowy=false"));
                if (nonSnowyMetadata != null)
                    nonSnowyMetadata.snowyVariant = model;
            }*/
            return model;
        }
        return null;
    }
}
