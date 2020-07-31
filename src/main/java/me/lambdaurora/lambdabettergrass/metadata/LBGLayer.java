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
import me.lambdaurora.lambdabettergrass.util.LBGTextureGenerator;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Represents a layer.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LBGLayer
{
    /**
     * Parent metadata.
     */
    private final LBGMetadata parentMetadata;

    public final int colorIndex;

    /* Masks */
    private final Identifier connectMask;
    private final Identifier blendUpMask;
    private final Identifier blendArchMask;

    /* Textures */
    private final Identifier topTexture;
    private final Identifier sideTexture;

    /* Generated textures */
    private SpriteIdentifier connectTexture;
    private SpriteIdentifier blendUpTexture;
    private SpriteIdentifier blendUpMirroredTexture;
    private SpriteIdentifier archTexture;

    private final Object2ObjectMap<String, Sprite> bakedSprites = new Object2ObjectOpenHashMap<>();

    public LBGLayer(@NotNull LBGMetadata metadata, @NotNull JsonObject json)
    {
        this.parentMetadata = metadata;

        /* JSON read */
        if (json.has("color_index")) this.colorIndex = json.get("color_index").getAsInt();
        else this.colorIndex = -1;

        Identifier topTexture = ModelLoader.MISSING;
        Identifier sideTexture = ModelLoader.MISSING;
        if (json.has("textures")) {
            JsonObject textures = json.getAsJsonObject("textures");
            if (textures.has("top"))
                topTexture = new Identifier(textures.get("top").getAsString());
            if (textures.has("side"))
                sideTexture = new Identifier(textures.get("side").getAsString());

            if (textures.has("overrides")) {
                JsonObject overrides = textures.getAsJsonObject("overrides");

                this.connectTexture = this.getOverridenTexture(overrides, "connect");
                this.blendUpTexture = this.getOverridenTexture(overrides, "blend_up");
                this.blendUpMirroredTexture = this.getOverridenTexture(overrides, "blend_up_m");
                this.archTexture = this.getOverridenTexture(overrides, "arch");
            }
        }
        this.topTexture = topTexture;
        this.sideTexture = sideTexture;

        Identifier connectMask = LambdaBetterGrass.BETTER_GRASS_SIDE_CONNECT_MASK;
        Identifier blendUpMask = LambdaBetterGrass.BETTER_GRASS_SIDE_BLEND_UP_MASK;
        Identifier blendArchMask = LambdaBetterGrass.BETTER_GRASS_SIDE_ARCH_BLEND_MASK;
        if (json.has("masks")) {
            JsonObject mask = json.getAsJsonObject("masks");
            if (mask.has("connect")) {
                connectMask = new Identifier(mask.get("connect").getAsString() + ".png");
            }
            if (mask.has("blend_up")) {
                blendUpMask = new Identifier(mask.get("blend_up").getAsString() + ".png");
            }
            if (mask.has("arch")) {
                blendArchMask = new Identifier(mask.get("arch").getAsString() + ".png");
            }
        }
        this.connectMask = connectMask;
        this.blendUpMask = blendUpMask;
        this.blendArchMask = blendArchMask;
    }

    private @Nullable SpriteIdentifier getOverridenTexture(@NotNull JsonObject overrides, @NotNull String name)
    {
        if (overrides.has(name)) {
            SpriteIdentifier id = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier(overrides.get(name).getAsString()));
            this.parentMetadata.textures.add(id);
            return id;
        }
        return null;
    }

    public void buildTextures()
    {
        final NativeImage top = LBGTextureGenerator.getNativeImage(this.parentMetadata.resourceManager, getTexturePath(this.topTexture));
        final NativeImage side = LBGTextureGenerator.getNativeImage(this.parentMetadata.resourceManager, getTexturePath(this.sideTexture));

        String name;
        {
            String[] path = this.parentMetadata.id.getPath().split("/");
            if (path.length == 0)
                name = "undefined";
            else
                name = path[path.length - 1];
        }

        if (this.connectTexture == null) {
            final NativeImage connectMask = LBGTextureGenerator.getNativeImage(this.parentMetadata.resourceManager, this.connectMask);
            this.parentMetadata.textures.add(this.connectTexture = genTexture(name + "_" + this.colorIndex + "_connect", side, top, connectMask));
            connectMask.close();
        }

        if (this.blendUpTexture == null || this.blendUpMirroredTexture == null) {
            final NativeImage blendUp = LBGTextureGenerator.getNativeImage(this.parentMetadata.resourceManager, this.blendUpMask);
            if (this.blendUpTexture == null) {
                this.parentMetadata.textures.add(this.blendUpTexture = genTexture(name + "_" + this.colorIndex + "_blend_up", side, top, blendUp));
            }

            if (this.blendUpMirroredTexture == null) {
                final NativeImage blendUpMirrored = LBGTextureGenerator.mirrorImage(blendUp);
                this.parentMetadata.textures.add(this.blendUpMirroredTexture = genTexture(name + "_" + this.colorIndex + "_blend_up_m", side, top, blendUpMirrored));
                blendUpMirrored.close();
            }
            blendUp.close();
        }

        if (this.archTexture == null) {
            final NativeImage archMask = LBGTextureGenerator.getNativeImage(this.parentMetadata.resourceManager, this.blendArchMask);
            this.parentMetadata.textures.add(this.archTexture = genTexture(name + "_" + this.colorIndex + "_arch", side, top, archMask));
            archMask.close();
        }

        top.close();
        side.close();
    }

    private static @NotNull Identifier getTexturePath(@NotNull Identifier id)
    {
        return new Identifier(id.getNamespace(), "textures/" + id.getPath() + ".png");
    }

    private static @NotNull SpriteIdentifier genTexture(@NotNull String name, @NotNull NativeImage side, @NotNull NativeImage top, @NotNull NativeImage mask)
    {
        return new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, LBGTextureGenerator.generateTexture(name, side, top, mask));
    }

    public void bakeSprites(@NotNull Function<SpriteIdentifier, Sprite> textureGetter)
    {
        this.tryBakeSprite("connect", this.connectTexture, textureGetter);
        this.tryBakeSprite("blend_up", this.blendUpTexture, textureGetter);
        this.tryBakeSprite("blend_up_m", this.blendUpMirroredTexture, textureGetter);
        this.tryBakeSprite("arch", this.archTexture, textureGetter);
    }

    private void tryBakeSprite(@NotNull String name, @Nullable SpriteIdentifier id, @NotNull Function<SpriteIdentifier, Sprite> textureGetter)
    {
        if (id == null)
            id = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModelLoader.MISSING);

        try {
            this.bakedSprites.put(name, textureGetter.apply(id));
        } catch (NullPointerException e) {
            LambdaBetterGrass.get().warn("Could not bake sprite `" + name + "` with id `" + id.toString() + "`!");

            this.bakedSprites.put(name, textureGetter.apply(new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModelLoader.MISSING)));
        }
    }

    public @Nullable Sprite getBakedSprite(@NotNull String name)
    {
        return this.bakedSprites.get(name);
    }

    @Override
    public String toString()
    {
        return "LBGLayer{" +
                "parentMetadata=" + parentMetadata +
                ", colorIndex=" + colorIndex +
                '}';
    }
}
