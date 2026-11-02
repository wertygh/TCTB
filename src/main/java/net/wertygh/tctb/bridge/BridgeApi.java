package net.wertygh.tctb.bridge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.wertygh.tctb.bridge.runtime.TetraAbilityBridge;
import net.wertygh.tctb.bridge.runtime.TetraBowFireHandler;
import se.mickelus.tetra.module.ItemModule;
import slimeknights.tconstruct.library.tools.item.IModifiable;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class BridgeApi {
    public static void pushSlotContext(@Nullable String slot) {
        TConSlotBridge.pushSlotContext(slot);
    }

    public static void popSlotContext() {
        TConSlotBridge.popSlotContext();
    }

    public static void setSlotContext(@Nullable String slot) {
        TConSlotBridge.setSlotContext(slot);
    }

    @Nullable
    public static String getSlotContext() {
        return TConSlotBridge.getSlotContext();
    }

    public static <T> T withSlotContext(@Nullable String slot, Supplier<T> action) {
        return TConSlotBridge.withSlotContext(slot, action);
    }

    public static String getBaseSlotKey(String slotKey) {
        return TConSlotBridge.getBaseSlotKey(slotKey);
    }

    public static String getModuleKey(ItemStack stack, String slotKey) {
        return TConSlotBridge.getModuleKey(stack, slotKey);
    }

    public static void setModule(ItemStack stack, String slotKey, String moduleKey, String variantKey) {
        TConSlotBridge.setModule(stack, slotKey, moduleKey, variantKey);
    }

    public static void removeModule(ItemStack stack, String slotKey) {
        TConSlotBridge.removeModule(stack, slotKey);
    }

    public static String getVariant(ItemStack stack, String slotKey, String moduleKey) {
        return TConSlotBridge.getVariant(stack, slotKey, moduleKey);
    }

    public static int getImprovementLevel(ItemStack stack, String slotKey, String improvementKey) {
        return TConSlotBridge.getImprovementLevel(stack, slotKey, improvementKey);
    }

    public static void setImprovement(ItemStack stack, String slotKey, String improvementKey, int level) {
        TConSlotBridge.setImprovement(stack, slotKey, improvementKey, level);
    }

    public static void removeImprovement(ItemStack stack, String slotKey, String improvementKey) {
        TConSlotBridge.removeImprovement(stack, slotKey, improvementKey);
    }

    public static void removeSlotImprovements(ItemStack stack, String slotKey) {
        TConSlotBridge.removeSlotImprovements(stack, slotKey);
    }

    @Nullable
    public static ItemModule findInstalledModule(String slotKey, ItemStack stack) {
        return TConSlotBridge.findInstalledModule(slotKey, stack);
    }

    public static void assembleHybrid(Item item, ItemStack stack, @Nullable Level world, float severity) {
        TConSlotBridge.assembleHybrid(item, stack, world, severity);
    }

    public static boolean isTConTool(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof IModifiable;
    }

    public static boolean isBridgeMelee(ItemStack stack) {
        return TetraAbilityBridge.isBridgeMelee(stack);
    }

    public static boolean hasTetraInteraction(ItemStack stack) {
        return TetraAbilityBridge.hasTetraInteraction(stack);
    }

    public static void pushTetraBreak() {
        TetraAbilityBridge.pushTetraBreak();
    }

    public static void popTetraBreak() {
        TetraAbilityBridge.popTetraBreak();
    }

    public static boolean isTetraBreaking() {
        return TetraAbilityBridge.isTetraBreaking();
    }

    public static InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return TetraAbilityBridge.use(level, player, hand);
    }

    public static int getUseDuration(ItemStack stack) {
        return TetraAbilityBridge.getUseDuration(stack);
    }

    public static UseAnim getUseAnimation(ItemStack stack) {
        return TetraAbilityBridge.getUseAnimation(stack);
    }

    public static void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        TetraAbilityBridge.releaseUsing(stack, level, entity, timeLeft);
    }

    public static void mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        TetraAbilityBridge.mineBlock(stack, level, state, pos, entity);
    }
    
    public static void handleRelease(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        TetraBowFireHandler.handleRelease(stack, level, entity, timeLeft);
    }
    
    public static boolean isBridged(ItemStack stack) {
        return TetraBowFireHandler.isBridged(stack);
    }

    public static void handleUseTick(Level level, LivingEntity living, ItemStack stack, int chargeRemaining) {
        TetraBowFireHandler.handleUseTick(level, living, stack, chargeRemaining);
    }
}