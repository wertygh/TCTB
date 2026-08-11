package net.wertygh.tctb.bridge;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.ModuleRegistry;
import se.mickelus.tetra.module.data.VariantData;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.definition.module.material.ToolMaterialHook;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TConModuleResolver {
    public static String[] getAllSlotKeys(Item item, ItemStack stack) {
        ToolStack tool = TConToolData.getToolStack(item, stack);
        if (tool == null) return new String[0];
        return TConSlotMapping.getSlotKeys(stack, tool);
    }

    public static String[] getMajorKeys(Item item, ItemStack stack) {
        ToolStack tool = TConToolData.getToolStack(item, stack);
        if (tool == null) return new String[0];
        List<MaterialStatsId> statTypes = ToolMaterialHook.stats(tool.getDefinition());
        String[] allKeys = TConSlotMapping.getSlotKeys(stack, tool);
        List<String> major = new ArrayList<>();
        for (int i = 0; i < Math.min(statTypes.size(), allKeys.length); i++) {
            if (TConSlotMapping.isMajor(statTypes.get(i))) major.add(allKeys[i]);
        }
        return major.toArray(new String[0]);
    }

    public static String[] getMinorKeys(Item item, ItemStack stack) {
        ToolStack tool = TConToolData.getToolStack(item, stack);
        if (tool == null) return new String[0];
        List<MaterialStatsId> statTypes = ToolMaterialHook.stats(tool.getDefinition());
        String[] allKeys = TConSlotMapping.getSlotKeys(stack, tool);
        List<String> minor = new ArrayList<>();
        for (int i = 0; i < Math.min(statTypes.size(), allKeys.length); i++) {
            if (TConSlotMapping.isMinor(statTypes.get(i))) minor.add(allKeys[i]);
        }
        return minor.toArray(new String[0]);
    }

    @Nullable
    public static VariantData readVariant(ItemStack stack, String slotKey, ItemModule module) {
        String variant = TConModuleData.getVariant(stack, slotKey, module.getKey());
        return !variant.isEmpty() ? module.getVariantData(variant) : module.getDefaultData();
    }

    @Nullable
    public static ItemModule findProxyModule(String slotKey, ItemStack stack) {
        if (slotKey == null || ModuleRegistry.instance == null) return null;
        if (TConModuleData.hasModule(stack, slotKey)) {
            String moduleName = TConModuleData.getModuleKey(stack, slotKey);
            ItemModule module = ItemUpgradeRegistry.instance.getModule(moduleName);
            if (module != null) return module;
        }
        String baseKey = TConSlotContext.getBaseSlotKey(slotKey);
        for (ItemModule module : ModuleRegistry.instance.getAllModules()) {
            if (baseKey != null && baseKey.equals(module.getSlot())) return module;
        }
        return null;
    }

    @Nullable
    public static ItemModule findInstalledModule(String slotKey, ItemStack stack) {
        if (slotKey == null || !TConModuleData.hasModule(stack, slotKey)) return null;
        return ItemUpgradeRegistry.instance.getModule(TConModuleData.getModuleKey(stack, slotKey));
    }

    public static Collection<ItemModule> getAllProxyModules(Item item, ItemStack stack) {
        List<ItemModule> result = new ArrayList<>();
        if (ModuleRegistry.instance == null) return result;
        String[] slotKeys = getAllSlotKeys(item, stack);
        Set<String> seenUninstalled = new HashSet<>();
        for (String slotKey : slotKeys) {
            ItemModule module = findProxyModule(slotKey, stack);
            if (module == null) continue;
            boolean installed = TConModuleData.hasModule(stack, slotKey);
            if (!installed && !seenUninstalled.add(module.getKey())) continue;
            result.add(module);
        }
        return result;
    }
}