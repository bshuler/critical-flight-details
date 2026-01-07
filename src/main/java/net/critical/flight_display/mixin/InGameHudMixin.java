//? if fabric {
package net.critical.flight_display.mixin;

import net.critical.flight_display.hud.FlightDisplayHud;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
//? if >=1.21 {
import net.minecraft.client.render.RenderTickCounter;
//?}
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Unique
    private FlightDisplayHud flightDisplayHud;

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void flight_display$onInit(MinecraftClient client, CallbackInfo ci) {
        this.flightDisplayHud = new FlightDisplayHud(client);
    }

    //? if >=1.21 {
    @Inject(method = "render", at = @At("TAIL"))
    private void flight_display$onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (client.player != null && client.player.isFallFlying()) {
            flightDisplayHud.render(context);
        }
    }
    //?} else {
    /*
    @Inject(method = "render", at = @At("TAIL"))
    private void flight_display$onRender(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (client.player != null && client.player.isFallFlying()) {
            flightDisplayHud.render(context);
        }
    }
    *///?}
}
//?} else {
/*
// This file is only used by Fabric. NeoForge/Forge use event handlers instead.
// See NeoForgeHudRenderer.java or ForgeHudRenderer.java for those implementations.
package net.critical.flight_display.mixin;
public abstract class InGameHudMixin {}
*///?}
