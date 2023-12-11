package com.natamus.randomvillagenames.forge.events;

import com.natamus.randomvillagenames.events.SetVillageSignEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ForgeSetVillageSignEvent {
	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent e) {
		Level level = e.world;
		if (level.isClientSide || !e.phase.equals(TickEvent.Phase.START)) {
			return;
		}

		SetVillageSignEvent.onWorldTick((ServerLevel)level);
	}

	@SubscribeEvent
	public void onChunkLoad(ChunkWatchEvent.Watch e) {
		ServerLevel serverLevel = e.getWorld();
		ChunkPos chunkPos = e.getPos();
		SetVillageSignEvent.onChunkLoad(serverLevel, serverLevel.getChunk(chunkPos.x, chunkPos.z));
	}
}
