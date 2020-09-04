/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdabettergrass.resource;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import me.lambdaurora.lambdabettergrass.mixin.NativeImageAccessor;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LBGResourcePack implements ResourcePack
{
    private static final Set<String> NAMESPACES = Sets.newHashSet(LambdaBetterGrass.MODID);

    private final Object2ObjectMap<String, byte[]> resources = new Object2ObjectOpenHashMap<>();
    private final LambdaBetterGrass                mod;

    public LBGResourcePack(@NotNull LambdaBetterGrass mod)
    {
        this.mod = mod;
    }

    public void putResource(String resource, byte[] data)
    {
        this.resources.put(resource, data);
    }

    public void putImage(String location, NativeImage image)
    {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        WritableByteChannel out = Channels.newChannel(byteOut);
        // Please forgive me
        ((NativeImageAccessor) (Object) image).lbg_write(out);

        // Debug
        if (this.mod.config.isDebug()) {
            File file = new File("debug/lbg_out/" + location);
            file.getParentFile().mkdirs();

            try {
                WritableByteChannel outF = FileChannel.open(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                ((NativeImageAccessor) (Object) image).lbg_write(outF);
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

    public @NotNull Identifier dynamicallyPutImage(String name, NativeImage image)
    {
        this.putImage("assets/" + LambdaBetterGrass.MODID + "/textures/bettergrass/" + name + ".png", image);
        return new Identifier(LambdaBetterGrass.MODID, "bettergrass/" + name);
    }

    @Override
    public InputStream openRoot(String fileName) throws IOException
    {
        byte[] data;
        if ((data = this.resources.get(fileName)) != null) {
            return new ByteArrayInputStream(data);
        }
        throw new IOException("Generated resources pack has no data or alias for " + fileName);
    }

    @Override
    public InputStream open(ResourceType type, Identifier id) throws IOException
    {
        if (type == ResourceType.SERVER_DATA) throw new IOException("Reading server data from LambdaBetterGrass client resource pack");
        return this.openRoot(type.getDirectory() + "/" + id.getNamespace() + "/" + id.getPath());
    }

    @Override
    public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, int maxDepth, Predicate<String> pathFilter)
    {
        if (type == ResourceType.SERVER_DATA) return Collections.emptyList();
        String start = "assets/" + namespace + "/" + prefix;
        return this.resources.keySet().stream()
                .filter(s -> s.startsWith(start) && pathFilter.test(s))
                .map(LBGResourcePack::fromPath)
                .collect(Collectors.toList());
    }

    @Override
    public boolean contains(ResourceType type, Identifier id)
    {
        String path = type.getDirectory() + "/" + id.getNamespace() + "/" + id.getPath();
        return this.resources.containsKey(path);
    }

    @Override
    public Set<String> getNamespaces(ResourceType type)
    {
        return NAMESPACES;
    }

    @Nullable
    @Override
    public <T> T parseMetadata(ResourceMetadataReader<T> metaReader) throws IOException
    {
        return null;
    }

    @Override
    public String getName()
    {
        return "LambdaBetterGrass generated resources";
    }

    @Override
    public void close()
    {
    }

    private static Identifier fromPath(String path)
    {
        String[] split = path.split("/", 2);
        return new Identifier(split[0], split[1]);
    }
}
