package net.wertygh.tctb.mixin.tetra.workbench;

import net.minecraft.world.item.ItemStack;
import net.wertygh.tctb.bridge.data.TConModuleResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import se.mickelus.tetra.blocks.workbench.gui.GuiModuleList;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.modular.IModularItem;
import slimeknights.tconstruct.library.tools.item.IModifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Mixin(value = GuiModuleList.class, remap = false)
public class GuiModuleListLayoutMixin {
    @Redirect(
        method = "updateMajorModules",
        at = @At(value = "INVOKE", target = "Lse/mickelus/tetra/items/modular/IModularItem;getMajorGuiOffsets(Lnet/minecraft/world/item/ItemStack;)Lse/mickelus/tetra/gui/GuiModuleOffsets;"),
        require = 1
    )
    private GuiModuleOffsets tctb$getMajorOffsets(IModularItem item, ItemStack stack) {
        if (!(stack.getItem() instanceof IModifiable)) return item.getMajorGuiOffsets(stack);
        return tctb$getOffsets(item, stack, item.getMajorModuleKeys(stack), true);
    }

    @Redirect(
        method = "updateMinorModules",
        at = @At(value = "INVOKE", target = "Lse/mickelus/tetra/items/modular/IModularItem;getMinorGuiOffsets(Lnet/minecraft/world/item/ItemStack;)Lse/mickelus/tetra/gui/GuiModuleOffsets;"),
        require = 1
    )
    private GuiModuleOffsets tctb$getMinorOffsets(IModularItem item, ItemStack stack) {
        if (!(stack.getItem() instanceof IModifiable)) return item.getMinorGuiOffsets(stack);
        return tctb$getOffsets(item, stack, item.getMinorModuleKeys(stack), false);
    }

    @Unique
    private static GuiModuleOffsets tctb$getOffsets(IModularItem item, ItemStack stack, String[] keys, boolean major) {
        List<String> allKeys = tctb$getSlotOrder(item, stack);
        int[][] centers = tctb$getCenters(allKeys.size());
        int[] offsets = new int[keys.length * 2];
        for (int i = 0; i < keys.length; i++) {
            int index = allKeys.indexOf(keys[i]);
            if (index < 0) {
                throw new IllegalStateException("Missing TCTB slot layout: " + keys[i]);
            }
            int centerX = centers[index][0];
            int centerY = centers[index][1];
            if (centerX > 0) {
                offsets[i * 2] = centerX - (major ? 8 : 5);
            } else {
                offsets[i * 2] = centerX + (major ? 7 : 4);
            }
            offsets[i * 2 + 1] = centerY - (major ? 8 : 5);
        }
        return new GuiModuleOffsets(offsets);
    }

    @Unique
    private static List<String> tctb$getSlotOrder(IModularItem item, ItemStack stack) {
        Set<String> visibleKeys = new LinkedHashSet<>();
        Collections.addAll(visibleKeys, item.getMajorModuleKeys(stack));
        Collections.addAll(visibleKeys, item.getMinorModuleKeys(stack));
        Set<String> orderedKeys = new LinkedHashSet<>();
        for (String key : TConModuleResolver.getAllSlotKeys(stack.getItem(), stack)) {
            if (visibleKeys.contains(key)) orderedKeys.add(key);
        }
        orderedKeys.addAll(visibleKeys);
        return new ArrayList<>(orderedKeys);
    }

    @Unique
    private static int[][] tctb$getCenters(int count) {
        switch (count) {
            case 0:
                return new int[0][2];
            case 1:
                return new int[][] {{12, 8}};
            case 2:
                return new int[][] {{12, 8}, {12, 26}};
            case 3:
                return new int[][] {{12, 8}, {12, 26}, {-25, 17}};
            case 4:
                return new int[][] {{12, 8}, {12, 26}, {-21, 26}, {-21, 8}};
            case 5:
                return new int[][] {{12, 8}, {12, 26}, {-16, 30}, {-25, 17}, {-16, 4}};
            default:
                return tctb$getExtendedCenters(count);
        }
    }

    @Unique
    private static int[][] tctb$getExtendedCenters(int count) {
        int[][] centers = new int[count][2];
        for (int i = 0; i < count; i++) {
            centers[i][0] = i % 2 == 0 ? 12 : -21;
            centers[i][1] = 8 + i / 2 * 18;
        }
        return centers;
    }
}