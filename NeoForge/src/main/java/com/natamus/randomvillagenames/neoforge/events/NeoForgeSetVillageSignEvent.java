package com.natamus.randomvillagenames.neoforge.events;

import com.natamus.randomvillagenames.events.SetVillageSignEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber
public class NeoForgeSetVillageSignEvent {
	@SubscribeEvent
	public static void onWorldTick(LevelTickEvent.Pre e) {
		Level level = e.getLevel();
		if (level.isClientSide) {
			return;
		}

		SetVillageSignEvent.onWorldTick((ServerLevel)level);
	}

	@SubscribeEvent
	public static void onChunkLoad(ChunkWatchEvent.Watch e) {
		SetVillageSignEvent.onChunkLoad(e.getLevel(), e.getChunk());
	}
}
