package net.wertygh.tctb.bridge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class TConBridgeTag {
    private static final String BRIDGE_TAG = "tctb_bridged";

    public static void markBridgeTag(ItemStack stack) {
        stack.getOrCreateTag().putBoolean(BRIDGE_TAG, true);
    }

    public static boolean hasBridgeTag(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(BRIDGE_TAG);
    }
}