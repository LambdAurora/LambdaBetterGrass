/*
 * Copyright © 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdabettergrass.util;

import me.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Environment(EnvType.CLIENT)
public enum LBGTextureGenerator {
    ; // No instantiation possible <3

    /**
     * Represents the fallback texture to load if texture loading failed.
     */
    private static final Identifier FALLBACK_TEXTURE = LambdaBetterGrass.mc("textures/block/transparent.png");

    /**
     * Returns the fallback {@link NativeImage} instance.
     *
     * @param resourceManager The resource manager.
     * @return The fallback {@link NativeImage} instance if possible, else a new instance with non-cleared buffer.
     */
    private static @NotNull NativeImage getFallbackNativeImage(@NotNull ResourceManager resourceManager) {
        if (!resourceManager.containsResource(FALLBACK_TEXTURE)) {
            LambdaBetterGrass.get().warn("Could not load fallback texture \"" + FALLBACK_TEXTURE.toString() + "\"!");
            return new NativeImage(16, 16, false);
        }

        try {
            return NativeImage.read(resourceManager.getResource(FALLBACK_TEXTURE).getInputStream());
        } catch (IOException e) {
            LambdaBetterGrass.get().warn("Could not load fallback texture \"" + FALLBACK_TEXTURE.toString() + "\"!");
            return new NativeImage(16, 16, false);
        }
    }

    /**
     * Returns the {@link NativeImage} instance from the texture at the specified path.
     *
     * @param resourceManager The resource manager.
     * @param path The texture path.
     * @return The {@link NativeImage} instance if possible, else the fallback texture.
     * @see #getFallbackNativeImage(ResourceManager)
     */
    public static @NotNull NativeImage getNativeImage(@NotNull ResourceManager resourceManager, @NotNull Identifier path) {
        if (!resourceManager.containsResource(path)) {
            LambdaBetterGrass.get().warn("Could not load texture \"" + path.toString() + "\"! Loading fallback texture instead.");
            return getFallbackNativeImage(resourceManager);
        }

        try {
            return NativeImage.read(resourceManager.getResource(path).getInputStream());
        } catch (IOException e) {
            LambdaBetterGrass.get().warn("Could not load texture \"" + path.toString() + "\"! Exception: " + e.getMessage() + ". Loading fallback texture instead.");
            return getFallbackNativeImage(resourceManager);
        }
    }

    /**
     * Returns a new texture which is the mirrored version of the specified texture.
     *
     * @param source The source texture.
     * @return The mirrored texture.
     */
    public static @NotNull NativeImage mirrorImage(@NotNull NativeImage source) {
        NativeImage result = new NativeImage(source.getWidth(), source.getHeight(), false);

        for (int y = 0; y < result.getHeight(); y++) {
            for (int x = 0; x < result.getWidth(); x++) {
                result.setPixelColor(source.getWidth() - 1 - x, y, source.getPixelColor(x, y));
            }
        }

        return result;
    }

    /**
     * Generates the side texture using the original side texture, the top texture and the mask texture.
     *
     * @param target The texture name.
     * @param side The original side texture.
     * @param top The top texture.
     * @param mask The mask texture.
     * @return The generated texture identifier.
     */
    public static Identifier generateTexture(@NotNull String target, @NotNull NativeImage side, @NotNull NativeImage top, @NotNull NativeImage mask) {
        NativeImage image = applyMask(side, top, mask);

        return LambdaBetterGrass.get().resourcePack.dynamicallyPutImage(target, image);
    }

    /**
     * Applies the mask on the source texture using the top and mask textures.
     * <p>
     * If a pixel is alpha 255 in the mask texture, then the pixel will be from the top texture, else it will be from the source texture.
     *
     * @param source The source texture.
     * @param top The top texture.
     * @param mask The mask texture.
     * @return The generated texture.
     */
    public static @NotNull NativeImage applyMask(@NotNull NativeImage source, @NotNull NativeImage top, @NotNull NativeImage mask) {
        // Determine the highest resolution from the images.
        final int width = Math.max(Math.max(source.getWidth(), top.getWidth()), mask.getWidth());
        final int height = Math.max(Math.max(source.getHeight(), top.getHeight()), mask.getHeight());

        NativeImage output = new NativeImage(width, height, false);

        // Time to do AND operation
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int sourceRGB = source.getPixelColor(getTrueCoordinate(width, source.getWidth(), x), getTrueCoordinate(height, source.getHeight(), y));
                int topRGB = top.getPixelColor(getTrueCoordinate(width, top.getWidth(), x), getTrueCoordinate(height, top.getHeight(), y));

                // If the mask pixel opacity is 255 (-1 because signed byte) use the top texture pixel color, else use the source pixel color.
                if (mask.getPixelOpacity(getTrueCoordinate(width, mask.getWidth(), x), getTrueCoordinate(height, mask.getHeight(), y)) == -1)
                    output.setPixelColor(x, y, topRGB);
                else
                    output.setPixelColor(x, y, sourceRGB);
            }
        }
        return output;
    }

    private static int getTrueCoordinate(int resolution, int targetResolution, int coordinate) {
        if (resolution == targetResolution)
            return coordinate;
        return (int) ((coordinate / (double) resolution) * targetResolution);
    }
}
