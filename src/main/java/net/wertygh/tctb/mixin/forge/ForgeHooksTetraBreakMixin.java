package net.wertygh.tctb.mixin.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.wertygh.tctb.bridge.BridgeApi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ForgeHooks.class, remap = false)
public abstract class ForgeHooksTetraBreakMixin {
    @Redirect(
        method = "onBlockBreakEvent(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/GameType;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;)I",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;canAttackBlock(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)Z", remap = true),
        remap = false,
        require = 1
    )
    private static boolean tctb$allowCreativeTetraEffect(Item item, BlockState state, Level level, BlockPos pos, Player player) {
        boolean original = item.canAttackBlock(state, level, pos, player);
        if (original) return true;
        if (!player.isCreative() || !BridgeApi.isTetraBreaking()) return false;
        ItemStack held = player.getMainHandItem();
        return !held.isEmpty() && held.getItem() == item && BridgeApi.isBridgeMelee(held);
    }
}