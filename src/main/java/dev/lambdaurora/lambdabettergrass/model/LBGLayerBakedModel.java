/*
 * Copyright © 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.model;

import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.metadata.LBGCompiledLayerMetadata;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Represents the LambdaBetterGrass baked model for layer method.
 *
 * @author LambdAurora
 * @version 1.1.2
 * @since 1.0.0
 */
public class LBGLayerBakedModel extends ForwardingBakedModel {
    private final List<LBGCompiledLayerMetadata> metadatas;

    public LBGLayerBakedModel(BakedModel baseModel, List<LBGCompiledLayerMetadata> metadatas) {
        this.wrapped = baseModel;
        this.metadatas = metadatas;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView world, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        if (!LambdaBetterGrass.get().config.hasBetterLayer()) {
            // Don't touch the model.
            super.emitBlockQuads(world, state, pos, randomSupplier, context);
            return;
        }

        for (var metadata : this.metadatas) {
            int success = metadata.emitBlockQuads(world, state, pos, randomSupplier, context);
            if (success != 0) {
                if (success == 1)
                    super.emitBlockQuads(world, state, pos, randomSupplier, context);
                return;
            }
        }

        super.emitBlockQuads(world, state, pos, randomSupplier, context);
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        throw new UnsupportedOperationException("LambdaBetterGrass models should never try to render as an item!");
    }
}
