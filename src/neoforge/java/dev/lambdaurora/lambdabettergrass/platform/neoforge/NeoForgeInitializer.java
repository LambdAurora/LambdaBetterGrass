/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of LambdaBetterGrass.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.lambdabettergrass.platform.neoforge;

import dev.lambdaurora.lambdabettergrass.LambdaBetterGrass;
import net.minecraft.network.chat.Text;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.io.ResourceType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@Mod(value = LambdaBetterGrass.NAMESPACE + "_runtime", dist = Dist.CLIENT)
public final class NeoForgeInitializer {
	public NeoForgeInitializer(ModContainer nativeContainer, IEventBus modBus) {
		nativeContainer.registerExtensionPoint(IConfigScreenFactory.class, NeoForgeConfigScreenProvider.INSTANCE);

		ModList.get().getModContainerById(LambdaBetterGrass.NAMESPACE)
				.ifPresent(parentContainer -> parentContainer.registerExtensionPoint(
						IConfigScreenFactory.class,
						NeoForgeConfigScreenProvider.INSTANCE
				));

		modBus.addListener(AddPackFindersEvent.class, this::addPackFinders);
	}

	private void addPackFinders(AddPackFindersEvent event) {
		if (event.getPackType() != ResourceType.CLIENT_RESOURCES) {
			return;
		}

		event.addPackFinders(
				Identifier.of(LambdaBetterGrass.NAMESPACE + "_runtime", "resourcepacks/default"),
				ResourceType.CLIENT_RESOURCES,
				Text.translatable("lambdabettergrass.resourcepack.default", Text.translatable(LambdaBetterGrass.NAMESPACE)),
				PackSource.BUILT_IN,
				true,
				Pack.Position.TOP
		);

		event.addPackFinders(
				Identifier.of(LambdaBetterGrass.NAMESPACE + "_runtime", "resourcepacks/32x"),
				ResourceType.CLIENT_RESOURCES,
				Text.literal("LambdaBetterGrass - 32x"),
				PackSource.BUILT_IN,
				false,
				Pack.Position.TOP
		);
	}
}
