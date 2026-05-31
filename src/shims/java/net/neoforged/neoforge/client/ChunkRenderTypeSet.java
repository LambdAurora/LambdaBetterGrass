package net.neoforged.neoforge.client;

import net.minecraft.client.renderer.RenderType;

import java.util.Collection;

public final class ChunkRenderTypeSet {
	public static ChunkRenderTypeSet of(RenderType... renderTypes) {
		throw new AssertionError("Shim only.");
	}

	public static ChunkRenderTypeSet union(Collection<ChunkRenderTypeSet> sets) {
		throw new AssertionError("Shim only.");
	}

	public boolean contains(RenderType renderType) {
		throw new AssertionError("Shim only.");
	}
}
