package com.github.basdxz.journeymappartyhelper;

import com.github.basdxz.journeymappartyhelper.commands.ShareWaypoint;
import com.github.basdxz.journeymappartyhelper.events.ClientChatHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

    @Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION)
public class JourneymapPartyHelper {
    public static final Logger LOGGER = LogManager.getLogger(Reference.MODID);

    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new ShareWaypoint());
        MinecraftForge.EVENT_BUS.register(new ClientChatHandler());
    }
}
