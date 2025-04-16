package com.tabdroid;

import com.mojang.brigadier.arguments.StringArgumentType;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NovylenAtlasUtil implements ModInitializer {
	public static final String MOD_ID = "novylen-atlas-util";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		AutoConfig.register(UserConfig.class, JanksonConfigSerializer::new);
		UserConfig config = AutoConfig.getConfigHolder(UserConfig.class).getConfig();

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("atlas-util")
					.then(ClientCommandManager.literal("create_request")
							.then(ClientCommandManager.argument("name", StringArgumentType.string())
							.then(ClientCommandManager.argument("type", StringArgumentType.word())
							.then(ClientCommandManager.argument("info", StringArgumentType.string())
									.executes(context -> {

										String server_ip = "null";
										if (context.getSource().getClient().getCurrentServerEntry() != null)
											server_ip = context.getSource().getClient().getCurrentServerEntry().address;

										if (!server_ip.equals("minecraft.novylen.net")){
											context.getSource().sendFeedback(Text.literal("[Atlas Util] You are not connected to novylen."));
											return 0;
										}

										if (!context.getSource().getClient().player.getWorld().getDimension().toString().contains("minecraft:overworld")) {
											context.getSource().sendFeedback(Text.literal("[Atlas Util] You are should be in smp's overworld."));
											return 0;
										}

										if (context.getSource().getClient().player.getWorld().getWorldBorder().getSize() != 40000)
										{
											context.getSource().sendFeedback(Text.literal("[Atlas Util] You are should be in smp's overworld."));
											return 0;
										}

										BlockPos position = context.getSource().getClient().player.getBlockPos();
										String name_var = StringArgumentType.getString(context, "name");
										CreatePOIRequest.PointType type_var = CreatePOIRequest.PointTypeFromString(StringArgumentType.getString(context, "type"));
										String info_var = StringArgumentType.getString(context, "info");

										if (type_var == CreatePOIRequest.PointType.Error)
										{
											context.getSource().sendFeedback(Text.literal("[Atlas Util] Invalid point type."));
											return 0;
										}

										CreatePOIRequest.CreateRequest(context, name_var, type_var, info_var, position);

										return 1;
									})))))
					.then(ClientCommandManager.literal("review_request").executes(context -> {
						CreatePOIRequest.ReviewRequest(context);
						return 1;
					}))
					.then(ClientCommandManager.literal("confirm_request").executes(context -> {
						CreatePOIRequest.ConfirmRequest(context);
						return 1;
					}))
					.then(ClientCommandManager.literal("send_request").executes(context -> {
						if (!CreatePOIRequest.SendRequest(context))
							return 0;
						return 1;
					}))
					.then(ClientCommandManager.literal("clear_request").executes(context -> {
						CreatePOIRequest.ClearRequest(context);
						return 1;
					})));
		});
	}
}