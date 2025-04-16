package com.tabdroid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class AtlasCommon {
    public static class ModRequestData {
        public boolean m_ActiveRequest = false;
        public boolean m_ReviewedRequest = false;
        public boolean m_ConfirmedRequest = false;
        public String m_Name = "null";
        public PointType m_Type = new PointType();
        public String m_Info = "null";
        public BlockPos m_Position = new BlockPos(0, 0, 0);

        public void Clear()
        {
            m_ActiveRequest = false;
            m_ReviewedRequest = false;
            m_ConfirmedRequest = false;
            m_Name = "null";
            m_Type = new PointType();
            m_Info = "null";
            m_Position = new BlockPos(0, 0, 0);
        }

        public boolean CanConfirm()
        {
            if (!IsRequestValid()) return false;
            return m_ReviewedRequest;
        }

        public boolean CanSend()
        {
            if (!IsRequestValid()) return false;
            if (!m_ReviewedRequest) return false;
            return m_ConfirmedRequest;
        }

        public boolean IsRequestValid()
        {
            if (m_Name.equals("null")) return false;
            if (!m_Type.isValid()) return false;
            return !m_Info.equals("null");
        }

    }


    public static class PointType {
        private enum PointTypeEnum {
            Build,
            Farm,
            Shop,
            Station,
            Error
        };

        final private PointTypeEnum m_PointTypeEnum;

        public PointType()
        {
            m_PointTypeEnum = PointTypeEnum.Error;
        }

        public PointType(String type)
        {
            switch (type.toUpperCase()) {
                case "BUILD" -> m_PointTypeEnum = PointTypeEnum.Build;
                case "FARM" -> m_PointTypeEnum = PointTypeEnum.Farm;
                case "SHOP" -> m_PointTypeEnum = PointTypeEnum.Shop;
                case "STATION" -> m_PointTypeEnum = PointTypeEnum.Station;
                default -> m_PointTypeEnum = PointTypeEnum.Error;
            };
        }

        public boolean isValid() {
            return !m_PointTypeEnum.equals(PointTypeEnum.Error);
        }

        public static String toString(PointType type)
        {
            return switch (type.m_PointTypeEnum) {
                case PointTypeEnum.Build -> "Build";
                case PointTypeEnum.Farm -> "Farm";
                case PointTypeEnum.Shop -> "Shop";
                case PointTypeEnum.Station -> "Station";
                default -> "Error";
            };
        }
    }

    public static boolean isInNovylenSMP(CommandContext<FabricClientCommandSource> context){
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

    public static class NearbySearchApiResponse {
        public int x;
        public int z;
        public int r;
        public int a;
        public int d;
        public List<DataEntry> data;

        public static class DataEntry {
            public String id;
            public String name;
            public String dial;
            public String x;
            public String z;

            @JsonProperty("marker_name")
            public String markerName;

            public String info;
            public String distance;
        }
    }

    public static class QuerySearchApiResponse {
        public String q;
        public int a;
        public String w;
        public int d;
        public List<DataEntry> data;

        public static class DataEntry {
            public String id;
            public String name;
            public String dial;
            public String x;
            public String z;

            @JsonProperty("marker_name")
            public String markerName;
            public String info;
        }
    }

}
