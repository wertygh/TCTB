package net.wertygh.tctb.bridge;

import com.google.common.cache.Cache;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.wertygh.tctb.bridge.data.TConSlotMapping;
import net.wertygh.tctb.bridge.stat.TConBowView;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.ItemProperties;
import se.mickelus.tetra.module.data.SynergyData;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public static void assemble(Item self, ItemStack stack, @Nullable Level world, float severity) {
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
        if (stack == null || stack.isEmpty()) return new EffectData();
        ToolStack tool = ToolStack.from(stack);
        boolean ranged = TConSlotMapping.isRanged(tool);
        String[] slotKeys = TConSlotBridge.getAllSlotKeys(self, stack);
        Map<String, List<String>> groups = new LinkedHashMap<>();
        for (String slotKey : slotKeys) {
            groups.computeIfAbsent(
                    TConSlotBridge.getBaseSlotKey(slotKey),
                    key -> new ArrayList<>()
            ).add(slotKey);
        }
        EffectData result = new EffectData();
        for (List<String> group : groups.values()) {
            EffectData groupEffects = new EffectData();
            int installedCount = 0;
            for (String slotKey : group) {
                ItemModule module = TConSlotBridge.findProxyModule(slotKey, stack);
                if (module == null) continue;
                boolean installed = TConSlotBridge.hasModule(stack, slotKey);
                if (installed) {
                    installedCount++;
                    EffectData moduleEffects = TConSlotBridge.withSlotContext(
                        slotKey, () -> module.getEffectData(stack)
                    );
                    addEffects(groupEffects, moduleEffects);
                } else if (module instanceof ItemModuleMajor majorModule) {
                    ImprovementData[] improvements = TConSlotBridge.withSlotContext(
                        slotKey, () -> majorModule.getImprovements(stack)
                    );
                    if (improvements == null) continue;
                    for (ImprovementData improvement : improvements) {
                        if (improvement != null) addEffects(groupEffects, improvement.effects);
                    }
                }
            }
            if (ranged && installedCount > 1) {
                groupEffects = averageEffectData(groupEffects, installedCount);
            }
            addEffects(result, groupEffects);
        }
        if (ranged) return applyBowVirtualEffects(stack, tool, result);
        return result;
    }

    private static void addEffects(EffectData target, @Nullable EffectData source) {
        if (source == null) return;
        source.levelMap.forEach((effect, value) ->target.levelMap.merge(effect, value, Float::sum));
        source.efficiencyMap.forEach((effect, value) ->target.efficiencyMap.merge(effect, value, Float::sum));
    }

    private static EffectData copyEffects(@Nullable EffectData source) {
        EffectData copy = new EffectData();
        if (source != null) {
            copy.levelMap.putAll(source.levelMap);
            copy.efficiencyMap.putAll(source.efficiencyMap);
        }
        return copy;
    }

    private static EffectData applyBowVirtualEffects(ItemStack stack, ToolStack tool, EffectData source) {
        EffectData result = copyEffects(source);
        float focusEff = result.efficiencyMap.getOrDefault(ItemEffect.focus, 0f);
        float moduleMultishot = result.levelMap.getOrDefault(ItemEffect.multishot, 0f);
        removeEffect(result, ItemEffect.velocity);
        removeEffect(result, ItemEffect.spread);
        removeEffect(result, ItemEffect.focus);
        TConBowView view = TConBowView.from(stack, tool, focusEff);
        if (Math.abs(view.velocityPercent()) > 1.0e-4f) {
            result.levelMap.put(ItemEffect.velocity, view.velocityPercent());
        }
        result.efficiencyMap.put(ItemEffect.spread, view.spreadEff());
        if (Math.abs(focusEff) > 1.0e-4f) {
            result.efficiencyMap.put(ItemEffect.focus, focusEff);
        }
        int desiredProjectiles = Math.max(1, view.tconDesiredProjectiles());
        int multishot = Math.max(Math.round(moduleMultishot), desiredProjectiles);
        if (multishot > 1) {
            result.levelMap.put(ItemEffect.multishot, (float) multishot);
        } else {
            removeEffect(result, ItemEffect.multishot);
        }
        return result;
    }

    private static void removeEffect(EffectData data, ItemEffect effect) {
        data.levelMap.remove(effect);
        data.efficiencyMap.remove(effect);
    }

    private static EffectData averageEffectData(EffectData data, int count) {
        if (count <= 1) return copyEffects(data);
        EffectData result = new EffectData();
        data.levelMap.forEach((effect, value) ->result.levelMap.put(effect, value / count));
        data.efficiencyMap.forEach((effect, value) -> result.efficiencyMap.put(effect, value / count));
        return result;
    }

    private static String[] translateSlotKeys(String[] keys) {
        return Arrays.stream(keys)
                .map(key -> Component.translatable("tetra.slot." + TConSlotBridge.getBaseSlotKey(key)).getString())
                .toArray(String[]::new);
    }

    public static String[] getMajorModuleNames(Item self, ItemStack stack) {
        return translateSlotKeys(TConSlotBridge.getMajorKeys(self, stack));
    }

    public static String[] getMinorModuleNames(Item self, ItemStack stack) {
        return translateSlotKeys(TConSlotBridge.getMinorKeys(self, stack));
    }

    public static ItemModuleMajor[] getMajorModules(Item self, ItemStack stack) {
        String[] keys = TConSlotBridge.getMajorKeys(self, stack);
        ItemModuleMajor[] modules = new ItemModuleMajor[keys.length];
        for (int i = 0; i < keys.length; i++) {
            ItemModule module = TConSlotBridge.findInstalledModule(keys[i], stack);
            if (module instanceof ItemModuleMajor major) modules[i] = major;
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