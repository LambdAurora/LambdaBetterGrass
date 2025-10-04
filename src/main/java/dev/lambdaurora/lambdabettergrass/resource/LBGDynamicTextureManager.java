/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.resource;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the dynamic texture manager of LambdaBetterGrass to handle any runtime-generated textures.
 *
 * @version 2.5.0
 * @since 2.0.0
 * @author LambdAurora
 */
public class LBGDynamicTextureManager {
	private final Map<Identifier, SpriteContents> sprites = new ConcurrentHashMap<>();
	private CompletableFuture<List<SpriteContents>> future = CompletableFuture.completedFuture(List.of());

	public void reset() {
		this.sprites.clear();
		this.future = new CompletableFuture<>();
	}

	public void finish() {
		this.future.complete(List.copyOf(this.sprites.values()));
	}

	public CompletableFuture<List<SpriteContents>> awaitSprites() {
		return this.future;
	}

	public void registerSprite(Identifier id, NativeImage image) {
		var sprite = this.createSpriteContents(id, image);
		var oldSprite = this.sprites.put(id, sprite);
		if (oldSprite != null) {
			oldSprite.close();
		}
	}

	/**
	 * Creates the sprite contents from a given identifier and a given image.
	 *
	 * @param id the identifier of the sprite
	 * @param image the image of the sprite
	 * @return the sprite contents
	 */
	private SpriteContents createSpriteContents(Identifier id, NativeImage image) {
		return new SpriteContents(id, new FrameSize(image.getWidth(), image.getHeight()), image);
	}
}
