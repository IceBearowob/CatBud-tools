package ice.catbudtools.client.mixin;

import ice.catbudtools.client.SpecialItemInfoOverlay;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(
            method = "onMouseScroll",
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

        SpecialItemInfoOverlay.changeEnchantPage(
                vertical > 0 ? -1 : 1
        );
    }
}