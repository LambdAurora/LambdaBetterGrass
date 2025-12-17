/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass;

import com.mojang.logging.LogUtils;
import dev.lambdaurora.lambdabettergrass.metadata.LBGGrassState;
import dev.lambdaurora.lambdabettergrass.metadata.LBGState;
import dev.lambdaurora.lambdabettergrass.metadata.layer.LBGLayerState;
import dev.lambdaurora.lambdabettergrass.resource.LBGDynamicTextureManager;
import dev.lambdaurora.lambdabettergrass.resource.LBGResourceReloader;
import dev.yumi.mc.core.api.ModContainer;
import dev.yumi.mc.core.api.YumiMods;
import dev.yumi.mc.core.api.entrypoint.client.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

/**
 * Represents the LambdaBetterGrass mod.
 *
 * @author LambdAurora
 * @version 2.6.0
 * @since 1.0.0
 */
public class LambdaBetterGrass implements ClientModInitializer {
	/**
	 * The namespace of this mod, whose value is {@value}.
	 */
	public static final String NAMESPACE = "lambdabettergrass";
	public static final Logger LOGGER = LogUtils.getLogger();

	@ApiStatus.Internal
	public static final LambdaBetterGrass INSTANCE = new LambdaBetterGrass();
	public final LBGConfig config = new LBGConfig(this);
	private final ThreadLocal<Boolean> betterLayerDisabled = ThreadLocal.withInitial(() -> false);

	public final LBGResourceReloader resourceReloader = new LBGResourceReloader();
	public final LBGDynamicTextureManager dynamicTextureManager = new LBGDynamicTextureManager();
	private String version;

	@Override
	public void onInitializeClient(ModContainer mod) {
		this.version = mod.getVersionString();

		log(LOGGER, "Initializing LambdaBetterGrass...");
		this.config.load();

		this.registerBuiltinResourcePacks(mod);

		LBGState.registerType("grass", LBGGrassState::new);
		LBGState.registerType("layer", LBGLayerState::new);

		var resourceLoader = ResourceLoader.get(PackType.CLIENT_RESOURCES);
		resourceLoader.registerReloader(LBGResourceReloader.ID, this.resourceReloader);
		resourceLoader.addReloaderOrdering(LBGResourceReloader.ID, ResourceReloaderKeys.Client.MODELS);
		resourceLoader.addReloaderOrdering(LBGResourceReloader.ID, ResourceReloaderKeys.Client.ATLAS);

		PreparableModelLoadingPlugin.register(
				(sharedState, applyExecutor) -> sharedState.get(LBGResourceReloader.SHARED_STATE_KEY).awaitContext(),
				(lbgCtx, pluginCtx) -> {
					pluginCtx.modifyBlockModelOnLoad().register(ModelModifier.WRAP_LAST_PHASE, (model, context) -> {
						// Get cached states metadata.
						var state = lbgCtx.getState(context.state().getBlock());

						// If states metadata found, search for corresponding metadata and if exists replace the model.
						if (state != null) {
							var newModel = state.getCustomUnbakedModel(context.state(), model);

							if (newModel != null) {
								return newModel;
							}
						}

						return model;
					});
				}
		);
	}

	/**
	 * Gets the currently running version of LambdaBetterGrass.
	 *
	 * @return the version
	 */
	public String getVersion() {
		return this.version;
	}

	private void registerBuiltinResourcePacks(dev.yumi.mc.core.api.ModContainer mod) {
		var fabricMod = FabricLoader.getInstance().getModContainer(mod.id()).orElseThrow();

		ResourceLoader.registerBuiltinPack(
				id("default"), fabricMod,
				Component.translatable("lambdabettergrass.resourcepack.default", Component.translatable(NAMESPACE)),
				PackActivationType.DEFAULT_ENABLED
		);
		ResourceLoader.registerBuiltinPack(id("x32"), fabricMod, PackActivationType.NORMAL);
	}

	/**
	 * Logs an informational message.
	 *
	 * @param logger the logger to use
	 * @param msg the message to log
	 */
	public static void log(Logger logger, String msg) {
		if (!YumiMods.get().isDevelopmentEnvironment()) {
			msg = "[LambdaBetterGrass] " + msg;
		}

		logger.info(msg);
	}

	/**
	 * Logs a warning message.
	 *
	 * @param logger the logger to use
	 * @param msg the message to log
	 */
	public static void warn(Logger logger, String msg) {
		if (!YumiMods.get().isDevelopmentEnvironment()) {
			msg = "[LambdaBetterGrass] " + msg;
		}

		logger.warn(msg);
	}

	/**
	 * Logs a warning message.
	 *
	 * @param logger the logger to use
	 * @param msg the message to log
	 */
	public static void warn(Logger logger, String msg, Object... args) {
		if (!YumiMods.get().isDevelopmentEnvironment()) {
			msg = "[LambdaBetterGrass] " + msg;
		}

		logger.warn(msg, args);
	}

	/**
	 * Logs an error message.
	 *
	 * @param logger the logger to use
	 * @param msg the message to log
	 */
	public static void error(Logger logger, String msg, Object... args) {
		if (!YumiMods.get().isDevelopmentEnvironment()) {
			msg = "[LambdaBetterGrass] " + msg;
		}

		logger.error(msg, args);
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
	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(NAMESPACE, path);
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
