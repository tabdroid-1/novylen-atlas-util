package com.tabdroid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;


import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.tabdroid.common.AtlasPackets.*;
import static com.tabdroid.common.AtlasCommon.*;


public class AtlasSearchUtils {

    public AtlasSearchUtils()
    {
    }

    public void QueryPOIs(CommandContext<FabricClientCommandSource> context) {

        if (!IsInNovylenSMP(context))
            return;

        String query = StringArgumentType.getString(context, "query");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://novy.pigwin.eu/api/search.php?q=" + query + "&a=20&w=both"))
                .GET()
                .build();

        GetHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    ObjectMapper mapper = new ObjectMapper();

                    try {
                        QuerySearchApiResponse parsed_response = mapper.readValue(response, QuerySearchApiResponse.class);

                        context.getSource().sendFeedback(Text.literal("[Atlas Util] Found POIs:"));
                        for (QuerySearchApiResponse.DataEntry entry : parsed_response.data) {
                            context.getSource().sendFeedback(Text.literal(
                                    "Name: '" + entry.name + "' Dial(s): '" + entry.dial + "' Type: " + entry.markerName + " Position: " + entry.x + " / " + entry.z
                            ));
                        }

                    } catch (JsonProcessingException e) {
                        context.getSource().sendFeedback(Text.literal("[Atlas Util] Failed to parse response."));
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    };

    public void GetPOIsNearBy(CommandContext<FabricClientCommandSource> context) {

        if (!IsInNovylenSMP(context))
            return;

        BlockPos position = context.getSource().getClient().player.getBlockPos();
        Integer radius = IntegerArgumentType.getInteger(context, "radius");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://novy.pigwin.eu/api/cords.php?x=" + position.getX() + "&z=" + position.getZ() + "&r=" + radius + "&a=5&d=0"))
                .GET()
                .build();

        GetHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        NearbySearchApiResponse parsed_response = mapper.readValue(response, NearbySearchApiResponse.class);

                        context.getSource().sendFeedback(Text.literal("[Atlas Util] Near POIs:"));
                        for (NearbySearchApiResponse.DataEntry entry : parsed_response.data) {
                            context.getSource().sendFeedback(Text.literal(
                                    "Name: '" + entry.name + "' Dial(s): '" + entry.dial + "' Type: " + entry.markerName + " Position: " + entry.x + " / " + entry.z
                            ));
                        }

                    } catch (JsonProcessingException e) {
                        context.getSource().sendFeedback(Text.literal("[Atlas Util] Failed to parse response."));
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    };
}
