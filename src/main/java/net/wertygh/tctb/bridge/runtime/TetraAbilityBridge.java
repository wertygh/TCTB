package net.wertygh.tctb.bridge.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.wertygh.tctb.TCTBMod;
import net.wertygh.tctb.bridge.data.TConBridgeTag;
import se.mickelus.tetra.effect.ChargedAbilityEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableLauncherItem;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class TetraAbilityBridge {
    public static final int CHARGE_DURATION = 72000;
    private static ChargedAbilityEffect[] ABILITIES;
    private static final ThreadLocal<Integer> BREAK_DEPTH = ThreadLocal.withInitial(() -> 0);

    public static ChargedAbilityEffect[] getAbilities() {
        if (ABILITIES == null) {
            try {
                Field field = ItemModularHandheld.class.getDeclaredField("abilities");
                field.setAccessible(true);
                ABILITIES = (ChargedAbilityEffect[]) field.get(null);
            } catch (Exception e) {
                ABILITIES = new ChargedAbilityEffect[0];
            }
        }
        return ABILITIES;
    }

    public static boolean isBridgeMelee(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!TConBridgeTag.hasBridgeTag(stack)) return false;
        if (!(stack.getItem() instanceof IModifiable)) return false;
        if (stack.getItem() instanceof ModifiableLauncherItem) return false;
        return stack.getItem() instanceof IModularItem;
    }

    public static boolean hasTetraInteraction(ItemStack stack) {
        if (!isBridgeMelee(stack)) return false;
        return TCTBMod.PROXY_ITEM.get().getUseDuration(stack) > 0;
    }

    public static boolean hasChargedAbility(ItemStack stack) {
        if (!isBridgeMelee(stack)) return false;
        TConHandheldProxy proxy = TCTBMod.PROXY_ITEM.get();
        for (ChargedAbilityEffect ability : getAbilities()) {
            if (ability.isAvailable(proxy, stack)) {
                return true;
            }
        }
        return false;
    }

    public static void pushTetraBreak() {
        BREAK_DEPTH.set(BREAK_DEPTH.get() + 1);
    }

    public static void popTetraBreak() {
        int depth = BREAK_DEPTH.get() - 1;
        if (depth <= 0) {
            BREAK_DEPTH.remove();
        } else {
            BREAK_DEPTH.set(depth);
        }
    }

    public static boolean isTetraBreaking() {
        return BREAK_DEPTH.get() > 0;
    }

    public static InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return TCTBMod.PROXY_ITEM.get().use(level, player, hand);
    }

    public static int getUseDuration(ItemStack stack) {
        return TCTBMod.PROXY_ITEM.get().getUseDuration(stack);
    }

    public static UseAnim getUseAnimation(ItemStack stack) {
        return TCTBMod.PROXY_ITEM.get().getUseAnimation(stack);
    }

    public static void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        TCTBMod.PROXY_ITEM.get().releaseUsing(stack, level, entity, timeLeft);
    }

    public static void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        TConHandheldProxy.withSuppressedDamage(() ->
                TCTBMod.PROXY_ITEM.get().hurtEnemy(stack, target, attacker));
    }

    public static void mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        IModularItem item = (IModularItem) stack.getItem();
        TConHandheldProxy.withSuppressedDamage(() ->
                TCTBMod.PROXY_ITEM.get().mineBlock(stack, level, state, pos, entity));
    }

    public static void handleChargedAbility(Player player, InteractionHand hand, @Nullable LivingEntity target, @Nullable BlockPos targetPos, @Nullable Vec3 hitVec, int ticksUsed) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isBridgeMelee(stack)) return;
        TConHandheldProxy proxy = TCTBMod.PROXY_ITEM.get();
        for (ChargedAbilityEffect ability : getAbilities()) {
            if (ability.canPerform(player, proxy, stack, target, targetPos, ticksUsed)) {
                ability.perform(player, hand, proxy, stack, target, targetPos, hitVec, ticksUsed);
                break;
            }
        }
        player.stopUsingItem();
    }
}