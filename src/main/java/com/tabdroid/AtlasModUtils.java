package com.tabdroid;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.tabdroid.AtlasCommon.*;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

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
        PointType type = new PointType(StringArgumentType.getString(context, "type"));
        String info = StringArgumentType.getString(context, "info");

        if (!type.isValid())
        {
            context.getSource().sendFeedback(Text.literal("[Atlas Util] Invalid point type."));
            return false;
        }


        m_RequestData.m_Name = name;
        m_RequestData.m_Type = type;
        m_RequestData.m_Info = info;
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
        context.getSource().sendFeedback(Text.literal("    Type: " + m_RequestData.m_Type.toString()));
        context.getSource().sendFeedback(Text.literal("    Info: " + m_RequestData.m_Info));
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

        // TODO: Actually send the request.
        context.getSource().sendFeedback(Text.literal("[Atlas Util] Request has been sent."));

        m_RequestData.Clear();
        return true;
    };



}
