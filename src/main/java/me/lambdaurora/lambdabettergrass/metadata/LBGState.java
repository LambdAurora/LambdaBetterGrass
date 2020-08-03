/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdabettergrass.metadata;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.aperlambda.lambdacommon.LambdaConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents grass model states with its different {@link LBGMetadata}.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LBGState {
    private static final Map<Identifier, LBGState> LBG_STATES = new HashMap<>();

    public final Identifier id;
    private final LBGMetadata metadata;
    private final Object2ObjectMap<String, LBGMetadata> metadatas = new Object2ObjectOpenHashMap<>();

    public LBGState(@NotNull Identifier id, @NotNull ResourceManager resourceManager, @NotNull JsonObject json) {
        this.id = id;

        // Look for variants.
        if (json.has("variants")) {
            JsonObject variants = json.getAsJsonObject("variants");
            variants.entrySet().forEach(entry -> {
                JsonObject variant = entry.getValue().getAsJsonObject();
                if (variant.has("data")) {
                    Identifier metadataId = new Identifier(variant.get("data").getAsString());

                    metadatas.put(entry.getKey(), this.loadMetadata(resourceManager, metadataId));
                }
            });
            this.metadata = null;
        } else if (json.has("data")) { // Look for a common metadata if no variants are specified.
            Identifier metadataId = new Identifier(json.get("data").getAsString());
            this.metadata = this.loadMetadata(resourceManager, metadataId);
        } else // The state file is invalid, cannot find any metadata.
            this.metadata = null;

        LBG_STATES.put(this.id, this);
    }

    /**
     * Loads the metadata from the resource manager.
     *
     * @param resourceManager The resource manager.
     * @param metadataId      The metadata identifier.
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

    /**
     * Returns the state from the cache using its identifier.
     *
     * @param id The identifier of the state.
     * @return The state if cached, else null.
     */
    public static @Nullable LBGState getMetadataState(@NotNull Identifier id) {
        return LBG_STATES.get(id);
    }

    /**
     * Resets all the known states cache.
     */
    public static void reset() {
        LBG_STATES.clear();
    }
}
