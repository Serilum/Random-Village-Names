package com.serilum.randomvillagenames.util;

import com.natamus.collective.data.GlobalVariables;
import com.natamus.collective.functions.DimensionFunctions;
import com.natamus.collective.functions.FABFunctions;
import com.natamus.collective.functions.SignFunctions;
import com.natamus.collective.functions.WorldFunctions;
import com.serilum.randomvillagenames.config.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Util {
	private static final List<String> zoneprefixes = new ArrayList<>(Arrays.asList("[na]", "[area]", "[region]", "[zone]"));
	private static final HashMap<ServerLevel, CopyOnWriteArrayList<BlockPos>> signedVillages = new HashMap<>();
	private static final HashMap<ServerLevel, CopyOnWriteArrayList<String>> usedVillageNames = new HashMap<>();

	public static boolean hasAreasSignNeaby(Level level, BlockPos pos, int radius) {
		List<BlockPos> signsaround = FABFunctions.getAllTileEntityPositionsNearbyPosition(BlockEntityTypes.SIGN, radius, level, pos);
		for (BlockPos signpos : signsaround) {
			BlockEntity te = level.getBlockEntity(signpos);
			if (te instanceof SignBlockEntity) {
				if (isAreasSign((SignBlockEntity) te)) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isAreasSign(SignBlockEntity signentity) {
		int i = -1;
		for (String line : SignFunctions.getSignText(signentity)) {
			i += 1;

			if (i == 0 && hasZonePrefix(line)) {
				return true;
			}
			break;
		}

		return false;
	}

	private static boolean hasZonePrefix(String line) {
		for (String prefix : zoneprefixes) {
			if (line.toLowerCase().startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isSign(Block block) {
		return block instanceof StandingSignBlock || block instanceof WallSignBlock;
	}

	public static boolean isOverwritableBlockOrSign(Block block) {
		return block.equals(Blocks.AIR) || Util.isSign(block) || (block instanceof BushBlock) || (block instanceof SnowLayerBlock);
	}

	public static List<String> getVillageNames() {
		List<String> villageNames = new ArrayList<>(ConfigHandler.customVillageNames);
		if (ConfigHandler.useCollectiveVillageNames) {
			villageNames.addAll(GlobalVariables.areaNames);
		}
		return villageNames;
	}

	public static String getRandomVillageName(ServerLevel serverLevel) {
		List<String> villageNames = getVillageNames();
		if (villageNames.isEmpty()) {
			return null;
		}

		if (!ConfigHandler.useEachNameOnce) {
			return villageNames.get(GlobalVariables.random.nextInt(villageNames.size()));
		}

		CopyOnWriteArrayList<String> usedNames = getUsedVillageNames(serverLevel);
		List<String> unusedNames = new ArrayList<>(villageNames);
		unusedNames.removeAll(usedNames);
		if (unusedNames.isEmpty()) {
			usedNames.clear();
			unusedNames = villageNames;
		}

		String villageName = unusedNames.get(GlobalVariables.random.nextInt(unusedNames.size()));
		usedNames.add(villageName);
		writeDataLines(serverLevel, "used_names.txt", usedNames);
		return villageName;
	}

	public static List<String> getVillageSignLines(String villageName) {
		List<String> signLines = new ArrayList<>(Arrays.asList("[Area] 60", ""));
		if (villageName == null) {
			return signLines;
		}

		String[] nameWords = villageName.split(" ");

		signLines.add(nameWords[0]);
		if (nameWords.length > 1) {
			signLines.add(String.join(" ", Arrays.copyOfRange(nameWords, 1, nameWords.length)));
		}
		return signLines;
	}

	public static boolean canPlaceSignUnderground(Level level, BlockPos pos) {
		if (level.getBlockEntity(pos) != null) {
			return false;
		}

		BlockState belowState = level.getBlockState(pos.below());
		return level.getBlockState(pos).getFluidState().isEmpty() && !belowState.isAir() && belowState.getFluidState().isEmpty();
	}

	public static boolean areChunksAroundLoaded(Level level, BlockPos pos) {
		int chunkX = pos.getX() >> 4;
		int chunkZ = pos.getZ() >> 4;

		for (int x = chunkX - 1; x <= chunkX + 1; x++) {
			for (int z = chunkZ - 1; z <= chunkZ + 1; z++) {
				if (!level.hasChunk(x, z)) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean isVillageSigned(ServerLevel serverLevel, BlockPos villagePos) {
		return getSignedVillages(serverLevel).contains(villagePos);
	}

	public static void markVillageSigned(ServerLevel serverLevel, BlockPos villagePos) {
		CopyOnWriteArrayList<BlockPos> villages = getSignedVillages(serverLevel);
		if (villages.contains(villagePos)) {
			return;
		}

		villages.add(villagePos);
		writeSignedVillages(serverLevel, villages);
	}

	private static CopyOnWriteArrayList<BlockPos> getSignedVillages(ServerLevel serverLevel) {
		if (!signedVillages.containsKey(serverLevel)) {
			signedVillages.put(serverLevel, readSignedVillages(serverLevel));
		}
		return signedVillages.get(serverLevel);
	}

	private static CopyOnWriteArrayList<String> getUsedVillageNames(ServerLevel serverLevel) {
		if (!usedVillageNames.containsKey(serverLevel)) {
			usedVillageNames.put(serverLevel, new CopyOnWriteArrayList<>(readDataLines(serverLevel, "used_names.txt")));
		}
		return usedVillageNames.get(serverLevel);
	}

	private static CopyOnWriteArrayList<BlockPos> readSignedVillages(ServerLevel serverLevel) {
		CopyOnWriteArrayList<BlockPos> villages = new CopyOnWriteArrayList<>();
		for (String coordinateString : readDataLines(serverLevel, "villages.txt")) {
			String[] coordinates = coordinateString.split(",");
			if (coordinates.length != 2) {
				continue;
			}

			try {
				villages.add(new BlockPos(Integer.parseInt(coordinates[0]), 0, Integer.parseInt(coordinates[1])));
			}
			catch (NumberFormatException ignored) { }
		}
		return villages;
	}

	private static void writeSignedVillages(ServerLevel serverLevel, List<BlockPos> villages) {
		List<String> coordinateStrings = new ArrayList<>();
		for (BlockPos villagePos : villages) {
			coordinateStrings.add(villagePos.getX() + "," + villagePos.getZ());
		}
		writeDataLines(serverLevel, "villages.txt", coordinateStrings);
	}

	private static String getDataFolder(ServerLevel serverLevel) {
		return WorldFunctions.getWorldPath(serverLevel) + File.separator + "data" + File.separator + Reference.MOD_ID + File.separator + DimensionFunctions.getSimpleDimensionString(serverLevel);
	}

	private static List<String> readDataLines(ServerLevel serverLevel, String fileName) {
		List<String> lines = new ArrayList<>();

		String filePath = getDataFolder(serverLevel) + File.separator + fileName;
		if (!(new File(filePath)).isFile()) {
			return lines;
		}

		try {
			String rawContent = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
			for (String line : rawContent.split("\n")) {
				if (!line.isBlank()) {
					lines.add(line.strip());
				}
			}
		}
		catch (IOException ignored) { }

		return lines;
	}

	private static void writeDataLines(ServerLevel serverLevel, String fileName, List<String> lines) {
		String dataFolder = getDataFolder(serverLevel);
		File dataDir = new File(dataFolder);
		if (!dataDir.isDirectory()) {
			boolean ignored = dataDir.mkdirs();
		}

		try {
			PrintWriter writer = new PrintWriter(dataFolder + File.separator + fileName, StandardCharsets.UTF_8);
			for (String line : lines) {
				writer.println(line);
			}
			writer.close();
		}
		catch (IOException ignored) { }
	}
}
