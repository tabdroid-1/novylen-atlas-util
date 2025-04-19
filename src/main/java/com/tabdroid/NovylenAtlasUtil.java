package com.tabdroid;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.tabdroid.common.AtlasMarker.Markers;
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
	public static AtlasSuggestUtils ATLAS_SUGGEST_UTILS = new AtlasSuggestUtils();
	public static AtlasSearchUtils  ATLAS_SEARCH_UTILS = new AtlasSearchUtils();

	@Override
	public void onInitialize() {
		AutoConfig.register(UserConfig.class, JanksonConfigSerializer::new);

		Markers.FetchMarkers(LOGGER);

		// atlas-util request create command
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("atlas-util")
					.then(ClientCommandManager.literal("request")
						.then(ClientCommandManager.literal("create")
							.then(ClientCommandManager.argument("name", StringArgumentType.string())
							.then(ClientCommandManager.argument("dial", StringArgumentType.string())
							.then(ClientCommandManager.argument("type", StringArgumentType.word())
							.then(ClientCommandManager.argument("info", StringArgumentType.string())
							.then(ClientCommandManager.argument("reason", StringArgumentType.string())
							.executes(commandContext -> {
								if (!ATLAS_SUGGEST_UTILS.CreateSuggestion(commandContext))
									return 0;

								return 1;
					})))))))));
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("atlas-util")
					.then(ClientCommandManager.literal("request")
							.then(ClientCommandManager.literal("get")
									// reviewed then id
									.then(ClientCommandManager.argument("reviewed", BoolArgumentType.bool())
											.then(ClientCommandManager.argument("id", IntegerArgumentType.integer())
													.executes(commandContext -> {
														if (!ATLAS_SUGGEST_UTILS.GetSuggestions(commandContext, true, true))
															return 0;

														return 1;
													})
											)
											.executes(commandContext -> {
												if (!ATLAS_SUGGEST_UTILS.GetSuggestions(commandContext, true, false))
													return 0;

												return 1;
											})
									)
									// id then reviewed (optional)
									.then(ClientCommandManager.argument("id", IntegerArgumentType.integer())
											.then(ClientCommandManager.argument("reviewed", BoolArgumentType.bool())
													.executes(commandContext -> {
														if (!ATLAS_SUGGEST_UTILS.GetSuggestions(commandContext, true, true))
															return 0;

														return 1;
													})
											)
											.executes(commandContext -> {
												if (!ATLAS_SUGGEST_UTILS.GetSuggestions(commandContext, false, true))
													return 0;

												return 1;
											})
									)
									// No args
									.executes(commandContext -> {
										if (!ATLAS_SUGGEST_UTILS.GetSuggestions(commandContext, false, false))
											return 0;

										return 1;
									})
							)
					)
			);
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("atlas-util")
					.then(ClientCommandManager.literal("request")
							.then(ClientCommandManager.literal("edit")
									.then(ClientCommandManager.argument("id", IntegerArgumentType.integer())
									.then(ClientCommandManager.argument("name", StringArgumentType.string())
									.then(ClientCommandManager.argument("dial", StringArgumentType.string())
									.then(ClientCommandManager.argument("type", StringArgumentType.word())
									.then(ClientCommandManager.argument("info", StringArgumentType.string())
									.then(ClientCommandManager.argument("reason", StringArgumentType.string())
											.executes(commandContext -> {
												if (!ATLAS_SUGGEST_UTILS.EditSuggestion(commandContext))
													return 0;

												return 1;
											}))))))))));
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