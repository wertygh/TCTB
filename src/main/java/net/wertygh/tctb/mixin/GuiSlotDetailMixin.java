package net.wertygh.tctb.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.blocks.workbench.gui.GuiSlotDetail;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import net.wertygh.tctb.bridge.TConSlotBridge;

import java.util.Map;

@Mixin(value = GuiSlotDetail.class, remap = false)
public class GuiSlotDetailMixin {
    @Inject(method = "onTileEntityChange", at = @At("HEAD"))
    private void tctb$pushContextOnTileChange(Player player, WorkbenchTile tileEntity,
                                               ItemStack itemStack, String selectedSlot,
                                               UpgradeSchematic currentSchematic,
                                               CallbackInfo ci) {
        TConSlotBridge.pushSlotContext(selectedSlot);
    }

    @Inject(method = "onTileEntityChange", at = @At("RETURN"))
    private void tctb$popContextOnTileChange(Player player, WorkbenchTile tileEntity,
                                              ItemStack itemStack, String selectedSlot,
                                              UpgradeSchematic currentSchematic,
                                              CallbackInfo ci) {
        TConSlotBridge.popSlotContext();
    }

    @Inject(method = "update", at = @At("HEAD"))
    private void tctb$pushContextOnUpdate(Player player, WorkbenchTile tileEntity,
                                          Map<ToolAction, Integer> availableTools,
                                          CallbackInfo ci) {
        TConSlotBridge.pushSlotContext(tileEntity.getCurrentSlot());
    }

    @Inject(method = "update", at = @At("RETURN"))
    private void tctb$popContextOnUpdate(Player player, WorkbenchTile tileEntity,
                                         Map<ToolAction, Integer> availableTools,
                                         CallbackInfo ci) {
        TConSlotBridge.popSlotContext();
    }

    @Inject(method = "updatePreview", at = @At("HEAD"))
    private void tctb$pushContextOnPreview(UpgradeSchematic schematic, String slot, 
                                           ItemStack itemStack, ItemStack previewStack,
                                           CallbackInfo ci) {
        TConSlotBridge.pushSlotContext(slot);
    }

    @Inject(method = "updatePreview", at = @At("RETURN"))
    private void tctb$popContextOnPreview(UpgradeSchematic schematic, String slot,
                                          ItemStack itemStack, ItemStack previewStack,
                                          CallbackInfo ci) {
        TConSlotBridge.popSlotContext();
    }
}