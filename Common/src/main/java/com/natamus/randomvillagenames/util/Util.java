package com.natamus.randomvillagenames.util;

import com.natamus.collective.functions.FABFunctions;
import com.natamus.collective.functions.SignFunctions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {
	private static final List<String> zoneprefixes = new ArrayList<String>(Arrays.asList("[na]", "[area]", "[region]", "[zone]"));

	public static boolean hasAreasSignNeaby(Level level, BlockPos pos, int radius) {
		List<BlockPos> signsaround = FABFunctions.getAllTileEntityPositionsNearbyPosition(BlockEntityType.SIGN, radius, level, pos);
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
}