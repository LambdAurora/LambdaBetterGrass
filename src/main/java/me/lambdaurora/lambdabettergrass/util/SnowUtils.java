/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdabettergrass.util;

import me.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Represents utilities about snow.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class SnowUtils
{
    private SnowUtils()
    {
        throw new UnsupportedOperationException("SnowUtils only contains static definitions.");
    }

    public static @Nullable UnbakedModel getSnowLayerModel(@NotNull Function<Identifier, UnbakedModel> modelGetter)
    {
        return modelGetter.apply(LambdaBetterGrass.mc("block/snowy_layer"));
    }

    public static int getNearbySnowyBlocks(@NotNull BlockRenderView world, @NotNull BlockPos pos, @NotNull Block type)
    {
        int nearbySnow = 0;
        for (Direction direction : Direction.values()) {
            if (direction.getAxis().isHorizontal()) {
                BlockPos offsetPos = pos.offset(direction);
                Block block = world.getBlockState(offsetPos).getBlock();
                if (block == type) {
                    if (getNearbySnowLayers(world, offsetPos) > 1)
                        nearbySnow++;
                } else if (block == Blocks.SNOW) {
                    nearbySnow++;
                }
            }
        }
        return nearbySnow;
    }

    public static int getNearbySnowLayers(@NotNull BlockRenderView world, @NotNull BlockPos pos)
    {
        int nearbySnow = 0;
        for (Direction direction : Direction.values()) {
            if (direction.getAxis().isHorizontal()) {
                if (world.getBlockState(pos.offset(direction)).getBlock() == Blocks.SNOW)
                    nearbySnow++;
            }
        }
        return nearbySnow;
    }
}
