/*
 * Copyright © 2021-2023 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.resource;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.texture.NativeImage;
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.resource.loader.api.InMemoryResourcePack;

import java.io.IOException;
import java.util.Set;

public class LBGResourcePack extends InMemoryResourcePack {
	private static final Set<String> NAMESPACES = Sets.newHashSet(LambdaBetterGrass.NAMESPACE);

	private final LambdaBetterGrass mod;

	public LBGResourcePack(LambdaBetterGrass mod) {
		this.mod = mod;
	}

	public Identifier dynamicallyPutImage(String name, NativeImage image) {
		final var id = new Identifier(LambdaBetterGrass.NAMESPACE, "block/bettergrass/" + name);

		try {
			this.putImage(new Identifier(id.getNamespace(), "textures/" + id.getPath() + ".png"), image);
		} catch (IOException e) {
			this.mod.warn("Could not put image {}.", id, e);
		}

		return id;
	}

	@Override
	public Set<String> getNamespaces(ResourceType type) {
		return NAMESPACES;
	}

	@Override
	public String getName() {
		return "LambdaBetterGrass generated resources";
	}
}
