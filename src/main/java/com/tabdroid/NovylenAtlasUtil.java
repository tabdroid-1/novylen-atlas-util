package com.tabdroid;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.tabdroid.AtlasCommon.*;

import com.mojang.brigadier.arguments.StringArgumentType;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NovylenAtlasUtil implements ModInitializer {
	public static final String MOD_ID = "novylen-atlas-util";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static AtlasModUtils  ATLAS_MOD_UTILS = new AtlasModUtils();
	public static AtlasSearchUtils  ATLAS_SEARCH_UTILS = new AtlasSearchUtils();

	@Override
	public void onInitialize() {
		AutoConfig.register(UserConfig.class, JanksonConfigSerializer::new);
		UserConfig config = AutoConfig.getConfigHolder(UserConfig.class).getConfig();

		// atlas-util request create command
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("atlas-util")
					.then(ClientCommandManager.literal("request")
						.then(ClientCommandManager.literal("create")
							.then(ClientCommandManager.argument("name", StringArgumentType.string())
							.then(ClientCommandManager.argument("type", StringArgumentType.word())
							.then(ClientCommandManager.argument("info", StringArgumentType.string())
							.executes(commandContext -> {
								if (!ATLAS_MOD_UTILS.CreateRequest(commandContext))
									return 0;

								return 1;
					})))))));
		});

		// atlas-util request review command
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("atlas-util")
					.then(ClientCommandManager.literal("request")
					.then(ClientCommandManager.literal("review")
					.executes(commandContext -> {
						ATLAS_MOD_UTILS.ReviewRequest(commandContext);
						return 1;
					}))));
		});

		// atlas-util request confirm command
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("atlas-util")
					.then(ClientCommandManager.literal("request")
					.then(ClientCommandManager.literal("confirm")
					.executes(commandContext -> {
						ATLAS_MOD_UTILS.ConfirmRequest(commandContext);
						return 1;
					}))));
		});

		// atlas-util request send command
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("atlas-util")
					.then(ClientCommandManager.literal("request")
					.then(ClientCommandManager.literal("send")
					.executes(commandContext -> {
						ATLAS_MOD_UTILS.SendRequest(commandContext);
						return 1;
					}))));
		});

		// atlas-util request clear command
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("atlas-util")
					.then(ClientCommandManager.literal("request")
					.then(ClientCommandManager.literal("clear")
					.executes(commandContext -> {
						ATLAS_MOD_UTILS.ClearRequest(commandContext);
						return 1;
					}))));
		});

		// atlas-util search nearby <radius>
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("atlas-util")
					.then(ClientCommandManager.literal("search")
							.then(ClientCommandManager.literal("nearby")
									.then(ClientCommandManager.argument("radius", IntegerArgumentType.integer(10, 20000))
									.executes(commandContext -> {
										ATLAS_SEARCH_UTILS.GetPOIsNearBy(commandContext);
										return 1;
									})))));
		});

		// atlas-util search query <name>
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("atlas-util")
					.then(ClientCommandManager.literal("search")
							.then(ClientCommandManager.literal("query")
									.then(ClientCommandManager.argument("query", StringArgumentType.string())
											.executes(commandContext -> {
												ATLAS_SEARCH_UTILS.QueryPOIs(commandContext);
												return 1;
											})))));
		});


	}
}