package de.maxhenkel.peek.mixin;

import de.maxhenkel.peek.tooltips.ClientContainerTooltip;
import de.maxhenkel.peek.tooltips.ContainerTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientTooltipComponent.class)
public interface ClientTooltipComponentMixin {

    @Inject(method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;", at = @At("HEAD"), cancellable = true)
    private static void create(TooltipComponent component, CallbackInfoReturnable<ClientTooltipComponent> ci) {
        if (component instanceof ContainerTooltip tooltip) {
            ci.setReturnValue(new ClientContainerTooltip(tooltip));
        }
    }

}
