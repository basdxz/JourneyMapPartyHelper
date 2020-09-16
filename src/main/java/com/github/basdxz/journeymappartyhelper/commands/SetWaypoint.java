package com.github.basdxz.journeymappartyhelper.commands;

import journeymap.client.waypoint.WaypointStore;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import java.util.ArrayList;
import java.util.List;

public class SetWaypoint extends CommandBase {
    final private ArrayList<String> aliases = new ArrayList<>();
    public SetWaypoint() {
        aliases.add("setWaypoint");
    }

    @Override
    public String getCommandName() {
        return aliases.get(0);
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "/" + getCommandName() + " xPos yPos zPos";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender iCommandSender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender iCommandSender, String[] strings) {
        if (iCommandSender instanceof EntityClientPlayerMP) {
            WaypointStore.instance().getAll().forEach(wp -> System.out.println(wp.getName()));
            if (strings.length < 3) {
                throw new SyntaxErrorException(getCommandUsage(iCommandSender));
            } else {
                //Waypoint waypoint = new Waypoint("playerName" + " " + "gamer",
                //        Integer.parseInt(strings[0]), Integer.parseInt(strings[1]), Integer.parseInt(strings[2]),
                //        Color.red, Waypoint.Type.Normal, 0);
                //WaypointStore.instance().add(waypoint);
                //WaypointStore.instance().save(waypoint);
                ArrayList<Integer> dims = new ArrayList<>();
                dims.add(0);
                dims.add(1);
                dims.add(-1);
                //addNewWaypoint("Test", 0, 0, 0, Waypoint.Type.Normal, 255, 0, 0, dims);
            }
        }

    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender iCommandSender, String[] strings) {
        return aliases;
    }
}
