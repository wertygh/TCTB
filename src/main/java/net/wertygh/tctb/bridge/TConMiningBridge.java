package net.wertygh.tctb.bridge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.util.ToolActionHelper;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public class TConMiningBridge {
    public static float getDestroySpeed(ItemStack stack, BlockState state, float original) {
        if (original > 1f) return original;
        if (!isEffectiveFor(stack, state)) return original;
        float speed = ToolStack.from(stack).getStats().get(ToolStats.MINING_SPEED);
        return Math.max(1, speed);
    }

    public static boolean isCorrectToolForDrops(ItemStack stack, BlockState state, boolean original) {
        if (original) return true;
        return isEffectiveFor(stack, state);
    }

    private static boolean isEffectiveFor(ItemStack stack, BlockState state) {
        CompoundTag bridgeData = TConToolData.readBridgeData(stack);
        if (bridgeData == null || !bridgeData.contains("tools", Tag.TAG_COMPOUND)) {
            return false;
        }
        CompoundTag tools = bridgeData.getCompound("tools");
        for (String key : tools.getAllKeys()) {
            ToolAction action = ToolAction.get(key);
            if (action != null && ToolActionHelper.isEffectiveOn(action, state)) {
                return true;
            }
        }
        return false;
    }
}