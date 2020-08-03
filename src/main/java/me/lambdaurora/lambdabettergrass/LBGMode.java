/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdabettergrass;

import me.lambdaurora.spruceui.SpruceTexts;
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
 * @version 1.0.0
 * @since 1.0.0
 */
public enum LBGMode implements Nameable {
    OFF(SpruceTexts.OPTIONS_OFF, Formatting.RED),
    FASTEST(SpruceTexts.OPTIONS_GENERIC_FASTEST, Formatting.GOLD),
    FAST(SpruceTexts.OPTIONS_GENERIC_FAST, Formatting.YELLOW),
    FANCY(SpruceTexts.OPTIONS_GENERIC_FANCY, Formatting.GREEN);

    private final Text text;
    private final Formatting formatting;

    LBGMode(@NotNull Text text, @NotNull Formatting formatting) {
        this.text = text.copy().formatted(formatting);
        this.formatting = formatting;
    }

    /**
     * Returns whether this mode enables better grass.
     *
     * @return True if the mode enables better grass, else false.
     */
    public boolean isEnabled() {
        return this != OFF;
    }

    /**
     * Returns the next better grass mode available.
     *
     * @return The next available better grass mode.
     */
    public LBGMode next() {
        LBGMode[] v = values();
        if (v.length == this.ordinal() + 1)
            return v[0];
        return v[this.ordinal() + 1];
    }

    /**
     * Returns the translated text of the better grass mode.
     *
     * @return The translated text of the better grass mode.
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
     * @param id The identifier of the better grass mode.
     * @return The better grass mode if found, else empty.
     */
    public static @NotNull Optional<LBGMode> byId(@NotNull String id) {
        return Arrays.stream(values()).filter(mode -> mode.getName().equalsIgnoreCase(id)).findFirst();
    }
}
