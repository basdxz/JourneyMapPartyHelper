package com.github.basdxz.journeymappartyhelper.events;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import journeymap.client.model.Waypoint;
import journeymap.client.waypoint.WaypointStore;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import static com.github.basdxz.journeymappartyhelper.things.ChatFriendlyWaypoint.decompressJsonWaypoint;
import static com.github.basdxz.journeymappartyhelper.things.ChatFriendlyWaypoint.getWaypointFromString;

public class ClientChatHandler {
    @SubscribeEvent
    public void onMessageReceived(ClientChatReceivedEvent event) {
        String message = event.message.getFormattedText();
        //Checks that both { and } exist
        int startIndex = message.lastIndexOf("{");
        int endIndex = message.lastIndexOf("}");
        if (startIndex == -1 || endIndex == -1 || endIndex < startIndex) {
            return;
        }

        String waypointJSONCompressed;
        String waypointJSONUncompressed;
        Waypoint waypoint;
        try {
            waypointJSONCompressed = message.substring(startIndex, endIndex + 1);
            waypointJSONUncompressed = decompressJsonWaypoint(waypointJSONCompressed);
            waypoint = getWaypointFromString(waypointJSONUncompressed);
        } catch (Exception ignored) {
            return;
        }
        if (waypoint == null || waypointJSONUncompressed == null) {
            return;
        }
        WaypointStore.instance().save(waypoint);
        //Replaces the message to include the expanded Waypoint JSON
        message = message.replace(waypointJSONCompressed, waypointJSONUncompressed);
        event.message = new ChatComponentText(message);
    }
}
