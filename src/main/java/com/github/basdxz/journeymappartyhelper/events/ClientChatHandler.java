package com.github.basdxz.journeymappartyhelper.events;

import com.github.basdxz.journeymappartyhelper.model.ChatFriendlyWaypoint;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import journeymap.client.waypoint.WaypointStore;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientChatHandler {
    private static final Pattern PATTERN = Pattern.compile("\\{(.?)}");

    @SubscribeEvent
    public void onMessageReceived(ClientChatReceivedEvent event) {
        String message = event.message.getFormattedText();

        List<String> list = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(message);
        while (matcher.find()) {
            list.add(matcher.group());
        }

        for (String waypointString : list) {
            ChatFriendlyWaypoint waypoint = ChatFriendlyWaypoint.fromChatString(waypointString);
            //The fromChatString method will set the dimentions to ERROR_DIM_ID if something goes wrong
            if ((int) waypoint.getDimensions().toArray()[0] != ChatFriendlyWaypoint.ERROR_DIM_ID) {
                WaypointStore.instance().save(waypoint);
                message = message.replace(waypointString, waypoint.toChatReadableString());
            }
        }
        event.message = new ChatComponentText(message);
    }
}
