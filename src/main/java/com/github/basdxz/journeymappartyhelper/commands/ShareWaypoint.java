package com.github.basdxz.journeymappartyhelper.commands;

import journeymap.client.waypoint.WaypointStore;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import java.util.ArrayList;
import java.util.List;

import static com.github.basdxz.journeymappartyhelper.util.WaypointHelper.waypointToString;

public class ShareWaypoint extends CommandBase {
    final private ArrayList<String> aliases = new ArrayList<>();
    public ShareWaypoint() {
        aliases.add("shareAllWaypoints");
    }

    @Override
    public String getCommandName() {
        return aliases.get(0);
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "/" + getCommandName();
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender iCommandSender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender iCommandSender, String[] strings) {
        if (iCommandSender instanceof EntityClientPlayerMP) {
            EntityClientPlayerMP playerClient = (EntityClientPlayerMP) iCommandSender;
            WaypointStore.instance().getAll().forEach(wp -> playerClient.sendChatMessage("/w Player712 " + waypointToString(wp)));
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender iCommandSender, String[] strings) {
        return aliases;
    }
}
