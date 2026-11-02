package net.wertygh.tctb.bridge.asm;

import net.wertygh.tctb.TCTBMod;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableLauncherItem;

public class TCTBHooks {
    public static boolean isModularHandheldLike(Object obj) {
        if (obj instanceof ItemModularHandheld) return true;
        if (obj instanceof ModifiableLauncherItem) return false;
        return obj instanceof IModifiable && obj instanceof IModularItem;
    }

    public static ItemModularHandheld castModularHandheld(Object obj) {
        if (obj == null) return null;
        if (obj instanceof ItemModularHandheld handheld) return handheld;
        if (obj instanceof ModifiableLauncherItem) {
            throw new ClassCastException(obj.getClass().getName() + " cannot be cast to ItemModularHandheld");
        }
        if (obj instanceof IModifiable && obj instanceof IModularItem) {
            return TCTBMod.PROXY_ITEM.get();
        }
        throw new ClassCastException(obj.getClass().getName() + " cannot be cast to ItemModularHandheld");
    }
}