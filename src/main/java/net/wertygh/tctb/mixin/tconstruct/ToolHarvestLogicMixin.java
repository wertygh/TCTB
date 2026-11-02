package net.wertygh.tctb.mixin.tconstruct;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.tools.helper.ToolHarvestLogic;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import net.wertygh.tctb.bridge.BridgeApi;

import javax.annotation.Nullable;

@Mixin(value = ToolHarvestLogic.class, remap = false)
public class ToolHarvestLogicMixin {
    @Inject(method = "runBlockBreak", at = @At("RETURN"))
    private static void tctb$afterRunBlockBreak(ItemStack stack, IToolStackView tool, BlockState state, BlockPos pos, Direction sideHit, ServerPlayer player, @Nullable Projectile projectile, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() > 0 && BridgeApi.isBridgeMelee(stack)) {
            BridgeApi.mineBlock(stack, player.serverLevel(), state, pos, player);
        }
    }
}