package net.wertygh.tctb.bridge.stat;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.wertygh.tctb.bridge.data.TConToolData;
import net.wertygh.tctb.config.TCTBConfig;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public class TConHoneBridge {
    private static final String HONING_KEY = "honing_progress";

    public static int getHoneBase(Item item, ItemStack stack) {
        ToolStack tool = TConToolData.getToolStack(item, stack);
        if (tool == null) return TCTBConfig.COMMON.honeMinBase.get();
        try {
            int tierLevel = tool.getStats().get(ToolStats.HARVEST_TIER).getLevel();
            return Math.max(TCTBConfig.COMMON.honeMinBase.get(),
                    tierLevel * TCTBConfig.COMMON.honeTierMultiplier.get());
        } catch (Exception e) {
            return TCTBConfig.COMMON.honeMinBase.get();
        }
    }

    public static int getHoneIntegrityMultiplier(Item item, ItemStack stack) {
        return TCTBConfig.COMMON.honeIntegrityMultiplier.get();
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