package com.github.basdxz.journeymappartyhelper.events;

import com.github.basdxz.journeymappartyhelper.things.ChatFriendlyWaypoint;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import journeymap.client.waypoint.WaypointStore;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class ClientChatHandler {
    @SubscribeEvent
    public void onMessageReceived(ClientChatReceivedEvent event) {
        String message = event.message.getFormattedText();
        //Checks that both { and } exist
        int startIndex = message.lastIndexOf("{");
        int endIndex = message.lastIndexOf("}");
        if (startIndex == -1 || endIndex == -1 || startIndex > endIndex) {
            return;
        }

        String waypointString;
        ChatFriendlyWaypoint waypoint = new ChatFriendlyWaypoint();
        waypointString = message.substring(startIndex, endIndex + 1);
        waypoint.fromChatFriendlyString(waypointString);
        WaypointStore.instance().save(waypoint);

        String waypointReadableString = "[Name: " + waypoint.getName() + " X:" + waypoint.getX() +
                " Y:" + waypoint.getY() + " Z:" + waypoint.getZ() + " D:" + waypoint.getDimensions().toArray()[0] + "]";
        message = message.replace(waypointString, waypointReadableString);
        event.message = new ChatComponentText(message);
    }
}
