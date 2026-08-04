package net.wertygh.tctb.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.data.VariantData;
import net.wertygh.tctb.bridge.TConSlotBridge;
import net.wertygh.tctb.bridge.TConToolPredicate;

@Mixin(value = ItemModule.class, remap = false)
public class ItemModuleMixin {
    private static final int MAX_INDEXED_SLOTS = 8;

    @Inject(
        method = "getVariantData(Lnet/minecraft/world/item/ItemStack;)Lse/mickelus/tetra/module/data/VariantData;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void tctb$getVariantDataIndexed(ItemStack stack,
                                             CallbackInfoReturnable<VariantData> cir) {
        if (!TConToolPredicate.isTConTool(stack)) return;
        ItemModule self = (ItemModule) (Object) this;
        String contextSlot = TConSlotBridge.getSlotContext();
        if (contextSlot != null) {
            String variant = TConSlotBridge.getVariant(stack, contextSlot, self.getKey());
            if (!variant.isEmpty()) {
                cir.setReturnValue(self.getVariantData(variant));
                return;
            }
            cir.setReturnValue(self.getDefaultData());
            return;
        }
        String baseSlot = self.getSlot();
        String moduleKey = self.getKey();
        String variant = findVariantForSlot(stack, baseSlot, moduleKey);
        if (variant == null) {
            for (int i = 1; i <= MAX_INDEXED_SLOTS; i++) {
                variant = findVariantForSlot(stack, baseSlot + "#" + i, moduleKey);
                if (variant != null) break;
            }
        }
        if (variant != null) cir.setReturnValue(self.getVariantData(variant));
    }

    private String findVariantForSlot(ItemStack stack, String slotKey, String moduleKey) {
        if (moduleKey.equals(TConSlotBridge.getModuleKey(stack, slotKey))) {
            String variant = TConSlotBridge.getVariant(stack, slotKey, moduleKey);
            if (!variant.isEmpty()) return variant;
        }
        return null;
    }

    @Inject(method = "addModule", at = @At("HEAD"), cancellable = true)
    private void tctb$addModuleIndexed(ItemStack targetStack, String variantKey,
                                        Player player, CallbackInfo ci) {
        if (!TConToolPredicate.isTConTool(targetStack)) return;
        String slot = TConSlotBridge.getSlotContext();
        if (slot == null || !slot.contains("#")) return;
        ItemModule self = (ItemModule) (Object) this;
        TConSlotBridge.setModule(targetStack, slot, self.getKey(), variantKey);
        ci.cancel();
    }

    @Inject(
        method = "removeModule(Lnet/minecraft/world/item/ItemStack;Z)[Lnet/minecraft/world/item/ItemStack;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void tctb$removeModuleIndexed(ItemStack targetStack, boolean upgrade,
                                           CallbackInfoReturnable<ItemStack[]> cir) {
        if (!TConToolPredicate.isTConTool(targetStack)) return;
        String slot = TConSlotBridge.getSlotContext();
        if (slot == null || !slot.contains("#")) return;
        TConSlotBridge.removeModule(targetStack, slot);
        if (!upgrade) TConSlotBridge.removeSlotImprovements(targetStack, slot);
        cir.setReturnValue(new ItemStack[0]);
    }
}