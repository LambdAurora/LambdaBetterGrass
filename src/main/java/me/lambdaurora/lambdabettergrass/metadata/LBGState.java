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
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.data.client.model.BlockStateVariantMap;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Represents LambdaBetterGrass model states.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class LBGState
{
    private static final Map<String, BlockStateVariantMap.TriFunction<Identifier, ResourceManager, JsonObject, LBGState>> LBG_STATES_TYPE = new HashMap<>();
    private static final Map<Identifier, LBGState>                                                                        LBG_STATES      = new HashMap<>();

    public final Identifier id;

    public LBGState(@NotNull Identifier id)
    {
        this.id = id;
        putState(id, this);
    }

    public abstract @Nullable UnbakedModel getCustomUnbakedModel(@NotNull ModelIdentifier modelId, @NotNull UnbakedModel originalModel, @NotNull Function<Identifier, UnbakedModel> modelGetter);

    protected static void putState(@NotNull Identifier id, @NotNull LBGState state)
    {
        LBG_STATES.put(id, state);
    }

    /**
     * Returns the state from the cache using its identifier.
     *
     * @param id The identifier of the state.
     * @return The state if cached, else null.
     */
    public static @Nullable LBGState getMetadataState(@NotNull Identifier id)
    {
        return LBG_STATES.get(id);
    }

    /**
     * Resets all the known states cache.
     */
    public static void reset()
    {
        LBG_STATES.clear();
    }

    public static void registerType(@NotNull String type, @NotNull BlockStateVariantMap.TriFunction<Identifier, ResourceManager, JsonObject, LBGState> stateGenerator)
    {
        LBG_STATES_TYPE.put(type, stateGenerator);
    }

    public static @Nullable LBGState getOrLoadMetadataState(@NotNull Identifier id, @NotNull ResourceManager resourceManager, @NotNull JsonObject json)
    {
        LBGState state = getMetadataState(id);
        if (state != null)
            return state;

        String type = "grass";
        if (json.has("type"))
            type = json.get("type").getAsString();

        if (!LBG_STATES_TYPE.containsKey(type))
            return null;

        return LBG_STATES_TYPE.get(type).apply(id, resourceManager, json);
    }
}
