package net.wertygh.tctb.modifier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public class TetraBridgeModifier extends Modifier {
    public static final ResourceLocation DATA_KEY = new ResourceLocation("tctb", "tetra_bridge");

    public TetraBridgeModifier() {
        super(buildHooks());
    }
    
    @Override
    public Component getDisplayName(int level) {
        return super.getDisplayName();
    }
    
    private static ModuleHookMap buildHooks() {
        ModuleHookMap.Builder builder = ModuleHookMap.builder();
        builder.addHook(
                (context, modifier, statBuilder) -> {
                    CompoundTag data = getBridgeData(context.getPersistentData());

                    if (data.contains("mining_speed", Tag.TAG_FLOAT))
                        ToolStats.MINING_SPEED.add(statBuilder, data.getFloat("mining_speed"));
                    if (data.contains("attack_damage", Tag.TAG_FLOAT))
                        ToolStats.ATTACK_DAMAGE.add(statBuilder, data.getFloat("attack_damage"));
                    if (data.contains("attack_speed", Tag.TAG_FLOAT))
                        ToolStats.ATTACK_SPEED.add(statBuilder, data.getFloat("attack_speed"));
                    if (data.contains("durability", Tag.TAG_INT))
                        ToolStats.DURABILITY.add(statBuilder, data.getInt("durability"));
                },
            ModifierHooks.TOOL_STATS
        );

        builder.addHook(
                (tool, modifier, toolAction) -> {
                    CompoundTag data = getBridgeData(tool.getPersistentData());
                    if (!data.contains("tools", Tag.TAG_COMPOUND)) return false;
                    return data.getCompound("tools").contains(toolAction.name());
                },
            ModifierHooks.TOOL_ACTION
        );

        builder.addHook(
            new MeleeHitModifierHook() {
                @Override
                public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier,
                                          @NotNull ToolAttackContext context, float damageDealt) {
                    LivingEntity attacker = context.getAttacker();
                    LivingEntity target = context.getLivingTarget();
                    if (target == null) return;

                    ItemStack stack = attacker.getItemInHand(context.getHand());
                    if (stack.isEmpty()) return;

                    if (stack.getItem() instanceof IModularItem modularItem) {
                        ItemEffectHandler.applyHitEffects(stack, target, attacker);
                        modularItem.tickHoningProgression(attacker, stack, 1);
                    }
                }
            },
            ModifierHooks.MELEE_HIT
        );

        builder.addHook(
                (tool, modifier, context) -> {
                    LivingEntity entity = context.getLiving();
                    ItemStack stack = entity.getMainHandItem();
                    if (!stack.isEmpty() && stack.getItem() instanceof IModularItem modularItem) {
                        modularItem.tickHoningProgression(entity, stack, 1);
                    }
                },
            ModifierHooks.BLOCK_BREAK
        );

        return builder.build();
    }

    private static CompoundTag getBridgeData(IModDataView persData) {
        return persData.get(DATA_KEY, CompoundTag::getCompound);
    }
}