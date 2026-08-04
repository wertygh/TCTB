package net.wertygh.tctb.bridge;

import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.item.IModifiable;

public class TConToolPredicate {
    public static boolean isTConTool(ItemStack stack) {
        return stack != null
            && !stack.isEmpty()
            && stack.getItem() instanceof IModifiable;
    }
}
