//? if forge {
package net.critical.flight_display;

import net.critical.flight_display.hud.FlightDisplayHud;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ForgeHudRenderer {
    private static FlightDisplayHud flightDisplayHud;

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new ForgeHudRenderer());
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiOverlayEvent.Post event) {
        // Only render after the hotbar overlay
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
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
// This file is only used by Forge (1.20.1 and earlier).
// NeoForge uses NeoForgeHudRenderer.java, Fabric uses mixins.
package net.critical.flight_display;
public class ForgeHudRenderer {
    public static void register() {}
}
*///?}
