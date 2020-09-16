package com.github.basdxz.journeymappartyhelper.things;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import journeymap.client.model.Waypoint;
import journeymap.client.waypoint.WaypointStore;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

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
        WaypointStore.instance().getAll().forEach(waypoint -> chatFriendlyWaypoints.add(
                waypoint.getId().replaceAll("\\s+", "")));
        return chatFriendlyWaypoints;
    }

    public static String compressJsonWaypoint(String string) {
        System.out.println(string);
        //Honestly, this just tests if it's a long enough string
        try {
            string = string.substring(1, string.length() - 1);
        } catch (Exception ignored) {
            return null;
        }
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        //Adds one 0 byte at the end of my string, if the string length was odd. Stops the last character being mangled.
        //Since UTF-8 uses 8 bytes per character and UTF-16 wants 16, you need to have an even number of characters.
        if ((string.length() & 1) != 0) {
            bytes = ArrayUtils.add(bytes, (byte) 0);
        }
        //UTF_16LE or UTF_16BE are better than UTF_16 because they don't add trash at the start and end of my string.
        string = new String(bytes, StandardCharsets.UTF_16LE);
        return "{" + string + "}";
    }

    public static String compressJsonWaypoint(Waypoint waypoint) {
        return compressJsonWaypoint(waypointToString(waypoint));
    }

    public static String decompressJsonWaypoint(String string) {
        //Honestly, this just tests if it's a long enough string
        try {
            string = string.substring(1, string.length() - 1);
        } catch (Exception ignored) {
            return null;
        }
        byte[] bytes = string.getBytes(StandardCharsets.UTF_16LE);
        string = new String(bytes, StandardCharsets.UTF_8);
        return "{" + string + "}";
    }
}
