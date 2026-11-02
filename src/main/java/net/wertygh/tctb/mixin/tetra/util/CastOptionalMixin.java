package net.wertygh.tctb.mixin.tetra.util;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import net.wertygh.tctb.TCTBMod;

import java.util.Optional;

@Mixin(value = CastOptional.class, remap = false)
public class CastOptionalMixin {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "cast", at = @At("HEAD"), cancellable = true)
    private static void tctb$cast(Object obj, Class clazz, CallbackInfoReturnable<Optional> cir) {
        if (clazz == ItemModularHandheld.class
                && !(obj instanceof ItemModularHandheld)
                && obj instanceof IModularItem
                && obj instanceof IModifiable) {
            cir.setReturnValue(Optional.of(TCTBMod.PROXY_ITEM.get()));
        }
    }
}