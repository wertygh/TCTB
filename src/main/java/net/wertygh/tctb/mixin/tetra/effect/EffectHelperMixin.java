package net.wertygh.tctb.mixin.tetra.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.effect.EffectHelper;
import net.wertygh.tctb.bridge.BridgeApi;

@Mixin(value = EffectHelper.class, remap = false)
public class EffectHelperMixin {
    @Inject(method = "breakBlock", at = @At("HEAD"))
    private static void tctb$beforeBreak(Level world, Player breakingPlayer, ItemStack toolStack, BlockPos pos, BlockState blockState, boolean harvest, boolean tryReplant, CallbackInfoReturnable<Boolean> cir) {
        BridgeApi.pushTetraBreak();
    }

    @Inject(method = "breakBlock", at = @At("RETURN"))
    private static void tctb$afterBreak(Level world, Player breakingPlayer, ItemStack toolStack, BlockPos pos, BlockState blockState, boolean harvest, boolean tryReplant, CallbackInfoReturnable<Boolean> cir) {
        BridgeApi.popTetraBreak();
    }
}