package net.wertygh.tctb.mixin.tetra.workbench;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetra.blocks.workbench.gui.GuiModuleList;
import net.wertygh.tctb.bridge.BridgeApi;

@Mixin(value = GuiModuleList.class, remap = false)
public class GuiModuleListMixin {
    @Inject(method = "update", at = @At("HEAD"))
    private void tctb$saveSlotContextScope(CallbackInfo ci) {
        BridgeApi.pushSlotContext(BridgeApi.getSlotContext());
    }

    @ModifyArg(
        method = "updateMajorModules",
        at = @At(value = "INVOKE", target = "Lse/mickelus/tetra/blocks/workbench/gui/GuiModuleMajor;<init>(IILse/mickelus/mutil/gui/GuiAttachment;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;Ljava/lang/String;Lse/mickelus/tetra/module/ItemModuleMajor;Lse/mickelus/tetra/module/ItemModuleMajor;Ljava/util/function/Consumer;Ljava/util/function/BiConsumer;)V"),
        index = 5
    )
    private String tctb$setSlotContextForMajor(String slotKey) {
        BridgeApi.setSlotContext(slotKey);
        return slotKey;
    }

    @ModifyArg(
        method = "getMinorModule",
        at = @At(value = "INVOKE", target = "Lse/mickelus/tetra/blocks/workbench/gui/GuiModule;<init>(IILse/mickelus/mutil/gui/GuiAttachment;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;Ljava/lang/String;Lse/mickelus/tetra/module/ItemModule;Lse/mickelus/tetra/module/ItemModule;Ljava/util/function/Consumer;Ljava/util/function/BiConsumer;)V"),
        index = 5
    )
    private String tctb$setSlotContextForMinor(String slotKey) {
        BridgeApi.setSlotContext(slotKey);
        return slotKey;
    }

    @Inject(method = "update", at = @At("RETURN"))
    private void tctb$restoreSlotContextScope(CallbackInfo ci) {
        BridgeApi.popSlotContext();
    }
}