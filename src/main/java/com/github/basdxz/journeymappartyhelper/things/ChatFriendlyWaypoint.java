package com.github.basdxz.journeymappartyhelper.things;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import journeymap.client.model.Waypoint;
import journeymap.client.waypoint.WaypointStore;

public class ChatFriendlyWaypoint {
    public static final Gson GSON = new GsonBuilder().create();
    private final String N;
    private final int X;
    private final int Y;
    private final int Z;
    private final byte E;
    private final byte T;
    private final TreeSet<Integer> D;

    public ChatFriendlyWaypoint(Waypoint waypoint) {
        this.N = waypoint.getName();
        this.X = waypoint.getX();
        this.Y = waypoint.getY();
        this.Z = waypoint.getZ();
        this.E = (byte) (waypoint.isEnable() ? 1 : 0);
        this.T = (byte) (waypoint.getType().equals(Waypoint.Type.Normal) ? 1 : 0);
        this.D = (TreeSet<Integer>) waypoint.getDimensions();
    }

    public String toString() {
        return GSON.toJson(this);
    }

    public Waypoint getWaypoint() {
        boolean enable = E == 1;
        Waypoint.Type type = T == 1 ? Waypoint.Type.Normal : Waypoint.Type.Death;
        return new Waypoint(N, X, Y, Z, enable, 255, 255, 255, type, Waypoint.Origin.JourneyMap, Iterables.get(D, 0), D);
    }

    public static String waypointToString(Waypoint waypoint){
        return new ChatFriendlyWaypoint(waypoint).toString();
    }

    public static ChatFriendlyWaypoint fromString(String json) {
        return GSON.fromJson(json, ChatFriendlyWaypoint.class);
    }

    public static Waypoint getWaypointFromString(String json) {
        return ChatFriendlyWaypoint.fromString(json).getWaypoint();
    }

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
