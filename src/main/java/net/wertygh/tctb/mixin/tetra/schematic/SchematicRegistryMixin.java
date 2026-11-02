package net.wertygh.tctb.mixin.tetra.schematic;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.CraftingContext;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import net.wertygh.tctb.bridge.BridgeApi;

@Mixin(value = SchematicRegistry.class, remap = false)
public class SchematicRegistryMixin {
    @Inject(
        method = "getSchematics(Lse/mickelus/tetra/module/schematic/CraftingContext;)[Lse/mickelus/tetra/module/schematic/UpgradeSchematic;",
        at = @At("HEAD")
    )
    private static void tctb$pushSchematicContext(CraftingContext context, CallbackInfoReturnable<UpgradeSchematic[]> cir) {
        BridgeApi.pushSlotContext(context.slot);
    }

    @Inject(
        method = "getSchematics(Lse/mickelus/tetra/module/schematic/CraftingContext;)[Lse/mickelus/tetra/module/schematic/UpgradeSchematic;",
        at = @At("RETURN")
    )
    private static void tctb$popSchematicContext(CraftingContext context, CallbackInfoReturnable<UpgradeSchematic[]> cir) {
        BridgeApi.popSlotContext();
    }

    @Inject(
        method = "getPreviewSchematics(Lse/mickelus/tetra/module/schematic/CraftingContext;Z)[Lse/mickelus/tetra/module/schematic/UpgradeSchematic;",
        at = @At("HEAD")
    )
    private static void tctb$pushPreviewSchematicContext(CraftingContext context, boolean ignoreRequirements, CallbackInfoReturnable<UpgradeSchematic[]> cir) {
        BridgeApi.pushSlotContext(context.slot);
    }

    @Inject(
        method = "getPreviewSchematics(Lse/mickelus/tetra/module/schematic/CraftingContext;Z)[Lse/mickelus/tetra/module/schematic/UpgradeSchematic;",
        at = @At("RETURN")
    )
    private static void tctb$popPreviewSchematicContext(CraftingContext context, boolean ignoreRequirements, CallbackInfoReturnable<UpgradeSchematic[]> cir) {
        BridgeApi.popSlotContext();
    }
}