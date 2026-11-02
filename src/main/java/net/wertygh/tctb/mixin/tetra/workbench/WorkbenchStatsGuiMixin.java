package net.wertygh.tctb.mixin.tetra.workbench;

import net.wertygh.tctb.client.stat.TConMiningSpeedGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchStatsGui;
import se.mickelus.tetra.gui.stats.StatsHelper;
import se.mickelus.tetra.gui.stats.bar.GuiStatBar;
import se.mickelus.tetra.gui.stats.getter.IStatFormat;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.LabelGetterBasic;
import se.mickelus.tetra.gui.stats.getter.StatFormat;
import se.mickelus.tetra.gui.stats.getter.TooltipGetterMultiValue;

@Mixin(value = WorkbenchStatsGui.class, remap = false)
public class WorkbenchStatsGuiMixin {
    @Unique
    private static boolean tctb$miningSpeedRegistered;

    @Inject(method = "initializeStaticBars", at = @At("RETURN"))
    private static void tctb$registerMiningSpeed(CallbackInfo ci) {
        if (tctb$miningSpeedRegistered) return;
        tctb$miningSpeedRegistered = true;
        IStatGetter getter = new TConMiningSpeedGetter();
        GuiStatBar bar = new GuiStatBar(
                0, 0, StatsHelper.barLength, "tctb.stats.mining_speed",
                0, 50, false, getter,
                new LabelGetterBasic("%.2f", "%+.2f", false),
                new TooltipGetterMultiValue(
                        "tctb.stats.mining_speed.tooltip",
                        new IStatGetter[] {getter},
                        new IStatFormat[] {new StatFormat("%.2f")}
                )
        );
        WorkbenchStatsGui.addBar(bar);
    }
}