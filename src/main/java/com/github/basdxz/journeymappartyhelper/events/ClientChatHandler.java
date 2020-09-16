package com.github.basdxz.journeymappartyhelper.events;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import journeymap.client.waypoint.WaypointStore;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import static com.github.basdxz.journeymappartyhelper.things.ChatFriendlyWaypoint.getWaypointFromString;

public class ClientChatHandler {
    @SubscribeEvent
    public void onMessageReceived(ClientChatReceivedEvent event) {
        String message = event.message.getUnformattedText();
        int startIndex = message.lastIndexOf("{");
        int endIndex = message.lastIndexOf("}");
        if (startIndex == -1 || endIndex == -1 || endIndex < startIndex) return;
        message = message.substring(startIndex, endIndex + 1);
        WaypointStore.instance().save(getWaypointFromString(message));
    }
}
