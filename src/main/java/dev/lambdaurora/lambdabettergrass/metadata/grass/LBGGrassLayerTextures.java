/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata.grass;

import com.mojang.blaze3d.platform.NativeImage;
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.util.LBGTextureGenerator;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.io.ResourceManager;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.Optional;
import java.util.function.Function;

public record LBGGrassLayerTextures(
		@NotNull Texture connect,
		@NotNull Texture blendUp,
		@NotNull Texture blendUpMirrored,
		@NotNull Texture arch
) implements Closeable {
	public Identifier resolveConnect() {
		return resolveTexture(this.connect);
	}

	public Identifier resolveBlendUp() {
		return resolveTexture(this.blendUp);
	}

	public Identifier resolveBlendUpMirrored() {
		return resolveTexture(this.blendUpMirrored);
	}

	public Identifier resolveArch() {
		return resolveTexture(this.arch);
	}

	private static Identifier resolveTexture(@NotNull Texture texture) {
		final var id = texture.resolveId();

		if (texture instanceof DirectTexture directTexture) {
			LambdaBetterGrass.get().dynamicTextureManager.registerSprite(id, directTexture.image);
		}

		return id;
	}

	public LBGGrassLayerTextures merge(LBGGrassLayerTextures other) {
		final NativeImage connectTexture = LBGTextureGenerator.applyMask(
				this.connect.getImage(), other.connect.getImage(), other.connect.getImage()
		);
		final NativeImage blendUpTexture = LBGTextureGenerator.applyMask(
				this.blendUp.getImage(), other.blendUp.getImage(), other.blendUp.getImage()
		);
		final NativeImage blendUpMirroredTexture = LBGTextureGenerator.applyMask(
				this.blendUpMirrored.getImage(), other.blendUpMirrored.getImage(), other.blendUpMirrored.getImage()
		);
		final NativeImage archTexture = LBGTextureGenerator.applyMask(
				this.arch.getImage(), other.arch.getImage(), other.arch.getImage()
		);

		return new LBGGrassLayerTextures(
				new DirectTexture(this.connect.resolveId(), connectTexture),
				new DirectTexture(this.blendUp.resolveId(), blendUpTexture),
				new DirectTexture(this.blendUpMirrored.resolveId(), blendUpMirroredTexture),
				new DirectTexture(this.arch.resolveId(), archTexture)
		);
	}

	@Override
	public void close() {
		this.connect.close();
		this.blendUp.close();
		this.blendUpMirrored.close();
		this.arch.close();
	}

	public static LBGGrassLayerTextures generate(
			ResourceManager resourceManager, Identifier metadataId, LBGLoadingGrassLayer layer
	) {
		final var textures = layer.textures();
		final var overrides = textures.overrides();

		String name;
		{
			String[] path = metadataId.path().split("/");
			if (path.length == 0)
				name = "undefined";
			else
				name = path[path.length - 1];
		}

		if (layer.colorIndex() > -1) {
			name += "_" + layer.colorIndex();
		}

		try (final var context = new ResolutionContext(resourceManager, textures, layer.masks().withPngSuffix())) {
			final var connectTexture = resolve(
					resourceManager, context, name + "_connect", ResolutionContext::getConnectMaskTexture, overrides.connect()
			);
			final var blendUpTexture = resolve(
					resourceManager, context, name + "_blend_up", ResolutionContext::getBlendUpMaskTexture, overrides.blendUp()
			);
			final var blendUpMirroredTexture = resolve(
					resourceManager, context, name + "_blend_up_m",
					ResolutionContext::getBlendUpMirroredMaskTexture, overrides.blendUpMirrored()
			);
			final var archTexture = resolve(
					resourceManager, context, name + "_arch", ResolutionContext::getArchMaskTexture, overrides.arch()
			);

			return new LBGGrassLayerTextures(
					connectTexture, blendUpTexture, blendUpMirroredTexture, archTexture
			);
		}
	}

	private static Texture resolve(
			ResourceManager resourceManager, ResolutionContext context,
			String name, Function<ResolutionContext, NativeImage> maskGetter, Optional<Identifier> override
	) {
		if (override.isPresent()) {
			return new FromDiskTexture(resourceManager, getTexturePath(override.get()));
		} else {
			final var id = LambdaBetterGrass.id("block/bettergrass/" + name);
			final var image = LBGTextureGenerator.applyMask(context.getSideTexture(), context.getTopTexture(), maskGetter.apply(context));

			return new DirectTexture(id, image);
		}
	}

	private static Identifier getTexturePath(Identifier id) {
		return new Identifier(id.namespace(), "textures/" + id.path() + ".png");
	}

	private static final class ResolutionContext implements AutoCloseable {
		private final ResourceManager resourceManager;
		private final LBGLoadingGrassLayer.Textures textureIds;
		private final LBGGrassMasks masks;
		private NativeImage topTexture;
		private NativeImage sideTexture;
		private NativeImage connectMaskTexture;
		private NativeImage blendUpMaskTexture;
		private NativeImage blendUpMirroredMaskTexture;
		private NativeImage archMaskTexture;

		private ResolutionContext(ResourceManager resourceManager, LBGLoadingGrassLayer.Textures textureIds, LBGGrassMasks masks) {
			this.resourceManager = resourceManager;
			this.textureIds = textureIds;
			this.masks = masks;
		}

		public NativeImage getTopTexture() {
			if (this.topTexture == null) {
				this.topTexture = LBGTextureGenerator.getNativeImage(this.resourceManager, getTexturePath(this.textureIds.top()));
			}

			return this.topTexture;
		}

		public NativeImage getSideTexture() {
			if (this.sideTexture == null) {
				this.sideTexture = LBGTextureGenerator.getNativeImage(this.resourceManager, getTexturePath(this.textureIds.side()));
			}

			return this.sideTexture;
		}

		public NativeImage getConnectMaskTexture() {
			if (this.connectMaskTexture == null) {
				this.connectMaskTexture = LBGTextureGenerator.getNativeImage(this.resourceManager, this.masks.connect());
			}

			return this.connectMaskTexture;
		}

		public NativeImage getBlendUpMaskTexture() {
			if (this.blendUpMaskTexture == null) {
				this.blendUpMaskTexture = LBGTextureGenerator.getNativeImage(this.resourceManager, this.masks.blendUp());
			}

			return this.blendUpMaskTexture;
		}

		public NativeImage getBlendUpMirroredMaskTexture() {
			if (this.blendUpMirroredMaskTexture == null) {
				this.blendUpMirroredMaskTexture = LBGTextureGenerator.mirrorImage(this.getBlendUpMaskTexture());
			}

			return this.blendUpMirroredMaskTexture;
		}

		public NativeImage getArchMaskTexture() {
			if (this.archMaskTexture == null) {
				this.archMaskTexture = LBGTextureGenerator.getNativeImage(this.resourceManager, this.masks.arch());
			}

			return this.archMaskTexture;
		}

		@Override
		public void close() {
			if (this.topTexture != null) this.topTexture.close();
			if (this.sideTexture != null) this.sideTexture.close();
			if (this.connectMaskTexture != null) this.connectMaskTexture.close();
			if (this.blendUpMaskTexture != null) this.blendUpMaskTexture.close();
			if (this.blendUpMirroredMaskTexture != null) this.blendUpMirroredMaskTexture.close();
			if (this.archMaskTexture != null) this.archMaskTexture.close();
		}
	}

	public sealed interface Texture extends Closeable {
		Identifier resolveId();

		NativeImage getImage();

		@Override
		void close();
	}

	public record DirectTexture(Identifier id, NativeImage image) implements Texture {
		@Override
		public Identifier resolveId() {
			return this.id;
		}

		@Override
		public NativeImage getImage() {
			return this.image;
		}

		@Override
		public void close() {
		}
	}

	public static final class FromDiskTexture implements Texture {
		private final ResourceManager resourceManager;
		private final Identifier id;
		private NativeImage cached;

		public FromDiskTexture(ResourceManager resourceManager, Identifier id) {
			this.resourceManager = resourceManager;
			this.id = id;
		}

		@Override
		public Identifier resolveId() {
			return this.id;
		}

		@Override
		public NativeImage getImage() {
			if (this.cached == null) {
				this.cached = LBGTextureGenerator.getNativeImage(this.resourceManager, this.id);
			}

			return this.cached;
		}

		@Override
		public void close() {
			this.cached.close();
			this.cached = null;
		}
	}
}
