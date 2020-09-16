package com.github.basdxz.journeymappartyhelper.things;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Since;
import journeymap.client.model.Waypoint;


import java.util.TreeSet;

public class ChatFriendlyWaypoint {
    public static final Gson GSON = (new GsonBuilder()).setVersion(2.0D).create();
    @Since(2.0D)
    protected String N;
    @Since(2.0D)
    protected int X;
    @Since(2.0D)
    protected int Y;
    @Since(2.0D)
    protected int Z;
    @Since(2.0D)
    protected int R;
    @Since(2.0D)
    protected int G;
    @Since(2.0D)
    protected int B;
    @Since(2.0D)
    protected byte E;
    @Since(2.0D)
    protected byte T;
    @Since(2.0D)
    protected TreeSet<Integer> D;

    public ChatFriendlyWaypoint(Waypoint waypoint) {
        this.N = waypoint.getName();
        this.X = waypoint.getX();
        this.Y = waypoint.getY();
        this.Z = waypoint.getZ();
        this.R = waypoint.getR();
        this.G = waypoint.getG();
        this.B = waypoint.getB();
        this.E = (byte) (waypoint.isEnable() ? 1 : 0);
        this.T = (byte) (waypoint.getType().equals(Waypoint.Type.Normal) ? 1 : 0);
        this.D = (TreeSet<Integer>) waypoint.getDimensions();
    }

    public String toString() {
        return GSON.toJson(this);
    }

    public static String toString(Waypoint waypoint){
        return new ChatFriendlyWaypoint(waypoint).toString();
    }

    public static ChatFriendlyWaypoint fromString(String json) {
        return GSON.fromJson(json, ChatFriendlyWaypoint.class);
    }

    public Waypoint getWaypoint() {
        boolean enable = E == 1;
        Waypoint.Type type = T == 1 ? Waypoint.Type.Normal : Waypoint.Type.Death;
        return new Waypoint(N, X, Y, Z, enable, R, G, B, type, Waypoint.Origin.JourneyMap, Iterables.get(D, 0), D);
    }

    public static Waypoint getWaypoint(String json) {
        return ChatFriendlyWaypoint.fromString(json).getWaypoint();
    }
}
