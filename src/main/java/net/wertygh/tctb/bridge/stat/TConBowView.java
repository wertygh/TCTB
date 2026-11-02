package net.wertygh.tctb.bridge.stat;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.modifiers.hook.ranged.BowAmmoModifierHook;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public record TConBowView(float drawSeconds, float drawTicks, float strength, float projectileDamage, float velocityFactor, float velocityPercent, float inaccuracy, float spreadEff, float focusEff, int tconDesiredProjectiles) {
    public static TConBowView from(ItemStack stack, ToolStack tool) {
        return from(stack, tool, 0f);
    }

    public static TConBowView from(ItemStack stack, ToolStack tool, float focusEff) {
        float draw = Math.max(0.001f, tool.getStats().get(ToolStats.DRAW_SPEED));
        float damage = Math.max(0f, tool.getStats().get(ToolStats.PROJECTILE_DAMAGE));
        float velocity = Math.max(0f, tool.getStats().get(ToolStats.VELOCITY));
        float accuracy = Mth.clamp(tool.getStats().get(ToolStats.ACCURACY), 0.1f, 1.0f);
        float inaccuracy = 3f * (1f / accuracy - 1f);
        float spreadEff = 100f - inaccuracy - focusEff;
        int desired = BowAmmoModifierHook.getDesiredProjectiles(tool);
        return new TConBowView(1f / draw, 20f / draw, damage * 3f, damage, velocity, (velocity - 1f) * 100f, inaccuracy, spreadEff, focusEff, desired);
    }
}