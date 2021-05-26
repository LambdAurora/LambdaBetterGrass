/*
 * Copyright © 2021 LambdAurora <aurora42lambda@gmail.com>
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
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a compiled layer metadata.
 * <p>
 * This holds the custom models to use when the layer variation should be used.
 *
 * @author LambdAurora
 * @version 1.1.2
 * @since 1.0.0
 */
public class LBGCompiledLayerMetadata {
    public final LBGLayerType layerType;
    public final UnbakedModel layerModel;
    public final UnbakedModel alternateModel;
    private BakedModel bakedLayerModel;
    private BakedModel bakedAlternateModel;

    public LBGCompiledLayerMetadata(LBGLayerType layerType, @Nullable UnbakedModel layerModel, @Nullable UnbakedModel alternateModel) {
        this.layerType = layerType;
        this.layerModel = layerModel;
        this.alternateModel = alternateModel;
    }

    public void fetchModelDependencies(Collection<Identifier> ids) {
        if (this.layerModel != null) {
            ids.addAll(this.layerModel.getModelDependencies());
        }

        if (this.alternateModel != null) {
            ids.addAll(this.alternateModel.getModelDependencies());
        }
    }

    public void fetchTextureDependencies(Collection<SpriteIdentifier> ids, Function<Identifier, UnbakedModel> unbakedModelGetter,
                                         Set<Pair<String, String>> unresolvedTextureReferences) {
        if (this.layerModel != null) {
            ids.addAll(this.layerModel.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences));
        }

        if (this.alternateModel != null) {
            ids.addAll(this.alternateModel.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences));
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
        if (this.layerModel != null) {
            this.bakedLayerModel = this.layerModel.bake(loader, textureGetter, rotationContainer, modelId);
        }

        if (this.alternateModel != null) {
            this.bakedAlternateModel = this.alternateModel.bake(loader, textureGetter, rotationContainer, modelId);
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
    public int emitBlockQuads(BlockRenderView world, BlockState state, BlockPos pos, Supplier<Random> randomSupplier,
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
