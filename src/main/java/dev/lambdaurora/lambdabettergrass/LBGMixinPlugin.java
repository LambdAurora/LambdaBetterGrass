/*
 * Copyright © 2020 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * LambdaBetterGrass mixin plugin for conditional mixins.
 *
 * @author LambdAurora
 * @version 2.0.0
 * @since 2.0.0
 */
public class LBGMixinPlugin implements IMixinConfigPlugin {
	private final Object2BooleanMap<String> conditionalMixins = new Object2BooleanOpenHashMap<>();

	public LBGMixinPlugin() {
		this.conditionalMixins.put(
				"dev.lambdaurora.lambdabettergrass.mixin.sodium.SodiumOptionsGuiMixin",
				LBGCompat.isSodiumInstalled()
		);
	}

	@Override
	public void onLoad(String mixinPackage) {
	}

	@Override
	public @Nullable String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return this.conditionalMixins.getOrDefault(mixinClassName, true);
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public @Nullable List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
}
