package net.wertygh.tctb.bridge;

import com.google.common.cache.Cache;
import com.google.common.collect.Multimap;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.ItemProperties;
import se.mickelus.tetra.module.data.SynergyData;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

public class ModularItemDelegates {
    public static Item getItem(Item self) {
        return self;
    }

    public static void clearCaches(Item self) {
    }

    public static String[] getMajorModuleKeys(Item self, ItemStack stack) {
        return TConSlotBridge.getMajorKeys(self, stack);
    }

    public static String[] getMinorModuleKeys(Item self, ItemStack stack) {
        return TConSlotBridge.getMinorKeys(self, stack);
    }

    public static String[] getRequiredModules(Item self, ItemStack stack) {
        return new String[0];
    }

    public static int getHoneBase(Item self, ItemStack stack) {
        return TConSlotBridge.getHoneBase(self, stack);
    }

    public static int getHoneIntegrityMultiplier(Item self, ItemStack stack) {
        return TConSlotBridge.getHoneIntegrityMultiplier(self, stack);
    }

    public static boolean canGainHoneProgress(Item self, ItemStack stack) {
        return TConSlotBridge.canGainHoneProgress(self, stack);
    }

    @Nullable
    public static Cache<String, Multimap<Attribute, AttributeModifier>> getAttributeModifierCache(Item self) {
        return null;
    }

    @Nullable
    public static Cache<String, EffectData> getEffectDataCache(Item self) {
        return null;
    }

    @Nullable
    public static Cache<String, ItemProperties> getPropertyCache(Item self) {
        return null;
    }

    public static SynergyData[] getAllSynergyData(Item self, ItemStack stack) {
        return new SynergyData[0];
    }

    public static ItemProperties getProperties(Item self, ItemStack stack) {
        return TConSlotBridge.getProperties(self, stack);
    }

    @Nullable
    public static Multimap<Attribute, AttributeModifier> getAttributeModifiers(Item self, ItemStack stack) {
        return TConSlotBridge.getAttributeModifiers(self, stack);
    }

    public static void assemble(Item self, ItemStack stack, Level world, float severity) {
        TConSlotBridge.assembleHybrid(self, stack, world, severity);
    }

    @Nullable
    public static ItemModule getModuleFromSlot(Item self, ItemStack stack, String slot) {
        if (slot == null) return null;
        return TConSlotBridge.findInstalledModule(slot, stack);
    }

    public static Collection<ItemModule> getAllModules(Item self, ItemStack stack) {
        return TConSlotBridge.getAllProxyModules(self, stack);
    }

    public static EffectData getEffectData(Item self, ItemStack stack) {
        EffectData result = null;
        String[] slotKeys = TConSlotBridge.getAllSlotKeys(self, stack);
        for (String slotKey : slotKeys) {
            ItemModule module = TConSlotBridge.findProxyModule(slotKey, stack);
            if (module == null) continue;
            boolean hasInstalledModule = TConSlotBridge.hasModule(stack, slotKey);
            if (hasInstalledModule) {
                EffectData moduleEffects = TConSlotBridge.withSlotContext(
                    slotKey,
                    () -> module.getEffectData(stack)
                );
                if (moduleEffects != null) {
                    result = EffectData.merge(result, moduleEffects);
                }
            } else if (module instanceof ItemModuleMajor majorModule) {
                ImprovementData[] improvements = TConSlotBridge.withSlotContext(
                    slotKey,
                    () -> majorModule.getImprovements(stack)
                );
                for (ImprovementData improvement : improvements) {
                    if (improvement.effects != null) {
                        result = EffectData.merge(result, improvement.effects);
                    }
                }
            }
        }
        return result;
    }

    public static String[] getMajorModuleNames(Item self, ItemStack stack) {
        String[] keys = TConSlotBridge.getMajorKeys(self, stack);
        return Arrays.stream(keys)
            .map(key -> I18n.get("tetra.slot." + TConSlotBridge.getBaseSlotKey(key)))
            .toArray(String[]::new);
    }

    public static String[] getMinorModuleNames(Item self, ItemStack stack) {
        String[] keys = TConSlotBridge.getMinorKeys(self, stack);
        return Arrays.stream(keys)
            .map(key -> I18n.get("tetra.slot." + TConSlotBridge.getBaseSlotKey(key)))
            .toArray(String[]::new);
    }

    public static ItemModuleMajor[] getMajorModules(Item self, ItemStack stack) {
        String[] keys = TConSlotBridge.getMajorKeys(self, stack);
        ItemModuleMajor[] modules = new ItemModuleMajor[keys.length];
        for (int i = 0; i < keys.length; i++) {
            ItemModule module = TConSlotBridge.findInstalledModule(keys[i], stack);
            if (module instanceof ItemModuleMajor major) {
                modules[i] = major;
            }
        }
        return modules;
    }

    public static ItemModule[] getMinorModules(Item self, ItemStack stack) {
        String[] keys = TConSlotBridge.getMinorKeys(self, stack);
        ItemModule[] modules = new ItemModule[keys.length];
        for (int i = 0; i < keys.length; i++) {
            modules[i] = TConSlotBridge.findInstalledModule(keys[i], stack);
        }
        return modules;
    }

    public static boolean clampDamage(Item self, ItemStack stack, int damage) {
        return TConSlotBridge.clampDamage(stack, damage);
    }

    public static float getDestroySpeed(Item self, ItemStack stack, BlockState state, float original) {
        return TConSlotBridge.getDestroySpeed(stack, state, original);
    }

    public static boolean isCorrectToolForDrops(Item self, ItemStack stack, BlockState state, boolean original) {
        return TConSlotBridge.isCorrectToolForDrops(stack, state, original);
    }
}