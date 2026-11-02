package net.wertygh.tctb.bridge.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TConModuleData {
    public static String getModuleKey(ItemStack stack, String slotKey) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) return "";
        return stack.getTag().getString(slotKey);
    }

    public static boolean hasModule(ItemStack stack, String slotKey) {
        return !getModuleKey(stack, slotKey).isEmpty();
    }

    public static void setModule(ItemStack stack, String slotKey, String moduleKey, String variantKey) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(slotKey, moduleKey);
        tag.putString(slotKey + "_material", variantKey);
    }

    public static void removeModule(ItemStack stack, String slotKey) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) return;
        CompoundTag tag = stack.getTag();
        tag.remove(slotKey);
        tag.remove(slotKey + "_material");
    }

    public static String getVariant(ItemStack stack, String slotKey, String moduleKey) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) return "";
        CompoundTag tag = stack.getTag();
        String variant = tag.getString(slotKey + "_material");
        if (!variant.isEmpty()) return variant;
        return moduleKey == null ? "" : tag.getString(moduleKey + "_material");
    }

    public static int getImprovementLevel(ItemStack stack, String slotKey, String improvementKey) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) return -1;
        CompoundTag tag = stack.getTag();
        String key = slotKey + ":" + improvementKey;
        return tag.contains(key, Tag.TAG_INT) ? tag.getInt(key) : -1;
    }

    public static void setImprovement(ItemStack stack, String slotKey, String improvementKey, int level) {
        stack.getOrCreateTag().putInt(slotKey + ":" + improvementKey, level);
    }

    public static void removeImprovement(ItemStack stack, String slotKey, String improvementKey) {
        if (stack != null && !stack.isEmpty() && stack.hasTag()) {
            stack.getTag().remove(slotKey + ":" + improvementKey);
        }
    }

    public static void removeSlotImprovements(ItemStack stack, String slotKey) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) return;
        CompoundTag tag = stack.getTag();
        String prefix = slotKey + ":";
        List<String> toRemove = new ArrayList<>();
        for (String key : tag.getAllKeys()) {
            if (key.startsWith(prefix)) toRemove.add(key);
        }
        toRemove.forEach(tag::remove);
    }
}