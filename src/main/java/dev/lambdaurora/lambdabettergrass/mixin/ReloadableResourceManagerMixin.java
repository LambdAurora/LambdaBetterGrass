/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.mixin;

import com.mojang.datafixers.util.Unit;
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.io.LifecycledResourceManager;
import net.minecraft.resources.io.ReloadableResourceManager;
import net.minecraft.resources.io.ResourceManager;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Environment(EnvType.CLIENT)
@Mixin(ReloadableResourceManager.class)
public abstract class ReloadableResourceManagerMixin implements ResourceManager {
	@Shadow
	private LifecycledResourceManager activeManager;

	@Inject(
			method = "reload",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/packs/resources/SimpleReloadInstance;create(Lnet/minecraft/resources/io/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/server/packs/resources/ReloadInstance;"
			)
	)
	private void reload(
			Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage, List<PackResources> packs,
			CallbackInfoReturnable<ReloadInstance> cir
	) {
		final var mod = LambdaBetterGrass.get();
		LambdaBetterGrass.log(LambdaBetterGrass.LOGGER, "Reloading resources...");
		mod.dynamicTextureManager.reset();
		mod.resourceReloader.reload(this.activeManager);
		mod.dynamicTextureManager.finish();
	}
}