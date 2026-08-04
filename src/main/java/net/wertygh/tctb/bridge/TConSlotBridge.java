package net.wertygh.tctb.bridge;

import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.data.ItemProperties;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Supplier;

public class TConSlotBridge {
    public static void pushSlotContext(@Nullable String slot) {
        TConSlotContext.pushSlotContext(slot);
    }

    public static void popSlotContext() {
        TConSlotContext.popSlotContext();
    }

    public static void setSlotContext(@Nullable String slot) {
        TConSlotContext.setSlotContext(slot);
    }

    @Nullable
    public static String getSlotContext() {
        return TConSlotContext.getSlotContext();
    }

    public static <T> T withSlotContext(@Nullable String slot, Supplier<T> action) {
        return TConSlotContext.withSlotContext(slot, action);
    }

    public static String getBaseSlotKey(String slotKey) {
        return TConSlotContext.getBaseSlotKey(slotKey);
    }

    public static String getModuleKey(ItemStack stack, String slotKey) {
        return TConModuleData.getModuleKey(stack, slotKey);
    }

    public static boolean hasModule(ItemStack stack, String slotKey) {
        return TConModuleData.hasModule(stack, slotKey);
    }

    public static void setModule(ItemStack stack, String slotKey, String moduleKey, String variantKey) {
        TConModuleData.setModule(stack, slotKey, moduleKey, variantKey);
    }

    public static void removeModule(ItemStack stack, String slotKey) {
        TConModuleData.removeModule(stack, slotKey);
    }

    public static String getVariant(ItemStack stack, String slotKey, String moduleKey) {
        return TConModuleData.getVariant(stack, slotKey, moduleKey);
    }

    public static int getImprovementLevel(ItemStack stack, String slotKey, String improvementKey) {
        return TConModuleData.getImprovementLevel(stack, slotKey, improvementKey);
    }

    public static void setImprovement(ItemStack stack, String slotKey, String improvementKey, int level) {
        TConModuleData.setImprovement(stack, slotKey, improvementKey, level);
    }

    public static void removeImprovement(ItemStack stack, String slotKey, String improvementKey) {
        TConModuleData.removeImprovement(stack, slotKey, improvementKey);
    }

    public static void removeSlotImprovements(ItemStack stack, String slotKey) {
        TConModuleData.removeSlotImprovements(stack, slotKey);
    }

    public static String[] getAllSlotKeys(Item item, ItemStack stack) {
        return TConModuleResolver.getAllSlotKeys(item, stack);
    }

    public static String[] getMajorKeys(Item item, ItemStack stack) {
        return TConModuleResolver.getMajorKeys(item, stack);
    }

    public static String[] getMinorKeys(Item item, ItemStack stack) {
        return TConModuleResolver.getMinorKeys(item, stack);
    }

    @Nullable
    public static ItemModule findProxyModule(String slotKey, ItemStack stack) {
        return TConModuleResolver.findProxyModule(slotKey, stack);
    }

    @Nullable
    public static ItemModule findInstalledModule(String slotKey, ItemStack stack) {
        return TConModuleResolver.findInstalledModule(slotKey, stack);
    }

    public static Collection<ItemModule> getAllProxyModules(Item item, ItemStack stack) {
        return TConModuleResolver.getAllProxyModules(item, stack);
    }

    public static ItemProperties getProperties(Item item, ItemStack stack) {
        return TConPropertyBuilder.getProperties(item, stack);
    }

    @Nullable
    public static Multimap<Attribute, AttributeModifier> getAttributeModifiers(Item item, ItemStack stack) {
        return TConPropertyBuilder.getAttributeModifiers(item, stack);
    }

    public static void assembleHybrid(Item item, ItemStack stack, @Nullable Level world, float severity) {
        TConAssembler.assembleHybrid(item, stack, world, severity);
    }

    public static int getHoneBase(Item item, ItemStack stack) {
        return TConHoneBridge.getHoneBase(item, stack);
    }

    public static int getHoneIntegrityMultiplier(Item item, ItemStack stack) {
        return TConHoneBridge.getHoneIntegrityMultiplier(item, stack);
    }

    public static boolean canGainHoneProgress(Item item, ItemStack stack) {
        return TConHoneBridge.canGainHoneProgress(item, stack);
    }

    public static int getHoningProgress(Item item, ItemStack stack) {
        return TConHoneBridge.getHoningProgress(item, stack);
    }

    public static int getHoningLimit(Item item, ItemStack stack) {
        return TConHoneBridge.getHoningLimit(item, stack);
    }

    public static void setHoningProgress(Item item, ItemStack stack, int value) {
        TConHoneBridge.setHoningProgress(item, stack, value);
    }

    public static void markBridgeTag(ItemStack stack) {
        TConBridgeTag.markBridgeTag(stack);
    }

    public static boolean hasBridgeTag(ItemStack stack) {
        return TConBridgeTag.hasBridgeTag(stack);
    }

    @Nullable
    public static CompoundTag getBridgeData(ItemStack stack) {
        return TConToolData.readBridgeData(stack);
    }

    public static boolean clampDamage(ItemStack stack, int damage) {
        if (damage < 0) {
            ToolStack.from(stack).setDamage(0);
            return true;
        }
        return false;
    }

    public static float getDestroySpeed(ItemStack stack, BlockState state, float original) {
        return TConMiningBridge.getDestroySpeed(stack, state, original);
    }

    public static boolean isCorrectToolForDrops(ItemStack stack, BlockState state, boolean original) {
        return TConMiningBridge.isCorrectToolForDrops(stack, state, original);
    }
}