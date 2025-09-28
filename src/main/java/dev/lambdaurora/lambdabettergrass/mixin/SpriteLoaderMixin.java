/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.io.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
@Mixin(SpriteLoader.class)
public class SpriteLoaderMixin {
	@WrapOperation(
			method = "loadAndStitch(Lnet/minecraft/resources/io/ResourceManager;Lnet/minecraft/resources/Identifier;ILjava/util/concurrent/Executor;Ljava/util/Set;)Ljava/util/concurrent/CompletableFuture;",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/concurrent/CompletableFuture;thenCompose(Ljava/util/function/Function;)Ljava/util/concurrent/CompletableFuture;"
			)
	)
	private static CompletableFuture<List<SpriteContents>> lbg$onComposeRunSpriteSuppliers(
			CompletableFuture<?> instance, Function<?, ?> fn,
			Operation<CompletableFuture<List<SpriteContents>>> original,
			ResourceManager resourceManager, Identifier id
	) {
		var future = original.call(instance, fn);

		if (id.namespace().equals(Identifier.DEFAULT_NAMESPACE) && id.path().equals("blocks")) {
			var dynamicSprites = LambdaBetterGrass.get().dynamicTextureManager.awaitSprites();
			return future.thenCombine(
					dynamicSprites,
					(a, b) -> Stream.concat(a.stream(), b.stream()).toList()
			);
		}

		return future;
	}
}
