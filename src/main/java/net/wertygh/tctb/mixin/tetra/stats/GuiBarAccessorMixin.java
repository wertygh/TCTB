package net.wertygh.tctb.mixin.tetra.stats;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import se.mickelus.tetra.gui.stats.bar.GuiBar;

@Mixin(value = GuiBar.class, remap = false)
public interface GuiBarAccessorMixin {
    @Accessor("invertedDiff")
    boolean tctb$getInvertedDiff();

    @Accessor("invertedDiff")
    void tctb$setInvertedDiff(boolean inverted);
}