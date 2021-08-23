/*
 * Copyright © 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass;

import com.google.gson.JsonParser;
import dev.lambdaurora.lambdabettergrass.metadata.LBGGrassState;
import dev.lambdaurora.lambdabettergrass.metadata.LBGLayerState;
import dev.lambdaurora.lambdabettergrass.metadata.LBGState;
import dev.lambdaurora.lambdabettergrass.resource.LBGResourcePack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the LambdaBetterGrass mod.
 *
 * @author LambdAurora
 * @version 1.2.1
 * @since 1.0.0
 */
public class LambdaBetterGrass implements ClientModInitializer {
    public static final String NAMESPACE = "lambdabettergrass";
    public static final JsonParser JSON_PARSER = new JsonParser();
    /* Default masks */
    public static final Identifier BETTER_GRASS_SIDE_CONNECT_MASK = mc("bettergrass/mask/standard_block_side_connect.png");
    public static final Identifier BETTER_GRASS_SIDE_BLEND_UP_MASK = mc("bettergrass/mask/grass_block_side_blend_up.png");
    public static final Identifier BETTER_GRASS_SIDE_ARCH_BLEND_MASK = mc("bettergrass/mask/grass_block_side_arch_blend.png");

    private static LambdaBetterGrass INSTANCE;
    public final Logger logger = LogManager.getLogger("lambdabettergrass");
    public final LBGConfig config = new LBGConfig(this);
    private final ThreadLocal<Boolean> betterLayerDisabled = ThreadLocal.withInitial(() -> false);
    public LBGResourcePack resourcePack;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        this.log("Initializing LambdaBetterGrass...");
        this.config.load();

        FabricLoader.getInstance().getModContainer(NAMESPACE).ifPresent(modContainer -> {
            ResourceManagerHelper.registerBuiltinResourcePack(mc("default"), modContainer, ResourcePackActivationType.DEFAULT_ENABLED);
            ResourceManagerHelper.registerBuiltinResourcePack(mc("32x"), modContainer, ResourcePackActivationType.NORMAL);
        });

        LBGState.registerType("grass", (id, resourceManager, json, deserializationContext) -> new LBGGrassState(id, resourceManager, json));
        LBGState.registerType("layer", LBGLayerState::new);
    }

    /**
     * Prints a message to the terminal.
     *
     * @param info the message to print
     */
    public void log(String info) {
        this.logger.info("[LambdaBetterGrass] " + info);
    }

    /**
     * Prints a warning message to the terminal.
     *
     * @param info the message to print
     */
    public void warn(String info) {
        this.logger.warn("[LambdaBetterGrass] " + info);
    }

    /**
     * Returns whether the better layer feature is enabled or not.
     *
     * @return {@code true} if the better layer feature is enabled, otherwise {@code false}
     */
    public boolean hasBetterLayer() {
        if (this.config.hasBetterLayer())
            return !this.betterLayerDisabled.get();
        return false;
    }

    /**
     * {@return a LambdaBetterGrass Minecraft identifier}
     *
     * @param path the path
     */
    public static Identifier mc(@NotNull String path) {
        return new Identifier(NAMESPACE, path);
    }

    /**
     * {@return the LambdaBetterGrass mod instance}
     */
    public static LambdaBetterGrass get() {
        return INSTANCE;
    }

    /**
     * Pushes the force-disable of the better layer feature.
     */
    public static void pushDisableBetterLayer() {
        get().betterLayerDisabled.set(true);
    }

    /**
     * Pops the force-disable of the better layer feature.
     */
    public static void popDisableBetterLayer() {
        get().betterLayerDisabled.remove();
    }
}
