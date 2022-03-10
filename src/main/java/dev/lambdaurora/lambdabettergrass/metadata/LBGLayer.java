/*
 * Copyright © 2021, 2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata;

import com.google.gson.JsonObject;
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.util.LBGTextureGenerator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

/**
 * Represents a layer.
 *
 * @author LambdAurora
 * @version 1.2.0
 * @since 1.0.0
 */
public class LBGLayer {
	/**
	 * Parent metadata.
	 */
	private final LBGMetadata parentMetadata;

	public final int index;
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

	private final Map<String, Sprite> bakedSprites = new Object2ObjectOpenHashMap<>();

	public LBGLayer(LBGMetadata metadata, JsonObject json) {
		this.parentMetadata = metadata;
		this.index = metadata.nextLayerIndex();

		/* JSON read */
		if (json.has("color_index")) this.colorIndex = json.get("color_index").getAsInt();
		else this.colorIndex = -1;

		Identifier topTexture = ModelLoader.MISSING_ID;
		Identifier sideTexture = ModelLoader.MISSING_ID;
		if (json.has("textures")) {
			var textures = json.getAsJsonObject("textures");
			if (textures.has("top"))
				topTexture = new Identifier(textures.get("top").getAsString());
			if (textures.has("side"))
				sideTexture = new Identifier(textures.get("side").getAsString());

			if (textures.has("overrides")) {
				var overrides = textures.getAsJsonObject("overrides");

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
			var mask = json.getAsJsonObject("masks");
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

	/**
	 * Returns the overriden texture with the specified name if it exists.
	 *
	 * @param overrides the overrides JSON object
	 * @param name the name of the texture
	 * @return {@code null} if not specified, else the identifier of the overriden texture
	 */
	private @Nullable SpriteIdentifier getOverridenTexture(JsonObject overrides, String name) {
		if (overrides.has(name)) {
			var id = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier(overrides.get(name).getAsString()));
			this.parentMetadata.textures.add(id);
			return id;
		}
		return null;
	}

	/**
	 * Builds the non-overriden textures.
	 * <p>
	 * This will apply the specified masks with the top and side textures if no override texture was specified.
	 */
	public void buildTextures() {
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
			this.parentMetadata.textures.add(this.connectTexture = genTexture(name + "_" + this.index + "_connect", side, top, connectMask));
			connectMask.close();
		}

		if (this.blendUpTexture == null || this.blendUpMirroredTexture == null) {
			final NativeImage blendUp = LBGTextureGenerator.getNativeImage(this.parentMetadata.resourceManager, this.blendUpMask);
			if (this.blendUpTexture == null) {
				this.parentMetadata.textures.add(this.blendUpTexture = genTexture(name + "_" + this.index + "_blend_up", side, top, blendUp));
			}

			if (this.blendUpMirroredTexture == null) {
				final NativeImage blendUpMirrored = LBGTextureGenerator.mirrorImage(blendUp);
				this.parentMetadata.textures.add(
						this.blendUpMirroredTexture = genTexture(name + "_" + this.index + "_blend_up_m", side, top, blendUpMirrored)
				);
				blendUpMirrored.close();
			}
			blendUp.close();
		}

		if (this.archTexture == null) {
			final NativeImage archMask = LBGTextureGenerator.getNativeImage(this.parentMetadata.resourceManager, this.blendArchMask);
			this.parentMetadata.textures.add(this.archTexture = genTexture(name + "_" + this.index + "_arch", side, top, archMask));
			archMask.close();
		}

		top.close();
		side.close();
	}

	private static Identifier getTexturePath(Identifier id) {
		return new Identifier(id.getNamespace(), "textures/" + id.getPath() + ".png");
	}

	private static SpriteIdentifier genTexture(String name, NativeImage side, NativeImage top, NativeImage mask) {
		return new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, LBGTextureGenerator.generateTexture(name, side, top, mask));
	}

	/**
	 * Bakes the textures of this layer.
	 *
	 * @param textureGetter the texture getter
	 */
	public void bakeTextures(Function<SpriteIdentifier, Sprite> textureGetter) {
		this.tryBakeSprite("connect", this.connectTexture, textureGetter);
		this.tryBakeSprite("blend_up", this.blendUpTexture, textureGetter);
		this.tryBakeSprite("blend_up_m", this.blendUpMirroredTexture, textureGetter);
		this.tryBakeSprite("arch", this.archTexture, textureGetter);
	}

	private void tryBakeSprite(String name, @Nullable SpriteIdentifier id, Function<SpriteIdentifier, Sprite> textureGetter) {
		if (id == null)
			id = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModelLoader.MISSING_ID);

		try {
			this.bakedSprites.put(name, textureGetter.apply(id));
		} catch (NullPointerException e) {
			LambdaBetterGrass.get().warn("Could not bake sprite `" + name + "` with id `" + id + "`!");

			this.bakedSprites.put(name, textureGetter.apply(new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ModelLoader.MISSING_ID)));
		}
	}

	/**
	 * Return the baked texture by its name.
	 *
	 * @param name the name of the baked texture
	 * @return the baked texture if found, else {@code null}
	 */
	public @Nullable Sprite getBakedTexture(String name) {
		return this.bakedSprites.get(name);
	}

	@Override
	public String toString() {
		return "LBGLayer{" +
				"index=" + this.index +
				", colorIndex=" + this.colorIndex +
				'}';
	}

	/**
	 * Merges two layers.
	 *
	 * @param parent the parent layer
	 * @param child the child layer
	 * @return the merged layer
	 */
	public static LBGLayer mergeLayers(LBGLayer parent, LBGLayer child) {
		if (parent.colorIndex != child.colorIndex)
			return parent;

		String name;
		{
			String[] path = parent.parentMetadata.id.getPath().split("/");
			if (path.length == 0)
				name = "undefined";
			else
				name = path[path.length - 1];
		}

		// Merge textures into parent
		if (parent.connectTexture != null && child.connectTexture != null) {
			final NativeImage parentConnect
					= LBGTextureGenerator.getNativeImage(parent.parentMetadata.resourceManager, getTexturePath(parent.connectTexture.getTextureId()));
			final NativeImage childConnect
					= LBGTextureGenerator.getNativeImage(child.parentMetadata.resourceManager, getTexturePath(child.connectTexture.getTextureId()));

			parent.parentMetadata.textures.remove(parent.connectTexture);
			parent.parentMetadata.textures.add(
					parent.connectTexture = genTexture(name + "_" + parent.index + "_connect", parentConnect, childConnect, childConnect)
			);
			parentConnect.close();
			childConnect.close();
		}

		if (parent.blendUpTexture != null && child.blendUpTexture != null) {
			final NativeImage parentBlendUp =
					LBGTextureGenerator.getNativeImage(parent.parentMetadata.resourceManager, getTexturePath(parent.blendUpTexture.getTextureId()));
			final NativeImage childBlendUp =
					LBGTextureGenerator.getNativeImage(child.parentMetadata.resourceManager, getTexturePath(child.blendUpTexture.getTextureId()));

			parent.parentMetadata.textures.remove(parent.blendUpTexture);
			parent.parentMetadata.textures.add(
					parent.blendUpTexture = genTexture(name + "_" + parent.index + "_blend_up", parentBlendUp, childBlendUp, childBlendUp)
			);
			parentBlendUp.close();
			childBlendUp.close();
		}

		if (parent.blendUpMirroredTexture != null && child.blendUpMirroredTexture != null) {
			final NativeImage parentBlendUp =
					LBGTextureGenerator.getNativeImage(parent.parentMetadata.resourceManager,
							getTexturePath(parent.blendUpMirroredTexture.getTextureId()));
			final NativeImage childBlendUp =
					LBGTextureGenerator.getNativeImage(child.parentMetadata.resourceManager,
							getTexturePath(child.blendUpMirroredTexture.getTextureId()));

			parent.parentMetadata.textures.remove(parent.blendUpMirroredTexture);
			parent.parentMetadata.textures.add(
					parent.blendUpMirroredTexture = genTexture(name + "_" + parent.index + "_blend_up_m",
							parentBlendUp, childBlendUp, childBlendUp)
			);
			parentBlendUp.close();
			childBlendUp.close();
		}

		if (parent.archTexture != null && child.archTexture != null) {
			final NativeImage parentArch =
					LBGTextureGenerator.getNativeImage(parent.parentMetadata.resourceManager, getTexturePath(parent.archTexture.getTextureId()));
			final NativeImage childArch =
					LBGTextureGenerator.getNativeImage(child.parentMetadata.resourceManager, getTexturePath(child.archTexture.getTextureId()));

			parent.parentMetadata.textures.remove(parent.archTexture);
			parent.parentMetadata.textures.add(
					parent.archTexture = genTexture(name + "_" + parent.index + "_arch", parentArch, childArch, childArch)
			);
			parentArch.close();
			childArch.close();
		}

		return parent;
	}
}
