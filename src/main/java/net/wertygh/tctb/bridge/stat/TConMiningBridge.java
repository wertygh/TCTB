package net.wertygh.tctb.bridge.stat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.ToolAction;
import net.wertygh.tctb.bridge.data.TConToolData;
import net.wertygh.tctb.config.TCTBConfig;
import se.mickelus.tetra.util.TierHelper;
import se.mickelus.tetra.util.ToolActionHelper;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public class TConMiningBridge {
    public static float getDestroySpeed(ItemStack stack, BlockState state, float original) {
        if (!isValidStack(stack)) return original;
        if (original > TCTBConfig.COMMON.miningBridgeThreshold.get().floatValue()) return original;
        if (ToolStack.from(stack).isBroken()) return original;
        if (!hasMatchingTool(stack, state, false)) return original;
        float speed = ToolStack.from(stack).getStats().get(ToolStats.MINING_SPEED);
        return Math.max(TCTBConfig.COMMON.minMiningSpeed.get().floatValue(), speed);
    }

    public static boolean isCorrectToolForDrops(ItemStack stack, BlockState state, boolean original) {
        if (original) return true;
        if (!isValidStack(stack)) return false;
        if (ToolStack.from(stack).isBroken()) return false;
        return hasMatchingTool(stack, state, true);
    }

    private static boolean isValidStack(ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && stack.getCount() == 1
                && TConToolData.isTConTool(stack.getItem(), stack);
    }

    private static boolean hasMatchingTool(ItemStack stack, BlockState state, boolean checkHarvestTier) {
        CompoundTag bridgeData = TConToolData.readBridgeData(stack);
        if (bridgeData == null || !bridgeData.contains("tools", Tag.TAG_COMPOUND)) return false;
        CompoundTag tools = bridgeData.getCompound("tools");
        for (String key : tools.getAllKeys()) {
            if (!tools.contains(key, Tag.TAG_INT)) continue;
            int level = tools.getInt(key);
            if (level <= 0) continue;
            ToolAction action = ToolAction.get(key);
            if (action == null || !ToolActionHelper.isEffectiveOn(action, state)) continue;
            if (!checkHarvestTier || !state.requiresCorrectToolForDrops()) return true;
            if (TierHelper.tiers.isEmpty()) continue;
            Tier tier = TierHelper.getTier(level - 1);
            if (tier != null && TierSortingRegistry.isCorrectTierForDrops(tier, state)) return true;
        }
        return false;
    }
}