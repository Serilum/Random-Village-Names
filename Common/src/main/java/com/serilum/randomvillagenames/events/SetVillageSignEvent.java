package com.serilum.randomvillagenames.events;

import com.natamus.collective.functions.BlockPosFunctions;
import com.natamus.collective.functions.HashMapFunctions;
import com.natamus.collective.functions.TileEntityFunctions;
import com.serilum.randomvillagenames.config.ConfigHandler;
import com.serilum.randomvillagenames.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SetVillageSignEvent {
	private static final HashMap<ServerLevel, CopyOnWriteArrayList<BlockPos>> villagesToSign = new HashMap<>();

	public static void onWorldTick(ServerLevel serverlevel) {
		CopyOnWriteArrayList<BlockPos> villages = HashMapFunctions.computeIfAbsent(villagesToSign, serverlevel, k -> new CopyOnWriteArrayList<>());
		for (BlockPos villagepos : villages) {
			if (!serverlevel.hasChunk(villagepos.getX() >> 4, villagepos.getZ() >> 4)) {
				villages.remove(villagepos);
				continue;
			}

			if (!Util.areChunksAroundLoaded(serverlevel, villagepos.north(2))) {
				continue;
			}

			villages.remove(villagepos);
			setVillageSign(serverlevel, villagepos);
		}
	}

	public static void onChunkLoad(ServerLevel serverlevel, LevelChunk chunk) {
		if (!serverlevel.getServer().getWorldData().worldGenOptions().generateStructures()) {
			return;
		}

		Registry<Structure> structureRegistry = serverlevel.registryAccess().registryOrThrow(Registries.STRUCTURE);
		for (StructureStart structureStart : chunk.getAllStarts().values()) {
			if (!structureStart.isValid()) {
				continue;
			}

			if (!structureRegistry.wrapAsHolder(structureStart.getStructure()).is(StructureTags.VILLAGE)) {
				continue;
			}

			BlockPos villagepos = structureStart.getChunkPos().getWorldPosition();
			if (Util.isVillageSigned(serverlevel, villagepos)) {
				continue;
			}

			CopyOnWriteArrayList<BlockPos> villages = HashMapFunctions.computeIfAbsent(villagesToSign, serverlevel, k -> new CopyOnWriteArrayList<>());
			if (!villages.contains(villagepos)) {
				villages.add(villagepos);
			}
		}
	}

	public static void setVillageSign(ServerLevel serverlevel, BlockPos villagepos) {
		if (Util.isVillageSigned(serverlevel, villagepos)) {
			return;
		}
		Util.markVillageSigned(serverlevel, villagepos);

		BlockPos twonorth = villagepos.north(2);
		BlockPos signpos = BlockPosFunctions.getSurfaceBlockPos(serverlevel, twonorth.getX(), twonorth.getZ());
		if (Util.hasAreasSignNeaby(serverlevel, signpos, 15)) {
			return;
		}

		BlockState state = serverlevel.getBlockState(signpos);
		Block block = state.getBlock();
		while (!Util.isOverwritableBlockOrSign(block)) {
			signpos = signpos.above().immutable();
			if (signpos.getY() >= 256) {
				return;
			}

			state = serverlevel.getBlockState(signpos);
			block = state.getBlock();
		}

		if (ConfigHandler.placeSignsUnderground) {
			BlockPos undergroundpos = signpos.below(ConfigHandler.undergroundSignDepth);
			if (Util.canPlaceSignUnderground(serverlevel, undergroundpos)) {
				signpos = undergroundpos;
			}
		}

		try {
			Block northblock = serverlevel.getBlockState(signpos.north()).getBlock();
			if (!Util.isOverwritableBlockOrSign(northblock)) {
				serverlevel.setBlockAndUpdate(signpos, Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING, Direction.SOUTH));
			} else {
				serverlevel.setBlockAndUpdate(signpos, Blocks.OAK_SIGN.defaultBlockState());
			}
		} catch (ConcurrentModificationException ignored) {
		}

		BlockEntity te = serverlevel.getBlockEntity(signpos);
		if (!(te instanceof SignBlockEntity)) {
			return;
		}

		SignBlockEntity signentity = (SignBlockEntity) te;
		SignText signtext = signentity.getFrontText();
		int lineindex = 0;
		for (String line : Util.getVillageSignLines(Util.getRandomVillageName(serverlevel))) {
			signtext = signtext.setMessage(lineindex, Component.literal(line));
			lineindex += 1;
		}
		signentity.setText(signtext, true);
		TileEntityFunctions.updateTileEntity(serverlevel, signpos, signentity);
	}
}
