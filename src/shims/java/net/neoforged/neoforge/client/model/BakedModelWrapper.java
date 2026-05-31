package net.neoforged.neoforge.client.model;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.List;

public class BakedModelWrapper<T extends BakedModel> implements BakedModel {
	protected final T originalModel;

	public BakedModelWrapper(T originalModel) {
		this.originalModel = originalModel;
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction direction, RandomSource random) {
		return this.originalModel.getQuads(state, direction, random);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return this.originalModel.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return this.originalModel.isGui3d();
	}

	@Override
	public boolean usesBlockLight() {
		return this.originalModel.usesBlockLight();
	}

	@Override
	public boolean isCustomRenderer() {
		return this.originalModel.isCustomRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return this.originalModel.getParticleIcon();
	}

	@Override
	public ItemTransforms getTransforms() {
		return this.originalModel.getTransforms();
	}

	@Override
	public ItemOverrides getOverrides() {
		return this.originalModel.getOverrides();
	}

	public ModelData getModelData(BlockAndTintGetter world, BlockPos pos, BlockState state, ModelData modelData) {
		return modelData;
	}

	public List<BakedQuad> getQuads(
			BlockState state, Direction side, RandomSource random, ModelData extraData,
			RenderType renderType
	) {
		return this.originalModel.getQuads(state, side, random);
	}

	public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
		return ChunkRenderTypeSet.of(RenderType.solid());
	}
}
