package com.natamus.randomvillagenames.events;

import com.natamus.collective.functions.BlockPosFunctions;
import com.natamus.collective.functions.HashMapFunctions;
import com.natamus.collective.functions.TileEntityFunctions;
import com.natamus.randomvillagenames.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SetVillageSignEvent {
	private static final HashMap<ServerLevel, List<ChunkPos>> processChunks = new HashMap<ServerLevel, List<ChunkPos>>();

	private static final HashMap<ServerLevel, CopyOnWriteArrayList<BlockPos>> existingvillages = new HashMap<ServerLevel, CopyOnWriteArrayList<BlockPos>>();
	private static final HashMap<ServerLevel, ArrayList<ChunkPos>> cachedchunks = new HashMap<ServerLevel, ArrayList<ChunkPos>>();

	public static void onWorldTick(ServerLevel serverlevel) {
		if (HashMapFunctions.computeIfAbsent(processChunks, serverlevel, k -> new ArrayList<ChunkPos>()).size() > 0) {
			ChunkPos chunkpos = processChunks.get(serverlevel).get(0);

			if (!HashMapFunctions.computeIfAbsent(cachedchunks, serverlevel, k -> new ArrayList<ChunkPos>()).contains(chunkpos)) {
				cachedchunks.get(serverlevel).add(chunkpos);

				BlockPos worldpos = chunkpos.getWorldPosition();

				if (serverlevel.sectionsToVillage(SectionPos.of(worldpos)) <= 4) {
					for (BlockPos existingvillage : HashMapFunctions.computeIfAbsent(existingvillages, serverlevel, k -> new CopyOnWriteArrayList<BlockPos>())) {
						if (Math.abs(existingvillage.getX() - worldpos.getX()) <= 200) {
							if (Math.abs(existingvillage.getZ() - worldpos.getZ()) <= 200) {
								return;
							}
						}
					}

					BlockPos villagepos = BlockPosFunctions.getNearbyVillage(serverlevel, worldpos);
					if (villagepos == null) {
						return;
					}

					if (existingvillages.get(serverlevel).contains(villagepos)) {
						return;
					}
					existingvillages.get(serverlevel).add(villagepos);

					BlockPos twonorth = villagepos.immutable().north(2);
					if (Util.hasAreasSignNeaby(serverlevel, twonorth, 15)) {
						return;
					}

					BlockPos signpos = BlockPosFunctions.getSurfaceBlockPos(serverlevel, twonorth.getX(), twonorth.getZ());

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
					signentity.setMessage(0, new TextComponent("[Area] 60"));
					TileEntityFunctions.updateTileEntity(serverlevel, signpos, signentity);
				}
			}

			processChunks.get(serverlevel).remove(0);
		}
	}

	public static void onChunkLoad(ServerLevel serverlevel, LevelChunk chunk) {
		if (!serverlevel.getServer().getWorldData().worldGenSettings().generateFeatures()) {
			return;
		}

		ChunkPos chunkpos = chunk.getPos();

		if (HashMapFunctions.computeIfAbsent(cachedchunks, serverlevel, k -> new ArrayList<ChunkPos>()).contains(chunkpos)) {
			return;
		}

		HashMapFunctions.computeIfAbsent(processChunks, serverlevel, k -> new ArrayList<ChunkPos>()).add(chunkpos);
	}
}
