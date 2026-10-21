package com.serilum.randomvillagenames.config;

import com.natamus.collective.config.DuskConfig;
import com.serilum.randomvillagenames.util.Reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConfigHandler extends DuskConfig {
	public static HashMap<String, List<String>> configMetaData = new HashMap<>();

	@Entry public static List<String> customVillageNames = new ArrayList<>();
	@Entry public static boolean useCollectiveVillageNames = true;
	@Entry public static boolean useEachNameOnce = false;
	@Entry public static boolean placeSignsUnderground = false;
	@Entry(min = 1, max = 10) public static int undergroundSignDepth = 3;

	public static void initConfig() {
		configMetaData.put("customVillageNames", Arrays.asList(
			"Your own village names, one per line in the config screen. A village gets a random name from this list, together with Collective's names when 'useCollectiveVillageNames' is enabled."
		));
		configMetaData.put("useCollectiveVillageNames", Arrays.asList(
			"Whether villages can also get a name from Collective's built-in list, which is editable in ./config/serilum/collective/area_names.json."
		));
		configMetaData.put("useEachNameOnce", Arrays.asList(
			"If enabled, a village name is only used again once every name has been given to a village in that dimension."
		));
		configMetaData.put("placeSignsUnderground", Arrays.asList(
			"If enabled, the village name sign is placed underground below the village center, where it can't be broken by accident. Areas still finds it."
		));
		configMetaData.put("undergroundSignDepth", Arrays.asList(
			"How many blocks below the surface the sign is placed when 'placeSignsUnderground' is enabled."
		));

		DuskConfig.init(Reference.NAME, Reference.MOD_ID, ConfigHandler.class);
	}
}
