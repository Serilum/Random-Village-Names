package com.natamus.randomvillagenames.forge.events;

import com.natamus.randomvillagenames.events.SetVillageSignEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeSetVillageSignEvent {
	@SubscribeEvent
	public static void onWorldTick(TickEvent.LevelTickEvent e) {
		Level level = e.level;
		if (level.isClientSide || !e.phase.equals(TickEvent.Phase.START)) {
			return;
		}

		SetVillageSignEvent.onWorldTick((ServerLevel)level);
	}

	@SubscribeEvent
	public static void onChunkLoad(ChunkWatchEvent.Watch e) {
		SetVillageSignEvent.onChunkLoad(e.getLevel(), e.getChunk());
	}
}
