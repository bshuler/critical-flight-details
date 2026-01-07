//? if neoforge {
/*package net.critical.flight_display;

import net.critical.flight_display.hud.FlightDisplayHud;
import net.minecraft.client.MinecraftClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

@OnlyIn(Dist.CLIENT)
public class NeoForgeHudRenderer {
    private static FlightDisplayHud flightDisplayHud;

    public static void register() {
        NeoForge.EVENT_BUS.register(new NeoForgeHudRenderer());
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiLayerEvent.Post event) {
        if (!event.getName().equals(VanillaGuiLayers.HOTBAR)) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !client.player.isFallFlying()) {
            return;
        }

        if (flightDisplayHud == null) {
            flightDisplayHud = new FlightDisplayHud(client);
        }

        flightDisplayHud.render(event.getGuiGraphics());
    }
}
*///?}
