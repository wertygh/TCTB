package net.wertygh.tctb.mixin.tetra.workbench;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchScreen;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import net.wertygh.tctb.bridge.BridgeApi;

@Mixin(value = WorkbenchScreen.class, remap = false)
public class WorkbenchScreenMixin {
    @Inject(method = "buildPreviewStack", at = @At("HEAD"))
    private void tctb$pushPreviewSlotContext(UpgradeSchematic schematic, ItemStack targetStack, String slot, ItemStack[] materials, CallbackInfoReturnable<ItemStack> cir) {
        BridgeApi.pushSlotContext(slot);
    }

    @Inject(method = "buildPreviewStack", at = @At("RETURN"), cancellable = true)
    private void tctb$assemblePreview(UpgradeSchematic schematic, ItemStack targetStack, String slot, ItemStack[] materials, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        try {
            if (!result.isEmpty() && BridgeApi.isTConTool(result)) {
                float severity = schematic.getSeverity(targetStack, materials, slot);
                IModularItem item = (IModularItem) result.getItem();
                item.assemble(result, null, severity);
                cir.setReturnValue(result);
            }
        } finally {
            BridgeApi.popSlotContext();
        }
    }
}