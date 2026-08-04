package net.wertygh.tctb.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.ImprovementData;
import net.wertygh.tctb.bridge.TConSlotBridge;
import net.wertygh.tctb.bridge.TConToolPredicate;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = ItemModuleMajor.class, remap = false)
public class ItemModuleMajorMixin {
    private static final int MAX_INDEXED_SLOTS = 8;

    @Unique
    private static String tctb$getEffectiveSlot() {
        return TConSlotBridge.getSlotContext();
    }

    @Inject(
        method = "addImprovement(Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;Ljava/lang/String;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void tctb$onAddImprovement(ItemStack itemStack, String slot,
                                               String improvement, int level, CallbackInfo ci) {
        if (TConToolPredicate.isTConTool(itemStack)) {
            TConSlotBridge.setImprovement(itemStack, slot, improvement, level);
            ci.cancel();
        }
    }

    @Inject(
        method = "addImprovement(Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void tctb$onAddImprovementIndexed(ItemStack itemStack, String improvementKey,
                                               int level, CallbackInfo ci) {
        if (!TConToolPredicate.isTConTool(itemStack)) return;
        String effectiveSlot = tctb$getEffectiveSlot();
        if (effectiveSlot == null) return;
        TConSlotBridge.setImprovement(itemStack, effectiveSlot, improvementKey, level);
        ci.cancel();
    }

    @Inject(
        method = "removeImprovement(Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void tctb$onRemoveImprovementIndexed(ItemStack itemStack, String improvement,
                                                  CallbackInfo ci) {
        if (!TConToolPredicate.isTConTool(itemStack)) return;
        String effectiveSlot = tctb$getEffectiveSlot();
        if (effectiveSlot == null) return;
        TConSlotBridge.removeImprovement(itemStack, effectiveSlot, improvement);
        ci.cancel();
    }

    @Inject(
        method = "removeModule(Lnet/minecraft/world/item/ItemStack;Z)[Lnet/minecraft/world/item/ItemStack;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void tctb$removeMajorModuleIndexed(ItemStack targetStack, boolean upgrade,
                                                CallbackInfoReturnable<ItemStack[]> cir) {
        if (!TConToolPredicate.isTConTool(targetStack)) return;
        String slot = tctb$getEffectiveSlot();
        if (slot == null || !slot.contains("#")) return;
        TConSlotBridge.removeModule(targetStack, slot);
        if (!upgrade) TConSlotBridge.removeSlotImprovements(targetStack, slot);
        cir.setReturnValue(new ItemStack[0]);
    }

    @Inject(
        method = "getImprovements(Lnet/minecraft/world/item/ItemStack;)[Lse/mickelus/tetra/module/data/ImprovementData;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void tctb$getImprovementsIndexed(ItemStack itemStack,
                                              CallbackInfoReturnable<ImprovementData[]> cir) {
        if (!TConToolPredicate.isTConTool(itemStack)) return;
        ItemModuleMajor self = (ItemModuleMajor) (Object) this;
        String baseSlot = self.getSlot();
        ImprovementData[] allImps = ((ItemModuleMajorAccessorMixin) self).tctb$getImprovementsArray();
        if (allImps == null) {
            cir.setReturnValue(new ImprovementData[0]);
            return;
        }
        String contextSlot = tctb$getEffectiveSlot();
        List<ImprovementData> found = new ArrayList<>();
        if (contextSlot != null) {
            for (ImprovementData imp : allImps) {
                if (imp != null && TConSlotBridge.getImprovementLevel(itemStack, contextSlot, imp.key) == imp.level) {
                    found.add(imp);
                }
            }
        } else {
            for (ImprovementData imp : allImps) {
                if (imp == null) continue;
                if (TConSlotBridge.getImprovementLevel(itemStack, baseSlot, imp.key) == imp.level) {
                    found.add(imp);
                    continue;
                }
                for (int i = 1; i <= MAX_INDEXED_SLOTS; i++) {
                    if (TConSlotBridge.getImprovementLevel(itemStack, baseSlot + "#" + i, imp.key) == imp.level) {
                        found.add(imp);
                        break;
                    }
                }
            }
        }
        cir.setReturnValue(found.toArray(new ImprovementData[0]));
    }

    @Inject(
        method = "getImprovementLevel(Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;)I",
        at = @At("HEAD"),
        cancellable = true
    )
    private void tctb$getImprovementLevelIndexed(ItemStack itemStack, String improvementKey,
                                                  CallbackInfoReturnable<Integer> cir) {
        if (!TConToolPredicate.isTConTool(itemStack)) return;
        String contextSlot = tctb$getEffectiveSlot();
        if (contextSlot != null) {
            cir.setReturnValue(TConSlotBridge.getImprovementLevel(itemStack, contextSlot, improvementKey));
            return;
        }
        ItemModuleMajor self = (ItemModuleMajor) (Object) this;
        String baseSlot = self.getSlot();
        int level = TConSlotBridge.getImprovementLevel(itemStack, baseSlot, improvementKey);
        if (level >= 0) {
            cir.setReturnValue(level);
            return;
        }
        for (int i = 1; i <= MAX_INDEXED_SLOTS; i++) {
            level = TConSlotBridge.getImprovementLevel(itemStack, baseSlot + "#" + i, improvementKey);
            if (level >= 0) {
                cir.setReturnValue(level);
                return;
            }
        }
        cir.setReturnValue(-1);
    }

    @Inject(
        method = "getImprovement(Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;)Lse/mickelus/tetra/module/data/ImprovementData;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void tctb$getImprovementIndexed(ItemStack itemStack, String improvementKey,
                                             CallbackInfoReturnable<ImprovementData> cir) {
        if (!TConToolPredicate.isTConTool(itemStack)) return;
        ItemModuleMajor self = (ItemModuleMajor) (Object) this;
        String baseSlot = self.getSlot();
        ImprovementData[] allImps = ((ItemModuleMajorAccessorMixin) self).tctb$getImprovementsArray();
        if (allImps == null) {
            cir.setReturnValue(null);
            return;
        }
        String contextSlot = tctb$getEffectiveSlot();
        if (contextSlot != null) {
            for (ImprovementData imp : allImps) {
                if (imp != null && improvementKey.equals(imp.key)
                        && TConSlotBridge.getImprovementLevel(itemStack, contextSlot, imp.key) == imp.level) {
                    cir.setReturnValue(imp);
                    return;
                }
            }
            cir.setReturnValue(null);
            return;
        }
        for (ImprovementData imp : allImps) {
            if (imp == null || !improvementKey.equals(imp.key)) continue;
            if (TConSlotBridge.getImprovementLevel(itemStack, baseSlot, imp.key) == imp.level) {
                cir.setReturnValue(imp);
                return;
            }
            for (int i = 1; i <= MAX_INDEXED_SLOTS; i++) {
                if (TConSlotBridge.getImprovementLevel(itemStack, baseSlot + "#" + i, imp.key) == imp.level) {
                    cir.setReturnValue(imp);
                    return;
                }
            }
        }
        cir.setReturnValue(null);
    }
}