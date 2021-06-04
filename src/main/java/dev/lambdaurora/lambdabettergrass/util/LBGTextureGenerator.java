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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

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
     * @param resourceManager the resource manager
     * @return the fallback {@link NativeImage} instance if possible, otherwise a new instance with non-cleared buffer
     */
    private static NativeImage getFallbackNativeImage(ResourceManager resourceManager) {
        if (!resourceManager.containsResource(FALLBACK_TEXTURE)) {
            LambdaBetterGrass.get().warn("Could not load fallback texture \"" + FALLBACK_TEXTURE + "\"!");
            return new NativeImage(16, 16, false);
        }

        try {
            return NativeImage.read(resourceManager.getResource(FALLBACK_TEXTURE).getInputStream());
        } catch (IOException e) {
            LambdaBetterGrass.get().warn("Could not load fallback texture \"" + FALLBACK_TEXTURE + "\"!");
            return new NativeImage(16, 16, false);
        }
    }

    /**
     * Returns the {@link NativeImage} instance from the texture at the specified path.
     *
     * @param resourceManager the resource manager
     * @param path the texture path
     * @return the {@link NativeImage} instance if possible, otherwise the fallback texture
     * @see #getFallbackNativeImage(ResourceManager)
     */
    public static NativeImage getNativeImage(ResourceManager resourceManager, Identifier path) {
        if (!resourceManager.containsResource(path)) {
            LambdaBetterGrass.get().warn("Could not load texture \"" + path + "\"! Loading fallback texture instead.");
            return getFallbackNativeImage(resourceManager);
        }

        try {
            return NativeImage.read(resourceManager.getResource(path).getInputStream());
        } catch (IOException e) {
            LambdaBetterGrass.get().warn("Could not load texture \"" + path + "\"! Exception: " + e.getMessage()
                    + ". Loading fallback texture instead.");
            return getFallbackNativeImage(resourceManager);
        }
    }

    /**
     * Returns a new texture which is the mirrored version of the specified texture.
     *
     * @param source the source texture
     * @return the mirrored texture
     */
    public static NativeImage mirrorImage(NativeImage source) {
        var result = new NativeImage(source.getWidth(), source.getHeight(), false);

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
     * @param target the texture name
     * @param side the original side texture
     * @param top the top texture
     * @param mask the mask texture
     * @return the generated texture identifier
     */
    public static Identifier generateTexture(String target, NativeImage side, NativeImage top, NativeImage mask) {
        var image = applyMask(side, top, mask);

        return LambdaBetterGrass.get().resourcePack.dynamicallyPutImage(target, image);
    }

    /**
     * Applies the mask on the source texture using the top and mask textures.
     * <p>
     * If a pixel is alpha 255 in the mask texture, then the pixel will be from the top texture, else it will be from the source texture.
     *
     * @param source the source texture
     * @param top the top texture
     * @param mask the mask texture
     * @return the generated texture
     */
    public static NativeImage applyMask(NativeImage source, NativeImage top, NativeImage mask) {
        // Determine the highest resolution from the images.
        final int width = Math.max(Math.max(source.getWidth(), top.getWidth()), mask.getWidth());
        final int height = Math.max(Math.max(source.getHeight(), top.getHeight()), mask.getHeight());

        var output = new NativeImage(width, height, false);

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
