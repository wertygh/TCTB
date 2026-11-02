package net.wertygh.tctb.client.stat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.wertygh.tctb.bridge.data.TConModuleData;
import net.wertygh.tctb.bridge.data.TConModuleResolver;
import net.wertygh.tctb.bridge.data.TConSlotContext;
import net.wertygh.tctb.bridge.stat.TConAssembler;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.ImprovementData;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import javax.annotation.Nullable;

public class TConMiningSpeedGetter implements IStatGetter {
    @Override
    public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack) {
        return isApplicable(currentStack) || isApplicable(previewStack);
    }

    @Override
    public double getValue(Player player, ItemStack stack) {
        return getMiningSpeed(stack);
    }

    @Override
    public double getValue(Player player, ItemStack stack, String slot) {
        return getContribution(stack, slot, null);
    }

    @Override
    public double getValue(Player player, ItemStack stack, String slot, String improvement) {
        return getContribution(stack, slot, improvement);
    }

    private static boolean isApplicable(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof IModifiable)) return false;
        return ToolStack.from(stack).getStats().hasStat(ToolStats.MINING_SPEED);
    }

    private static double getMiningSpeed(ItemStack stack) {
        if (!isApplicable(stack)) return 0;
        return ToolStack.from(stack).getStats().get(ToolStats.MINING_SPEED);
    }

    private static double getContribution(ItemStack stack, @Nullable String slot, @Nullable String improvement) {
        if (!isApplicable(stack) || slot == null || slot.isEmpty()) return 0;
        ItemModule module = TConModuleResolver.findInstalledModule(slot, stack);
        if (module == null) return 0;
        if (improvement != null) {
            if (!(module instanceof ItemModuleMajor major)) return 0;
            ImprovementData data = TConSlotContext.withSlotContext(slot, () -> major.getImprovement(stack, improvement));
            if (data == null) return 0;
        }
        return TConSlotContext.withSlotContext(null, () -> {
            double total = getMiningSpeed(stack);
            ItemStack without = stack.copy();
            if (improvement == null) {
                TConModuleData.removeSlotImprovements(without, slot);
                TConModuleData.removeModule(without, slot);
            } else {
                TConModuleData.removeImprovement(without, slot, improvement);
            }
            TConAssembler.assembleHybrid(without.getItem(), without, null, 0);
            double contribution = total - getMiningSpeed(without);
            if (!Double.isFinite(contribution) || Math.abs(contribution) < 1.0e-6d) return 0d;
            return contribution;
        });
    }
}