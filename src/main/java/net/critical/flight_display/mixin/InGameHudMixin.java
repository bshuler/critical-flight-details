package net.critical.flight_display.mixin;

import net.critical.flight_display.hud.FlightDisplayHud;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
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
    private void onInit(MinecraftClient client, CallbackInfo ci) {
        this.flightDisplayHud = new FlightDisplayHud(client);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (client.player != null && client.player.isFallFlying()) {
            flightDisplayHud.render(context);
        }
    }
}
