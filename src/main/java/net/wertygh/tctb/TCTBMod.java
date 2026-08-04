package net.wertygh.tctb;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.wertygh.tctb.modifier.TetraModifiers;
import slimeknights.tconstruct.library.modifiers.ModifierManager.ModifierRegistrationEvent;

@Mod(TCTBMod.MOD_ID)
public class TCTBMod {
    public static final String MOD_ID = "tctb";

    public TCTBMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::onModifierRegistration);
    }

    public void onModifierRegistration(ModifierRegistrationEvent event) {
        TetraModifiers.registerAll(event);
    }
}