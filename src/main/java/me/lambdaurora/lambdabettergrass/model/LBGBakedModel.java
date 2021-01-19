/*
 * Copyright © 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdabettergrass.model;

import me.lambdaurora.lambdabettergrass.LBGMode;
import me.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import me.lambdaurora.lambdabettergrass.metadata.LBGLayer;
import me.lambdaurora.lambdabettergrass.metadata.LBGMetadata;
import me.lambdaurora.lambdabettergrass.util.LayeredBlockUtils;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowyBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Represents the LambdaBetterGrass baked model.
 *
 * @author LambdAurora
 * @version 1.0.2
 * @since 1.0.0
 */
public class LBGBakedModel extends ForwardingBakedModel {
    private final LBGMetadata metadata;

    public LBGBakedModel(@NotNull BakedModel baseModel, @NotNull LBGMetadata metadata) {
        this.wrapped = baseModel;
        this.metadata = metadata;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView world, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        LBGMode mode = LambdaBetterGrass.get().config.getMode();

        if (mode == LBGMode.OFF) {
            // Don't touch the model.
            super.emitBlockQuads(world, state, pos, randomSupplier, context);
            return;
        }

        if (this.metadata.getSnowyModelVariant() != null && LambdaBetterGrass.get().config.hasBetterLayer()
                && state.getProperties().contains(Properties.SNOWY) && !state.get(Properties.SNOWY)) {
            BlockPos upPos = pos.up();
            BlockState up = world.getBlockState(upPos);
            if (!up.isAir()) {
                Identifier blockId = Registry.BLOCK.getId(up.getBlock());
                Identifier stateId = new Identifier(blockId.getNamespace(), "bettergrass/states/" + blockId.getPath());
                if (LayeredBlockUtils.shouldGrassBeSnowy(world, pos, stateId, up.getBlock())) {
                    ((FabricBakedModel) this.metadata.getSnowyModelVariant()).emitBlockQuads(world, state.with(Properties.SNOWY, true), pos, randomSupplier, context);
                    return;
                }
            }
        }

        World clientWorld = MinecraftClient.getInstance().world;

        context.pushTransform(quad -> {
            if (quad.nominalFace().getAxis() != Direction.Axis.Y) {
                this.metadata.getLayer(quad.colorIndex()).ifPresent(layer -> {
                    if (mode == LBGMode.FASTEST) {
                        spriteBake(quad, layer, "connect");
                        return;
                    }

                    Direction face = quad.nominalFace();
                    Direction right = face.rotateYClockwise();
                    Direction left = face.rotateYCounterclockwise();

                    if (canFullyConnect(clientWorld, state, pos, face)) {
                        if (spriteBake(quad, layer, "connect"))
                            return;
                    }

                    if (mode != LBGMode.FANCY)
                        return;

                    boolean rightMatch = canConnect(clientWorld, state, pos.down(), right)
                            || (canConnect(clientWorld, state, pos, right) && canFullyConnect(clientWorld, state, pos.offset(right), face));
                    boolean leftMatch = canConnect(clientWorld, state, pos.down(), left)
                            || (canConnect(clientWorld, state, pos, left) && canFullyConnect(clientWorld, state, pos.offset(left), face));

                    if (rightMatch && leftMatch)
                        spriteBake(quad, layer, "arch");
                    else if (rightMatch)
                        spriteBake(quad, layer, "blend_up_m");
                    else if (leftMatch)
                        spriteBake(quad, layer, "blend_up");
                });
            }
            return true;
        });
        super.emitBlockQuads(world, state, pos, randomSupplier, context);
        context.popTransform();
    }

    private static boolean canFullyConnect(@NotNull BlockRenderView world, @NotNull BlockState self, @NotNull BlockPos selfPos, @NotNull Direction direction) {
        return canConnect(world, self, selfPos, selfPos.offset(direction).down());
    }

    private static boolean canConnect(@NotNull BlockRenderView world, @NotNull BlockState self, @NotNull BlockPos start, @NotNull Direction direction) {
        return canConnect(world, self, start, start.offset(direction));
    }

    private static boolean canConnect(@NotNull BlockRenderView world, @NotNull BlockState self, @NotNull BlockPos selfPos, @NotNull BlockPos adjacentPos) {
        BlockState adjacent = world.getBlockState(adjacentPos);
        if (LambdaBetterGrass.get().config.hasBetterLayer() &&
                self.getBlock() instanceof SnowyBlock) {
            boolean selfSnowy = self.get(Properties.SNOWY);

            if (selfSnowy) {
                BlockState up = world.getBlockState(adjacentPos.up());
                if (!up.isAir()) {
                    if (up.isOf(Blocks.SNOW))
                        return true;
                    else if (adjacent.getBlock() instanceof SnowyBlock) {
                        Identifier blockId = Registry.BLOCK.getId(up.getBlock());
                        Identifier stateId = new Identifier(blockId.getNamespace(), "bettergrass/states/" + blockId.getPath());
                        if (LayeredBlockUtils.shouldGrassBeSnowy(world, adjacentPos, stateId, up.getBlock()))
                            return true;
                    }
                }
            }
        }
        return canConnect(self, adjacent);
    }

    private static boolean canConnect(@NotNull BlockState self, @NotNull BlockState adjacent) {
        return self == adjacent;
    }

    private static boolean spriteBake(@NotNull MutableQuadView quad, @NotNull LBGLayer layer, @NotNull String texture) {
        Sprite sprite = layer.getBakedTexture(texture);
        if (sprite != null)
            quad.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
        return sprite != null;
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        throw new UnsupportedOperationException("LambdaBetterGrass models should never try to render as an item!");
    }
}
