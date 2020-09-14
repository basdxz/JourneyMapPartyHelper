package com.github.basdxz.journeymappartyhelper;

import cpw.mods.fml.common.Mod;
import journeymap.common.Journeymap;

import static com.github.basdxz.journeymappartyhelper.Reference.*;

@Mod(modid = MODID, name = Reference.NAME, version = VERSION)
public class JourneymapPartyHelper {
    static{
        System.out.println(Journeymap.MOD_ID);
    }
}
