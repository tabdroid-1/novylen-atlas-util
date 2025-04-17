package com.tabdroid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.tabdroid.AtlasCommon.*;

import com.mojang.brigadier.context.CommandContext;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.tabdroid.AtlasCommon.GetHttpClient;
import static com.tabdroid.AtlasCommon.isInNovylenSMP;


public class AtlasModUtils {

    private AtlasCommon.ModRequestData m_RequestData;

    public AtlasModUtils()
    {
        m_RequestData = new ModRequestData();
    }

    public boolean CreateRequest(CommandContext<FabricClientCommandSource> context)
    {
        if (!isInNovylenSMP(context))
            return false;

        BlockPos position = context.getSource().getClient().player.getBlockPos();
        String name = StringArgumentType.getString(context, "name");
        String dial = StringArgumentType.getString(context, "dial");
        PointType type = new PointType(StringArgumentType.getString(context, "type"));
        String info = StringArgumentType.getString(context, "info");
        String reason = StringArgumentType.getString(context, "reason");

        if (!type.IsValid())
        {
            context.getSource().sendFeedback(Text.literal("[Atlas Util] Invalid point type."));
            return false;
        }


        m_RequestData.m_Name = name;
        m_RequestData.m_Dial = dial;
        m_RequestData.m_Type = type;
        m_RequestData.m_Info = info;
        m_RequestData.m_Reason = reason;
        m_RequestData.m_Position = position;
        m_RequestData.m_ReviewedRequest = false;
        m_RequestData.m_ConfirmedRequest = false;
        m_RequestData.m_ActiveRequest = true;

        if (!m_RequestData.IsRequestValid())
        {
            context.getSource().sendFeedback(Text.literal("[Atlas Util] Submitted request is invalid."));
            ClearRequest(context);

            return false;
        }

        context.getSource().sendFeedback(Text.literal("[Atlas Util] Request has been created. It needs to be reviewed and sent by you."));

        return true;
    };

    public void ClearRequest(CommandContext<FabricClientCommandSource> context)
    {
        m_RequestData.Clear();
        context.getSource().sendFeedback(Text.literal("[Atlas Util] Request has been cleared."));
    };

    public void ReviewRequest(CommandContext<FabricClientCommandSource> context)
    {
        if (!m_RequestData.m_ActiveRequest)
        {
            context.getSource().sendFeedback(Text.literal("[Atlas Util] There is no active request."));
            return;
        }
        context.getSource().sendFeedback(Text.literal("[Atlas Util] -----------------------------------------------------"));
        context.getSource().sendFeedback(Text.literal("\nRequest Review:"));
        context.getSource().sendFeedback(Text.literal("    Name: " + m_RequestData.m_Name));
        context.getSource().sendFeedback(Text.literal("    Dial: " + m_RequestData.m_Dial));
        context.getSource().sendFeedback(Text.literal("    Type: " + m_RequestData.m_Type.ToString()));
        context.getSource().sendFeedback(Text.literal("    Info: " + m_RequestData.m_Info));
        context.getSource().sendFeedback(Text.literal("    Reason: " + m_RequestData.m_Reason));
        context.getSource().sendFeedback(Text.literal("    Position: " + m_RequestData.m_Position.getX() + " " + m_RequestData.m_Position.getY() + " " + m_RequestData.m_Position.getZ()));
        context.getSource().sendFeedback(Text.literal("[Atlas Util] To confirm request, call /atlas-util confirm-request."));

        m_RequestData.m_ReviewedRequest = true;

    };

    public void ConfirmRequest(CommandContext<FabricClientCommandSource> context)
    {
        if (!m_RequestData.CanConfirm())
        {
            context.getSource().sendFeedback(Text.literal("[Atlas Util] Request has not been reviewed yet."));
            return;
        }

        m_RequestData.m_ConfirmedRequest = true;
        context.getSource().sendFeedback(Text.literal("[Atlas Util] Request has been confirmed."));
    };

    public boolean SendRequest(CommandContext<FabricClientCommandSource> context)
    {
        if (!m_RequestData.CanSend())
        {
            context.getSource().sendFeedback(Text.literal("[Atlas Util] There is no confirmed request."));
            return false;
        }

        UserConfig config = AutoConfig.getConfigHolder(UserConfig.class).getConfig();

        if (config.user_api == null)
        {
            context.getSource().sendFeedback(Text.literal("[Atlas Util] Api key is null. Please add your api key to mod config."));
            return false;
        }

        RequestPacket request_packet = new RequestPacket();
        request_packet.name = m_RequestData.m_Name;
        request_packet.dial = m_RequestData.m_Dial;
        request_packet.x = m_RequestData.m_Position.getX();
        request_packet.z = m_RequestData.m_Position.getZ();
        request_packet.marker = m_RequestData.m_Type.ToMarkerInteger();
        request_packet.info = m_RequestData.m_Info;
        request_packet.reason = m_RequestData.m_Reason;
        request_packet.source = "novylen-atlas-util-fabric";

        ObjectMapper mapper = new ObjectMapper();
        String json;

        try {
            json = mapper.writeValueAsString(request_packet);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://novy.pigwin.eu/admin/api/mcsuggest.php?key=" + config.user_api))
                .GET()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        GetHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    if (response.equals("{\"status\":\"ok\"}"))
                        context.getSource().sendFeedback(Text.literal("[Atlas Util] Request has been sent.  " +  response));
                    else
                        context.getSource().sendFeedback(Text.literal("[Atlas Util] Failed to send request." + response));

                })
                .exceptionally(e -> {
                    context.getSource().sendFeedback(Text.literal("[Atlas Util] Failed to send request."));
                    e.printStackTrace();
                    return null;
                });


        m_RequestData.Clear();
        return true;
    };



}
