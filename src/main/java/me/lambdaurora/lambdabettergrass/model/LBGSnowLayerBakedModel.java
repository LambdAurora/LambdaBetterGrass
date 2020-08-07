/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdabettergrass.model;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Represents the LambdaBetterGrass baked model for snow layer method.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LBGSnowLayerBakedModel extends ForwardingBakedModel
{
    private final BakedModel snowLayerModel;

    public LBGSnowLayerBakedModel(@NotNull BakedModel baseModel, @NotNull BakedModel snowLayerModel)
    {
        this.wrapped = baseModel;
        this.snowLayerModel = snowLayerModel;
    }

    @Override
    public boolean isVanillaAdapter()
    {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView world, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context)
    {
        int nearbySnow = 0;
        for (Direction direction : Direction.values()) {
            if (direction.getAxis().isHorizontal()) {
                if (world.getBlockState(pos.offset(direction)).getBlock() == Blocks.SNOW)
                    nearbySnow++;
            }
        }
        if (nearbySnow > 1) {
            Vec3d offset = state.getModelOffset(world, pos);
            Vector3f offsetVec = new Vector3f((float) offset.x, (float) offset.y, (float) offset.z);
            context.pushTransform(quad -> {
                Vector3f vec = null;
                for (int i = 0; i < 4; i++) {
                    vec = quad.copyPos(i, vec);
                    vec.subtract(offsetVec);
                    quad.pos(i, vec);
                }
                return true;
            });
            ((FabricBakedModel) this.snowLayerModel).emitBlockQuads(world, state, pos, randomSupplier, context);
            context.popTransform();
        }
        super.emitBlockQuads(world, state, pos, randomSupplier, context);
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context)
    {
        throw new UnsupportedOperationException("LambdaBetterGrass models should never try to render as an item!");
    }
}
