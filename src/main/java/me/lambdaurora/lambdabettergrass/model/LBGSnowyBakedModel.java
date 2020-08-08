/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdabettergrass.model;

import me.lambdaurora.lambdabettergrass.util.SnowUtils;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Represents the LambdaBetterGrass baked model for snowy method.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LBGSnowyBakedModel extends ForwardingBakedModel
{
    private final BakedModel snowyModel;
    private final BakedModel snowLayerModel;

    public LBGSnowyBakedModel(@NotNull BakedModel baseModel, @Nullable BakedModel snowyModel, @Nullable BakedModel snowLayerModel)
    {
        this.wrapped = baseModel;
        this.snowyModel = snowyModel;
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
        if (SnowUtils.getNearbySnowyBlocks(world, pos, state.getBlock()) > 1 && this.snowLayerModel != null) {
            final BlockPos downPos = pos.down();
            final BlockState downState = world.getBlockState(downPos);
            if (Block.isSideSolidFullSquare(downState, world, downPos, Direction.UP)) {
                Vec3d offset = state.getModelOffset(world, pos);
                boolean pushed = false;
                if (offset.x != 0.0D || offset.y != 0.0D || offset.z != 0.0D) {
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
                    pushed = true;
                }
                ((FabricBakedModel) this.snowLayerModel).emitBlockQuads(world, state, pos, randomSupplier, context);
                if (pushed)
                    context.popTransform();
            }
        }

        if (SnowUtils.getNearbySnowyBlocks(world, pos, state.getBlock()) > 1 && this.snowyModel != null) {
            ((FabricBakedModel) this.snowyModel).emitBlockQuads(world, state, pos, randomSupplier, context);
        } else {
            super.emitBlockQuads(world, state, pos, randomSupplier, context);
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context)
    {
        throw new UnsupportedOperationException("LambdaBetterGrass models should never try to render as an item!");
    }
}
