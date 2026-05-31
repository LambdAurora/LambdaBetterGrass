/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.model;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

public final class NeoForgeModelHelper {
	private static final MethodHandle GET_QUADS = findGetQuads();
	private static final MethodHandle GET_RENDER_TYPES = findGetRenderTypes();

	private NeoForgeModelHelper() {
		throw new UnsupportedOperationException("NeoForgeModelHelper only contains static definitions.");
	}

	@SuppressWarnings("unchecked")
	public static List<BakedQuad> getQuads(
			BakedModel model, @Nullable BlockState state, @Nullable Direction side, RandomSource random,
			ModelData modelData, @Nullable RenderType renderType
	) {
		if (GET_QUADS != null) {
			try {
				return (List<BakedQuad>) GET_QUADS.invoke(model, state, side, random, modelData, renderType);
			} catch (Throwable ignored) {
			}
		}

		return model.getQuads(state, side, random);
	}

	public static boolean canRenderIn(
			BakedModel model, BlockState state, RandomSource random, ModelData modelData, @Nullable RenderType renderType
	) {
		if (renderType == null || GET_RENDER_TYPES == null) {
			return true;
		}

		try {
			var renderTypes = GET_RENDER_TYPES.invoke(model, state, random, modelData);
			return (boolean) renderTypes.getClass().getMethod("contains", RenderType.class).invoke(renderTypes, renderType);
		} catch (Throwable ignored) {
			return true;
		}
	}

	private static MethodHandle findGetQuads() {
		try {
			return MethodHandles.publicLookup().findVirtual(
					BakedModel.class,
					"getQuads",
					MethodType.methodType(
							List.class,
							BlockState.class, Direction.class, RandomSource.class, ModelData.class, RenderType.class
					)
			);
		} catch (NoSuchMethodException | IllegalAccessException ignored) {
			return null;
		}
	}

	private static MethodHandle findGetRenderTypes() {
		try {
			return MethodHandles.publicLookup().findVirtual(
					BakedModel.class,
					"getRenderTypes",
					MethodType.methodType(
							Class.forName("net.neoforged.neoforge.client.ChunkRenderTypeSet"),
							BlockState.class, RandomSource.class, ModelData.class
					)
			);
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException ignored) {
			return null;
		}
	}
}
