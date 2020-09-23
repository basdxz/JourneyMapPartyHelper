package com.github.basdxz.journeymappartyhelper.commands;

import com.github.basdxz.journeymappartyhelper.model.ChatFriendlyWaypoint;
import journeymap.client.model.Waypoint;
import journeymap.client.waypoint.WaypointStore;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ShareWaypoint extends CommandBase {
    private static final Pattern REG_EX_SPACES = Pattern.compile("\\s+"); //TODO see ClientChatHandler Pattern comment
    private final ArrayList<String> aliases = new ArrayList<>();

    public ShareWaypoint() {
        aliases.add("shareWaypoint");
    }

    //TODO:  public int getRequiredPermissionLevel() is missing, this wont work in SMP

    @Override
    public String getCommandName() {
        return aliases.get(0);
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "/" + getCommandName() + " player waypointID";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender iCommandSender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender iCommandSender, String[] strings) {
        if (iCommandSender instanceof EntityClientPlayerMP) {
            if (strings.length < 2) {
                throw new SyntaxErrorException(getCommandUsage(iCommandSender));
            }
            String playerRecipient = strings[0];
            String waypointIDNoSpaces = strings[1];
            EntityClientPlayerMP playerClient = (EntityClientPlayerMP) iCommandSender;
            for (Waypoint waypoint : WaypointStore.instance().getAll()) {
                if (REG_EX_SPACES.matcher(waypoint.getId()).replaceAll("").equals(waypointIDNoSpaces)) {
                    ChatFriendlyWaypoint chatFriendlyWaypoint = new ChatFriendlyWaypoint(waypoint);
                    String out = chatFriendlyWaypoint.toChatString();
                    playerClient.sendChatMessage("./tell " + playerRecipient + " {" + out + "}"); //TODO: Use some kind of unique character to prevent acidental detection
                }
            }
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender iCommandSender, String[] strings) {
        List<String> outputList;
        //TODO switch with too few cases, better use if, elseif, else
        switch (strings.length) {
            case 1:
                //noinspection unchecked
                outputList = getListOfStringsMatchingLastWord(strings, getPlayers());
                break;
            case 2:
                //noinspection unchecked
                outputList = getListOfStringsFromIterableMatchingLastWord(strings,
                        ChatFriendlyWaypoint.getAllChatFriendlyWaypoints());
                break;
            default:
                outputList = null;
        }
        return outputList;
    }

    protected String[] getPlayers() {
        return MinecraftServer.getServer().getAllUsernames();
    }

    @Override
    public boolean isUsernameIndex(String[] strings, int index) {
        return index == 0;
    }
}
