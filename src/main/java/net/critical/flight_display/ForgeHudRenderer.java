//? if forge && >=1.19.3 {
/*package net.critical.flight_display;

import net.critical.flight_display.hud.FlightDisplayHud;
import net.minecraft.client.MinecraftClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class ForgeHudRenderer {
    private static FlightDisplayHud flightDisplayHud;

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new ForgeHudRenderer());
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !client.player.isFallFlying()) {
            return;
        }

        if (flightDisplayHud == null) {
            flightDisplayHud = new FlightDisplayHud(client);
        }

        //? if >=1.20 {
        /*flightDisplayHud.render(event.getGuiGraphics());
        *///?} else {
        flightDisplayHud.render(event.getPoseStack());
        //?}
    }
}
*///?} elif forge {
/*package net.critical.flight_display;

import net.critical.flight_display.hud.FlightDisplayHud;
import net.minecraft.client.MinecraftClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

@OnlyIn(Dist.CLIENT)
public class ForgeHudRenderer {
    private static FlightDisplayHud flightDisplayHud;

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new ForgeHudRenderer());
    }

    @SubscribeEvent
    public void onRenderGui(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !client.player.isFallFlying()) {
            return;
        }

        if (flightDisplayHud == null) {
            flightDisplayHud = new FlightDisplayHud(client);
        }

        flightDisplayHud.render(event.getMatrixStack());
    }
}
*///?}
