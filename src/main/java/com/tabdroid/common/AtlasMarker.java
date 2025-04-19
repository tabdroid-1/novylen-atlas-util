package com.tabdroid.common;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import java.io.Console;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

import static com.tabdroid.common.AtlasCommon.*;
import static com.tabdroid.common.AtlasPackets.*;

public class AtlasMarker {
    public static class Markers {
        private static HashMap<String, Integer> s_Types;

        public HashMap<String, Integer> GetMarkers() { return s_Types; }

        public static void FetchMarkers(Logger logger) {
            s_Types = new HashMap<>();

            HttpClient client = GetHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://novy.pigwin.eu/api/markers.php?"))
                    .GET()
                    .build();

            logger.info("Fetching markers...");

            GetHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(response -> {
                        ObjectMapper mapper = new ObjectMapper();

                        try {
                            MarkersApiResponse parsed_response = mapper.readValue(response, MarkersApiResponse.class);

                            for (MarkersApiResponse.Marker entry : parsed_response.markers) {
                                s_Types.put(entry.name.toUpperCase(), entry.id);
                                logger.info("Name: {}  ID: {}", entry.name, entry.id);
                            }
                            logger.info("Successfully fetched markers.");

                        } catch (JsonProcessingException e) {
                            logger.error("Failed to parse response. Commands will not be functional. Please report this to a Developer");
                        }
                    })
                    .exceptionally(e -> {
                        e.printStackTrace();
                        return null;
                    });

        };
    };

    public static class Marker {

        final private Integer m_MarkerID;
        final private String m_MarkerName;

        public Marker()
        {
            m_MarkerID = null;
            m_MarkerName = null;
        }

        public Marker(String type)
        {
            m_MarkerID = Markers.s_Types.get(type.toUpperCase());

            if (m_MarkerID != null)
                m_MarkerName = type;
            else
                m_MarkerName = null;
        }

        public Marker(int type)
        {
            String key = null;
            Integer value = null;

            for(var entry : Markers.s_Types.entrySet()) {
                key = entry.getKey();
                value = entry.getValue();

                if (value == type)
                    break;
            }

            m_MarkerName = key;
            m_MarkerID = value;

        }

        public boolean IsValid() {
            return m_MarkerName != null && m_MarkerID != null;
        }

        public String GetName()
        {
            return m_MarkerName;
        }

        public Integer GetID()
        {
            return m_MarkerID;
        }
    }

}
