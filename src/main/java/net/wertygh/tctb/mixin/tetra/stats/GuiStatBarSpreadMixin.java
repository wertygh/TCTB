package net.wertygh.tctb.mixin.tetra.stats;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetra.gui.stats.GuiStats;
import se.mickelus.tetra.gui.stats.bar.GuiBar;
import se.mickelus.tetra.gui.stats.bar.GuiStatBar;
import se.mickelus.tetra.gui.stats.getter.ILabelGetter;
import se.mickelus.tetra.gui.stats.getter.LabelGetterBasic;
import se.mickelus.tetra.items.modular.IModularItem;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableBowItem;

@Mixin(value = GuiStatBar.class, remap = false)
public abstract class GuiStatBarSpreadMixin {
    @Shadow
    protected GuiBar bar;

    @Shadow
    protected ILabelGetter labelGetter;

    @Shadow
    protected boolean inverted;

    @Unique
    private boolean tctb$spreadOverrideActive;

    @Unique
    private ILabelGetter tctb$originalLabelGetter;

    @Unique
    private boolean tctb$originalInverted;

    @Unique
    private boolean tctb$originalBarInverted;

    @Inject(
        method = "update(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;Ljava/lang/String;)V",
        at = @At("HEAD")
    )
    private void tctb$configureSpreadDisplay(Player player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement, CallbackInfo ci) {
        if ((Object) this != GuiStats.spread) return;
        boolean bridgeBow = tctb$isTConBow(currentStack) || tctb$isTConBow(previewStack);
        GuiBarAccessorMixin accessor = (GuiBarAccessorMixin) (Object) bar;
        if (bridgeBow) {
            if (!tctb$spreadOverrideActive) {
                tctb$originalLabelGetter = labelGetter;
                tctb$originalInverted = inverted;
                tctb$originalBarInverted = accessor.tctb$getInvertedDiff();
                tctb$spreadOverrideActive = true;
            }
            inverted = true;
            accessor.tctb$setInvertedDiff(true);
            labelGetter = LabelGetterBasic.decimalLabelInverted;
        } else if (tctb$spreadOverrideActive) {
            inverted = tctb$originalInverted;
            accessor.tctb$setInvertedDiff(tctb$originalBarInverted);
            labelGetter = tctb$originalLabelGetter;
            tctb$originalLabelGetter = null;
            tctb$spreadOverrideActive = false;
        }
    }

    @Unique
    private static boolean tctb$isTConBow(ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && stack.getItem() instanceof ModifiableBowItem
                && stack.getItem() instanceof IModularItem;
    }
}