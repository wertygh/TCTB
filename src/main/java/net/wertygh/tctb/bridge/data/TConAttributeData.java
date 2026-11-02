package net.wertygh.tctb.bridge.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.BiConsumer;

public class TConAttributeData {
    public static final String LIST_KEY = "attribute_modifiers";
    public static final String LEGACY_KEY = "attributes";

    public static void add(CompoundTag bridgeData, Attribute attribute, double amount, AttributeModifier.Operation operation) {
        if (!Double.isFinite(amount) || Math.abs(amount) < 1.0e-6d) return;
        ResourceLocation id = ForgeRegistries.ATTRIBUTES.getKey(attribute);
        if (id == null) return;
        ListTag entries = bridgeData.getList(LIST_KEY, Tag.TAG_COMPOUND);
        CompoundTag entry = new CompoundTag();
        entry.putString("attribute", id.toString());
        entry.putDouble("amount", amount);
        entry.putInt("operation", operation.ordinal());
        entries.add(entry);
        bridgeData.put(LIST_KEY, entries);
    }

    public static void forEach(@Nullable CompoundTag bridgeData, BiConsumer<Attribute, AttributeModifier> consumer) {
        if (bridgeData == null) return;
        if (bridgeData.contains(LIST_KEY, Tag.TAG_LIST)) {
            ListTag entries = bridgeData.getList(LIST_KEY, Tag.TAG_COMPOUND);
            for (int i = 0; i < entries.size(); i++) {
                CompoundTag entry = entries.getCompound(i);
                ResourceLocation id = ResourceLocation.tryParse(entry.getString("attribute"));
                if (id == null) continue;
                Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(id);
                if (attribute == null) continue;
                int operationIndex = entry.getInt("operation");
                AttributeModifier.Operation[] operations = AttributeModifier.Operation.values();
                if (operationIndex < 0 || operationIndex >= operations.length) continue;
                double amount = entry.getDouble("amount");
                if (!Double.isFinite(amount) || Math.abs(amount) < 1.0e-6d) continue;
                AttributeModifier.Operation operation = operations[operationIndex];
                consumer.accept(attribute, createModifier(id, amount, operation));
            }
            return;
        }
        if (bridgeData.contains(LEGACY_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag attributes = bridgeData.getCompound(LEGACY_KEY);
            for (String key : attributes.getAllKeys()) {
                ResourceLocation id = ResourceLocation.tryParse(key);
                if (id == null) continue;
                Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(id);
                if (attribute == null) continue;
                double amount = attributes.getDouble(key);
                if (!Double.isFinite(amount) || Math.abs(amount) < 1.0e-6d) continue;
                consumer.accept(
                    attribute,
                    createModifier(id, amount, AttributeModifier.Operation.ADDITION)
                );
            }
        }
    }

    private static AttributeModifier createModifier(ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        String uuidKey = "tctb_attr_" + id;
        if (operation != AttributeModifier.Operation.ADDITION) {
            uuidKey += "_" + operation.ordinal();
        }
        return new AttributeModifier(
            UUID.nameUUIDFromBytes(uuidKey.getBytes(StandardCharsets.UTF_8)),
            "tctb_bridge_attribute",
            amount,
            operation
        );
    }
}