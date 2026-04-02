package com.natamus.randomvillagenames;

import com.natamus.collective.check.RegisterMod;
import com.natamus.collective.check.ShouldLoadCheck;
import com.natamus.randomvillagenames.events.SetVillageSignEvent;
import com.natamus.randomvillagenames.util.Reference;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;

public class ModFabric implements ModInitializer {
	
	@Override
	public void onInitialize() {
		if (!ShouldLoadCheck.shouldLoad(Reference.MOD_ID)) {
			return;
		}

		setGlobalConstants();
		ModCommon.init();

		loadEvents();

		RegisterMod.register(Reference.NAME, Reference.MOD_ID, Reference.VERSION, Reference.ACCEPTED_VERSIONS);
	}

	private void loadEvents() {
		ServerTickEvents.START_LEVEL_TICK.register((ServerLevel level) -> {
			SetVillageSignEvent.onWorldTick(level);
		});

		ServerChunkEvents.CHUNK_LOAD.register((serverLevel, chunk, generated) -> {
			SetVillageSignEvent.onChunkLoad(serverLevel, chunk);
		});
	}

	private static void setGlobalConstants() {

	}
}
