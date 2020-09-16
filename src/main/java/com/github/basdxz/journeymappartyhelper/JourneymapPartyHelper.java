package com.github.basdxz.journeymappartyhelper;

import com.github.basdxz.journeymappartyhelper.commands.ShareWaypoint;
import com.github.basdxz.journeymappartyhelper.events.ClientChatHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;

import static com.github.basdxz.journeymappartyhelper.Reference.*;

@Mod(modid = MODID, name = Reference.NAME, version = VERSION)
public class JourneymapPartyHelper {
    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new ShareWaypoint());
        MinecraftForge.EVENT_BUS.register(new ClientChatHandler());
    }
}
