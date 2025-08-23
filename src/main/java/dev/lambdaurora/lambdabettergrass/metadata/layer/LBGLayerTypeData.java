/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.metadata.layer;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.lambdabettergrass.util.CodecUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents the metadata of a layer type.
 *
 * @param state the block state associated with this layer type
 *
 * @author LambdAurora
 * @version 2.0.0
 * @since 2.0.0
 */
public record LBGLayerTypeData(@NotNull BlockState state, @Unmodifiable List<Matcher> matchers) {
	public static final Codec<LBGLayerTypeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockState.CODEC.fieldOf("block").forGetter(LBGLayerTypeData::state),
			Matcher.LIST_CODEC.optionalFieldOf("match", List.of()).forGetter(LBGLayerTypeData::matchers)
	).apply(instance, LBGLayerTypeData::new));

	/**
	 * Returns whether the given block state can be used for this layer type.
	 *
	 * @param state the block state to match
	 * @return {@code true} if the given block state matches this layer type, or {@code false} otherwise
	 */
	public boolean match(BlockState state) {
		if (this.matchers.isEmpty()) {
			return state.is(this.state.getBlock());
		}

		return this.matchers.stream().anyMatch(matcher -> matcher.test(state));
	}

	public sealed interface Matcher extends Predicate<BlockState> {
		Codec<Matcher> CODEC = Codec.either(BlockMatch.CODEC, Codec.either(TagMatch.CODEC, BlockStateMatch.CODEC))
				.xmap(either -> {
					return either.map(
							Function.identity(), inner -> inner.map(Function.identity(), Function.identity()));
				}, matcher -> switch (matcher) {
					case BlockMatch blockMatch -> Either.left(blockMatch);
					case TagMatch tagMatch -> Either.right(Either.left(tagMatch));
					case BlockStateMatch blockStateMatch -> Either.right(Either.right(blockStateMatch));
				});
		Codec<List<Matcher>> LIST_CODEC = Codec.withAlternative(
				CODEC.listOf(),
				CODEC.xmap(List::of, List::getFirst)
		);
	}

	public record BlockStateMatch(@NotNull Block block, @Unmodifiable Collection<Property.Value<?>> properties) implements Matcher {
		public static final Codec<BlockStateMatch> CODEC = BuiltInRegistries.BLOCK.byNameCodec().dispatch(
				"block",
				BlockStateMatch::block,
				block -> block.defaultState().getValues().isEmpty()
						? MapCodec.unit(new BlockStateMatch(block, List.of()))
						: CodecUtils.propertiesCodec(block.defaultState()).lenientOptionalFieldOf("properties", List.of())
						.xmap(values -> new BlockStateMatch(block, values), BlockStateMatch::properties)
		);

		@Override
		public boolean test(BlockState state) {
			if (!state.is(this.block)) return false;

			for (var value : this.properties) {
				if (!state.get(value.property()).equals(value.value())) {
					return false;
				}
			}

			return true;
		}
	}

	public record BlockMatch(@NotNull Block block) implements Matcher {
		public static final Codec<BlockMatch> CODEC = BuiltInRegistries.BLOCK.byNameCodec()
				.xmap(BlockMatch::new, BlockMatch::block);

		@Override
		public boolean test(BlockState state) {
			return state.is(this.block);
		}
	}

	public record TagMatch(@NotNull TagKey<Block> tag) implements Matcher {
		public static final Codec<TagMatch> CODEC = TagKey.hashedCodec(Registries.BLOCK)
				.xmap(TagMatch::new, TagMatch::tag);

		@Override
		public boolean test(BlockState state) {
			return state.is(this.tag);
		}
	}
}
