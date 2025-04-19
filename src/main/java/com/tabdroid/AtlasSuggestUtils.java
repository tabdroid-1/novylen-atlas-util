package com.tabdroid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import com.mojang.brigadier.context.CommandContext;
import com.tabdroid.common.AtlasMarker;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

import static com.tabdroid.common.AtlasCommon.*;
import static com.tabdroid.common.AtlasPackets.*;
import static com.tabdroid.common.AtlasMarker.*;



public class AtlasSuggestUtils {

    public boolean CreateSuggestion(CommandContext<FabricClientCommandSource> context)
    {
        if (!IsInNovylenSMP(context))
            return false;

        if (!HasApiKey(context))
            return false;

        UserConfig config = AutoConfig.getConfigHolder(UserConfig.class).getConfig();

        BlockPos position = context.getSource().getClient().player.getBlockPos();
        String name = StringArgumentType.getString(context, "name");
        String dial = StringArgumentType.getString(context, "dial");
        Marker marker = new Marker(StringArgumentType.getString(context, "type"));
        String info = StringArgumentType.getString(context, "info");
        String reason = StringArgumentType.getString(context, "reason");

        if (!marker.IsValid())
        {
            context.getSource().sendFeedback(Text.literal("[Atlas Util] Invalid point type."));
            return false;
        }


        SuggestPayload suggest = new SuggestPayload();
        suggest.name = name;
        suggest.dial = dial;
        suggest.marker = marker.GetID();
        suggest.info = info;
        suggest.reason = reason;
        suggest.x = position.getX();
        suggest.z = position.getZ();

        if (!suggest.IsRequestValid())
        {
            context.getSource().sendFeedback(Text.literal("[Atlas Util] Submitted request is invalid."));
            return false;
        }

        ObjectMapper mapper = new ObjectMapper();
        String json;

        try {
            json = mapper.writeValueAsString(suggest);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://novy.pigwin.eu/api/suggest.php?key=" + config.user_api))
                .GET()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        GetHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    if (response.equals("{\"status\":\"ok\"}"))
                        context.getSource().sendFeedback(Text.literal("[Atlas Util] Request has been sent. " +  response));
                    else
                        context.getSource().sendFeedback(Text.literal("[Atlas Util] Failed to send request." + response));

                })
                .exceptionally(e -> {
                    context.getSource().sendFeedback(Text.literal("[Atlas Util] Failed to send request."));
                    e.printStackTrace();
                    return null;
                });


        context.getSource().sendFeedback(Text.literal("[Atlas Util] Request has been created."));

        return true;
    };

    public boolean GetSuggestions(CommandContext<FabricClientCommandSource> context, boolean has_argument_reviewed, boolean has_argument_id)
    {
        if (!IsInNovylenSMP(context))
            return false;

        if (!HasApiKey(context))
            return false;

        UserConfig config = AutoConfig.getConfigHolder(UserConfig.class).getConfig();
        String link = "https://novy.pigwin.eu/api/get_suggestions.php";
        Integer reviewed = null;
        Integer id = null;

        if (has_argument_id)
        {
            id = IntegerArgumentType.getInteger(context, "id");
            link += "?id=" + id;
        }
        if (has_argument_reviewed)
        {
            reviewed = BoolArgumentType.getBool(context, "reviewed") ? 1 : 0;
            if (has_argument_id)
                link += "&reviewed=" + reviewed;
            else
                link += "?reviewed=" + reviewed;
        }

        if(has_argument_reviewed || has_argument_id)
            link += "&key=" + config.user_api;
        else
            link += "?key=" + config.user_api;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(link))
                .GET()
                .build();

        GetHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    ObjectMapper mapper = new ObjectMapper();

                    if (!response.equals("{\"error\":\"No suggestions found\"}")) {
                        try {
                            SuggestsApiResponse parsed_response = mapper.readValue(response, SuggestsApiResponse.class);
                            context.getSource().sendFeedback(Text.literal("[Atlas Util] Request has been received. "));
                            for (var suggestion : parsed_response.suggestions) {
                                context.getSource().sendFeedback(Text.literal("Suggestion " + suggestion.name + " with id " + suggestion.id));
                                context.getSource().sendFeedback(Text.literal("    Dial: " + suggestion.dial));
                                context.getSource().sendFeedback(Text.literal("    Position: (x: " + suggestion.x + "  z: " + suggestion.z + ")"));
                                context.getSource().sendFeedback(Text.literal("    Marker: " + new Marker(suggestion.marker).GetName()));
                                context.getSource().sendFeedback(Text.literal("    Info: " + suggestion.info));
                                context.getSource().sendFeedback(Text.literal("    Reason: " + suggestion.reason));
                                context.getSource().sendFeedback(Text.literal("    Action: " + suggestion.action));
                                context.getSource().sendFeedback(Text.literal("    Source: " + suggestion.source));
                                context.getSource().sendFeedback(Text.literal("    Created at: " + suggestion.created_at));
                                context.getSource().sendFeedback(Text.literal("    Reviewed: " + (suggestion.reviewed != 0)));
                                context.getSource().sendFeedback(Text.literal("    Reviewed by: " + (suggestion.reviewed_by != null ? suggestion.reviewed_by : "No one")));
                                context.getSource().sendFeedback(Text.literal("    Reviewed at: " + (suggestion.reviewed_at != null ? suggestion.reviewed_at : "Never")));
                                context.getSource().sendFeedback(Text.literal("    Admin action: " + (suggestion.admin_action != null ? suggestion.admin_action : "Nothing")));
                                context.getSource().sendFeedback(Text.literal("    Admin notes: " + (suggestion.admin_notes != null ? suggestion.admin_notes : "None")));

                            }

                        } catch (JsonProcessingException e) {
                            context.getSource().sendFeedback(Text.literal("[Atlas Util] Failed to parse response." + response));
                        }
                    }
                    else
                        context.getSource().sendFeedback(Text.literal("[Atlas Util] Failed to send request." + response));

                })
                .exceptionally(e -> {
                    context.getSource().sendFeedback(Text.literal("[Atlas Util] Failed to send request."));
                    e.printStackTrace();
                    return null;
                });

        return true;
    }

    public boolean EditSuggestion(CommandContext<FabricClientCommandSource> context)
    {
        if (!IsInNovylenSMP(context))
            return false;

        if (!HasApiKey(context))
            return false;

        UserConfig config = AutoConfig.getConfigHolder(UserConfig.class).getConfig();

        BlockPos position = context.getSource().getClient().player.getBlockPos();
        int id = IntegerArgumentType.getInteger(context, "id");
        String name = StringArgumentType.getString(context, "name");
        String dial = StringArgumentType.getString(context, "dial");

        Marker marker = StringArgumentType.getString(context, "type").equalsIgnoreCase("NULL") ? new Marker() : new Marker(StringArgumentType.getString(context, "type"));
        String info = StringArgumentType.getString(context, "info");
        String reason = StringArgumentType.getString(context, "reason");


        String json = "{";
        json += "\"sugg_id\": " + id;
        if (!name.equalsIgnoreCase("null"))
            json += ",\"name\": \"" + name + "\"";
        if (!dial.equalsIgnoreCase("null"))
            json += ",\"dial\": \"" + dial + "\"";
        json += ",\"x\": " + position.getX();
        json += ",\"z\": " + position.getZ();
        if (marker.GetName() != null)
            json += ",\"marker\": \"" + marker.GetID() + "\"";
        if (!info.equalsIgnoreCase("null"))
            json += ",\"info\": \"" + info + "\"";
        if (!reason.equalsIgnoreCase("null"))
            json += ",\"reason\": \"" + reason + "\"";
        json += "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://novy.pigwin.eu/api/edit_suggestion.php?key=" + config.user_api))
                .GET()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        String finalJson = json;
        GetHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    if (response.equals("{\"success\":\"Suggestion updated successfully\"}"))
                        context.getSource().sendFeedback(Text.literal("[Atlas Util] Request has been sent. " + response));
                    else
                        context.getSource().sendFeedback(Text.literal("[Atlas Util] Failed to send request." + response));

                })
                .exceptionally(e -> {
                    context.getSource().sendFeedback(Text.literal("[Atlas Util] Failed to send request."));
                    e.printStackTrace();
                    return null;
                });


        context.getSource().sendFeedback(Text.literal("[Atlas Util] Request has been created."));

        return true;
    };

}
