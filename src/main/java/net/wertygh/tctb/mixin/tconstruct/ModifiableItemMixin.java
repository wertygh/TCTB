package net.wertygh.tctb.mixin.tconstruct;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.ItemProperties;
import se.mickelus.tetra.module.data.SynergyData;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import net.wertygh.tctb.bridge.ModularItemDelegates;
import net.wertygh.tctb.bridge.BridgeApi;
import net.wertygh.tctb.TCTBMod;

import javax.annotation.Nullable;
import java.util.Collection;

@Mixin(value = ModifiableItem.class, remap = false)
@Implements(@Interface(iface = IModularItem.class, prefix = "tctb$"))
public abstract class ModifiableItemMixin {
    @Unique
    private Cache<String, Multimap<Attribute, AttributeModifier>> tctb$attributeModifierCache;
    @Unique
    private Cache<String, EffectData> tctb$effectDataCache;
    @Unique
    private Cache<String, ItemProperties> tctb$propertyCache;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructed(CallbackInfo ci) {
        ensureCaches();
    }

    private void ensureCaches() {
        if (tctb$attributeModifierCache == null) {
            tctb$attributeModifierCache = CacheBuilder.newBuilder().maximumSize(64).build();
        }
        if (tctb$effectDataCache == null) {
            tctb$effectDataCache = CacheBuilder.newBuilder().maximumSize(64).build();
        }
        if (tctb$propertyCache == null) {
            tctb$propertyCache = CacheBuilder.newBuilder().maximumSize(64).build();
        }
    }

    @Inject(
        method = "setDamage(Lnet/minecraft/world/item/ItemStack;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void clampSetDamage(ItemStack stack, int damage, CallbackInfo ci) {
        if (ModularItemDelegates.clampDamage((Item) (Object) this, stack, damage)) {
            ci.cancel();
        }
    }

    public Item tctb$getItem() {
        return ModularItemDelegates.getItem((Item) (Object) this);
    }

    public void tctb$clearCaches() {
        ensureCaches();
        tctb$attributeModifierCache.invalidateAll();
        tctb$effectDataCache.invalidateAll();
        tctb$propertyCache.invalidateAll();
    }

    public String[] tctb$getMajorModuleKeys(ItemStack stack) {
        return ModularItemDelegates.getMajorModuleKeys((Item) (Object) this, stack);
    }

    public String[] tctb$getMinorModuleKeys(ItemStack stack) {
        return ModularItemDelegates.getMinorModuleKeys((Item) (Object) this, stack);
    }

    public String[] tctb$getRequiredModules(ItemStack stack) {
        return ModularItemDelegates.getRequiredModules((Item) (Object) this, stack);
    }

    public int tctb$getHoneBase(ItemStack stack) {
        return ModularItemDelegates.getHoneBase((Item) (Object) this, stack);
    }

    public int tctb$getHoneIntegrityMultiplier(ItemStack stack) {
        return ModularItemDelegates.getHoneIntegrityMultiplier((Item) (Object) this, stack);
    }

    public boolean tctb$canGainHoneProgress(ItemStack stack) {
        return ModularItemDelegates.canGainHoneProgress((Item) (Object) this, stack);
    }

    @Nullable
    public Cache<String, Multimap<Attribute, AttributeModifier>> tctb$getAttributeModifierCache() {
        ensureCaches();
        return tctb$attributeModifierCache;
    }

    @Nullable
    public Cache<String, EffectData> tctb$getEffectDataCache() {
        ensureCaches();
        return tctb$effectDataCache;
    }

    @Nullable
    public Cache<String, ItemProperties> tctb$getPropertyCache() {
        ensureCaches();
        return tctb$propertyCache;
    }

    public SynergyData[] tctb$getAllSynergyData(ItemStack stack) {
        return new SynergyData[0];
    }

    public ItemProperties tctb$getProperties(ItemStack stack) {
        return ModularItemDelegates.getProperties((Item) (Object) this, stack);
    }

    @Nullable
    public Multimap<Attribute, AttributeModifier> tctb$getAttributeModifiers(ItemStack stack) {
        return ModularItemDelegates.getAttributeModifiers((Item) (Object) this, stack);
    }

    public void tctb$assemble(ItemStack stack, Level world, float severity) {
        ModularItemDelegates.assemble((Item) (Object) this, stack, world, severity);
    }

    @Nullable
    public ItemModule tctb$getModuleFromSlot(ItemStack stack, String slot) {
        return ModularItemDelegates.getModuleFromSlot((Item) (Object) this, stack, slot);
    }

    public Collection<ItemModule> tctb$getAllModules(ItemStack stack) {
        return ModularItemDelegates.getAllModules((Item) (Object) this, stack);
    }

    public EffectData tctb$getEffectData(ItemStack stack) {
        return ModularItemDelegates.getEffectData((Item) (Object) this, stack);
    }

    public String[] tctb$getMajorModuleNames(ItemStack stack) {
        return ModularItemDelegates.getMajorModuleNames((Item) (Object) this, stack);
    }

    public String[] tctb$getMinorModuleNames(ItemStack stack) {
        return ModularItemDelegates.getMinorModuleNames((Item) (Object) this, stack);
    }

    public ItemModuleMajor[] tctb$getMajorModules(ItemStack stack) {
        return ModularItemDelegates.getMajorModules((Item) (Object) this, stack);
    }

    public ItemModule[] tctb$getMinorModules(ItemStack stack) {
        return ModularItemDelegates.getMinorModules((Item) (Object) this, stack);
    }

    @Inject(
        method = "getDestroySpeed(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)F",
        at = @At("RETURN"),
        cancellable = true,
        remap = true
    )
    private void bridgeGetDestroySpeed(ItemStack stack, BlockState state, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(ModularItemDelegates.getDestroySpeed(
            (Item) (Object) this, stack, state, cir.getReturnValue()));
    }

    @Inject(
        method = "isCorrectToolForDrops(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)Z",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void bridgeIsCorrectToolForDrops(ItemStack stack, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (BridgeApi.isBridgeMelee(stack)) {
            cir.setReturnValue(TCTBMod.PROXY_ITEM.get().isCorrectToolForDrops(stack, state));
        }
    }
}