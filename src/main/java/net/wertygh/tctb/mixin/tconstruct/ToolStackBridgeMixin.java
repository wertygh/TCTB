package net.wertygh.tctb.mixin.tconstruct;

import net.minecraft.nbt.CompoundTag;
import net.wertygh.tctb.bridge.stat.TConAssembler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;

@Mixin(value = ToolStack.class, remap = false)
public abstract class ToolStackBridgeMixin {
    @Shadow
    private CompoundTag nbt;

    @Redirect(
        method = "rebuildStats()V",
        at = @At(value = "INVOKE", target = "Lslimeknights/tconstruct/library/tools/stat/ModifierStatsBuilder;build()Lslimeknights/tconstruct/library/tools/nbt/StatsNBT;"),
        require = 1
    )
    private StatsNBT tctb$buildHybridStats(ModifierStatsBuilder builder) {
        StatsNBT nativeStats = builder.build();
        ToolStack self = (ToolStack) (Object) this;
        return TConAssembler.rebuildHybridStats(self, nbt, builder, nativeStats);
    }
}