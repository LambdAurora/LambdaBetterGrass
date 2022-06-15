/*
 * Copyright © 2021-2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata;

import com.mojang.datafixers.util.Pair;
import dev.lambdaurora.lambdabettergrass.util.LayeredBlockUtils;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a compiled layer metadata.
 * <p>
 * This holds the custom models to use when the layer variation should be used.
 *
 * @author LambdAurora
 * @version 1.2.1
 * @since 1.0.0
 */
public class LBGCompiledLayerMetadata {
	public final LBGLayerType layerType;
	private final @Nullable Vec3f offset;
	public final LBGLayerMetadata.LayerUnbakedModels unbakedModels;
	private BakedModel bakedLayerModel;
	private BakedModel bakedAlternateModel;

	public LBGCompiledLayerMetadata(LBGLayerType layerType, @Nullable Vec3f offset, LBGLayerMetadata.LayerUnbakedModels unbakedModels) {
		this.layerType = layerType;
		this.offset = offset;
		this.unbakedModels = unbakedModels;
	}

	public @Nullable Vec3f offset() {
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

	public void fetchTextureDependencies(Collection<SpriteIdentifier> ids, Function<Identifier, UnbakedModel> unbakedModelGetter,
	                                     Set<Pair<String, String>> unresolvedTextureReferences) {
		if (this.unbakedModels.layerModel() != null) {
			ids.addAll(this.unbakedModels.layerModel().getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences));
		}

		if (this.unbakedModels.alternateModel() != null) {
			ids.addAll(this.unbakedModels.alternateModel().getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences));
		}
	}

	/**
	 * Bakes the hold unbaked models.
	 *
	 * @param loader The model loader.
	 * @param textureGetter The texture getter.
	 * @param rotationContainer The rotation container.
	 * @param modelId The model identifier.
	 */
	public void bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		if (this.unbakedModels.layerModel() != null) {
			this.bakedLayerModel = this.unbakedModels.layerModel().bake(loader, textureGetter, rotationContainer, modelId);
		}

		if (this.unbakedModels.alternateModel() != null) {
			this.bakedAlternateModel = this.unbakedModels.alternateModel().bake(loader, textureGetter, rotationContainer, modelId);
		}
	}

	/**
	 * Emits the block quads.
	 *
	 * @param world The world.
	 * @param state The block state.
	 * @param pos The block position.
	 * @param randomSupplier The random supplier.
	 * @param context The render context.
	 * @return 0 if no custom models have emitted quads, 1 if only the layer model has emitted quads,
	 * or 2 if the custom alternative model has emitted quads.
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
					var offsetVec = new Vec3f((float) offset.x, (float) offset.y, (float) offset.z);
					context.pushTransform(quad -> {
						Vec3f vec = null;
						for (int i = 0; i < 4; i++) {
							vec = quad.copyPos(i, vec);
							vec.subtract(offsetVec);
							quad.pos(i, vec);
						}
						quad.material(RendererAccess.INSTANCE.getRenderer().materialFinder().disableAo(0, false).find());
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
