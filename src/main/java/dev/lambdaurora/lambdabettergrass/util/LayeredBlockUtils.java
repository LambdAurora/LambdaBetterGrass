/*
 * Copyright © 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.util;

import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.metadata.LBGLayerState;
import dev.lambdaurora.lambdabettergrass.metadata.LBGState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;

/**
 * Represents utilities about snow.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public final class LayeredBlockUtils {
    private LayeredBlockUtils() {
        throw new UnsupportedOperationException("LayeredBlockUtils only contains static definitions.");
    }

    /**
     * Returns the unbaked model of snow layer.
     *
     * @param modelGetter The model getter.
     * @return The unbaked model.
     */
    public static @Nullable UnbakedModel getSnowLayerModel(@NotNull Function<Identifier, UnbakedModel> modelGetter) {
        return modelGetter.apply(LambdaBetterGrass.mc("block/snowy_layer"));
    }

    public static boolean shouldGrassBeSnowy(@NotNull BlockRenderView world, @NotNull BlockPos pos, @NotNull Identifier stateId, @NotNull BlockState upState, boolean onlyPureSnow) {
        LBGState state = LBGState.getMetadataState(stateId);
        if (!(state instanceof LBGLayerState))
            return false;

        Collection<Property<?>> properties = upState.getProperties();
        String[] modelVariant = new String[properties.size()];

        int i = 0;
        for (Property<?> property : properties) {
            String end = ",";
            if (modelVariant.length == i +1)
                end = "";
            modelVariant[i] = property.getName() + '=' + nameValue(property, upState.get(property)) + end;
            i++;
        }

        boolean[] shouldTry = {false};
        ((LBGLayerState) state).forEach(modelVariant, metadata -> {
            if (metadata.layerType.getName().equals("snow")) {
                shouldTry[0] = true;
            }
        });

        return shouldTry[0] && getNearbySnowyBlocks(world, pos.up(), upState.getBlock(), onlyPureSnow) > 1;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String nameValue(Property<T> property, Comparable<?> value) {
        return property.name((T) value);
    }

    public static int getNearbySnowyBlocks(@NotNull BlockRenderView world, @NotNull BlockPos pos, @NotNull Block type, boolean onlyPureSnow) {
        return getNearbyLayeredBlocks(world, pos, Blocks.SNOW, type, onlyPureSnow);
    }

    public static int getNearbyLayeredBlocks(@NotNull BlockRenderView world, @NotNull BlockPos pos, @NotNull Block layerBlock, @NotNull Block type, boolean onlySourceBlock) {
        int nearbySnow = 0;
        for (Direction direction : Direction.values()) {
            if (direction.getAxis().isHorizontal()) {
                BlockPos offsetPos = pos.offset(direction);
                Block block = world.getBlockState(offsetPos).getBlock();
                if (block == type && !onlySourceBlock) {
                    if (getNearbySnowLayers(world, offsetPos) > 1)
                        nearbySnow++;
                } else if (block == layerBlock) {
                    nearbySnow++;
                }
            }
        }
        return nearbySnow;
    }

    public static int getNearbySnowLayers(@NotNull BlockRenderView world, @NotNull BlockPos pos) {
        return getNearbyBlockLayers(world, pos, Blocks.SNOW);
    }

    public static int getNearbyBlockLayers(@NotNull BlockRenderView world, @NotNull BlockPos pos, @NotNull Block layerBlock) {
        int nearbySnow = 0;
        for (Direction direction : Direction.values()) {
            if (direction.getAxis().isHorizontal()) {
                if (world.getBlockState(pos.offset(direction)).getBlock() == layerBlock)
                    nearbySnow++;
            }
        }
        return nearbySnow;
    }
}
