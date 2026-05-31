package net.neoforged.neoforge.event;

import net.minecraft.network.chat.Text;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.io.ResourceType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.Event;

public class AddPackFindersEvent extends Event {
	public ResourceType getPackType() {
		throw new AssertionError("Shim only.");
	}

	public void addPackFinders(
			Identifier id, ResourceType packType, Text displayName, PackSource packSource,
			boolean alwaysActive, Pack.Position position
	) {
		throw new AssertionError("Shim only.");
	}
}
