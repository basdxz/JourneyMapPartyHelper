package com.github.basdxz.journeymappartyhelper.events;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import journeymap.client.model.Waypoint;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.github.basdxz.journeymappartyhelper.util.WaypointHelper.addNewWaypoint;

public class ClientChatHandler {
    @SubscribeEvent
    public void onMessageReceived(ClientChatReceivedEvent event) {
        //Dirty, contains player name etc etc
        //[bruv, 500, 50, 0, [0], Normal, 255, 0, 0]
        //[playerName gamer, 0, 50, 0, [0], Normal, 255, 0, 0]

        String msg = event.message.getUnformattedText();
        List<Integer> startBracketIndex = findSubString(msg, "[");
        List<Integer> endBracketIndex = findSubString(msg, "]");
        if (startBracketIndex.size() < 2 || endBracketIndex.size() < 2){
            return;
        }

        int begin = startBracketIndex.get(startBracketIndex.size() - 2);
        int end = endBracketIndex.get(endBracketIndex.size() - 1);

        msg = msg.substring(begin + 1, end);
        if ((findSubString(msg,",").size() != 8)){
            return;
        }
        String[] msgSplit = msg.split("\\s*,\\s*");
        if (msgSplit.length != 9){
            return;
        }
        String wpName = msgSplit[0];
        int wpX = Integer.parseInt(msgSplit[1]);
        int wpY = Integer.parseInt(msgSplit[2]);
        int wpZ = Integer.parseInt(msgSplit[3]);

        String wpDimString = msgSplit[4];
        String[] wpDimListString = wpDimString.replace("[", "").replace("]", "").split("\\s*,\\s*");
        ArrayList<Integer> wpDimListInteger = new ArrayList<>();
        Arrays.stream(wpDimListString).forEach(s -> wpDimListInteger.add(Integer.parseInt(s)));

        String wpTypeString = msgSplit[5];
        Waypoint.Type wpType;
        if (wpTypeString.equals("Normal")){
            wpType = Waypoint.Type.Normal;
        } else if (wpTypeString.equals("Death")){
            wpType = Waypoint.Type.Death;
        } else {
            return;
        }

        Random rnd = new Random();
        int red = Integer.parseInt(msgSplit[6]);
        int green = Integer.parseInt(msgSplit[7]);
        int blue = Integer.parseInt(msgSplit[8]);
        addNewWaypoint(wpName, wpX, wpY, wpZ, wpType, red, green, blue, wpDimListInteger);
    }

    private List<Integer> findSubString(String source, String target) {
        int lastIndex = 0;
        List<Integer> result = new ArrayList<>();

        while (lastIndex != -1) {
            lastIndex = source.indexOf(target, lastIndex);
            if (lastIndex != -1) {
                result.add(lastIndex);
                lastIndex += 1;
            }
        }
        return result;
    }
}
