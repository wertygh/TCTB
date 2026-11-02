package net.wertygh.tctb.mixin.tetra.schematic;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.module.schematic.BookEnchantSchematic;
import net.wertygh.tctb.bridge.BridgeApi;

@Mixin(value = BookEnchantSchematic.class, remap = false)
public class BookEnchantSchematicMixin {
    @Inject(
        method = "isRelevant(Lnet/minecraft/world/item/ItemStack;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void tctb$notRelevantForTCon(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (BridgeApi.isTConTool(stack)) {
            cir.setReturnValue(false);
        }
    }
    
    @Inject(
        method = "isApplicableForSlot(Ljava/lang/String;Lnet/minecraft/world/item/ItemStack;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void tctb$notApplicableForTCon(String slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (BridgeApi.isTConTool(stack)) {
            cir.setReturnValue(false);
        }
    }
    
    @Inject(
        method = "applyUpgrade(Lnet/minecraft/world/item/ItemStack;[Lnet/minecraft/world/item/ItemStack;ZLjava/lang/String;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void tctb$blockApplyForTCon(ItemStack itemStack, ItemStack[] materials, boolean consume, String slot, Player player, CallbackInfoReturnable<ItemStack> cir) {
        if (BridgeApi.isTConTool(itemStack)) {
            cir.setReturnValue(itemStack.copy());
        }
    }
}