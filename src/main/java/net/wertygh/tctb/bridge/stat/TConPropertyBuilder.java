package net.wertygh.tctb.bridge.stat;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.wertygh.tctb.bridge.data.TConAttributeData;
import net.wertygh.tctb.bridge.data.TConModuleResolver;
import net.wertygh.tctb.bridge.data.TConSlotContext;
import net.wertygh.tctb.bridge.data.TConSlotMapping;
import net.wertygh.tctb.bridge.data.TConToolData;
import net.wertygh.tctb.config.TCTBConfig;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.data.ItemProperties;
import se.mickelus.tetra.properties.TetraAttributes;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.definition.module.material.ToolMaterialHook;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.tools.stats.HandleMaterialStats;
import slimeknights.tconstruct.tools.stats.HeadMaterialStats;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class TConPropertyBuilder {
    public static ItemProperties getProperties(Item item, ItemStack stack) {
        ItemProperties props = new ItemProperties();
        props.tags = Set.of();
        ToolStack tool = TConToolData.getToolStack(item, stack);
        if (tool == null) return props;
        props.durability = tool.getStats().getInt(ToolStats.DURABILITY);
        props.durabilityMultiplier = 1;
        props.integrity = computeIntegrityBase(tool);
        props.integrityUsage = 0;
        for (String slotKey : TConSlotMapping.getSlotKeys(stack, tool)) {
            ItemModule module = TConModuleResolver.findInstalledModule(slotKey, stack);
            if (module == null) continue;
            ItemProperties modProps = TConSlotContext.withSlotContext(slotKey, () -> module.getProperties(stack));
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
        if (!TConSlotMapping.isRanged(tool)) return getNativeAttributeView(stack);
        ArrayListMultimap<Attribute, AttributeModifier> result = ArrayListMultimap.create();
        CompoundTag bridgeData = TConToolData.readBridgeData(stack);
        TConAttributeData.forEach(bridgeData, (attribute, modifier) -> result.put(attribute, modifier));
        TConBowView view = TConBowView.from(stack, tool);
        result.put(TetraAttributes.drawSpeed.get(), modifier(
                "tctb_draw_spd", "tctb_bridge_draw_speed",
                view.drawSeconds(), AttributeModifier.Operation.ADDITION));
        result.put(TetraAttributes.drawStrength.get(), modifier(
                "tctb_draw_str", "tctb_bridge_draw_strength",
                view.strength(), AttributeModifier.Operation.ADDITION));
        return result;
    }

    @Nullable
    private static Multimap<Attribute, AttributeModifier> getNativeAttributeView(ItemStack stack) {
        Multimap<Attribute, AttributeModifier> nativeModifiers = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
        ArrayListMultimap<Attribute, AttributeModifier> result = ArrayListMultimap.create();
        for (Map.Entry<Attribute, Collection<AttributeModifier>> entry : nativeModifiers.asMap().entrySet()) {
            Attribute attribute = entry.getKey();
            double addition = 0;
            double multiplyBase = 0;
            double multiplyTotal = 1;
            for (AttributeModifier mod : entry.getValue()) {
                if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                    addition += mod.getAmount();
                } else if (mod.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE) {
                    multiplyBase += mod.getAmount();
                } else if (mod.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
                    multiplyTotal *= 1 + mod.getAmount();
                }
            }
            double factor = (1 + multiplyBase) * multiplyTotal;
            String key = "tctb_native_" + attribute.getDescriptionId();
            if (Double.isFinite(addition) && Math.abs(addition) > 1.0e-6d) {
                result.put(attribute, modifier(
                        key + "_add", "tctb_native_attribute",
                        addition, AttributeModifier.Operation.ADDITION));
            }
            if (Double.isFinite(factor) && Math.abs(factor - 1) > 1.0e-6d) {
                result.put(attribute, modifier(
                        key + "_multiply", "tctb_native_attribute",
                        factor - 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
        return result.isEmpty() ? null : result;
    }

    private static AttributeModifier modifier(String uuidKey, String name, double amount, AttributeModifier.Operation operation) {
        return new AttributeModifier(
                UUID.nameUUIDFromBytes(uuidKey.getBytes(StandardCharsets.UTF_8)),
                name, amount, operation);
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
        int partA = Math.min(TCTBConfig.COMMON.integrityMaxTier.get(), maxTier);
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
        float avgHandleMultiplier = handleCount > 0 ? handleMultiplierSum / handleCount : 0;
        float partBRaw = TCTBConfig.COMMON.integrityDurabilityFactor.get().floatValue() * avgHeadDurability
                + TCTBConfig.COMMON.integrityMiningFactor.get().floatValue() * avgHeadMiningSpeed
                + TCTBConfig.COMMON.integrityAttackFactor.get().floatValue() * avgHeadAttackDamage
                + TCTBConfig.COMMON.integrityHandleFactor.get().floatValue() * avgHandleMultiplier;
        return partA + Math.round(partBRaw);
    }
}