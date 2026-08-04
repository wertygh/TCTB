package net.wertygh.tctb.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.module.schematic.ConfigSchematic;
import net.wertygh.tctb.bridge.TConSlotBridge;
import net.wertygh.tctb.bridge.TConToolPredicate;

@Mixin(value = ConfigSchematic.class, remap = false)
public class ConfigSchematicMixin {
    @Inject(
        method = "isApplicableForSlot(Ljava/lang/String;Lnet/minecraft/world/item/ItemStack;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void tctb$isApplicableForIndexedSlot(
        String slot,
        ItemStack targetStack,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (!TConToolPredicate.isTConTool(targetStack)
            || slot == null
            || !slot.contains("#")) {
            return;
        }
        ConfigSchematic self = (ConfigSchematic) (Object) this;
        String baseSlot = TConSlotBridge.getBaseSlotKey(slot);
        cir.setReturnValue(self.isApplicableForSlot(baseSlot, targetStack));
    }
}