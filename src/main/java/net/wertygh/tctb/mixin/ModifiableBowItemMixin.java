package net.wertygh.tctb.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableBowItem;
import net.wertygh.tctb.bridge.TetraBowFireHandler;

@Mixin(value = ModifiableBowItem.class, remap = false)
public class ModifiableBowItemMixin {
    @Inject(
        method = "releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V",
        at = @At("HEAD"),
        cancellable = true,
        remap = true
    )
    private void tctb$bridgeReleaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft, CallbackInfo ci) {
        if (TetraBowFireHandler.isBridged(stack)) {
            TetraBowFireHandler.handleRelease(stack, level, entity, timeLeft);
            ci.cancel();
        }
    }
}