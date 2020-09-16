package com.github.basdxz.journeymappartyhelper.commands;

import com.github.basdxz.journeymappartyhelper.util.*;
import com.github.basdxz.journeymappartyhelper.things.ChatFriendlyWaypoint;
import journeymap.client.waypoint.WaypointStore;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

import static com.github.basdxz.journeymappartyhelper.util.WaypointHelper.getAllChatFriendlyWaypoints;

public class ShareWaypoint extends CommandBase {
    final private ArrayList<String> aliases = new ArrayList<>();

    public ShareWaypoint() {
        aliases.add("shareWaypoint");
    }

    @Override
    public String getCommandName() {
        return aliases.get(0);
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "/" + getCommandName() + " player waypoint";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender iCommandSender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender iCommandSender, String[] strings) {
        if (iCommandSender instanceof EntityClientPlayerMP) {
            if (strings.length < 3) {
                throw new SyntaxErrorException(getCommandUsage(iCommandSender));
            }
            EntityClientPlayerMP playerClient = (EntityClientPlayerMP) iCommandSender;
            WaypointStore.instance().getAll().forEach(
                    waypoint -> playerClient.sendChatMessage(ChatFriendlyWaypoint.toString(waypoint)));
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender iCommandSender, String[] strings) {
        switch (strings.length) {
            case 1:
                //noinspection unchecked
                return getListOfStringsMatchingLastWord(strings, this.getPlayers());
            case 2:
                //noinspection unchecked
                return getListOfStringsFromIterableMatchingLastWord(strings, getAllChatFriendlyWaypoints());
            default:
                return null;
        }
    }

    protected String[] getPlayers() {
        return MinecraftServer.getServer().getAllUsernames();
    }

    public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
        return p_82358_2_ == 0;
    }
}
