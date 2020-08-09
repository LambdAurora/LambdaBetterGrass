/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdabettergrass.model;

import me.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import me.lambdaurora.lambdabettergrass.metadata.LBGCompiledLayerMetadata;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Represents the LambdaBetterGrass baked model for layer method.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LBGLayerBakedModel extends ForwardingBakedModel
{
    private final List<LBGCompiledLayerMetadata> metadatas;

    public LBGLayerBakedModel(@NotNull BakedModel baseModel, @NotNull List<LBGCompiledLayerMetadata> metadatas)
    {
        this.wrapped = baseModel;
        this.metadatas = metadatas;
    }

    @Override
    public boolean isVanillaAdapter()
    {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView world, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context)
    {
        if (!LambdaBetterGrass.get().config.hasBetterLayer()) {
            // Don't touch the model.
            super.emitBlockQuads(world, state, pos, randomSupplier, context);
            return;
        }

        for (LBGCompiledLayerMetadata metadata : this.metadatas) {
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
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context)
    {
        throw new UnsupportedOperationException("LambdaBetterGrass models should never try to render as an item!");
    }
}
