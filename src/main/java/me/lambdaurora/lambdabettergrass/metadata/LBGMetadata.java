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
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents a metadata.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LBGMetadata {
    /**
     * Represents the identifier of the metadata.
     */
    public final Identifier id;

    protected final @NotNull ResourceManager resourceManager;
    protected final List<SpriteIdentifier> textures = new ArrayList<>();

    private final List<LBGLayer> layers = new ArrayList<>();

    private int lastLayerIndex = 0;

    public LBGMetadata(@NotNull ResourceManager resourceManager, @NotNull Identifier id, @NotNull JsonObject json) {
        this.id = id;
        this.resourceManager = resourceManager;

        /* JSON read */
        if (json.has("layers")) {
            json.getAsJsonArray("layers").forEach(layer -> this.layers.add(new LBGLayer(this, layer.getAsJsonObject())));
        }

        this.buildTextures();

        /* Merge layers */
        Int2ObjectMap<LBGLayer> parentLayers = new Int2ObjectArrayMap<>();
        for (LBGLayer layer : this.layers) {
            if (!parentLayers.containsKey(layer.colorIndex)) {
                parentLayers.put(layer.colorIndex, layer);
            } else {
                // Merge layer
                LBGLayer.mergeLayers(parentLayers.get(layer.colorIndex), layer);
            }
        }

        this.layers.clear();
        this.layers.addAll(parentLayers.values());
    }

    /**
     * Returns the next layer index to assign and increments the internal layer index counter.
     *
     * @return The next layer index.
     */
    protected int nextLayerIndex() {
        return this.lastLayerIndex++;
    }

    private void buildTextures() {
        for (LBGLayer layer : this.layers)
            layer.buildTextures();
    }

    /**
     * Bakes the textures.
     *
     * @param textureGetter The texture getter.
     */
    public void bakeTextures(@NotNull Function<SpriteIdentifier, Sprite> textureGetter) {
        for (LBGLayer layer : this.layers) {
            layer.bakeTextures(textureGetter);
        }
    }

    /**
     * Returns the layer assigned to the specified color index.
     *
     * @param colorIndex The color index.
     * @return The optional layer.
     */
    public @NotNull Optional<LBGLayer> getLayer(int colorIndex) {
        for (LBGLayer layer : this.layers) {
            if (layer.colorIndex == colorIndex)
                return Optional.of(layer);
        }
        return Optional.empty();
    }

    /**
     * Returns the textures.
     *
     * @return The textures.
     */
    public Collection<SpriteIdentifier> getTextures() {
        return this.textures;
    }

    @Override
    public String toString() {
        return "LBGMetadata{" +
                "id=" + id +
                '}';
    }
}
