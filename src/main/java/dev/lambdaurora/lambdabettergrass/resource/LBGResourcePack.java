/*
 * Copyright © 2021-2022 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.resource;

import com.google.common.collect.Sets;
import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import dev.lambdaurora.lambdabettergrass.mixin.NativeImageAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.resource.pack.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LBGResourcePack implements ResourcePack {
	private static final Set<String> NAMESPACES = Sets.newHashSet(LambdaBetterGrass.NAMESPACE);

	private final Object2ObjectMap<String, byte[]> resources = new Object2ObjectOpenHashMap<>();
	private final LambdaBetterGrass mod;

	public LBGResourcePack(LambdaBetterGrass mod) {
		this.mod = mod;
	}

	public void putResource(String resource, byte[] data) {
		this.resources.put(resource, data);
	}

	public void putImage(String location, NativeImage image) {
		var byteOut = new ByteArrayOutputStream();
		var out = Channels.newChannel(byteOut);
		// Please forgive me
		((NativeImageAccessor) (Object) image).lbg$write(out);

		// Debug
		if (this.mod.config.isDebug()) {
			var file = new File("debug/lbg_out/" + location);
			file.getParentFile().mkdirs();

			try {
				var outF = FileChannel.open(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				((NativeImageAccessor) (Object) image).lbg$write(outF);
				outF.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		this.putResource(location, byteOut.toByteArray());
		try {
			out.close();
		} catch (IOException e) {
			this.mod.warn("Could not close output channel for texture " + location + ". Exception: " + e.getMessage());
		}
	}

	public Identifier dynamicallyPutImage(String name, NativeImage image) {
		this.putImage("assets/" + LambdaBetterGrass.NAMESPACE + "/textures/bettergrass/" + name + ".png", image);
		return new Identifier(LambdaBetterGrass.NAMESPACE, "bettergrass/" + name);
	}

	@Override
	public InputStream openRoot(String fileName) throws IOException {
		byte[] data;
		if ((data = this.resources.get(fileName)) != null) {
			return new ByteArrayInputStream(data);
		}
		throw new IOException("Generated resources pack has no data or alias for " + fileName);
	}

	@Override
	public InputStream open(ResourceType type, Identifier id) throws IOException {
		if (type == ResourceType.SERVER_DATA) throw new IOException("Reading server data from LambdaBetterGrass client resource pack");
		return this.openRoot(type.getDirectory() + "/" + id.getNamespace() + "/" + id.getPath());
	}

	@Override
	public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, int maxDepth, Predicate<String> pathFilter) {
		if (type == ResourceType.SERVER_DATA) return Collections.emptyList();
		var start = "assets/" + namespace + "/" + prefix;
		return this.resources.keySet().stream()
				.filter(s -> s.startsWith(start) && pathFilter.test(s))
				.map(LBGResourcePack::fromPath)
				.collect(Collectors.toList());
	}

	@Override
	public boolean contains(ResourceType type, Identifier id) {
		var path = type.getDirectory() + "/" + id.getNamespace() + "/" + id.getPath();
		return this.resources.containsKey(path);
	}

	@Override
	public Set<String> getNamespaces(ResourceType type) {
		return NAMESPACES;
	}

	@Override
	public <T> @Nullable T parseMetadata(ResourceMetadataReader<T> metaReader) throws IOException {
		return null;
	}

	@Override
	public String getName() {
		return "LambdaBetterGrass generated resources";
	}

	@Override
	public void close() {
	}

	private static Identifier fromPath(String path) {
		String[] split = path.split("/", 2);
		return new Identifier(split[0], split[1]);
	}
}
