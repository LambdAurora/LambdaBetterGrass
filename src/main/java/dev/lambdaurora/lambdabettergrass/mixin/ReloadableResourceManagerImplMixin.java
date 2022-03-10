/*
 * Copyright © 2021-2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.mixin;

import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.resource.LBGResourcePack;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;

@Mixin(ReloadableResourceManager.class)
public abstract class ReloadableResourceManagerImplMixin {
	@Shadow
	@Final
	private ResourceType type;

	@ModifyArg(
			method = "reload",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/MultiPackResourceManager;<init>(Lnet/minecraft/resource/ResourceType;Ljava/util/List;)V"),
			index = 1
	)
	private List<ResourcePack> onReload(List<ResourcePack> packs) {
		if (this.type != ResourceType.CLIENT_RESOURCES)
			return packs;

		var mod = LambdaBetterGrass.get();
		mod.log("Inject generated resource packs.");
		var list = new ArrayList<>(packs);
		list.add(mod.resourcePack = new LBGResourcePack(mod));

		return list;
	}
}
