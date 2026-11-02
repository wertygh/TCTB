package net.wertygh.tctb;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.wertygh.tctb.config.TCTBConfig;
import net.wertygh.tctb.bridge.runtime.TConHandheldProxy;
import net.wertygh.tctb.modifier.TetraModifiers;
import slimeknights.tconstruct.library.modifiers.ModifierManager.ModifierRegistrationEvent;

@Mod(TCTBMod.MOD_ID)
public class TCTBMod {
    public static final String MOD_ID = "tctb";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final RegistryObject<TConHandheldProxy> PROXY_ITEM = ITEMS.register("proxy_handheld", TConHandheldProxy::new);

    public TCTBMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        bus.addListener(this::onModifierRegistration);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TCTBConfig.SPEC);
    }

    public void onModifierRegistration(ModifierRegistrationEvent event) {
        TetraModifiers.registerAll(event);
    }
}