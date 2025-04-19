package com.tabdroid.common;

import com.mojang.brigadier.context.CommandContext;
import com.tabdroid.UserConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import java.net.http.HttpClient;

public class AtlasCommon {
    private static HttpClient m_Client = HttpClient.newHttpClient();

    public static HttpClient GetHttpClient() {
        return m_Client;
    }

    public static boolean HasApiKey(CommandContext<FabricClientCommandSource> context) {
        UserConfig config = AutoConfig.getConfigHolder(UserConfig.class).getConfig();

        if (config.user_api == null)
        {
            context.getSource().sendFeedback(Text.literal("[Atlas Util] Api key is null. Please add your api key to mod config."));
            return false;
        }

        if (config.user_api.length() != 64)
        {
            context.getSource().sendFeedback(Text.literal("[Atlas Util] Api key is invalid. Please check for typo or copy-paste from site."));
            return false;
        }

        return true;
    }

    public static boolean IsInNovylenSMP(CommandContext<FabricClientCommandSource> context){
        String server_ip = "null";
        if (context.getSource().getClient().getCurrentServerEntry() != null)
            server_ip = context.getSource().getClient().getCurrentServerEntry().address;

        if (!server_ip.equals("minecraft.novylen.net") && !server_ip.equals("mc.novylen.net")){
            context.getSource().sendFeedback(Text.literal("[Atlas Util] You are not connected to novylen."));
            return false;
        }

        if (!context.getSource().getClient().player.getWorld().getDimension().toString().contains("minecraft:overworld")) {
            context.getSource().sendFeedback(Text.literal("[Atlas Util] You should be in smp's overworld."));
            return false;
        }

        if (context.getSource().getClient().player.getWorld().getWorldBorder().getSize() != 40000)
        {
            context.getSource().sendFeedback(Text.literal("[Atlas Util] You should be in smp's overworld."));
            return false;
        }

        return true;
    }
}
