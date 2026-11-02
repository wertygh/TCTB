package net.wertygh.tctb.bridge.stat;

import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolAction;
import net.wertygh.tctb.bridge.data.TConAttributeData;
import net.wertygh.tctb.bridge.data.TConBridgeTag;
import net.wertygh.tctb.bridge.data.TConModuleResolver;
import net.wertygh.tctb.bridge.data.TConSlotContext;
import net.wertygh.tctb.bridge.data.TConSlotMapping;
import net.wertygh.tctb.bridge.data.TConToolData;
import net.wertygh.tctb.config.TCTBConfig;
import net.wertygh.tctb.modifier.TetraModifiers;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.ModuleRegistry;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.ItemProperties;
import se.mickelus.tetra.module.data.ToolData;
import se.mickelus.tetra.properties.AttributeHelper;
import se.mickelus.tetra.properties.TetraAttributes;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.IToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TConAssembler {
    private static final double EPSILON = 1.0e-6d;

    public static void assembleHybrid(Item item, ItemStack stack, @Nullable Level world, float severity) {
        ToolStack tool = TConToolData.getToolStack(item, stack);
        if (tool == null || !canRebuild(tool) || !modulesReady()) return;
        tool.ensureHasData();
        TConBridgeTag.markBridgeTag(stack);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("HideFlags", tag.getInt("HideFlags") | 1);
        if (tool.getUpgrades().getLevel(TetraModifiers.BRIDGE_ID) < 1) {
            tool.addModifier(TetraModifiers.BRIDGE_ID, 1);
        } else {
            tool.rebuildStats();
        }
        tool.updateStack(stack, false);
        IModularItem.updateIdentifier(stack);
    }

    public static StatsNBT rebuildHybridStats(ToolStack tool, CompoundTag tag, ModifierStatsBuilder nativeBuilder, StatsNBT nativeStats) {
        if (!(tool.getItem() instanceof IModularItem) || !modulesReady()) return nativeStats;
        boolean bridged = tag.getBoolean("tctb_bridged")
                || tool.getUpgrades().getLevel(TetraModifiers.BRIDGE_ID) > 0;
        if (!bridged) return nativeStats;
        ItemStack stack = tool.createStack();
        List<SlotData> slots = readInstalledSlots(stack, tool);
        BridgeStats stats = collectStats(slots);
        Map<FloatToolStat, Float> targets = buildTargets(nativeStats, nativeBuilder, stats, TConSlotMapping.isRanged(tool));
        StatsNBT result = applyTargets(nativeStats, targets);
        CompoundTag bridgeData = buildBridgeData(nativeStats, targets, stats);
        CompoundTag previousBridge = TConToolData.readBridgeData(tool.getPersistentData());
        StatsNBT previousStats = tool.getStats();
        boolean changed = !result.equals(previousStats)
                || previousBridge == null
                || !bridgeData.equals(previousBridge);
        tool.getPersistentData().put(TetraModifiers.BRIDGE_ID, bridgeData);
        if (changed) IModularItem.updateIdentifier(tag);
        return result;
    }

    private static boolean canRebuild(ToolStack tool) {
        return tool.getDefinition().isDataLoaded()
                && MaterialRegistry.isFullyLoaded()
                && ModifierManager.INSTANCE.isDynamicModifiersLoaded()
                && TinkerTags.isTagsLoaded();
    }

    private static boolean modulesReady() {
        return ModuleRegistry.instance != null
                && ItemUpgradeRegistry.instance != null
                && !ModuleRegistry.instance.getAllModules().isEmpty();
    }

    private static class SlotData {
        final String key;
        final Multimap<Attribute, AttributeModifier> attributes;
        final ItemProperties properties;
        final ToolData tools;
        final EffectData effects;

        SlotData(String key, Multimap<Attribute, AttributeModifier> attributes, ItemProperties properties, ToolData tools, EffectData effects) {
            this.key = key;
            this.attributes = attributes;
            this.properties = properties;
            this.tools = tools;
            this.effects = effects;
        }
    }

    private static class AttributeValue {
        static final AttributeValue EMPTY = new AttributeValue(0, 1, false);

        final double addition;
        final double multiplier;
        final boolean present;

        AttributeValue(double addition, double multiplier, boolean present) {
            this.addition = addition;
            this.multiplier = multiplier;
            this.present = present;
        }
    }

    private static class ExtraAttribute {
        double addition;
        double multiplier = 1;
    }

    private static class BridgeStats {
        double attackAddition;
        double attackMultiplier = 1;
        boolean hasAttack;
        double speedMultiplier = 1;
        double speedOffset;
        boolean hasSpeed;
        double durability;
        double durabilityMultiplier = 1;
        double miningSpeed;
        final Map<ToolAction, Integer> toolLevels = new LinkedHashMap<>();
        double drawAddition;
        double drawMultiplier = 1;
        boolean hasDraw;
        double strengthAddition;
        double strengthMultiplier = 1;
        boolean hasStrength;
        double velocityPercent;
        boolean hasVelocity;
        double inaccuracyDelta;
        boolean hasAccuracy;
        final Map<Attribute, ExtraAttribute> extraAttributes = new LinkedHashMap<>();
    }

    private static List<SlotData> readInstalledSlots(ItemStack stack, ToolStack tool) {
        List<SlotData> result = new ArrayList<>();
        for (String slotKey : TConSlotMapping.getSlotKeys(stack, tool)) {
            ItemModule module = TConModuleResolver.findInstalledModule(slotKey, stack);
            if (module == null) continue;
            result.add(TConSlotContext.withSlotContext(slotKey,
                    () -> new SlotData(slotKey,
                            module.getAttributeModifiers(stack),
                            module.getProperties(stack),
                            module.getToolData(stack),
                            module.getEffectData(stack))));
        }
        return result;
    }

    private static BridgeStats collectStats(List<SlotData> slots) {
        BridgeStats stats = new BridgeStats();
        Map<String, List<SlotData>> groups = new LinkedHashMap<>();
        for (SlotData slot : slots) {
            groups.computeIfAbsent(TConSlotContext.getBaseSlotKey(slot.key), k -> new ArrayList<>()).add(slot);
        }
        for (List<SlotData> group : groups.values()) collectGroup(group, stats);
        collectToolStats(slots, stats);
        collectAttackSpeed(slots, stats);
        return stats;
    }

    private static void collectToolStats(List<SlotData> slots, BridgeStats stats) {
        ToolData combined = new ToolData();
        for (SlotData slot : slots) {
            if (slot.tools == null) continue;
            slot.tools.levelMap.forEach((action, value) -> {
                if (value != null && Float.isFinite(value)) {
                    combined.levelMap.merge(action, value, Float::sum);
                }
            });
            slot.tools.efficiencyMap.forEach((action, value) -> {
                if (value != null && Float.isFinite(value)) {
                    combined.efficiencyMap.merge(action, value, Float::sum);
                }
            });
        }
        double scale = TCTBConfig.COMMON.miningEfficiencyScale.get();
        for (ToolAction action : combined.getValues()) {
            int level = combined.getLevel(action);
            if (level <= 0) continue;
            stats.toolLevels.put(action, level);
            double efficiency = combined.getEfficiency(action) * scale;
            if (Double.isFinite(efficiency)) {
                stats.miningSpeed = Math.max(stats.miningSpeed, efficiency);
            }
        }
    }

    private static void collectGroup(List<SlotData> group, BridgeStats stats) {
        int count = group.size();
        if (count == 0) return;
        double damageStack = TCTBConfig.COMMON.damageStackBonus.get();
        double durabilityStack = TCTBConfig.COMMON.durabilityStackBonus.get();
        double extraStack = TCTBConfig.COMMON.extraAttributeStackBonus.get();
        double limbStack = TCTBConfig.COMMON.limbStack.get();
        AttributeValue attack = combineAttribute(group, Attributes.ATTACK_DAMAGE, damageStack);
        if (attack.present) {
            stats.hasAttack = true;
            stats.attackAddition += attack.addition;
            stats.attackMultiplier *= attack.multiplier;
        }
        AttributeValue draw = combineAttribute(group, TetraAttributes.drawSpeed.get(), limbStack);
        if (draw.present) {
            stats.hasDraw = true;
            stats.drawAddition += draw.addition;
            stats.drawMultiplier *= draw.multiplier;
        }
        AttributeValue strength = combineAttribute(group, TetraAttributes.drawStrength.get(), limbStack);
        if (strength.present) {
            stats.hasStrength = true;
            stats.strengthAddition += strength.addition;
            stats.strengthMultiplier *= strength.multiplier;
        }
        double groupDurability = 0;
        double groupVelocity = 0;
        boolean groupHasVelocity = false;
        double spreadEfficiency = 0;
        int spreadProviders = 0;
        double focusEfficiency = 0;
        boolean groupHasFocus = false;
        Set<Attribute> extraKeys = new LinkedHashSet<>();
        for (SlotData slot : group) {
            if (slot.properties != null) {
                groupDurability += slot.properties.durability;
                if (Float.isFinite(slot.properties.durabilityMultiplier)) {
                    stats.durabilityMultiplier *= Math.max(0, slot.properties.durabilityMultiplier);
                }
            }
            if (slot.effects != null) {
                if (slot.effects.levelMap.containsKey(ItemEffect.velocity)) {
                    groupHasVelocity = true;
                    groupVelocity += slot.effects.levelMap.getOrDefault(ItemEffect.velocity, 0f);
                }
                if (slot.effects.efficiencyMap.containsKey(ItemEffect.spread)) {
                    spreadProviders++;
                    spreadEfficiency += slot.effects.getEfficiency(ItemEffect.spread);
                }
                if (slot.effects.efficiencyMap.containsKey(ItemEffect.focus)) {
                    groupHasFocus = true;
                    focusEfficiency += slot.effects.getEfficiency(ItemEffect.focus);
                }
            }
            if (slot.attributes != null) {
                for (Attribute attribute : slot.attributes.keySet()) {
                    if (!isSpecialAttribute(attribute)) extraKeys.add(attribute);
                }
            }
        }
        stats.durability += combineAddition(groupDurability, count, durabilityStack);
        if (groupHasVelocity) {
            stats.hasVelocity = true;
            stats.velocityPercent += combineAddition(groupVelocity, count, limbStack);
        }
        if (spreadProviders > 0) {
            stats.hasAccuracy = true;
            stats.inaccuracyDelta += 99 - spreadEfficiency / spreadProviders;
        }
        if (groupHasFocus) {
            stats.hasAccuracy = true;
            stats.inaccuracyDelta -= focusEfficiency / count;
        }
        for (Attribute attribute : extraKeys) {
            AttributeValue value = combineAttribute(group, attribute, extraStack);
            if (!value.present) continue;
            ExtraAttribute extra = stats.extraAttributes.computeIfAbsent(attribute, k -> new ExtraAttribute());
            extra.addition += value.addition;
            extra.multiplier *= value.multiplier;
        }
    }

    private static boolean isSpecialAttribute(Attribute attribute) {
        return attribute == Attributes.ATTACK_DAMAGE
                || attribute == Attributes.ATTACK_SPEED
                || attribute == TetraAttributes.drawSpeed.get()
                || attribute == TetraAttributes.drawStrength.get();
    }

    private static AttributeValue resolveAttribute(SlotData slot, Attribute attribute) {
        Collection<AttributeModifier> modifiers = slot.attributes == null
                ? Collections.emptyList()
                : slot.attributes.get(attribute);
        if (modifiers.isEmpty()) return AttributeValue.EMPTY;
        double addition = AttributeHelper.getAdditionAmount(modifiers);
        double multiplier = AttributeHelper.getMultiplyAmount(modifiers);
        if (!Double.isFinite(addition) || !Double.isFinite(multiplier)) return AttributeValue.EMPTY;
        return new AttributeValue(addition, multiplier, true);
    }

    private static AttributeValue combineAttribute(List<SlotData> group, Attribute attribute, double stackBonus) {
        double addition = 0;
        double multiplierOffset = 0;
        boolean present = false;
        for (SlotData slot : group) {
            AttributeValue value = resolveAttribute(slot, attribute);
            addition += value.addition;
            multiplierOffset += value.multiplier - 1;
            present |= value.present;
        }
        if (!present || group.isEmpty()) return AttributeValue.EMPTY;
        double factor = (1 + stackBonus * Math.max(0, group.size() - 1)) / group.size();
        return new AttributeValue(addition * factor, Math.max(0, 1 + multiplierOffset * factor), true);
    }

    private static double combineAddition(double sum, int count, double stackBonus) {
        if (count <= 0) return 0;
        return sum / count * (1 + stackBonus * Math.max(0, count - 1));
    }
    private static void collectAttackSpeed(List<SlotData> slots, BridgeStats stats) {
        Map<String, List<SlotData>> groups = new LinkedHashMap<>();
        for (SlotData slot : slots) {
            groups.computeIfAbsent(TConSlotContext.getSpeedGroupKey(slot.key), k -> new ArrayList<>()).add(slot);
        }
        double tetraBase = TCTBConfig.COMMON.tetraAttackSpeedBase.get();
        double penalty = TCTBConfig.COMMON.attackSpeedStackPenalty.get();
        for (List<SlotData> group : groups.values()) {
            double relativeSum = 0;
            int count = 0;
            for (SlotData slot : group) {
                AttributeValue speed = resolveAttribute(slot, Attributes.ATTACK_SPEED);
                if (!speed.present) continue;
                double relative = (tetraBase + speed.addition) / tetraBase;
                relative *= speed.multiplier;
                relativeSum += relative;
                count++;
            }
            if (count == 0) continue;
            double groupRelative = Math.max(0, relativeSum / count * Math.max(0, 1 - penalty * (count - 1)));
            stats.hasSpeed = true;
            stats.speedMultiplier *= groupRelative;
            stats.speedOffset += groupRelative - 1;
        }
    }

    private static Map<FloatToolStat, Float> buildTargets(StatsNBT nativeStats, ModifierStatsBuilder nativeBuilder, BridgeStats stats, boolean ranged) {
        Map<FloatToolStat, Float> targets = new LinkedHashMap<>();
        if (stats.hasAttack) {
            double base = nativeStats.get(ToolStats.ATTACK_DAMAGE);
            double target = TCTBConfig.COMMON.damageMultiplierAffectsTconBase.get()
                    ? (base + stats.attackAddition) * stats.attackMultiplier
                    : base + stats.attackAddition * stats.attackMultiplier;
            putTarget(targets, nativeStats, ToolStats.ATTACK_DAMAGE, target);
        }
        if (stats.hasSpeed) {
            double base = nativeStats.get(ToolStats.ATTACK_SPEED);
            double target = TCTBConfig.COMMON.attackSpeedCombineMode.get()
                    == TCTBConfig.SpeedCombineMode.MULTIPLICATIVE
                    ? base * stats.speedMultiplier
                    : base + stats.speedOffset;
            target = Math.max(TCTBConfig.COMMON.minAttackSpeed.get(), target);
            putTarget(targets, nativeStats, ToolStats.ATTACK_SPEED, target);
        }
        double baseDurability = nativeStats.get(ToolStats.DURABILITY);
        double durabilityScale = Math.max(0, nativeBuilder.getMultiplier(ToolStats.DURABILITY));
        double bonus = Math.max(0, stats.durability * stats.durabilityMultiplier
                * TCTBConfig.COMMON.durabilityScale.get()) * durabilityScale;
        double cap = baseDurability * TCTBConfig.COMMON.durabilityMaxIncreaseRatio.get();
        putTarget(targets, nativeStats, ToolStats.DURABILITY, baseDurability + Math.min(bonus, cap));
        if (!stats.toolLevels.isEmpty()) {
            double baseMining = nativeStats.get(ToolStats.MINING_SPEED);
            double divisor = Math.max(1.0, TCTBConfig.COMMON.miningSpeedCombineDivisor.get());
            putTarget(targets, nativeStats, ToolStats.MINING_SPEED, (baseMining + stats.miningSpeed) / divisor);
        }
        if (ranged) addBowTargets(targets, nativeStats, stats);
        return targets;
    }

    private static void addBowTargets(Map<FloatToolStat, Float> targets, StatsNBT nativeStats, BridgeStats stats) {
        if (stats.hasDraw) {
            double baseDraw = nativeStats.get(ToolStats.DRAW_SPEED);
            if (baseDraw > 0) {
                double targetSeconds = (1 / baseDraw + stats.drawAddition) * stats.drawMultiplier;
                double minSeconds = Math.max(0.001, TCTBConfig.COMMON.bowMinDrawTick.get() / 20d);
                putTarget(targets, nativeStats, ToolStats.DRAW_SPEED, 1 / Math.max(minSeconds, targetSeconds));
            }
        }
        if (stats.hasStrength) {
            double baseProjectile = nativeStats.get(ToolStats.PROJECTILE_DAMAGE);
            double targetStrength = (baseProjectile * 3 + stats.strengthAddition) * stats.strengthMultiplier;
            putTarget(targets, nativeStats, ToolStats.PROJECTILE_DAMAGE, targetStrength / 3);
        }
        if (stats.hasVelocity) {
            double baseVelocity = nativeStats.get(ToolStats.VELOCITY);
            putTarget(targets, nativeStats, ToolStats.VELOCITY, baseVelocity + stats.velocityPercent / 100);
        }
        if (stats.hasAccuracy) {
            double baseAccuracy = nativeStats.get(ToolStats.ACCURACY);
            baseAccuracy = Math.max(0.1, Math.min(1, baseAccuracy));
            double baseInaccuracy = 3 * (1 / baseAccuracy - 1);
            double targetInaccuracy = Math.max(0, baseInaccuracy + stats.inaccuracyDelta);
            putTarget(targets, nativeStats, ToolStats.ACCURACY, 1 / (1 + targetInaccuracy / 3));
        }
    }

    private static void putTarget(Map<FloatToolStat, Float> targets, StatsNBT nativeStats, FloatToolStat stat, double target) {
        if (!Double.isFinite(target)) return;
        float value = stat.clamp((float) target);
        if (!Float.isFinite(value)) return;
        if (Math.abs(value - nativeStats.get(stat)) > EPSILON) {
            targets.put(stat, value);
        }
    }

    private static StatsNBT applyTargets(StatsNBT nativeStats, Map<FloatToolStat, Float> targets) {
        if (targets.isEmpty()) return nativeStats;
        StatsNBT.Builder builder = StatsNBT.builder();
        for (IToolStat<?> stat : nativeStats.getContainedStats()) {
            if (stat instanceof FloatToolStat numeric && targets.containsKey(numeric)) {
                builder.set(numeric, targets.get(numeric).floatValue());
            } else {
                copyStat(builder, nativeStats, stat);
            }
        }
        for (Map.Entry<FloatToolStat, Float> entry : targets.entrySet()) {
            if (!nativeStats.hasStat(entry.getKey())) {
                builder.set(entry.getKey(), entry.getValue().floatValue());
            }
        }
        return builder.build();
    }

    private static <T> void copyStat(StatsNBT.Builder builder, StatsNBT source, IToolStat<T> stat) {
        builder.set(stat, source.get(stat));
    }

    private static CompoundTag buildBridgeData(StatsNBT nativeStats, Map<FloatToolStat, Float> targets, BridgeStats stats) {
        CompoundTag data = new CompoundTag();
        data.putInt("schema_version", 2);
        data.put("native_stats", nativeStats.serializeToNBT());
        CompoundTag targetStats = new CompoundTag();
        for (Map.Entry<FloatToolStat, Float> entry : targets.entrySet()) {
            targetStats.putFloat(entry.getKey().getName().toString(), entry.getValue());
        }
        data.put("stat_targets", targetStats);
        CompoundTag toolsTag = new CompoundTag();
        for (Map.Entry<ToolAction, Integer> entry : stats.toolLevels.entrySet()) {
            if (entry.getValue() > 0) {
                toolsTag.putInt(entry.getKey().name(), entry.getValue());
            }
        }
        if (!toolsTag.isEmpty()) data.put("tools", toolsTag);
        for (Map.Entry<Attribute, ExtraAttribute> entry : stats.extraAttributes.entrySet()) {
            ExtraAttribute value = entry.getValue();
            TConAttributeData.add(data, entry.getKey(), value.addition, AttributeModifier.Operation.ADDITION);
            TConAttributeData.add(data, entry.getKey(), value.multiplier - 1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
        return data;
    }
}