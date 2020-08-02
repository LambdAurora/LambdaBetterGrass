/*
 * Copyright © 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.lambdaurora.lambdabettergrass;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.lambdaurora.lambdabettergrass.gui.SettingsScreen;

/**
 * Represents LambdaBetterGrass ModMenu integration.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class LambdaBetterGrassModMenu implements ModMenuApi
{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory()
    {
        return SettingsScreen::new;
    }
}
