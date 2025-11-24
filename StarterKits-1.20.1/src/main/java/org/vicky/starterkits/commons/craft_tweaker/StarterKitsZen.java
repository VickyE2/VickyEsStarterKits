package org.vicky.starterkits.commons.craft_tweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import org.openzen.zencode.java.ZenCodeType;
import org.vicky.starterkits.commons.StarterKitsAPI;

@ZenRegister
@ZenCodeType.Name("mods.starterkits.StarterKits")
public class StarterKitsZen {
    @ZenCodeType.Method
    public static void canUseKit(KitPermissionScript newScript) {
        StarterKitsAPI.script = newScript;
    }
}