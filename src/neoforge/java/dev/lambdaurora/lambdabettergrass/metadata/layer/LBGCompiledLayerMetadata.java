/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata.layer;

import dev.lambdaurora.lambdabettergrass.model.LBGQuadUtils;
import dev.lambdaurora.lambdabettergrass.model.NeoForgeModelHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Represents compiled layer metadata on NeoForge.
 *
 * @author LambdAurora
 * @version 2.0.4
 * @since 2.0.4
 */
public class LBGCompiledLayerMetadata {
	public final LBGLayerType layerType;
	private final boolean hasLayer;
	private final @Nullable Vector3f offset;
	public final LBGLayerMetadata.LayerUnbakedModels unbakedModels;
	private BakedModel bakedAlternateModel;

	public LBGCompiledLayerMetadata(
			LBGLayerType layerType, boolean hasLayer, @Nullable Vector3f offset, LBGLayerMetadata.LayerUnbakedModels unbakedModels
	) {
		this.layerType = layerType;
		this.hasLayer = hasLayer;
		this.offset = offset;
		this.unbakedModels = unbakedModels;
	}

	public RenderType renderType() {
		return this.layerType.renderType;
	}

	public @Nullable Vector3f offset() {
		return this.offset;
	}

	public void fetchModelDependencies(Collection<Identifier> ids) {
		if (this.unbakedModels.alternateModel() != null) {
			ids.addAll(this.unbakedModels.alternateModel().getDependencies());
		}
	}

	public void resolveParents(Function<Identifier, UnbakedModel> models) {
		if (this.unbakedModels.alternateModel() != null) {
			this.unbakedModels.alternateModel().resolveParents(models);
		}
	}

	public void bake(
			ModelBaker baker, Function<Material, TextureAtlasSprite> textureGetter, ModelState modelState
	) {
		if (this.unbakedModels.alternateModel() != null) {
			this.bakedAlternateModel = this.unbakedModels.alternateModel().bake(baker, textureGetter, modelState);
		}
	}

	public QuadResult getQuads(
			BlockAndTintGetter world, BlockState state, BlockPos pos, RandomSource random, ModelData modelData,
			@Nullable RenderType renderType, @Nullable Direction side
	) {
		int nearby = this.layerType.getNearbyLayeredBlocks(world, pos, state.getBlock(), false);
		boolean shouldLayer = false;
		var quads = new ArrayList<BakedQuad>();

		if (this.hasLayer) {
			final var downPos = pos.below();
			final var downState = world.getBlockState(downPos);
			shouldLayer = downState.isFaceSturdy(world, downPos, Direction.UP) && nearby > 1;

			if (shouldLayer && side == null && (renderType == null || renderType == this.layerType.renderType)) {
				this.collectLayerQuads(world, state, pos, random, modelData, quads);
			}
		}

		if (nearby > 1 && this.bakedAlternateModel != null) {
			if (NeoForgeModelHelper.canRenderIn(this.bakedAlternateModel, state, random, modelData, renderType)) {
				quads.addAll(NeoForgeModelHelper.getQuads(
						this.bakedAlternateModel, state, side, random, modelData, renderType
				));
			}
			return new QuadResult(2, quads);
		}

		if (shouldLayer) {
			return new QuadResult(1, quads);
		}

		return QuadResult.EMPTY;
	}

	private void collectLayerQuads(
			BlockAndTintGetter world, BlockState ownerState, BlockPos pos, RandomSource random, ModelData modelData,
			List<BakedQuad> output
	) {
		var layerState = this.layerType.data.state();
		var layerModel = this.layerType.getLayerModel();
		var blockOffset = ownerState.getOffset(world, pos);
		var offset = new Vector3f((float) -blockOffset.x, (float) -blockOffset.y, (float) -blockOffset.z);
		var offsetPos = new BlockPos.Mutable();

		for (var direction : Direction.values()) {
			if (direction.getAxis() != Direction.Axis.Y) {
				offsetPos.setWithOffset(pos, direction);
				if (!Block.shouldRenderFace(layerState, world, pos, direction, offsetPos)) {
					continue;
				}
			}

			for (var quad : NeoForgeModelHelper.getQuads(layerModel, layerState, direction, random, modelData, this.layerType.renderType)) {
				output.add(LBGQuadUtils.offset(quad, offset));
			}
		}

		for (var quad : NeoForgeModelHelper.getQuads(layerModel, layerState, null, random, modelData, this.layerType.renderType)) {
			output.add(LBGQuadUtils.offset(quad, offset));
		}
	}

	public record QuadResult(int status, List<BakedQuad> quads) {
		private static final QuadResult EMPTY = new QuadResult(0, List.of());
	}
}
