package net.wertygh.tctb.modifier;

import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.ModifierManager;

public class TetraModifiers {
    public static final TetraBridgeModifier TETRA_BRIDGE = new TetraBridgeModifier();
    public static final ModifierId BRIDGE_ID = id("tetra_bridge");

    public static void registerAll(
            ModifierManager.ModifierRegistrationEvent event) {
        event.registerStatic(BRIDGE_ID, TETRA_BRIDGE);
    }

    private static ModifierId id(String name) {
        return new ModifierId(new ResourceLocation("tctb", name));
    }
}