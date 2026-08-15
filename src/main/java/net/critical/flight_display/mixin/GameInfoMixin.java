package net.critical.flight_display.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.critical.flight_display.hud.FlightDisplayHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

@Environment(EnvType.CLIENT)
@Mixin(value = InGameHud.class)
public abstract class GameInfoMixin {
    private FlightDisplayHud hudInfo;

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "<init>(Lnet/minecraft/client/MinecraftClient;)V", at = @At(value = "RETURN"))
    private void onInit(MinecraftClient client, CallbackInfo ci) {
        // Start Mixin
        System.out.println("Init Coordinates Mixin");
        this.hudInfo = new FlightDisplayHud(client);
//        addDrawableChild(this.hudInfo);
    }

//    @Inject(method = "METHOD NAME OR SIGNATURE", at = @At("INJECTION POINT REFERENCE"))
    //Inject into renderMainHud so overlay can be hidden when the hud is hidden
    @Inject(method = "renderMainHud", at = @At(value = "HEAD"))
    private void onDraw(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        if (minecraftClient.player != null) {
            if (minecraftClient.player.isGliding()) {
                this.hudInfo.draw(context);
            }
        }
    }

    @Inject(method = "resetDebugHudChunk", at = @At(value = "RETURN"))
    private void onReset(CallbackInfo ci) {
    }
}