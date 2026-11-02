package net.wertygh.tctb.mixin.tetra.workbench;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import net.wertygh.tctb.bridge.BridgeApi;

@Mixin(value = WorkbenchTile.class, remap = false)
public class WorkbenchTileMixin {
    @Inject(method = "craft", at = @At("HEAD"))
    private void tctb$pushCraftSlotContext(Player player, CallbackInfo ci) {
        WorkbenchTile self = (WorkbenchTile) (Object) this;
        BridgeApi.pushSlotContext(self.getCurrentSlot());
    }

    @Inject(method = "craft", at = @At("RETURN"))
    private void tctb$popCraftSlotContext(Player player, CallbackInfo ci) {
        BridgeApi.popSlotContext();
    }

    @Redirect(
        method = "craft",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;setDamageValue(I)V",
            remap = true
        )
    )
    private void tctb$onSetDamageValue(ItemStack stack, int damage) {
        if (!BridgeApi.isTConTool(stack)) {
            stack.setDamageValue(damage);
        }
    }
}