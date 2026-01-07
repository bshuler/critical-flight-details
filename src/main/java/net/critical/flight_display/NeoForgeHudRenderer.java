//? if neoforge {
package net.critical.flight_display;

import net.critical.flight_display.hud.FlightDisplayHud;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = FlightDisplayClient.MOD_ID, value = Dist.CLIENT)
public class NeoForgeHudRenderer {
    private static FlightDisplayHud flightDisplayHud;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiLayerEvent.Post event) {
        // Only render after the main HUD layer
        if (!event.getName().equals(VanillaGuiLayers.CAMERA_OVERLAYS)) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || !client.player.isFallFlying()) {
            return;
        }

        // Lazy initialization of the HUD
        if (flightDisplayHud == null) {
            flightDisplayHud = new FlightDisplayHud(client);
        }

        flightDisplayHud.render(event.getGuiGraphics());
    }
}
//?} else {
/*
// This file is only used by NeoForge. Fabric uses mixins instead.
// See InGameHudMixin.java for the Fabric implementation.
package net.critical.flight_display;
public class NeoForgeHudRenderer {}
*///?}
