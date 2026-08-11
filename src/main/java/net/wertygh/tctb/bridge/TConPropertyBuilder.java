package net.wertygh.tctb.bridge;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.data.ItemProperties;
import se.mickelus.tetra.module.data.VariantData;
import se.mickelus.tetra.properties.TetraAttributes;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.definition.module.material.ToolMaterialHook;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.tools.stats.HandleMaterialStats;
import slimeknights.tconstruct.tools.stats.HeadMaterialStats;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TConPropertyBuilder {
    public static ItemProperties getProperties(Item item, ItemStack stack) {
        ItemProperties props = new ItemProperties();
        ToolStack tool = TConToolData.getToolStack(item, stack);
        if (tool == null) return props;
        props.durability = tool.getStats().getInt(ToolStats.DURABILITY);
        props.durabilityMultiplier = 1;
        props.integrity = computeIntegrityBase(tool);
        props.integrityUsage = 0;
        String[] slotKeys = TConSlotMapping.getSlotKeys(stack, tool);
        for (String slotKey : slotKeys) {
            ItemModule module = TConModuleResolver.findProxyModule(slotKey, stack);
            if (module == null) continue;
            ItemProperties modProps = TConSlotContext.withSlotContext(
                slotKey,
                () -> module.getProperties(stack)
            );
            if (modProps != null) {
                props.integrity += modProps.integrity;
                props.integrityUsage += modProps.integrityUsage;
            }
        }
        return props;
    }

    @Nullable
    public static Multimap<Attribute, AttributeModifier> getAttributeModifiers(Item item, ItemStack stack) {
        ToolStack tool = TConToolData.getToolStack(item, stack);
        if (tool == null) return null;
        ArrayListMultimap<Attribute, AttributeModifier> result = ArrayListMultimap.create();

        if (TConSlotMapping.isRanged(tool)) {
            float drawSpeed = 0;
            float drawStrength = 0;
            for (String slotKey : TConSlotMapping.getSlotKeys(stack, tool)) {
                ItemModule module = TConModuleResolver.findProxyModule(slotKey, stack);
                if (module == null || !TConModuleData.hasModule(stack, slotKey)) continue;
                VariantData vd = TConModuleResolver.readVariant(stack, slotKey, module);
                if (vd == null || vd.attributes == null) continue;
                for (AttributeModifier mod : vd.attributes.get(TetraAttributes.drawSpeed.get())) {
                    if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                        drawSpeed += mod.getAmount();
                    }
                }
                for (AttributeModifier mod : vd.attributes.get(TetraAttributes.drawStrength.get())) {
                    if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                        drawStrength += mod.getAmount();
                    }
                }
            }
            if (drawSpeed != 0) {
                result.put(TetraAttributes.drawSpeed.get(),
                    new AttributeModifier(UUID.nameUUIDFromBytes("tctb_draw_spd".getBytes()),
                        "tctb_bridge_draw_speed", drawSpeed, AttributeModifier.Operation.ADDITION));
            }
            if (drawStrength != 0) {
                result.put(TetraAttributes.drawStrength.get(),
                    new AttributeModifier(UUID.nameUUIDFromBytes("tctb_draw_str".getBytes()),
                        "tctb_bridge_draw_strength", drawStrength, AttributeModifier.Operation.ADDITION));
            }
            return result.isEmpty() ? null : result;
        }

        StatsNBT stats = tool.getStats();
        float atk = stats.get(ToolStats.ATTACK_DAMAGE);
        if (atk > 0) {
            result.put(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(UUID.nameUUIDFromBytes("tctb_atk".getBytes()),
                    "tctb_bridge_attack", atk - 1.0, AttributeModifier.Operation.ADDITION));
        }
        float spd = stats.get(ToolStats.ATTACK_SPEED);
        if (spd > 0) {
            result.put(Attributes.ATTACK_SPEED,
                new AttributeModifier(UUID.nameUUIDFromBytes("tctb_spd".getBytes()),
                    "tctb_bridge_speed", spd - 4.0, AttributeModifier.Operation.ADDITION));
        }
        return result.isEmpty() ? null : result;
    }

    private static int computeIntegrityBase(ToolStack tool) {
        List<MaterialStatsId> statTypes = ToolMaterialHook.stats(tool.getDefinition());
        List<MaterialVariant> materials = tool.getMaterials().getList();
        int maxTier = 0;
        for (MaterialVariant mat : materials) {
            try {
                maxTier = Math.max(maxTier, mat.get().getTier());
            } catch (Exception ignored) {}
        }
        int partA = Math.min(4, maxTier);
        if (!MaterialRegistry.isFullyLoaded()) return partA;
        int headCount = 0;
        float headDurabilitySum = 0;
        float headMiningSpeedSum = 0;
        float headAttackDamageSum = 0;
        int handleCount = 0;
        float handleMultiplierSum = 0;
        for (int i = 0; i < Math.min(statTypes.size(), materials.size()); i++) {
            MaterialStatsId statType = statTypes.get(i);
            MaterialVariant mat = materials.get(i);
            String path = statType.getPath();
            try {
                if ("head".equals(path)) {
                    Optional<? extends IMaterialStats> ms = MaterialRegistry.getInstance()
                        .getMaterialStats(mat.get().getIdentifier(), HeadMaterialStats.ID);
                    if (ms.isPresent() && ms.get() instanceof HeadMaterialStats hms) {
                        headCount++;
                        headDurabilitySum += hms.durability();
                        headMiningSpeedSum += hms.miningSpeed();
                        headAttackDamageSum += hms.attack();
                    }
                } else if ("handle".equals(path)) {
                    Optional<? extends IMaterialStats> ms = MaterialRegistry.getInstance()
                        .getMaterialStats(mat.get().getIdentifier(), HandleMaterialStats.ID);
                    if (ms.isPresent() && ms.get() instanceof HandleMaterialStats hms) {
                        handleCount++;
                        handleMultiplierSum += hms.durability();
                        handleMultiplierSum += hms.miningSpeed();
                        handleMultiplierSum += hms.meleeSpeed();
                        handleMultiplierSum += hms.attackDamage();
                    }
                }
            } catch (Exception ignored) {}
        }
        if (headCount == 0) return partA;
        float avgHeadDurability = headDurabilitySum / headCount;
        float avgHeadMiningSpeed = headMiningSpeedSum / headCount;
        float avgHeadAttackDamage = headAttackDamageSum / headCount;
        float avgHandleMultiplier = handleCount > 0 ? (handleMultiplierSum / handleCount) : 0;
        float partBRaw = 0.001f * avgHeadDurability + avgHeadMiningSpeed + avgHeadAttackDamage + avgHandleMultiplier;
        int partB = Math.round(partBRaw);
        return partA + partB;
    }
}