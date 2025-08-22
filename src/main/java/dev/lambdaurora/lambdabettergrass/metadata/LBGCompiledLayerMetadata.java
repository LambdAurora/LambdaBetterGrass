/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata;

import dev.lambdaurora.lambdabettergrass.util.LayeredBlockUtils;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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
 * @version 1.6.0
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
			ids.addAll(this.unbakedModels.layerModel().getDependencies());
		}

		if (this.unbakedModels.alternateModel() != null) {
			ids.addAll(this.unbakedModels.alternateModel().getDependencies());
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
	public void bake(ModelBaker baker, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer, Identifier modelId) {
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
	public int emitBlockQuads(BlockAndTintGetter world, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier,
			RenderContext context) {
		int success = 0;
		var layerState = this.layerType.block.defaultState();
		if (LayeredBlockUtils.getNearbyLayeredBlocks(world, pos, this.layerType.block, state.getBlock(), false) > 1
				&& this.bakedLayerModel != null) {
			final var downPos = pos.below();
			final var downState = world.getBlockState(downPos);
			if (downState.isFaceSturdy(world, downPos, Direction.UP)) {
				Vec3 offset = state.getOffset(world, pos);
				boolean pushed = false;

				final var materialFinder = RendererAccess.INSTANCE.getRenderer().materialFinder();
				context.pushTransform(quad -> {
					var originalMaterial = quad.material();
					var material = materialFinder.copyFrom(originalMaterial).ambientOcclusion(TriState.of(this.bakedLayerModel.useAmbientOcclusion())).find();
					quad.material(material);
					return true;
				});

				if (offset.x != 0.0D || offset.y != 0.0D || offset.z != 0.0D) {
					var offsetVec = new Vector3f((float) offset.x, (float) offset.y, (float) offset.z);
					context.pushTransform(quad -> {
						Vector3f vec = null;
						for (int i = 0; i < 4; i++) {
							vec = quad.copyPos(i, vec);
							vec.sub(offsetVec);
							quad.pos(i, vec);
						}
						return true;
					});
					pushed = true;
				}
				this.bakedLayerModel.emitBlockQuads(world, layerState, pos, randomSupplier, context);
				success = 1;
				if (pushed)
					context.popTransform();
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
