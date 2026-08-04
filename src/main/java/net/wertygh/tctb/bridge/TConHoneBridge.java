package net.wertygh.tctb.bridge;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public class TConHoneBridge {
    private static final String HONING_KEY = "honing_progress";

    public static int getHoneBase(Item item, ItemStack stack) {
        ToolStack tool = TConToolData.getToolStack(item, stack);
        if (tool == null) return 100;
        try {
            int tierLevel = tool.getStats().get(ToolStats.HARVEST_TIER).getLevel();
            return Math.max(10, tierLevel * 100);
        } catch (Exception e) {
            return 100;
        }
    }

    public static int getHoneIntegrityMultiplier(Item item, ItemStack stack) {
        return 1;
    }

    public static boolean canGainHoneProgress(Item item, ItemStack stack) {
        return TConToolData.isTConTool(item, stack);
    }

    public static int getHoningProgress(Item item, ItemStack stack) {
        return stack.getOrCreateTag().getInt(HONING_KEY);
    }

    public static int getHoningLimit(Item item, ItemStack stack) {
        return getHoneBase(item, stack);
    }

    public static void setHoningProgress(Item item, ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(HONING_KEY, Math.max(0, value));
    }
}