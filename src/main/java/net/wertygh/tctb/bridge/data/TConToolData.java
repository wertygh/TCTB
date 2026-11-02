package net.wertygh.tctb.bridge.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.IModDataView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import net.wertygh.tctb.modifier.TetraBridgeModifier;

import javax.annotation.Nullable;

public class TConToolData {
    public static boolean isTConTool(Item item, ItemStack stack) {
        return item instanceof IModifiable && stack != null && !stack.isEmpty();
    }

    @Nullable
    public static ToolStack getToolStack(Item item, ItemStack stack) {
        if (!isTConTool(item, stack)) return null;
        return ToolStack.from(stack);
    }

    @Nullable
    public static CompoundTag readBridgeData(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) return null;
        return readBridgeData(ToolStack.from(stack).getPersistentData());
    }

    @Nullable
    public static CompoundTag readBridgeData(IModDataView persData) {
        if (persData == null) return null;
        return persData.get(TetraBridgeModifier.DATA_KEY, (tag, name) -> tag.getCompound(name));
    }
}