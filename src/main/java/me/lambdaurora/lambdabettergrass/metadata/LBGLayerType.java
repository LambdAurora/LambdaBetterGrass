/*
 * Copyright © 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdabettergrass.metadata;

import com.google.gson.JsonObject;
import me.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.resource.ResourceManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.aperlambda.lambdacommon.LambdaConstants;
import org.aperlambda.lambdacommon.utils.Nameable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents the layer types.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LBGLayerType implements Nameable {
    private static List<LBGLayerType> LAYER_TYPES = new ArrayList<>();

    public final Identifier id;
    public final Block block;
    public final Identifier modelId;
    private final String name;

    public LBGLayerType(@NotNull Identifier id, @NotNull Block block, @NotNull Identifier modelId) {
        this.id = id;
        this.block = block;
        this.modelId = modelId;
        String[] path = this.id.getPath().split("/");
        this.name = path[path.length - 1];
    }

    /**
     * Returns the unbaked layer model.
     *
     * @param modelGetter The model getter.
     * @return The unbaked model.
     */
    public @NotNull UnbakedModel getLayerModel(@NotNull Function<Identifier, UnbakedModel> modelGetter) {
        return modelGetter.apply(this.modelId);
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    /**
     * Resets the registered layer types.
     */
    public static void reset() {
        LAYER_TYPES.clear();
    }

    public static void forEach(Consumer<LBGLayerType> consumer) {
        LAYER_TYPES.forEach(consumer);
    }

    public static void load(@NotNull Identifier resourceId, @NotNull ResourceManager resourceManager) {
        Identifier id = new Identifier(resourceId.getNamespace(), resourceId.getPath().replace(".json", ""));
        try {
            InputStream stream = resourceManager.getResource(resourceId).getInputStream();
            JsonObject json = LambdaConstants.JSON_PARSER.parse(new InputStreamReader(stream)).getAsJsonObject();

            Identifier affectId = new Identifier(json.get("block").getAsString());
            Block block = Registry.BLOCK.get(affectId);

            if (block == Blocks.AIR)
                return;

            if (!block.getDefaultState().getProperties().contains(Properties.LAYERS)) {
                LambdaBetterGrass.get().warn("Failed to load layer type \"" + id + "\", block does not have layer property.");
                return;
            }

            Identifier modelId = new Identifier(json.get("model").getAsString());

            stream.close();

            LAYER_TYPES.add(new LBGLayerType(id, block, modelId));
        } catch (IOException | IllegalStateException e) {
            LambdaBetterGrass.get().warn("Failed to load layer type \"" + id + "\".");
        }
    }

    @Override
    public String toString() {
        return "LBGLayerType{" +
                "id=" + id +
                ", block=" + block +
                ", modelId=" + modelId +
                '}';
    }
}
