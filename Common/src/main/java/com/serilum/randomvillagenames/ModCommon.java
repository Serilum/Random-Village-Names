package com.serilum.randomvillagenames;

import com.natamus.collective.config.GenerateJSONFiles;
import com.serilum.randomvillagenames.config.ConfigHandler;
import com.serilum.randomvillagenames.util.Reference;

public class ModCommon {

	public static void init() {
		ConfigHandler.initConfig();
		load();
	}

	private static void load() {
		GenerateJSONFiles.requestJSONFile(Reference.MOD_ID, "area_names.json");
	}
}