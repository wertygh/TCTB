package net.wertygh.tctb.bridge;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.properties.TetraAttributes;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.ranged.BowAmmoModifierHook;
import slimeknights.tconstruct.library.tools.capability.EntityModifierCapability;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableBowItem;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/*
 * This code refers to the ModularBowItem implementation from the https://github.com/mickelus/tetra project
 */
public class TetraBowFireHandler {
    private static final double VELOCITY_FACTOR = 1 / 8d;
    private static final int USE_DURATION = 72000;

    public static boolean isBridged(ItemStack stack) {
        if (stack.isEmpty() || !TConBridgeTag.hasBridgeTag(stack)) return false;
        if (!(stack.getItem() instanceof ModifiableBowItem)) return false;
        return TConSlotMapping.isRanged(ToolStack.from(stack));
    }

    public static void handleUseTick(Level level, LivingEntity entity, ItemStack stack, int chargeRemaining) {
        IModularItem item = (IModularItem) stack.getItem();
        int overbowed = item.getEffectLevel(stack, ItemEffect.overbowed);

        if (item.getEffectLevel(stack, ItemEffect.releaseLatch) > 0 && getProgress(stack, entity) >= 1f) {
            entity.releaseUsingItem();
        } else if (overbowed > 0 && isOverdrawn(entity, stack)) {
            entity.stopUsingItem();
            if (entity instanceof Player player) {
                player.getCooldowns().addCooldown(stack.getItem(), 10);
            }
        }
    }

    public static void handleRelease(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        IModularItem item = (IModularItem) stack.getItem();
        if (item.getEffectLevel(stack, ItemEffect.overbowed) > 0 && isOverdrawn(entity, stack)) {
            entity.stopUsingItem();
            if (entity instanceof Player player) {
                player.getCooldowns().addCooldown(stack.getItem(), 10);
            }
            return;
        }
        fire(stack, level, entity, timeLeft);
    }

    private static boolean isOverdrawn(LivingEntity entity, ItemStack stack) {
        IModularItem item = (IModularItem) stack.getItem();
        int overbowed = item.getEffectLevel(stack, ItemEffect.overbowed);
        int limit = overbowed * 2 + getDrawDuration(stack);
        int usedTicks = USE_DURATION - entity.getUseItemRemainingTicks();
        return usedTicks > limit;
    }

    private static float getProgress(ItemStack stack, LivingEntity entity) {
        if (entity == null || entity.getUseItemRemainingTicks() <= 0 || !stack.equals(entity.getUseItem())) {
            return 0f;
        }
        int usedTicks = USE_DURATION - entity.getUseItemRemainingTicks();
        return usedTicks * 1f / Math.max(1, getDrawDuration(stack));
    }

    private static int getDrawDuration(ItemStack stack) {
        IModularItem item = (IModularItem) stack.getItem();
        double speed = item.getAttributeValue(stack, TetraAttributes.drawSpeed.get());
        return Math.max(1, (int) (20 * Math.max(0.05, speed)));
    }

    private static float getDrawStrength(ItemStack stack) {
        IModularItem item = (IModularItem) stack.getItem();
        return (float) item.getAttributeValue(stack, TetraAttributes.drawStrength.get());
    }

    private static float getArrowVelocity(int charge, float strength, float velocityBonus, boolean suspend) {
        float velocity = charge / 20f;
        velocity = (velocity * velocity + velocity * 2f) / 3f;
        velocity = Math.min(1f, velocity);
        velocity *= (float) Math.max(1d, 1d + (strength - 6d) * VELOCITY_FACTOR);
        if (suspend && charge >= 20) {
            velocity *= 2f;
        } else {
            velocity *= 1f + velocityBonus;
        }
        return velocity;
    }

    private static void fire(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;
        ToolStack tool = ToolStack.from(stack);
        if (tool.isBroken()) return;

        IModularItem item = (IModularItem) stack.getItem();
        int drawProgress = Math.round(getProgress(stack, entity) * 20f);
        drawProgress = ForgeEventFactory.onArrowLoose(stack, level, player, drawProgress, true);
        if (drawProgress < 0) return;
        float strength = getDrawStrength(stack);
        float velocityBonus = item.getEffectLevel(stack, ItemEffect.velocity) / 100f;
        boolean suspend = item.getEffectLevel(stack, ItemEffect.suspend) > 0;
        float arrowVelocity = getArrowVelocity(drawProgress, strength, velocityBonus, suspend);
        if (arrowVelocity <= 0.1f) return;
        int multishot = Math.max(1, item.getEffectLevel(stack, ItemEffect.multishot));
        float spread = item.getEffectEfficiency(stack, ItemEffect.spread);
        float focusReduction = item.getEffectEfficiency(stack, ItemEffect.focus);
        float accuracy = Math.max(0f, 100f - spread - focusReduction);
        int piercing = item.getEffectLevel(stack, ItemEffect.piercing);
        int desired = Math.min(multishot, 64);
        ItemStack ammo = BowAmmoModifierHook.consumeAmmo(tool, stack, entity, player,
                stack.getItem() instanceof ProjectileWeaponItem pwi ? pwi.getAllSupportedProjectiles() : ProjectileWeaponItem.ARROW_ONLY,
                desired);
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
        float startAngle = -5f * (count - 1);
        int primaryIndex = count / 2;
        for (int i = 0; i < count; i++) {
            AbstractArrow arrow = arrowItem.createArrow(level, ammo, player);
            float angle = startAngle + 10f * i;
            arrow.shootFromRotation(player, player.getXRot() + angle, player.getYRot(), 0f, arrowVelocity * 3f, accuracy);
            if (drawProgress >= 20) arrow.setCritArrow(true);
            arrow.setBaseDamage(arrow.getBaseDamage() - 2f + strength / 3f);
            if (arrowVelocity > 1f) arrow.setBaseDamage(arrow.getBaseDamage() / arrowVelocity);
            if (piercing > 0) arrow.setPierceLevel((byte) piercing);
            if (suspend && drawProgress >= 20) arrow.setNoGravity(true);
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
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                1f, 1f / (level.getRandom().nextFloat() * 0.4f + 1.2f) + arrowVelocity * 0.5f);
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
    }
}