/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.model;

import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.metadata.layer.LBGCompiledLayerMetadata;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the LambdaBetterGrass baked model for layer method on NeoForge.
 *
 * @author LambdAurora
 * @version 2.0.4
 * @since 2.0.4
 */
public class LBGLayerBakedModel extends BakedModelWrapper<BakedModel> implements IDynamicBakedModel {
	private final List<LBGCompiledLayerMetadata> metadatas;

	public LBGLayerBakedModel(BakedModel baseModel, List<LBGCompiledLayerMetadata> metadatas) {
		super(baseModel);
		this.metadatas = metadatas;
	}

	@Override
	public ModelData getModelData(BlockAndTintGetter world, BlockPos pos, BlockState state, ModelData modelData) {
		return super.getModelData(world, pos, state, modelData)
				.derive()
				.with(LBGModelData.PROPERTY, new LBGModelData(world, pos))
				.build();
	}

	@Override
	public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
		var sets = new ArrayList<ChunkRenderTypeSet>();
		sets.add(super.getRenderTypes(state, rand, data));
		for (var metadata : this.metadatas) {
			sets.add(ChunkRenderTypeSet.of(metadata.renderType()));
		}
		return ChunkRenderTypeSet.union(sets);
	}

	@Override
	public List<BakedQuad> getQuads(
			@Nullable BlockState state, @Nullable Direction side, RandomSource random, ModelData extraData,
			@Nullable RenderType renderType
	) {
		if (state == null || !LambdaBetterGrass.get().hasBetterLayer()) {
			return super.getQuads(state, side, random, extraData, renderType);
		}

		var context = extraData.get(LBGModelData.PROPERTY);
		if (context == null) {
			return super.getQuads(state, side, random, extraData, renderType);
		}

		var baseRenderTypes = super.getRenderTypes(state, random, extraData);
		boolean renderBaseModel = renderType == null || baseRenderTypes.contains(renderType);

		for (var metadata : this.metadatas) {
			var result = metadata.getQuads(context.world(), state, context.pos(), random, extraData, renderType, side);
			if (result.status() != 0) {
				if (result.status() == 1) {
					var quads = new ArrayList<>(result.quads());
					if (renderBaseModel) {
						var baseQuads = super.getQuads(state, side, random, extraData, renderType);
						var offset = metadata.offset();
						if (offset != null) {
							for (var quad : baseQuads) {
								quads.add(LBGQuadUtils.offset(quad, offset));
							}
						} else {
							quads.addAll(baseQuads);
						}
					}
					return quads;
				}
				return result.quads();
			}
		}

		return renderBaseModel ? super.getQuads(state, side, random, extraData, renderType) : List.of();
	}
}
