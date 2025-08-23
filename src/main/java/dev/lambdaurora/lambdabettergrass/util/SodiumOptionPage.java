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
import dev.lambdaurora.lambdabettergrass.LBGCompat;
import net.minecraft.network.chat.Text;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * Represents utilities to inject a sodium option page for LambdaBetterGrass.
 *
 * @author LambdAurora
 * @version 2.0.0
 * @since 2.0.0
 */
public final class SodiumOptionPage {
	private static final MethodHandle CREATE_OPTION_PAGE;

	public static Object makeSodiumOptionPage(Text text) {
		try {
			return CREATE_OPTION_PAGE.invoke(text, ImmutableList.of());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	static {
		try {
			switch (LBGCompat.isSodiumInstalled()) {
				case V06X -> {
					Class<?> optionPage = Class.forName("net.caffeinemc.mods.sodium.client.gui.options.OptionPage");
					CREATE_OPTION_PAGE = MethodHandles.lookup().unreflectConstructor(optionPage.getConstructor(Text.class, ImmutableList.class));
				}
				case V05X -> {
					Class<?> optionPage = Class.forName("me.jellysquid.mods.sodium.client.gui.options.OptionPage");
					CREATE_OPTION_PAGE = MethodHandles.lookup().unreflectConstructor(optionPage.getConstructor(Text.class, ImmutableList.class));
				}
				default -> CREATE_OPTION_PAGE = null;
			}
		} catch (IllegalAccessException | NoSuchMethodException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
