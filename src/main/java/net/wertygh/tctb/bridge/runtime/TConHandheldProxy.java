package net.wertygh.tctb.bridge.runtime;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.wertygh.tctb.TCTBMod;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.ItemProperties;
import se.mickelus.tetra.module.data.SynergyData;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

public class TConHandheldProxy extends ItemModularHandheld {
    private static final ThreadLocal<Boolean> SUPPRESS_DAMAGE = ThreadLocal.withInitial(() -> false);

    public TConHandheldProxy() {
        super(new Item.Properties().stacksTo(1).fireResistant());
        this.majorModuleKeys = new String[0];
        this.minorModuleKeys = new String[0];
        this.requiredModules = new String[0];
    }

    private static boolean isSelf(ItemStack stack) {
        return stack.getItem() instanceof TConHandheldProxy;
    }

    private static IModularItem delegate(ItemStack stack) {
        return (IModularItem) stack.getItem();
    }

    public static void withSuppressedDamage(Runnable action) {
        boolean prev = SUPPRESS_DAMAGE.get();
        SUPPRESS_DAMAGE.set(true);
        try {
            action.run();
        } finally {
            SUPPRESS_DAMAGE.set(prev);
        }
    }

    @Override
    public void clearCaches() {}

    @Override
    public int getEffectLevel(ItemStack stack, ItemEffect effect) {
        if (isSelf(stack)) return 0;
        return delegate(stack).getEffectLevel(stack, effect);
    }

    @Override
    public float getEffectEfficiency(ItemStack stack, ItemEffect effect) {
        if (isSelf(stack)) return 0f;
        return delegate(stack).getEffectEfficiency(stack, effect);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiersCached(ItemStack stack) {
        if (isSelf(stack)) return ImmutableMultimap.of();
        return delegate(stack).getAttributeModifiersCached(stack);
    }

    @Override
    public double getAttributeValue(ItemStack stack, Attribute attribute) {
        if (isSelf(stack)) return 0d;
        return delegate(stack).getAttributeValue(stack, attribute);
    }

    @Override
    public double getAttributeValue(ItemStack stack, Attribute attribute, double base) {
        if (isSelf(stack)) return base;
        return delegate(stack).getAttributeValue(stack, attribute, base);
    }

    @Override
    public ItemProperties getPropertiesCached(ItemStack stack) {
        if (isSelf(stack)) return new ItemProperties();
        return delegate(stack).getPropertiesCached(stack);
    }

    @Override
    public void tickProgression(LivingEntity entity, ItemStack stack, int multiplier) {
        if (isSelf(stack)) return;
        delegate(stack).tickProgression(entity, stack, multiplier);
    }

    @Override
    public void tickHoningProgression(@Nullable LivingEntity entity, ItemStack stack, int multiplier) {
        if (isSelf(stack)) return;
        delegate(stack).tickHoningProgression(entity, stack, multiplier);
    }

    @Override
    public void applyDamage(int amount, ItemStack stack, @Nullable LivingEntity responsibleEntity) {
        if (isSelf(stack)) return;
        if (SUPPRESS_DAMAGE.get()) return;
        delegate(stack).applyDamage(amount, stack, responsibleEntity);
    }

    @Override
    public void applyPositiveUsageEffects(LivingEntity entity, ItemStack stack, double multiplier) {
        if (isSelf(stack)) return;
        delegate(stack).applyPositiveUsageEffects(entity, stack, multiplier);
    }

    @Override
    public void applyNegativeUsageEffects(LivingEntity entity, ItemStack stack, double multiplier) {
        if (isSelf(stack)) return;
        delegate(stack).applyNegativeUsageEffects(entity, stack, multiplier);
    }

    @Override
    public String[] getMajorModuleKeys(ItemStack stack) {
        if (isSelf(stack)) return new String[0];
        return delegate(stack).getMajorModuleKeys(stack);
    }

    @Override
    public String[] getMinorModuleKeys(ItemStack stack) {
        if (isSelf(stack)) return new String[0];
        return delegate(stack).getMinorModuleKeys(stack);
    }

    @Override
    public String[] getRequiredModules(ItemStack stack) {
        if (isSelf(stack)) return new String[0];
        return delegate(stack).getRequiredModules(stack);
    }

    @Override
    public Collection<ItemModule> getAllModules(ItemStack stack) {
        if (isSelf(stack)) return Collections.emptyList();
        return delegate(stack).getAllModules(stack);
    }

    @Override
    public ItemModuleMajor[] getMajorModules(ItemStack stack) {
        if (isSelf(stack)) return new ItemModuleMajor[0];
        return delegate(stack).getMajorModules(stack);
    }

    @Override
    public ItemModule[] getMinorModules(ItemStack stack) {
        if (isSelf(stack)) return new ItemModule[0];
        return delegate(stack).getMinorModules(stack);
    }

    @Override
    public ItemModule getModuleFromSlot(ItemStack stack, String slot) {
        if (isSelf(stack)) return null;
        return delegate(stack).getModuleFromSlot(stack, slot);
    }

    @Override
    public ItemProperties getProperties(ItemStack stack) {
        if (isSelf(stack)) return new ItemProperties();
        return delegate(stack).getProperties(stack);
    }

    @Override
    public EffectData getEffectData(ItemStack stack) {
        if (isSelf(stack)) return new EffectData();
        return delegate(stack).getEffectData(stack);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack stack) {
        if (isSelf(stack)) return ImmutableMultimap.of();
        return delegate(stack).getAttributeModifiers(stack);
    }

    @Override
    public SynergyData[] getAllSynergyData(ItemStack stack) {
        if (isSelf(stack)) return new SynergyData[0];
        return delegate(stack).getAllSynergyData(stack);
    }

    @Override
    public int getHoneBase(ItemStack stack) {
        if (isSelf(stack)) return 100;
        return delegate(stack).getHoneBase(stack);
    }

    @Override
    public int getHoneIntegrityMultiplier(ItemStack stack) {
        if (isSelf(stack)) return 1;
        return delegate(stack).getHoneIntegrityMultiplier(stack);
    }

    @Override
    public boolean canGainHoneProgress(ItemStack stack) {
        if (isSelf(stack)) return false;
        return delegate(stack).canGainHoneProgress(stack);
    }
}