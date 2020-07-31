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
public class LBGMetadata
{
    public final Identifier id;

    protected final @NotNull ResourceManager        resourceManager;
    protected final          List<SpriteIdentifier> textures = new ArrayList<>();

    private final List<LBGLayer> layers = new ArrayList<>();

    public LBGMetadata(@NotNull ResourceManager resourceManager, @NotNull Identifier id, @NotNull JsonObject json, @NotNull Identifier modelId)
    {
        this.id = id;
        this.resourceManager = resourceManager;

        /* JSON read */
        if (json.has("layers")) {
            json.getAsJsonArray("layers").forEach(layer -> this.layers.add(new LBGLayer(this, layer.getAsJsonObject())));
        }

        this.buildTextures();
    }

    private void buildTextures()
    {
        for (LBGLayer layer : this.layers)
            layer.buildTextures();
    }

    public void bakeSprites(@NotNull Function<SpriteIdentifier, Sprite> textureGetter)
    {
        for (LBGLayer layer : this.layers) {
            layer.bakeSprites(textureGetter);
        }
    }

    /**
     * Returns the layer assigned to the specified color index.
     *
     * @param colorIndex The color index.
     * @return The optional layer.
     */
    public @NotNull Optional<LBGLayer> getLayer(int colorIndex)
    {
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
    public Collection<SpriteIdentifier> getTextures()
    {
        return this.textures;
    }

    @Override
    public String toString()
    {
        return "LBGMetadata{" +
                "id=" + id +
                '}';
    }
}
