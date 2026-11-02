package net.wertygh.tctb.mixin.tetra.stats;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.wertygh.tctb.bridge.BridgeApi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.stats.getter.StatGetterFocus;
import se.mickelus.tetra.gui.stats.getter.StatGetterSpread;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.ImprovementData;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableBowItem;

import javax.annotation.Nullable;

@Mixin(value = StatGetterSpread.class, remap = false)
public abstract class StatGetterSpreadMixin {
    @Inject(
        method = "getValue(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;)D",
        at = @At("HEAD"),
        cancellable = true
    )
    private void tctb$getSlotSpread(Player player, ItemStack stack, String slot, CallbackInfoReturnable<Double> cir) {
        if (!tctb$isBow(stack)) return;
        if ((Object) this instanceof StatGetterFocus) return;
        cir.setReturnValue(tctb$getSlotSpreadAngle(stack, slot, null));
    }

    @Inject(
        method = "getValue(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;Ljava/lang/String;)D",
        at = @At("HEAD"),
        cancellable = true
    )
    private void tctb$getImprovementSpread(Player player, ItemStack stack, String slot, String improvement, CallbackInfoReturnable<Double> cir) {
        if (!tctb$isBow(stack)) return;
        if ((Object) this instanceof StatGetterFocus) return;
        cir.setReturnValue(tctb$getSlotSpreadAngle(stack, slot, improvement));
    }

    @Unique
    private static boolean tctb$isBow(ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && stack.getItem() instanceof IModularItem
                && (stack.getItem() instanceof ModularBowItem
                || stack.getItem() instanceof ModifiableBowItem);
    }

    @Unique
    private static double tctb$getSlotSpreadAngle(ItemStack stack, @Nullable String slot, @Nullable String improvement) {
        if (slot == null || slot.isEmpty()) return 0d;
        IModularItem item = (IModularItem) stack.getItem();
        ItemModule module = item.getModuleFromSlot(stack, slot);
        if (module == null) return 0d;
        float efficiency;
        if (improvement != null) {
            if (!(module instanceof ItemModuleMajor major)) return 0d;
            ImprovementData improvementData = BridgeApi.withSlotContext(
                slot, () -> major.getImprovement(stack, improvement)
            );
            if (improvementData == null || improvementData.effects == null) return 0d;
            efficiency = improvementData.effects.getEfficiency(ItemEffect.spread);
        } else {
            EffectData effects = BridgeApi.withSlotContext(
                slot, () -> module.getEffectData(stack)
            );
            if (effects == null) return 0d;
            efficiency = effects.getEfficiency(ItemEffect.spread);
        }
        if (efficiency <= 0f) return 0d;
        return tctb$offsetToAngle(efficiency);
    }

    @Unique
    private static double tctb$offsetToAngle(double efficiency) {
        double wrap = Math.max(0d, 100d - efficiency);
        return Math.atan(Math.sqrt(2 * Math.pow(wrap * 0.0172275d, 2))) * 180d / Math.PI;
    }
}