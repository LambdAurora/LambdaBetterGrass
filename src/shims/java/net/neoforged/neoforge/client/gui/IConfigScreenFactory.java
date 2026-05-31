package net.neoforged.neoforge.client.gui;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.IExtensionPoint;

public interface IConfigScreenFactory extends IExtensionPoint {
	Screen createScreen(ModContainer container, Screen modListScreen);
}
