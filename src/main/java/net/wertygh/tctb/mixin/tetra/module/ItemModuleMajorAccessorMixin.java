package net.wertygh.tctb.mixin.tetra.module;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.ImprovementData;

@Mixin(value = ItemModuleMajor.class, remap = false)
public interface ItemModuleMajorAccessorMixin {
    @Accessor("improvements")
    ImprovementData[] tctb$getImprovementsArray();
}