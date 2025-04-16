package com.tabdroid;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class CreatePOIRequest {

    public enum PointType {
        Build,
        Farm,
        Shop,
        Station,
        Error

    };

    private static boolean s_ActiveRequest = false;
    private static boolean s_ReviewedRequest = false;
    private static boolean s_ConfirmedRequest = false;
    private static String s_Name = "null";
    private static PointType s_Type = PointType.Error;
    private static String s_Info = "null";
    private static BlockPos s_Position = new BlockPos(0, 0, 0);

    public static void CreateRequest(CommandContext<FabricClientCommandSource> context, String name, PointType type, String info, BlockPos position)
    {
        s_Name = name;
        s_Type = type;
        s_Info = info;
        s_Position = position;
        s_ReviewedRequest = false;
        s_ConfirmedRequest = false;
        s_ActiveRequest = true;

        if (!IsRequestValid())
        {
            context.getSource().sendFeedback(Text.literal("[Atlas Util] Submitted request is invalid."));
            ClearRequest(context);

            return;
        }

        context.getSource().sendFeedback(Text.literal("[Atlas Util] Request has been created. It needs to be reviewed and sent by you."));
    };

    public static void ClearRequest(CommandContext<FabricClientCommandSource> context)
    {
        s_ActiveRequest = false;
        s_ReviewedRequest = false;
        s_ConfirmedRequest = false;
        s_Name = "null";
        s_Type = PointType.Error;
        s_Info = "null";
        s_Position = new BlockPos(0, 0, 0);
        context.getSource().sendFeedback(Text.literal("[Atlas Util] Request has been cleared."));
    };

    public static void ReviewRequest(CommandContext<FabricClientCommandSource> context)
    {
        if (!s_ActiveRequest)
        {
            context.getSource().sendFeedback(Text.literal("[Atlas Util] There is no active request."));
            return;
        }
        context.getSource().sendFeedback(Text.literal("[Atlas Util] -----------------------------------------------------"));
        context.getSource().sendFeedback(Text.literal("\nRequest Review:"));
        context.getSource().sendFeedback(Text.literal("    Name: " + s_Name));
        context.getSource().sendFeedback(Text.literal("    Type: " + PointTypeToString(s_Type)));
        context.getSource().sendFeedback(Text.literal("    Info: " + s_Info));
        context.getSource().sendFeedback(Text.literal("    Position: " + s_Position.getX() + " " + s_Position.getY() + " " + s_Position.getZ()));
        context.getSource().sendFeedback(Text.literal("[Atlas Util] To confirm request, call /atlas-util confirm-request."));

        s_ReviewedRequest = true;

    };

    public static void ConfirmRequest(CommandContext<FabricClientCommandSource> context)
    {
        if (!s_ReviewedRequest)
        {
            context.getSource().sendFeedback(Text.literal("[Atlas Util] Request has not been reviewed yet."));
            return;
        }

        s_ConfirmedRequest = true;
        context.getSource().sendFeedback(Text.literal("[Atlas Util] Request has been confirmed."));
    };

    public static boolean SendRequest(CommandContext<FabricClientCommandSource> context)
    {
        if (!s_ConfirmedRequest)
        {
            context.getSource().sendFeedback(Text.literal("[Atlas Util] There is no confirmed request."));
            return false;
        }

        if (!IsRequestValid())
        {
            context.getSource().sendFeedback(Text.literal("[Atlas Util] Submitted request is invalid."));
            return false;
        }

        // TODO: Actually send the request.
        context.getSource().sendFeedback(Text.literal("[Atlas Util] Request has been sent."));

        return true;
    };

    public static PointType PointTypeFromString(String type)
    {
        return switch (type.toUpperCase()) {
            case "BUILD" -> PointType.Build;
            case "FARM" -> PointType.Farm;
            case "SHOP" -> PointType.Shop;
            case "STATION" -> PointType.Station;
            default -> PointType.Error;
        };
    }

    public static String PointTypeToString(PointType type)
    {
        return switch (type) {
            case PointType.Build -> "Build";
            case PointType.Farm -> "Farm";
            case PointType.Shop -> "Shop";
            case PointType.Station -> "Station";
            default -> "Error";
        };
    }

    private static boolean IsRequestValid()
    {
        if (s_Name.equals("null")) return false;
        if (s_Type == PointType.Error) return false;
        if (s_Info.equals("null")) return false;

        return true;
    }

}
