/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Provides some codec-related utilities.
 *
 * @author LambdAurora
 * @version 2.0.0
 * @since 2.0.0
 */
public final class CodecUtils {
	private CodecUtils() {
		throw new UnsupportedOperationException("CodecUtils only contains static definitions.");
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static Codec<Collection<Property.Value<?>>> propertiesCodec(StateHolder<?, ?> state) {
		MapCodec<Map<Property<?>, Property.Value<?>>> mapCodec = MapCodec.of(Encoder.empty(), Decoder.unit(HashMap::new));

		for (var property : state.getProperties()) {
			mapCodec = appendPropertyCodec(mapCodec, property.getName(), (Property) property);
		}

		return mapCodec.xmap(
				Map::values,
				o -> o.stream().collect(Collectors.toMap(Property.Value::property, Function.identity()))
		).codec();
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> MapCodec<Map<Property<?>, Property.Value<?>>> appendPropertyCodec(
			MapCodec<Map<Property<?>, Property.Value<?>>> codec, String key, Property<T> property
	) {
		return Codec.mapPair(codec, property.valueCodec().optionalFieldOf(key))
				.xmap(
						pair -> {
							pair.getSecond().ifPresent(value -> pair.getFirst().put(property, value));
							return pair.getFirst();
						},
						stateHolder -> Pair.of(
								stateHolder,
								Optional.of((Property.Value<T>) stateHolder.get(property))
						)
				);
	}
}
