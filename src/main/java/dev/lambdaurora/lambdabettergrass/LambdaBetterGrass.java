/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass;

import com.mojang.logging.LogUtils;
import dev.lambdaurora.lambdabettergrass.metadata.LBGGrassState;
import dev.lambdaurora.lambdabettergrass.metadata.LBGLayerState;
import dev.lambdaurora.lambdabettergrass.metadata.LBGState;
import dev.lambdaurora.lambdabettergrass.resource.LBGResourcePack;
import dev.lambdaurora.lambdabettergrass.resource.LBGResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType;
import org.quiltmc.qsl.resource.loader.api.client.ClientResourceLoaderEvents;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Represents the LambdaBetterGrass mod.
 *
 * @author LambdAurora
 * @version 1.4.0
 * @since 1.0.0
 */
public class LambdaBetterGrass implements ClientModInitializer, ClientResourceLoaderEvents.EndResourcePackReload {
	public static final String NAMESPACE = "lambdabettergrass";
	public static final Logger LOGGER = LogUtils.getLogger();
	/* Default masks */
	public static final Identifier BETTER_GRASS_SIDE_CONNECT_MASK = id("bettergrass/mask/standard_block_side_connect.png");
	public static final Identifier BETTER_GRASS_SIDE_BLEND_UP_MASK = id("bettergrass/mask/grass_block_side_blend_up.png");
	public static final Identifier BETTER_GRASS_SIDE_ARCH_BLEND_MASK = id("bettergrass/mask/grass_block_side_arch_blend.png");

	@ApiStatus.Internal
	public static final LambdaBetterGrass INSTANCE = new LambdaBetterGrass();
	public final LBGConfig config = new LBGConfig(this);
	private final ThreadLocal<Boolean> betterLayerDisabled = ThreadLocal.withInitial(() -> false);
	public final LBGResourceReloader resourceReloader = new LBGResourceReloader();
	public LBGResourcePack resourcePack;

	@Override
	public void onInitializeClient(ModContainer mod) {
		this.log("Initializing LambdaBetterGrass...");
		this.config.load();

		ResourceLoader.registerBuiltinResourcePack(id("default"), mod, ResourcePackActivationType.DEFAULT_ENABLED);
		ResourceLoader.registerBuiltinResourcePack(id("32x"), mod, ResourcePackActivationType.NORMAL);

		ResourceLoader.get(ResourceType.CLIENT_RESOURCES).getRegisterTopResourcePackEvent()
				.register(id("register_pack"), context -> {
					this.log("Rebuilding resources and inject generated resource pack.");
					context.addResourcePack(this.resourcePack = new LBGResourcePack(this));
					this.resourceReloader.reload(context.resourceManager());
				});

		LBGState.registerType("grass", (id, block, resourceManager, json, deserializationContext) -> new LBGGrassState(id, resourceManager, json));
		LBGState.registerType("layer", LBGLayerState::new);
	}

	@Override
	public void onEndResourcePackReload(ClientResourceLoaderEvents.EndResourcePackReload.Context context) {
		if (this.config.isDebug()) {
			this.resourcePack.dumpTo(Path.of("debug/lbg_out"));
		}
	}

	/**
	 * Prints a message to the terminal.
	 *
	 * @param info the message to print
	 */
	public void log(String info) {
		LOGGER.info("[LambdaBetterGrass] " + info);
	}

	/**
	 * Prints a warning message to the terminal.
	 *
	 * @param info the message to print
	 */
	public void warn(String info) {
		LOGGER.warn("[LambdaBetterGrass] " + info);
	}

	/**
	 * Prints a warning message to the terminal.
	 *
	 * @param info the message to print
	 */
	public void warn(String info, Object... objects) {
		LOGGER.warn("[LambdaBetterGrass] " + info, objects);
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
	public static Identifier id(@NotNull String path) {
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
