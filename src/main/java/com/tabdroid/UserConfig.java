package com.tabdroid;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "novylen_atlas_util")
class UserConfig implements ConfigData {
    String user_api = "";
}
