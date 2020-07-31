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
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public enum LBGTextureGenerator
{
    ; // No instantiation possible <3

    public static @NotNull NativeImage getNativeImage(@NotNull ResourceManager resourceManager, @NotNull Identifier path)
    {
        if (!resourceManager.containsResource(path)) {
            LambdaBetterGrass.get().warn("Could not load texture " + path.toString() + "!");
            return new NativeImage(16, 16, false);
        }

        try {
            return NativeImage.read(resourceManager.getResource(path).getInputStream());
        } catch (IOException e) {
            LambdaBetterGrass.get().warn("Could not load texture " + path.toString() + "!");
            return new NativeImage(16, 16, false);
        }
    }

    public static @NotNull NativeImage mirrorImage(@NotNull NativeImage source)
    {
        NativeImage result = new NativeImage(source.getWidth(), source.getHeight(), false);

        for (int y = 0; y < result.getHeight(); y++) {
            for (int x = 0; x < result.getWidth(); x++) {
                result.setPixelColor(source.getWidth() - 1 - x, y, source.getPixelColor(x, y));
            }
        }

        return result;
    }

    public static Identifier generateTexture(@NotNull String target, @NotNull NativeImage side, @NotNull NativeImage top, @NotNull NativeImage mask)
    {
        NativeImage image = applyMask(side, top, mask);

        return LambdaBetterGrass.get().resourcePack.dynamicallyPutImage(target, image);
    }

    public static @NotNull NativeImage applyMask(@NotNull NativeImage source, @NotNull NativeImage top, @NotNull NativeImage mask)
    {
        final int width = Math.max(Math.max(source.getWidth(), top.getWidth()), mask.getWidth());
        final int height = Math.max(Math.max(source.getHeight(), top.getHeight()), mask.getHeight());

        NativeImage output = new NativeImage(width, height, false);

        // Time to do AND operation
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int sourceRGB = source.getPixelColor(getTrueCoordinate(width, source.getWidth(), x), getTrueCoordinate(height, source.getHeight(), y));
                int topRGB = top.getPixelColor(getTrueCoordinate(width, top.getWidth(), x), getTrueCoordinate(height, top.getHeight(), y));

                if (mask.getPixelOpacity(getTrueCoordinate(width, mask.getWidth(), x), getTrueCoordinate(height, mask.getHeight(), y)) == -1)
                    output.setPixelColor(x, y, topRGB);
                else
                    output.setPixelColor(x, y, sourceRGB);
            }
        }
        return output;
    }

    private static int getTrueCoordinate(int resolution, int targetResolution, int coordinate)
    {
        if (resolution == targetResolution)
            return coordinate;
        return (int) ((coordinate / (double) resolution) * targetResolution);
    }
}
