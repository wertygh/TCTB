package net.wertygh.tctb.modifier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.wertygh.tctb.bridge.data.TConAttributeData;
import net.wertygh.tctb.bridge.runtime.TetraAbilityBridge;
import org.jetbrains.annotations.NotNull;
import se.mickelus.tetra.effect.ItemEffectHandler;
import se.mickelus.tetra.items.modular.IModularItem;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IModDataView;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class TetraBridgeModifier extends Modifier {
    public static final ResourceLocation DATA_KEY = new ResourceLocation("tctb", "tetra_bridge");

    public TetraBridgeModifier() {
        super(buildHooks());
    }

    @Override
    public Component getDisplayName(int level) {
        return super.getDisplayName();
    }

    @Override
    public boolean shouldDisplay(boolean advanced) {
        return false;
    }

    private static ModuleHookMap buildHooks() {
        ModuleHookMap.Builder builder = ModuleHookMap.builder();
        builder.addHook((tool, modifier, action) -> {
            if (tool.isBroken()) return false;
            CompoundTag data = getBridgeData(tool.getPersistentData());
            if (!data.contains("tools", Tag.TAG_COMPOUND)) return false;
            CompoundTag tools = data.getCompound("tools");
            String key = action.name();
            return tools.contains(key, Tag.TAG_INT) && tools.getInt(key) > 0;
        }, ModifierHooks.TOOL_ACTION);

        builder.addHook(new MeleeHitModifierHook() {
            @Override
            public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, @NotNull ToolAttackContext context, float damageDealt) {
                LivingEntity attacker = context.getAttacker();
                LivingEntity target = context.getLivingTarget();
                if (target == null) return;
                ItemStack stack = attacker.getItemInHand(context.getHand());
                if (stack.isEmpty()) return;
                if (TetraAbilityBridge.isBridgeMelee(stack)) {
                    TetraAbilityBridge.hurtEnemy(stack, target, attacker);
                } else if (stack.getItem() instanceof IModularItem modularItem) {
                    ItemEffectHandler.applyHitEffects(stack, target, attacker);
                    modularItem.tickHoningProgression(attacker, stack, 1);
                }
            }
        }, ModifierHooks.MELEE_HIT);

        builder.addHook((tool, modifier, context) -> {
            LivingEntity entity = context.getLiving();
            if (entity == null) return;
            ItemStack stack = entity.getMainHandItem();
            if (!stack.isEmpty() && stack.getItem() instanceof IModularItem modularItem) {
                modularItem.tickHoningProgression(entity, stack, 1);
            }
        }, ModifierHooks.BLOCK_BREAK);

        builder.addHook((tool, modifier, slot, consumer) ->
                TConAttributeData.forEach(getBridgeData(tool.getPersistentData()), consumer),
                ModifierHooks.ATTRIBUTES);
        
        return builder.build();
    }

    private static CompoundTag getBridgeData(IModDataView data) {
        return data.get(DATA_KEY, CompoundTag::getCompound);
    }
}