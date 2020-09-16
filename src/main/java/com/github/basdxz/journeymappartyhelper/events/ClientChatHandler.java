package com.github.basdxz.journeymappartyhelper.events;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import journeymap.client.waypoint.WaypointStore;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import static com.github.basdxz.journeymappartyhelper.things.ChatFriendlyWaypoint.getWaypoint;

public class ClientChatHandler {
    @SubscribeEvent
    public void onMessageReceived(ClientChatReceivedEvent event) {
        String message = event.message.getUnformattedText();
        int startIndex = message.lastIndexOf("{");
        int endIndex = message.lastIndexOf("}") + 1;
        message = message.substring(startIndex, endIndex);
        WaypointStore.instance().save(getWaypoint(message));
    }
}
