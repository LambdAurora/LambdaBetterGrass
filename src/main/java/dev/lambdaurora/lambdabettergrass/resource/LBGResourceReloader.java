/*
 * Copyright © 2023 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.resource;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.metadata.LBGState;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.io.Resource;
import net.minecraft.resources.io.ResourceManager;
import net.minecraft.resources.io.ResourceReloader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Represents the LambdaBetterGrass resource reloader.
 *
 * @author LambdAurora
 * @version 2.5.0
 * @since 1.4.0
 */
public final class LBGResourceReloader implements ResourceReloader {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final Identifier ID = LambdaBetterGrass.id("resource_reloader");
	public static final ResourceReloader.StateKey<LBGSharedState> SHARED_STATE_KEY = new ResourceReloader.StateKey<>();

	@Override
	public String getName() {
		return ID.toString();
	}

	@Override
	public void prepareSharedState(SharedState sharedState) {
		LambdaBetterGrass.log(LambdaBetterGrass.LOGGER, "Reloading resources...");
		LambdaBetterGrass.get().dynamicTextureManager.reset();

		sharedState.set(SHARED_STATE_KEY, new LBGSharedState());
	}

	@Override
	public CompletableFuture<Void> reload(
			SharedState sharedState, Executor prepareExecutor, Synchronizer synchronizer, Executor applyExecutor
	) {
		return CompletableFuture.supplyAsync(
						() -> {
							var layerTypeManager = new LBGLayerTypeManager();
							layerTypeManager.load(sharedState.resourceManager());

							return new LBGContext(layerTypeManager);
						},
						prepareExecutor
				)
				.thenCompose(context -> this.loadStates(sharedState.resourceManager(), context, prepareExecutor))
				.thenAcceptAsync(context -> {
					var lbgSharedState = sharedState.get(SHARED_STATE_KEY);
					lbgSharedState.contextFuture.complete(context);
					LambdaBetterGrass.get().dynamicTextureManager.finish();
				}, prepareExecutor)
				.thenCompose(synchronizer::whenPrepared);
	}

	private CompletableFuture<LBGContext> loadStates(
			ResourceManager resourceManager, LBGContext context, Executor prepareExecutor
	) {
		var futures = resourceManager.findResources(LBGState.PATH_PREFIX, id -> id.path().endsWith(".json"))
				.entrySet().stream()
				.map(entry ->
						CompletableFuture.supplyAsync(
								() -> this.loadState(resourceManager, context, entry.getKey(), entry.getValue()),
								prepareExecutor
						)
				)
				.toList();

		return Util.sequence(futures).thenApplyAsync(states -> {
			states.stream().filter(Optional::isPresent).map(Optional::get)
					.forEach(state -> context.states.put(state.block(), state));
			return context;
		}, prepareExecutor);
	}

	/**
	 * Loads the given LambdaBetterGrass state.
	 *
	 * @param resourceManager the resource manager
	 * @param context the LambdaBetterGrass context
	 * @param id the resource identifier of the state
	 * @param resource the resource
	 * @return the loaded state if present, or {@link Optional#empty()} otherwise
	 */
	private Optional<LBGState> loadState(
			ResourceManager resourceManager, LBGContext context, Identifier id, Resource resource
	) {
		var stateId = Identifier.of(
				id.namespace(),
				id.path().substring(LBGState.PATH_PREFIX.length() + 1, id.path().length() - ".json".length())
		);

		var block = BuiltInRegistries.BLOCK.getOptional(stateId);
		if (block.isEmpty()) {
			// The block doesn't exist, so we just ignore the state file.
			return Optional.empty();
		}

		try (var reader = new InputStreamReader(resource.open())) {
			var json = JsonParser.parseReader(reader).getAsJsonObject();
			return LBGState.loadMetadataState(stateId, resourceManager, json, block.get().getStateDefinition(), context);
		} catch (IOException e) {
			LOGGER.warn("Failed to load LambdaBetterGrass state {}.", stateId, e);
			return Optional.empty();
		}
	}

	public static class LBGSharedState {
		private final CompletableFuture<LBGContext> contextFuture = new CompletableFuture<>();

		public @NotNull CompletableFuture<LBGContext> awaitContext() {
			return this.contextFuture;
		}
	}
}
