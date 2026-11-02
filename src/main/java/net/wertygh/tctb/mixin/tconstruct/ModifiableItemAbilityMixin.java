package net.wertygh.tctb.mixin.tconstruct;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.effect.EffectHelper;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import net.wertygh.tctb.bridge.BridgeApi;

@Mixin(value = ModifiableItem.class, remap = false)
public class ModifiableItemAbilityMixin {
    @Inject(method = "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;", at = @At("HEAD"), cancellable = true, remap = true)
    private void tctb$use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (BridgeApi.hasTetraInteraction(stack)) {
            InteractionResultHolder<ItemStack> result = BridgeApi.use(level, player, hand);
            if (result.getResult().consumesAction()) {
                cir.setReturnValue(result);
            }
        }
    }

    @Inject(method = "getUseDuration(Lnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true, remap = true)
    private void tctb$useDuration(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (BridgeApi.hasTetraInteraction(stack)) {
            cir.setReturnValue(BridgeApi.getUseDuration(stack));
        }
    }

    @Inject(method = "getUseAnimation(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/UseAnim;", at = @At("HEAD"), cancellable = true, remap = true)
    private void tctb$useAnimation(ItemStack stack, CallbackInfoReturnable<UseAnim> cir) {
        if (BridgeApi.hasTetraInteraction(stack)) {
            cir.setReturnValue(BridgeApi.getUseAnimation(stack));
        }
    }

    @Inject(method = "releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V", at = @At("HEAD"), cancellable = true, remap = true)
    private void tctb$releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft, CallbackInfo ci) {
        if (BridgeApi.hasTetraInteraction(stack)) {
            BridgeApi.releaseUsing(stack, level, entity, timeLeft);
            ci.cancel();
        }
    }

    @Inject(method = "onLeftClickEntity", at = @At("HEAD"))
    private void tctb$onLeftClickEntity(ItemStack stack, Player player, Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (BridgeApi.isBridgeMelee(stack)) {
            EffectHelper.setCooledAttackStrength(player, player.getAttackStrengthScale(0.5f));
            EffectHelper.setSprinting(player, player.isSprinting());
        }
    }

    @Inject(method = "onBlockStartBreak", at = @At("HEAD"), cancellable = true)
    private void tctb$onBlockStartBreak(ItemStack stack, BlockPos pos, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (BridgeApi.isBridgeMelee(stack) && BridgeApi.isTetraBreaking()) {
            cir.setReturnValue(false);
        }
    }
}