package net.wertygh.tctb.bridge.runtime;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import net.wertygh.tctb.bridge.data.TConBridgeTag;
import net.wertygh.tctb.bridge.data.TConSlotMapping;
import net.wertygh.tctb.bridge.stat.TConBowFusion;
import net.wertygh.tctb.config.TCTBConfig;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.ranged.BowAmmoModifierHook;
import slimeknights.tconstruct.library.tools.capability.EntityModifierCapability;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableBowItem;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/*
 * This code refers to the ModularBowItem implementation from the https://github.com/mickelus/tetra project
 */
public class TetraBowFireHandler {
    public static boolean isBridged(ItemStack stack) {
        if (stack.isEmpty() || !TConBridgeTag.hasBridgeTag(stack)) return false;
        if (!(stack.getItem() instanceof ModifiableBowItem)) return false;
        return TConSlotMapping.isRanged(ToolStack.from(stack));
    }

    public static void handleUseTick(Level level, LivingEntity entity, ItemStack stack, int chargeRemaining) {
        IModularItem item = (IModularItem) stack.getItem();
        TConBowFusion.FusedBow fused = TConBowFusion.fuse(stack, ToolStack.from(stack));
        int overbowed = item.getEffectLevel(stack, ItemEffect.overbowed);
        if (item.getEffectLevel(stack, ItemEffect.releaseLatch) > 0 && getProgress(fused, stack, entity) >= 1f) {
            entity.releaseUsingItem();
        } else if (overbowed > 0 && isOverdrawn(fused, entity, stack)) {
            entity.stopUsingItem();
            if (entity instanceof Player player) {
                player.getCooldowns().addCooldown(stack.getItem(), TCTBConfig.COMMON.bowOverbowedCooldownTicks.get());
            }
        }
    }

    public static void handleRelease(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        IModularItem item = (IModularItem) stack.getItem();
        TConBowFusion.FusedBow fused = TConBowFusion.fuse(stack, ToolStack.from(stack));
        if (item.getEffectLevel(stack, ItemEffect.overbowed) > 0 && isOverdrawn(fused, entity, stack)) {
            entity.stopUsingItem();
            if (entity instanceof Player player) {
                player.getCooldowns().addCooldown(stack.getItem(), TCTBConfig.COMMON.bowOverbowedCooldownTicks.get());
            }
            return;
        }

        fire(stack, level, entity, timeLeft);
    }

    private static boolean isOverdrawn(TConBowFusion.FusedBow fused, LivingEntity entity, ItemStack stack) {
        IModularItem item = (IModularItem) stack.getItem();
        int overbowed = item.getEffectLevel(stack, ItemEffect.overbowed);
        int limit = overbowed * TCTBConfig.COMMON.bowOverbowedLimitMultiplier.get()
                + Math.max(1, Math.round(fused.drawTicks));
        int usedTicks = TCTBConfig.COMMON.bowUseDuration.get() - entity.getUseItemRemainingTicks();
        return usedTicks > limit;
    }

    private static float getProgress(TConBowFusion.FusedBow fused, ItemStack stack, LivingEntity entity) {
        if (entity == null || entity.getUseItemRemainingTicks() <= 0 || !stack.equals(entity.getUseItem())) {
            return 0f;
        }

        int usedTicks = TCTBConfig.COMMON.bowUseDuration.get() - entity.getUseItemRemainingTicks();
        return usedTicks * 1f / Math.max(1f, fused.drawTicks);
    }

    private static float getArrowVelocity(int charge, float strength, boolean suspend) {
        float full = TCTBConfig.COMMON.bowFullChargeTicks.get().floatValue();
        float velocity = charge / full;
        velocity = (velocity * velocity + velocity * 2f) / 3f;
        velocity = Math.min(1f, velocity);
        if (TCTBConfig.COMMON.bowStrengthAffectsVelocity.get()) {
            float base = TCTBConfig.COMMON.bowStrengthVelocityBase.get().floatValue();
            float div = TCTBConfig.COMMON.bowStrengthVelocityDivisor.get().floatValue();
            velocity *= (float) Math.max(1d, 1d + (strength - base) / div);
        }
        if (suspend && charge >= TCTBConfig.COMMON.bowFullChargeTicks.get()) {
            velocity *= TCTBConfig.COMMON.bowSuspendVelocityMultiplier.get().floatValue();
        }
        return velocity;
    }

    private static void fire(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide) return;
        if (!(entity instanceof Player player)) return;
        ToolStack tool = ToolStack.from(stack);
        if (tool.isBroken()) return;
        TConBowFusion.FusedBow fused = TConBowFusion.fuse(stack, tool);
        IModularItem item = (IModularItem) stack.getItem();
        float full = TCTBConfig.COMMON.bowFullChargeTicks.get().floatValue();
        int drawProgress = Math.round(getProgress(fused, stack, entity) * full);
        drawProgress = ForgeEventFactory.onArrowLoose(stack, level, player, drawProgress, true);
        if (drawProgress < 0) return;
        float arrowVelocity = getArrowVelocity(drawProgress, fused.tetraStrength, fused.suspend) * fused.velocityFactor;
        if (arrowVelocity <= TCTBConfig.COMMON.bowMinArrowVelocity.get().floatValue()) return;
        int multishot = Math.max(1, fused.multishot);
        float multishotSpread = fused.multishotSpread;
        int piercing = item.getEffectLevel(stack, ItemEffect.piercing);
        int desired = Math.min(multishot, TCTBConfig.COMMON.bowMultishotMax.get());
        ItemStack ammo = BowAmmoModifierHook.consumeAmmo(
            tool,
            stack,
            entity,
            player,
            stack.getItem() instanceof ProjectileWeaponItem pwi
                    ? pwi.getAllSupportedProjectiles()
                    : ProjectileWeaponItem.ARROW_ONLY,
            desired
        );
        if (ammo.isEmpty()) {
            if (player.getAbilities().instabuild) {
                ammo = new ItemStack(Items.ARROW);
            } else {
                return;
            }
        }
        if (!(ammo.getItem() instanceof ArrowItem arrowItem)) return;
        int count = Math.max(1, Math.min(multishot, player.getAbilities().instabuild ? desired : ammo.getCount()));
        ModifierNBT modifiers = tool.getModifiers();
        float basePitch = player.getXRot();
        float baseYaw = player.getYRot();
        int primaryIndex = count / 2;
        for (int i = 0; i < count; i++) {
            AbstractArrow arrow = arrowItem.createArrow(level, ammo, player);
            float yaw = baseYaw - multishotSpread * (count - 1) / 2f + multishotSpread * i;
            arrow.shootFromRotation(
                player,
                basePitch,
                yaw,
                0f,
                arrowVelocity * TCTBConfig.COMMON.bowArrowVelocityScale.get().floatValue(),
                fused.inaccuracy
            );
            if (drawProgress >= TCTBConfig.COMMON.bowFullChargeTicks.get()) {
                arrow.setCritArrow(true);
            }
            arrow.setBaseDamage(arrow.getBaseDamage()
                    - TCTBConfig.COMMON.bowArrowBaseDamageOffset.get().floatValue()
                    + fused.projectileDamage);
            if (arrowVelocity > 1f) arrow.setBaseDamage(arrow.getBaseDamage() / arrowVelocity);
            if (piercing > 0) arrow.setPierceLevel((byte) piercing);
            if (fused.suspend && drawProgress >= TCTBConfig.COMMON.bowFullChargeTicks.get()) {
                arrow.setNoGravity(true);
            }
            if (player.getAbilities().instabuild) arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            EntityModifierCapability.getCapability(arrow).addModifiers(modifiers);
            ModDataNBT arrowData = PersistentDataCapability.getOrWarn(arrow);
            for (ModifierEntry entry : modifiers.getModifiers()) {
                entry.getHook(ModifierHooks.PROJECTILE_LAUNCH)
                        .onProjectileLaunch(tool, entry, player, ammo, arrow, arrow, arrowData, i == primaryIndex);
            }
            level.addFreshEntity(arrow);
        }
        ToolDamageUtil.damageAnimated(tool, count, entity, entity.getUsedItemHand());
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
            1f, 1f / (level.getRandom().nextFloat() * 0.4f + 1.2f) + arrowVelocity * 0.5f
        );
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
    }
}