package net.wertygh.tctb.bridge.stat;

import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.data.EffectData;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

public class TConBowFusion {
    public static class FusedBow {
        public float drawTicks;
        public float projectileDamage;
        public float velocityFactor;
        public float inaccuracy;
        public int multishot;
        public float multishotSpread;
        public float tetraStrength;
        public boolean suspend;
    }

    public static FusedBow fuse(ItemStack stack, ToolStack tool) {
        TConBowView view = TConBowView.from(stack, tool);
        FusedBow result = new FusedBow();
        result.drawTicks = view.drawTicks();
        result.projectileDamage = view.projectileDamage();
        result.velocityFactor = view.velocityFactor();
        result.inaccuracy = view.inaccuracy();
        result.tetraStrength = view.strength();
        result.multishot = Math.max(1, view.tconDesiredProjectiles());
        result.multishotSpread = 0f;
        result.suspend = false;
        if (stack.getItem() instanceof IModularItem modularItem) {
            EffectData effects = modularItem.getEffectData(stack);
            if (effects != null) {
                result.multishot = Math.max(result.multishot, effects.getLevel(ItemEffect.multishot));
                result.multishotSpread = effects.getEfficiency(ItemEffect.multishot);
                result.suspend = effects.getLevel(ItemEffect.suspend) > 0;
            }
        }
        return result;
    }

    public static float displaySpreadEfficiency(float inaccuracy) {
        return 100f - Math.max(0f, inaccuracy);
    }

    public static float displayTConAccuracy(float inaccuracy) {
        float value = 1f / (1f + Math.max(0f, inaccuracy) / 3f);
        return Math.max(0.1f, Math.min(1f, value));
    }
}