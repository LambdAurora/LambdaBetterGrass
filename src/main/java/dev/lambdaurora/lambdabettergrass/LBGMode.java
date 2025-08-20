/*
 * Copyright © 2021 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass;

import dev.lambdaurora.spruceui.SpruceTexts;
import dev.lambdaurora.spruceui.util.Nameable;
import net.minecraft.TextFormatting;
import net.minecraft.network.chat.Text;

import java.util.Arrays;
import java.util.Optional;

/**
 * Represents the better grass mode.
 *
 * @author LambdAurora
 * @version 1.2.1
 * @since 1.0.0
 */
public enum LBGMode implements Nameable {
	OFF(SpruceTexts.OPTIONS_OFF, TextFormatting.RED),
	FASTEST(SpruceTexts.OPTIONS_GENERIC_FASTEST, TextFormatting.GOLD),
	FAST(SpruceTexts.OPTIONS_GENERIC_FAST, TextFormatting.YELLOW),
	FANCY(SpruceTexts.OPTIONS_GENERIC_FANCY, TextFormatting.GREEN);

	private final Text text;

	LBGMode(Text text, TextFormatting formatting) {
		this.text = text.copy().withStyle(formatting);
	}

	/**
	 * Returns whether this mode enables better grass.
	 *
	 * @return {@code true} if the mode enables better grass, otherwise {@code false}
	 */
	public boolean isEnabled() {
		return this != OFF;
	}

	/**
	 * {@return the next available better grass mode}
	 */
	public LBGMode next() {
		var v = values();
		if (v.length == this.ordinal() + 1)
			return v[0];
		return v[this.ordinal() + 1];
	}

	/**
	 * {@return the translated text of the better grass mode}
	 */
	public Text getTranslatedText() {
		return this.text;
	}

	@Override
	public String getName() {
		return this.name().toLowerCase();
	}

	/**
	 * Gets the better grass mode from its identifier.
	 *
	 * @param id the identifier of the better grass mode
	 * @return the better grass mode if found, otherwise empty
	 */
	public static Optional<LBGMode> byId(String id) {
		return Arrays.stream(values()).filter(mode -> mode.getName().equalsIgnoreCase(id)).findFirst();
	}
}
