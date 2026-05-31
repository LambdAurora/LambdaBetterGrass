/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import org.joml.Vector3f;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

public final class LBGQuadUtils {
	private static final Constructor<BakedQuad> BAKED_QUAD_WITH_AO = findBakedQuadWithAmbientOcclusion();
	private static final Method HAS_AMBIENT_OCCLUSION = findHasAmbientOcclusion();

	private LBGQuadUtils() {
		throw new UnsupportedOperationException("LBGQuadUtils only contains static definitions.");
	}

	public static float x(BakedQuad quad, int vertex) {
		return component(quad, vertex, 0);
	}

	public static float y(BakedQuad quad, int vertex) {
		return component(quad, vertex, 1);
	}

	public static float z(BakedQuad quad, int vertex) {
		return component(quad, vertex, 2);
	}

	public static BakedQuad bakeSprite(BakedQuad quad, TextureAtlasSprite sprite) {
		var vertices = Arrays.copyOf(quad.getVertices(), quad.getVertices().length);
		var oldSprite = quad.getSprite();

		for (int i = 0; i < 4; i++) {
			int offset = i * IQuadTransformer.STRIDE + IQuadTransformer.UV0;
			float oldU = Float.intBitsToFloat(vertices[offset]);
			float oldV = Float.intBitsToFloat(vertices[offset + 1]);
			vertices[offset] = Float.floatToRawIntBits(sprite.getU(oldSprite.getUOffset(oldU)));
			vertices[offset + 1] = Float.floatToRawIntBits(sprite.getV(oldSprite.getVOffset(oldV)));
		}

		return copyWithVertices(quad, vertices, sprite);
	}

	public static BakedQuad offset(BakedQuad quad, Vector3f offset) {
		if (offset.x == 0.0F && offset.y == 0.0F && offset.z == 0.0F) {
			return quad;
		}

		var vertices = Arrays.copyOf(quad.getVertices(), quad.getVertices().length);
		for (int i = 0; i < 4; i++) {
			int vertex = i * IQuadTransformer.STRIDE + IQuadTransformer.POSITION;
			vertices[vertex] = Float.floatToRawIntBits(Float.intBitsToFloat(vertices[vertex]) + offset.x);
			vertices[vertex + 1] = Float.floatToRawIntBits(Float.intBitsToFloat(vertices[vertex + 1]) + offset.y);
			vertices[vertex + 2] = Float.floatToRawIntBits(Float.intBitsToFloat(vertices[vertex + 2]) + offset.z);
		}

		return copyWithVertices(quad, vertices, quad.getSprite());
	}

	private static float component(BakedQuad quad, int vertex, int component) {
		int offset = vertex * IQuadTransformer.STRIDE + IQuadTransformer.POSITION + component;
		return Float.intBitsToFloat(quad.getVertices()[offset]);
	}

	private static BakedQuad copyWithVertices(BakedQuad quad, int[] vertices, TextureAtlasSprite sprite) {
		if (BAKED_QUAD_WITH_AO != null) {
			try {
				return BAKED_QUAD_WITH_AO.newInstance(
						vertices,
						quad.getTintIndex(),
						quad.getDirection(),
						sprite,
						quad.isShade(),
						hasAmbientOcclusion(quad)
				);
			} catch (ReflectiveOperationException ignored) {
			}
		}

		return new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), sprite, quad.isShade());
	}

	private static boolean hasAmbientOcclusion(BakedQuad quad) {
		if (HAS_AMBIENT_OCCLUSION == null) {
			return true;
		}

		try {
			return (boolean) HAS_AMBIENT_OCCLUSION.invoke(quad);
		} catch (ReflectiveOperationException ignored) {
			return true;
		}
	}

	private static Constructor<BakedQuad> findBakedQuadWithAmbientOcclusion() {
		try {
			return BakedQuad.class.getConstructor(
					int[].class,
					int.class,
					net.minecraft.core.Direction.class,
					TextureAtlasSprite.class,
					boolean.class,
					boolean.class
			);
		} catch (NoSuchMethodException ignored) {
			return null;
		}
	}

	private static Method findHasAmbientOcclusion() {
		try {
			return BakedQuad.class.getMethod("hasAmbientOcclusion");
		} catch (NoSuchMethodException ignored) {
			return null;
		}
	}
}
