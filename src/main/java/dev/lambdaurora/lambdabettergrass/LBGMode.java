/*
 * Copyright © 2021 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass;

import dev.lambdaurora.spruceui.SpruceTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.aperlambda.lambdacommon.utils.Nameable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

/**
 * Represents the better grass mode.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public enum LBGMode implements Nameable {
    OFF(SpruceTexts.OPTIONS_OFF, Formatting.RED),
    FASTEST(SpruceTexts.OPTIONS_GENERIC_FASTEST, Formatting.GOLD),
    FAST(SpruceTexts.OPTIONS_GENERIC_FAST, Formatting.YELLOW),
    FANCY(SpruceTexts.OPTIONS_GENERIC_FANCY, Formatting.GREEN);

    private final Text text;

    LBGMode(@NotNull Text text, @NotNull Formatting formatting) {
        this.text = text.copy().formatted(formatting);
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
    public @NotNull Text getTranslatedText() {
        return this.text;
    }

    @Override
    public @NotNull String getName() {
        return this.name().toLowerCase();
    }

    /**
     * Gets the better grass mode from its identifier.
     *
     * @param id the identifier of the better grass mode
     * @return the better grass mode if found, otherwise empty
     */
    public static @NotNull Optional<LBGMode> byId(@NotNull String id) {
        return Arrays.stream(values()).filter(mode -> mode.getName().equalsIgnoreCase(id)).findFirst();
    }
}
