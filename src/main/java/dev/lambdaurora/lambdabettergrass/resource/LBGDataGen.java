package dev.lambdaurora.lambdabettergrass.resource;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.*;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LBGDataGen implements DataGeneratorEntrypoint {
	private static final Identifier BUTTON_DATA = new Identifier("bettergrass/data/button");
	private static final Identifier FLOWER_DATA = new Identifier("bettergrass/data/flower");

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		var pack = fabricDataGenerator.createPack();

		pack.addProvider(LayerDataProvider::new);
	}

	private static final class LayerDataProvider implements DataProvider {
		private final PackOutput.PathProvider pathProvider;
		private final CompletableFuture<HolderLookup.Provider> registryProvider;

		public LayerDataProvider(FabricDataOutput packOutput, CompletableFuture<HolderLookup.Provider> registryProvider) {
			this.pathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "bettergrass");
			this.registryProvider = registryProvider;
		}

		@Override
		public String getName() {
			return "lambdabettergrass:layer_data_provider";
		}

		@Override
		public @NotNull CompletableFuture<?> run(CachedOutput cachedOutput) {
			return this.registryProvider.thenCompose(provider -> {
				final var context = new Context();
				provider.lookupOrThrow(Registries.BLOCK).streamElements()
						.filter(Holder::isBound)
						.forEach(entry -> {
							final var id = entry.key().value();
							final var block = entry.value();

							if (block instanceof ButtonBlock) {
								context.addSimpleLayerState(id, BUTTON_DATA);
							} else if (block instanceof FlowerBlock) {
								context.addSimpleLayerState(id, FLOWER_DATA);
							} else if (block instanceof DoorBlock
									|| block instanceof FenceGateBlock
									|| block instanceof MushroomBlock
									|| block instanceof SaplingBlock
									|| block instanceof TallGrassBlock) {
								context.addEmptyLayerData(id);
								context.addSimpleLayerState(id, id.withPrefix("bettergrass/data/"));
							} else if (block instanceof TrapDoorBlock) {
								context.addEmptyLayerData(id);
								context.addLayerStateWithSimpleCondition(id, "half=top,waterlogged=false");
							} else if (block instanceof FenceBlock
									|| block instanceof WallBlock) {
								context.addEmptyLayerData(id);
								context.addWaterloggedSimpleLayerState(id);
							}
						});

				return CompletableFuture.allOf(
						context.sources.entrySet().stream()
								.map(entry ->
										saveStable(cachedOutput, entry.getValue(), this.pathProvider.json(entry.getKey()))
								)
								.toArray(CompletableFuture[]::new)
				);
			});
		}
	}

	private static final class Context {
		private static final JsonObject EMPTY = new JsonObject();
		private final Map<Identifier, JsonElement> sources = new HashMap<>();

		public void addData(Identifier id, JsonElement jsonElement) {
			this.sources.put(id.withPrefix("data/"), jsonElement);
		}

		public void addEmptyLayerData(Identifier id) {
			this.addData(id, EMPTY);
		}

		public void addState(Identifier id, JsonElement jsonElement) {
			this.sources.put(id.withPrefix("states/"), jsonElement);
		}

		public void addSimpleLayerState(Identifier id, Identifier data) {
			var json = new JsonObject();
			json.addProperty("type", "layer");
			json.addProperty("data", data.toString());
			this.addState(id, json);
		}

		public void addLayerStateWithSimpleCondition(Identifier id, String condition) {
			var state = new JsonObject();
			state.addProperty("type", "layer");
			var variants = new JsonObject();
			state.add("variants", variants);
			var variant = new JsonObject();
			variants.add(condition, variant);
			variant.addProperty("data", id.withPrefix("bettergrass/data/").toString());
			this.addState(id, state);
		}

		public void addWaterloggedSimpleLayerState(Identifier id) {
			this.addLayerStateWithSimpleCondition(id, "waterlogged=false");
		}
	}

	@SuppressWarnings("UnstableApiUsage")
	static CompletableFuture<?> saveStable(CachedOutput cachedOutput, JsonElement jsonElement, Path path) {
		return CompletableFuture.runAsync(() -> {
			try {
				var byteArrayOutputStream = new ByteArrayOutputStream();
				var hashingOutputStream = new HashingOutputStream(Hashing.sha256(), byteArrayOutputStream);

				try (var jsonWriter = new JsonWriter(new OutputStreamWriter(hashingOutputStream, StandardCharsets.UTF_8))) {
					jsonWriter.setSerializeNulls(false);
					jsonWriter.setIndent("\t");
					GsonHelper.writeValue(jsonWriter, jsonElement, DataProvider.KEY_COMPARATOR);
				}

				hashingOutputStream.write('\n');
				cachedOutput.writeIfNeeded(path, byteArrayOutputStream.toByteArray(), hashingOutputStream.hash());
			} catch (IOException e) {
				DataProvider.LOGGER.error("Failed to save file to {}", path, e);
			}
		}, Util.backgroundExecutor());
	}
}
