package com.github.basdxz.journeymappartyhelper.util;

import com.google.common.collect.Iterables;
import journeymap.client.model.Waypoint;
import journeymap.client.waypoint.WaypointStore;

import java.util.ArrayList;

public class WaypointHelper {
    public static String waypointToString(Waypoint wp) {
        //Format ends up as WP[playerName gamer, 0, 50, 0, [0], Normal, 255, 0, 0]
        return "[" + wp.getName() + ", " + wp.getX() + ", " + wp.getY() + ", " + wp.getZ() + ", " +
                wp.getDimensions() + ", " + wp.getType() + ", " + wp.getR() + ", " + wp.getG() + ", " + wp.getB() + "]";
    }

    public static void addNewWaypoint(String name, int x, int y, int z, Waypoint.Type type, int red, int green, int blue, ArrayList<Integer> dims) {
        Waypoint wp = new Waypoint(name, x, y, z, false, red, green, blue, type, Waypoint.Origin.JourneyMap, Iterables.get(dims, 0), dims);
        WaypointStore.instance().save(wp);
    }
}
