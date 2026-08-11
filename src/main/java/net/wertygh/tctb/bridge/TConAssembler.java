package net.wertygh.tctb.bridge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.ModuleRegistry;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.VariantData;
import se.mickelus.tetra.properties.TetraAttributes;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import net.wertygh.tctb.mixin.ItemModuleMajorAccessorMixin;
import net.wertygh.tctb.modifier.TetraModifiers;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TConAssembler {
    private static final float DURABILITY_SCALE = 0.3f;
    private static final float DURABILITY_MAX_INCREASE_RATIO = 0.8f;

    public static void assembleHybrid(Item item, ItemStack stack, @Nullable Level world, float severity) {
        TConBridgeTag.markBridgeTag(stack);
        if (stack.getDamageValue() > stack.getMaxDamage()) {
            stack.setDamageValue(stack.getMaxDamage());
        }
        stack.getOrCreateTag().putInt("HideFlags", 1);
        ToolStack tool = TConToolData.getToolStack(item, stack);
        if (tool == null) {
            IModularItem.updateIdentifier(stack);
            return;
        }
        BridgeStats stats = collectBridgeStats(stack, tool);
        applyBridgeStats(stack, tool, stats);
    }

    private static class BridgeStats {
        float moduleAtkDmg = 0;
        float moduleAtkSpd = 0;
        float moduleDmgMult = 0;
        float moduleSpdMult = 0;
        boolean moduleHasSpeed = false;
        float impAtkDmg = 0;
        float impAtkSpd = 0;
        float impDmgMult = 0;
        float impSpdMult = 0;
        float miningSpeed = 0;
        int durability = 0;
        float durabilityMultiplier = 1.0f;
        final Set<ToolAction> toolActions = new HashSet<>();
        float drawSpeed = 0;
        float drawStrength = 0;
        float velocityLevel = 0;
    }

    private static BridgeStats collectBridgeStats(ItemStack stack, ToolStack tool) {
        String[] slotKeys = TConSlotMapping.getSlotKeys(stack, tool);
        BridgeStats stats = new BridgeStats();
        if (ModuleRegistry.instance != null) {
            collectModuleStats(stack, slotKeys, stats);
            collectImprovementStats(stack, slotKeys, stats);
        }
        return stats;
    }

    private static void collectModuleStats(ItemStack stack, String[] slotKeys, BridgeStats stats) {
        Map<String, List<String>> slotGroups = new LinkedHashMap<>();
        for (String slotKey : slotKeys) {
            slotGroups.computeIfAbsent(TConSlotContext.getBaseSlotKey(slotKey), k -> new ArrayList<>()).add(slotKey);
        }
        for (List<String> group : slotGroups.values()) {
            collectModuleGroup(stack, group, stats);
        }
    }

    private static void collectModuleGroup(ItemStack stack, List<String> groupSlots, BridgeStats stats) {
        float groupAtkDmg = 0;
        float groupFinalSpeed = 0;
        float groupDmgMult = 0;
        float groupSpdMult = 0;
        int groupDurability = 0;
        int installedCount = 0;
        boolean groupHasSpeed = false;
        int speedCount = 0;
        for (String slotKey : groupSlots) {
            if (!TConModuleData.hasModule(stack, slotKey)) continue;
            ItemModule module = TConModuleResolver.findProxyModule(slotKey, stack);
            if (module == null) continue;
            VariantData vd = TConModuleResolver.readVariant(stack, slotKey, module);
            if (vd == null) continue;
            installedCount++;
            if (vd.attributes != null) {
                float slotDmg = 0;
                float slotSpdMod = 0;
                float slotDmgMult = 0;
                float slotSpdMult = 0;
                boolean slotHasSpeed = false;
                for (AttributeModifier mod : vd.attributes.get(Attributes.ATTACK_DAMAGE)) {
                    if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                        slotDmg += mod.getAmount();
                    } else if (mod.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
                        slotDmgMult += mod.getAmount();
                    }
                }
                if (vd.attributes.containsKey(Attributes.ATTACK_SPEED)) {
                    slotHasSpeed = true;
                    for (AttributeModifier mod : vd.attributes.get(Attributes.ATTACK_SPEED)) {
                        if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                            slotSpdMod += mod.getAmount();
                        } else if (mod.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
                            slotSpdMult += mod.getAmount();
                        }
                    }
                }
                groupAtkDmg += slotDmg;
                groupDmgMult += slotDmgMult;
                if (slotHasSpeed) {
                    speedCount++;
                    groupHasSpeed = true;
                    groupFinalSpeed += (4.0f + slotSpdMod);
                    groupSpdMult += slotSpdMult;
                }
                for (AttributeModifier mod : vd.attributes.get(TetraAttributes.drawSpeed.get())) {
                    if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                        stats.drawSpeed += mod.getAmount();
                    }
                }
                for (AttributeModifier mod : vd.attributes.get(TetraAttributes.drawStrength.get())) {
                    if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                        stats.drawStrength += mod.getAmount();
                    }
                }
            }
            if (vd.effects != null) {
                stats.velocityLevel += vd.effects.getLevel(ItemEffect.velocity);
            }
            if (vd.tools != null) {
                for (ToolAction ta : vd.tools.getValues()) {
                    float eff = vd.tools.getEfficiency(ta);
                    if (eff > stats.miningSpeed) stats.miningSpeed = eff;
                    stats.toolActions.add(ta);
                }
            }
            if (vd.durability > 0) groupDurability += vd.durability;
            if (vd.durabilityMultiplier != 1 && vd.durabilityMultiplier != 0) {
                stats.durabilityMultiplier += (vd.durabilityMultiplier - 1);
            }
        }
        if (installedCount > 0) {
            float multiplier = 1.0f + Math.max(0, installedCount - 1) / 10.0f;
            stats.moduleAtkDmg += (groupAtkDmg / installedCount) * multiplier;
            stats.moduleDmgMult += (groupDmgMult / installedCount) * multiplier;
            if (groupHasSpeed) {
                stats.moduleHasSpeed = true;
                float speedMultiplier = 1.0f + Math.max(0, speedCount - 1) / 10.0f;
                float avgFinalSpeed = groupFinalSpeed / speedCount;
                stats.moduleAtkSpd += (avgFinalSpeed - 4.0f) * speedMultiplier;
                stats.moduleSpdMult += (groupSpdMult / speedCount) * speedMultiplier;
            }
            stats.durability += Math.round((float) groupDurability / installedCount * multiplier);
        }
    }

    private static void collectImprovementStats(ItemStack stack, String[] slotKeys, BridgeStats stats) {
        for (String slotKey : slotKeys) {
            String moduleName = TConModuleData.getModuleKey(stack, slotKey);
            if (moduleName.isEmpty()) continue;
            ItemModule module = ItemUpgradeRegistry.instance.getModule(moduleName);
            if (!(module instanceof ItemModuleMajor major)) continue;
            ImprovementData[] allImps = ((ItemModuleMajorAccessorMixin) major).tctb$getImprovementsArray();
            if (allImps == null) continue;
            for (ImprovementData imp : allImps) {
                if (imp == null) continue;
                if (TConModuleData.getImprovementLevel(stack, slotKey, imp.key) != imp.level) {
                    continue;
                }
                accumulateImprovement(imp, stats);
            }
        }
    }

    private static void accumulateImprovement(ImprovementData imp, BridgeStats stats) {
        if (imp.attributes != null) {
            for (AttributeModifier mod : imp.attributes.get(Attributes.ATTACK_DAMAGE)) {
                if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                    stats.impAtkDmg += mod.getAmount();
                } else if (mod.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
                    stats.impDmgMult += mod.getAmount();
                }
            }
            for (AttributeModifier mod : imp.attributes.get(Attributes.ATTACK_SPEED)) {
                if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                    stats.impAtkSpd += mod.getAmount();
                } else if (mod.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
                    stats.impSpdMult += mod.getAmount();
                }
            }
            for (AttributeModifier mod : imp.attributes.get(TetraAttributes.drawSpeed.get())) {
                if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                    stats.drawSpeed += mod.getAmount();
                }
            }
            for (AttributeModifier mod : imp.attributes.get(TetraAttributes.drawStrength.get())) {
                if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                    stats.drawStrength += mod.getAmount();
                }
            }
        }
        if (imp.effects != null) {
            stats.velocityLevel += imp.effects.getLevel(ItemEffect.velocity);
        }
        if (imp.tools != null) {
            for (ToolAction ta : imp.tools.getValues()) {
                float eff = imp.tools.getEfficiency(ta);
                if (eff > stats.miningSpeed) stats.miningSpeed = eff;
                stats.toolActions.add(ta);
            }
        }
        if (imp.durability > 0) stats.durability += imp.durability;
        if (imp.durabilityMultiplier != 1 && imp.durabilityMultiplier != 0) {
            stats.durabilityMultiplier += (imp.durabilityMultiplier - 1);
        }
    }

    private static void applyBridgeStats(ItemStack stack, ToolStack tool, BridgeStats stats) {
        CompoundTag oldBridge = TConToolData.readBridgeData(tool.getPersistentData());
        float oldSpd = 0;
        float oldDmg = 0;
        int oldDur = 0;
        float oldMining = 0;
        float oldDraw = 0;
        float oldVel = 0;
        float oldProj = 0;
        if (oldBridge != null) {
            oldSpd = oldBridge.getFloat("attack_speed");
            oldDmg = oldBridge.getFloat("attack_damage");
            oldDur = oldBridge.getInt("durability");
            oldMining = oldBridge.getFloat("mining_speed");
            oldDraw = oldBridge.getFloat("draw_speed");
            oldVel = oldBridge.getFloat("velocity");
            oldProj = oldBridge.getFloat("projectile_damage");
        }
        float damageMult = tool.getMultipliers().get(ToolStats.ATTACK_DAMAGE);
        if (damageMult <= 0f) damageMult = 1f;
        float speedMult = tool.getMultipliers().get(ToolStats.ATTACK_SPEED);
        if (speedMult <= 0f) speedMult = 1f;
        float durabilityMult = tool.getMultipliers().get(ToolStats.DURABILITY);
        if (durabilityMult <= 0f) durabilityMult = 1f;
        float miningMult = tool.getMultipliers().get(ToolStats.MINING_SPEED);
        if (miningMult <= 0f) miningMult = 1f;
        float tconSpeed = tool.getStats().get(ToolStats.ATTACK_SPEED).floatValue() / speedMult - oldSpd;
        float tconMiningSpeed = tool.getStats().get(ToolStats.MINING_SPEED).floatValue() / miningMult - oldMining;
        float damageFactor = Math.max(0f, 1.0f + stats.moduleDmgMult + stats.impDmgMult);
        float tetraDamageBonus = (stats.moduleAtkDmg + stats.impAtkDmg) * damageFactor;
        float dmgDelta = tetraDamageBonus / damageMult;
        float speedFactor = Math.max(0f, 1.0f + stats.moduleSpdMult + stats.impSpdMult);
        float desiredSpeed;
        if (stats.moduleHasSpeed) {
            desiredSpeed = (4.0f + stats.moduleAtkSpd + stats.impAtkSpd) * speedFactor;
        } else {
            desiredSpeed = (tconSpeed + stats.impAtkSpd) * speedFactor;
        }
        desiredSpeed = Math.max(0.1f, desiredSpeed);
        float spdDelta = desiredSpeed / speedMult - tconSpeed;
        int durabilityDelta = Math.round(stats.durability * stats.durabilityMultiplier * DURABILITY_SCALE);
        int baseDurability = Math.max(0, tool.getStats().getInt(ToolStats.DURABILITY)
                - Math.round(oldDur * durabilityMult));
        float maxVisibleIncrease = baseDurability * DURABILITY_MAX_INCREASE_RATIO;
        if (durabilityDelta * durabilityMult > maxVisibleIncrease) {
            durabilityDelta = Math.max(0, Math.round(maxVisibleIncrease / durabilityMult));
        }
        float currentMiningSpeed = tconMiningSpeed * miningMult;
        float miningDelta = stats.miningSpeed > currentMiningSpeed
                ? (stats.miningSpeed - currentMiningSpeed) / miningMult
                : 0f;
        float drawMult = tool.getMultipliers().get(ToolStats.DRAW_SPEED);
        if (drawMult <= 0f) drawMult = 1f;
        float drawDelta = 0;
        if (stats.drawSpeed != 0) {
            float baseDraw = tool.getStats().get(ToolStats.DRAW_SPEED).floatValue() / drawMult - oldDraw;
            float desiredDraw = 1f / Math.max(0.1f, stats.drawSpeed);
            drawDelta = desiredDraw / drawMult - baseDraw;
        }
        float velMult = tool.getMultipliers().get(ToolStats.VELOCITY);
        if (velMult <= 0f) velMult = 1f;
        float velDelta = 0;
        if (stats.velocityLevel != 0) {
            float baseVel = tool.getStats().get(ToolStats.VELOCITY).floatValue() / velMult - oldVel;
            float desiredVel = baseVel * (1f + stats.velocityLevel / 100f);
            velDelta = desiredVel / velMult - baseVel;
        }
        float projMult = tool.getMultipliers().get(ToolStats.PROJECTILE_DAMAGE);
        if (projMult <= 0f) projMult = 1f;
        float projDelta = 0;
        if (stats.drawStrength != 0) {
            float baseProj = tool.getStats().get(ToolStats.PROJECTILE_DAMAGE).floatValue() / projMult - oldProj;
            float desiredProj = 2f + stats.drawStrength / 3f;
            projDelta = desiredProj / projMult - baseProj;
        }
        CompoundTag bridgeData = new CompoundTag();
        if (Math.abs(dmgDelta) > 0.00001f) bridgeData.putFloat("attack_damage", dmgDelta);
        if (Math.abs(spdDelta) > 0.00001f) bridgeData.putFloat("attack_speed", spdDelta);
        if (miningDelta > 0.00001f) bridgeData.putFloat("mining_speed", miningDelta);
        if (durabilityDelta > 0) bridgeData.putInt("durability", durabilityDelta);
        if (Math.abs(drawDelta) > 0.00001f) bridgeData.putFloat("draw_speed", drawDelta);
        if (Math.abs(velDelta) > 0.00001f) bridgeData.putFloat("velocity", velDelta);
        if (Math.abs(projDelta) > 0.00001f) bridgeData.putFloat("projectile_damage", projDelta);
        if (!stats.toolActions.isEmpty()) {
            CompoundTag toolsTag = new CompoundTag();
            for (ToolAction ta : stats.toolActions) toolsTag.putBoolean(ta.name(), true);
            bridgeData.put("tools", toolsTag);
        }
        tool.clearCache();
        tool.getPersistentData().put(TetraModifiers.BRIDGE_ID, bridgeData);
        if (tool.getUpgrades().getLevel(TetraModifiers.BRIDGE_ID) < 1) {
            tool.addModifier(TetraModifiers.BRIDGE_ID, 1);
        } else {
            tool.rebuildStats();
        }
        tool.clearCache();
        int newMax = tool.getStats().getInt(ToolStats.DURABILITY);
        int currentDamage = tool.getDamage();
        if (currentDamage >= newMax) tool.setDamage(Math.max(0, newMax - 1));
        tool.updateStack(stack, false);
        IModularItem.updateIdentifier(stack);
    }
}