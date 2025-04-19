package com.tabdroid.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import com.tabdroid.common.AtlasMarker.*;

public class AtlasPackets {
    public static class MarkersApiResponse {
        public List<Marker> markers;

        public static class Marker {
            public int id;
            public String name;
            public String color;
        }
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

    public static class SuggestsApiResponse {
        public List<Suggest> suggestions;

        public static class Suggest {
            public int id;
            public String name;
            public String dial;
            public Integer x;
            public Integer z;
            public Integer marker;
            public String info;
            public String reason;
            public String action;
            public String source;
            public String suggested_by;
            public Integer is_admin;
            public String created_at;
            public Integer reviewed;
            public String reviewed_by;
            public String reviewed_at;
            public String admin_action;
            public String admin_notes;
        }
    }

    public static class SuggestPayload {
        public String name;
        public String dial;
        public int x;
        public int z;
        public int marker;
        public String info;
        public String reason;
        public final String source = "novylen-atlas-util-fabric";

        public boolean IsRequestValid()
        {
            Marker marker_ = new Marker(marker);
            if (name == null) return false;
            if (dial == null) return false;
            if (info == null) return false;
            if (reason == null) return false;
            return marker_.IsValid();
        }
    }
}
