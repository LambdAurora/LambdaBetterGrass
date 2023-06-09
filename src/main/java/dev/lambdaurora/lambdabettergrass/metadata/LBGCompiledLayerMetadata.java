/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata;

import dev.lambdaurora.lambdabettergrass.util.LayeredBlockUtils;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.resource.Material;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a compiled layer metadata.
 * <p>
 * This holds the custom models to use when the layer variation should be used.
 *
 * @author LambdAurora
 * @version 1.4.0
 * @since 1.0.0
 */
public class LBGCompiledLayerMetadata {
	public final LBGLayerType layerType;
	private final @Nullable Vector3f offset;
	public final LBGLayerMetadata.LayerUnbakedModels unbakedModels;
	private BakedModel bakedLayerModel;
	private BakedModel bakedAlternateModel;

	public LBGCompiledLayerMetadata(LBGLayerType layerType, @Nullable Vector3f offset, LBGLayerMetadata.LayerUnbakedModels unbakedModels) {
		this.layerType = layerType;
		this.offset = offset;
		this.unbakedModels = unbakedModels;
	}

	public @Nullable Vector3f offset() {
		return this.offset;
	}

	public void fetchModelDependencies(Collection<Identifier> ids) {
		if (this.unbakedModels.layerModel() != null) {
			ids.addAll(this.unbakedModels.layerModel().getModelDependencies());
		}

		if (this.unbakedModels.alternateModel() != null) {
			ids.addAll(this.unbakedModels.alternateModel().getModelDependencies());
		}
	}

	public void resolveParents(Function<Identifier, UnbakedModel> models) {
		if (this.unbakedModels.layerModel() != null) {
			this.unbakedModels.layerModel().resolveParents(models);
		}

		if (this.unbakedModels.alternateModel() != null) {
			this.unbakedModels.alternateModel().resolveParents(models);
		}
	}

	/**
	 * Bakes the hold unbaked models.
	 *
	 * @param baker the model baker
	 * @param textureGetter the texture getter
	 * @param rotationContainer the rotation container
	 * @param modelId the model identifier
	 */
	public void bake(ModelBaker baker, Function<Material, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		if (this.unbakedModels.layerModel() != null) {
			this.bakedLayerModel = this.unbakedModels.layerModel().bake(baker, textureGetter, rotationContainer, modelId);
		}

		if (this.unbakedModels.alternateModel() != null) {
			this.bakedAlternateModel = this.unbakedModels.alternateModel().bake(baker, textureGetter, rotationContainer, modelId);
		}
	}

	/**
	 * Emits the block quads.
	 *
	 * @param world the world
	 * @param state the block state
	 * @param pos the block position
	 * @param randomSupplier the random supplier
	 * @param context the render context
	 * @return 0 if no custom models have emitted quads, 1 if only the layer model has emitted quads,
	 * or 2 if the custom alternative model has emitted quads
	 */
	public int emitBlockQuads(BlockRenderView world, BlockState state, BlockPos pos, Supplier<RandomGenerator> randomSupplier,
			RenderContext context) {
		int success = 0;
		if (LayeredBlockUtils.getNearbyLayeredBlocks(world, pos, this.layerType.block, state.getBlock(), false) > 1
				&& this.bakedLayerModel != null) {
			final var downPos = pos.down();
			final var downState = world.getBlockState(downPos);
			if (downState.isSideSolidFullSquare(world, downPos, Direction.UP)) {
				Vec3d offset = state.getModelOffset(world, pos);
				boolean pushed = false;
				if (offset.x != 0.0D || offset.y != 0.0D || offset.z != 0.0D) {
					var offsetVec = new Vector3f((float) offset.x, (float) offset.y, (float) offset.z);
					context.pushTransform(quad -> {
						Vector3f vec = null;
						for (int i = 0; i < 4; i++) {
							vec = quad.copyPos(i, vec);
							vec.sub(offsetVec);
							quad.pos(i, vec);
						}
						quad.material(RendererAccess.INSTANCE.getRenderer().materialFinder().ambientOcclusion(TriState.FALSE).find());
						return true;
					});
					pushed = true;
				}
				((FabricBakedModel) this.bakedLayerModel).emitBlockQuads(world, state, pos, randomSupplier, context);
				success = 1;
				if (pushed)
					context.popTransform();
			}
		}

		if (LayeredBlockUtils.getNearbyLayeredBlocks(world, pos, this.layerType.block, state.getBlock(), false) > 1
				&& this.bakedAlternateModel != null) {
			((FabricBakedModel) this.bakedAlternateModel).emitBlockQuads(world, state, pos, randomSupplier, context);
			success = 2;
		}

		return success;
	}
}
