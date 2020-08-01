/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdabettergrass;

import me.lambdaurora.lambdabettergrass.resource.LBGResourcePack;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the LambdaBetterGrass mod.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LambdaBetterGrass implements ClientModInitializer
{
    public static final String            MODID                             = "lambdabettergrass";
    /* Default masks */
    public static final Identifier        BETTER_GRASS_SIDE_CONNECT_MASK    = mc("bettergrass/mask/standard_block_side_connect.png");
    public static final Identifier        BETTER_GRASS_SIDE_BLEND_UP_MASK   = mc("bettergrass/mask/grass_block_side_blend_up.png");
    public static final Identifier        BETTER_GRASS_SIDE_ARCH_BLEND_MASK = mc("bettergrass/mask/grass_block_side_arch_blend.png");
    private static      LambdaBetterGrass INSTANCE;
    public final        Logger            logger                            = LogManager.getLogger("lambdabettergrass");
    //public final   BetterGrassConfig                        config = new BetterGrassConfig(this);
    public              LBGResourcePack   resourcePack;

    @Override
    public void onInitializeClient()
    {
        INSTANCE = this;
        this.log("Initializing LambdaBetterGrass...");
    }

    /**
     * Prints a message to the terminal.
     *
     * @param info The message to print.
     */
    public void log(String info)
    {
        this.logger.info("[LambdaBetterGrass] " + info);
    }

    /**
     * Prints a warning message to the terminal.
     *
     * @param info The message to print.
     */
    public void warn(String info)
    {
        this.logger.warn("[LambdaBetterGrass] " + info);
    }

    /**
     * Returns a LambdaBetterGrass Minecraft identifier.
     *
     * @param path The path.
     * @return The identifier.
     */
    public static net.minecraft.util.Identifier mc(@NotNull String path)
    {
        return new net.minecraft.util.Identifier(MODID, path);
    }

    /**
     * Returns the LambdaBetterGrass mod instance.
     *
     * @return The mod instance.
     */
    public static LambdaBetterGrass get()
    {
        return INSTANCE;
    }
}
