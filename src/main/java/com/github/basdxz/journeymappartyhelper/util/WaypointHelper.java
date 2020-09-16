package com.github.basdxz.journeymappartyhelper.util;

import com.github.basdxz.journeymappartyhelper.things.ChatFriendlyWaypoint;
import journeymap.client.waypoint.WaypointStore;

import java.util.ArrayList;
import java.util.List;

public class WaypointHelper {
    public static List<String> getAllChatFriendlyWaypoints() {
        List<String> chatFriendlyWaypoints = new ArrayList<>();
        WaypointStore.instance().getAll().forEach(waypoint -> {
            ChatFriendlyWaypoint chatFriendlyWaypoint = new ChatFriendlyWaypoint(waypoint);
            String chatFriendlyWaypointJson = chatFriendlyWaypoint.toString();
            if (chatFriendlyWaypointJson.length() <= 100) {
                chatFriendlyWaypoints.add(waypoint.getId().replaceAll("\\s+",""));
            }
        });
        return chatFriendlyWaypoints;
    }
}
