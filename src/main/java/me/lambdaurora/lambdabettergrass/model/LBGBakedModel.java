/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdabettergrass.model;

import me.lambdaurora.lambdabettergrass.metadata.LBGLayer;
import me.lambdaurora.lambdabettergrass.metadata.LBGMetadata;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Represents the LambdaBetterGrass baked model.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LBGBakedModel extends ForwardingBakedModel
{
    private final LBGMetadata metadata;

    public LBGBakedModel(@NotNull BakedModel baseModel, @NotNull LBGMetadata metadata)
    {
        if (!((FabricBakedModel) baseModel).isVanillaAdapter()) {
            throw new IllegalArgumentException("LambdaBetterGrass does not support non-vanilla models! This may be caused by having multiple resource packs affecting the same block.");
        }
        this.wrapped = baseModel;
        this.metadata = metadata;
    }

    @Override
    public boolean isVanillaAdapter()
    {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView world, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context)
    {
        context.pushTransform(quad -> {
            if (quad.nominalFace().getAxis() != Direction.Axis.Y) {
                this.metadata.getLayer(quad.colorIndex()).ifPresent(layer -> {
                    Direction face = quad.nominalFace();
                    Direction right = face.rotateYClockwise();
                    Direction left = face.rotateYCounterclockwise();

                    if (canFullyConnect(world, state, pos, face)) {
                        if (spriteBake(quad, layer, "connect"))
                            return;
                    }

                    boolean rightMatch = canConnect(world, state, pos.down(), right)
                            || (canConnect(world, state, pos, right) && canFullyConnect(world, state, pos.offset(right), face));
                    boolean leftMatch = canConnect(world, state, pos.down(), left)
                            || (canConnect(world, state, pos, left) && canFullyConnect(world, state, pos.offset(left), face));

                    if (rightMatch && leftMatch) {
                        spriteBake(quad, layer, "arch");
                    } else {
                        if (rightMatch) {
                            spriteBake(quad, layer, "blend_up_m");
                        } else if (leftMatch) {
                            spriteBake(quad, layer, "blend_up");
                        }
                    }
                });
            }
            return true;
        });
        super.emitBlockQuads(world, state, pos, randomSupplier, context);
        context.popTransform();
    }

    private static boolean canFullyConnect(@NotNull BlockRenderView world, @NotNull BlockState self, @NotNull BlockPos selfPos, @NotNull Direction direction)
    {
        return canConnect(world, self, selfPos.offset(direction).down());
    }

    private static boolean canConnect(@NotNull BlockRenderView world, @NotNull BlockState self, @NotNull BlockPos start, @NotNull Direction direction)
    {
        return canConnect(world, self, start.offset(direction));
    }

    private static boolean canConnect(@NotNull BlockRenderView world, @NotNull BlockState self, @NotNull BlockPos adjacentPos)
    {
        return canConnect(self, world.getBlockState(adjacentPos));
    }

    private static boolean canConnect(@NotNull BlockState self, @NotNull BlockState adjacent)
    {
        return self.getBlock() == adjacent.getBlock();
    }

    private static boolean spriteBake(@NotNull MutableQuadView quad, @NotNull LBGLayer layer, @NotNull String texture)
    {
        Sprite sprite = layer.getBakedSprite(texture);
        if (sprite != null)
            quad.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
        return sprite != null;
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context)
    {
        throw new UnsupportedOperationException("LambdaBetterGrass models should never try to render as an item!");
    }
}
