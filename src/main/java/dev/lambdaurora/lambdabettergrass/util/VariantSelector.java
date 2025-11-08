/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;

/**
 * Provides utilities for selecting variants.
 *
 * @author LambdAurora
 * @version 2.0.2
 * @since 2.0.2
 */
public final class VariantSelector {
	private VariantSelector() {
		throw new UnsupportedOperationException("VariantSelector only contains static definitions.");
	}

	/**
	 * Extract the block state properties the given variant cares about.
	 *
	 * @param stateDefinition the block state definition
	 * @param variant the variant to extract
	 * @return the properties and values extracted from the variant
	 */
	public static @Unmodifiable List<Property.Value<?>> extractProperties(
			StateDefinition<Block, BlockState> stateDefinition, String variant
	) {
		var list = ImmutableList.<Property.Value<?>>builder();

		for (var pair : variant.split(",")) {
			int separator = pair.indexOf('=');
			if (separator == -1) continue;

			var key = pair.substring(0, separator);
			var rawValue = pair.substring(separator + 1);
			var property = stateDefinition.getProperty(key);

			if (property == null) continue;

			makeValue(property, rawValue).ifPresent(list::add);
		}

		return list.build();
	}

	/**
	 * Matches whether the given block state satisfies the requirements of the given property values.
	 *
	 * @param state the block state to match
	 * @param values the property values to match
	 * @return {@code true} if the block state satisfies the requirements, or {@code false} otherwise
	 */
	public static boolean match(BlockState state, List<Property.Value<?>> values) {
		for (var value : values) {
			if (!state.getValue(value.property()).equals(value.value())) {
				return false;
			}
		}

		return true;
	}

	private static <T extends Comparable<T>> Optional<Property.Value<T>> makeValue(Property<T> property, String rawValue) {
		var value = property.getValue(rawValue);
		return value.map(property::value);
	}
}
