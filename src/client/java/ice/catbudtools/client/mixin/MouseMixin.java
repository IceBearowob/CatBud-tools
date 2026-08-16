package ice.catbudtools.client.mixin;

import ice.catbudtools.client.specialtooltip.SpecialInfoOverlay;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseMixin {

    @Inject(
            method = "onScroll",
            at = @At("HEAD")
    )
    private void catbud$onMouseScroll(
            long window,
            double horizontal,
            double vertical,
            CallbackInfo ci
    ) {

        if (vertical == 0) {
            return;
        }

        SpecialInfoOverlay.changeEnchantPage(
                vertical > 0 ? -1 : 1
        );
    }
}
