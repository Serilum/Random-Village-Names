package com.natamus.randomvillagenames.forge.events;

import com.natamus.randomvillagenames.events.SetVillageSignEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

import java.lang.invoke.MethodHandles;

public class ForgeSetVillageSignEvent {
	public static void registerEventsInBus() {
		BusGroup.DEFAULT.register(MethodHandles.lookup(), ForgeSetVillageSignEvent.class);
	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.LevelTickEvent.Pre e) {
		Level level = e.level();
		if (level.isClientSide()) {
			return;
		}

		SetVillageSignEvent.onWorldTick((ServerLevel)level);
	}

	@SubscribeEvent
	public static void onChunkLoad(ChunkWatchEvent.Watch e) {
		SetVillageSignEvent.onChunkLoad(e.getLevel(), e.getChunk());
	}
}
