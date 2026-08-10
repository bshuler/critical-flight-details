package net.critical.flight_display;

import net.critical.flight_display.hud.FlightHudRenderer;

//? if fabric {
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
//?} elif neoforge {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
*///?} elif forge && <1.19 {
/*import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.client.gui.OverlayRegistry;
*///?} elif forge {
/*import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
*///?}

/**
 * Client entry point. This mod is client-only (no server logic at all), so
 * everything - including this class - lives under {@code src/main/java}: the
 * Stonecraft/Loom setup used here never calls
 * {@code loom.splitEnvironmentSourceSets()}, so a {@code src/client/java}
 * directory is never wired into any compile task and any code placed there
 * is silently skipped ({@code NO-SOURCE}). See PLAN.md for how this was
 * verified.
 */
//? if fabric {
public final class FlightDisplayClient implements ClientModInitializer {
    public static final String MOD_ID = "flight_display";

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(FlightHudRenderer::onHudRender);
    }
}
//?} elif neoforge {
/*@Mod(FlightDisplayClient.MOD_ID)
public final class FlightDisplayClient {
    public static final String MOD_ID = "flight_display";

    public FlightDisplayClient(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(this::onRenderGuiPost);
    }

    private void onRenderGuiPost(RenderGuiEvent.Post event) {
        FlightHudRenderer.onRenderGuiPost(event);
    }
}
*///?} elif forge && <1.19 {
/*@Mod(FlightDisplayClient.MOD_ID)
public final class FlightDisplayClient {
    public static final String MOD_ID = "flight_display";

    public FlightDisplayClient() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        OverlayRegistry.registerOverlayTop("flight_display", FlightHudRenderer::renderLegacyOverlay);
    }
}
*///?} elif forge {
/*@Mod(FlightDisplayClient.MOD_ID)
public final class FlightDisplayClient {
    public static final String MOD_ID = "flight_display";

    public FlightDisplayClient() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterGuiOverlays);
    }

    private void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("flight_display", FlightHudRenderer::renderOverlay);
    }
}
*///?}
