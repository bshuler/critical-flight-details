package net.critical.flight_display.hud;

import net.critical.flight_display.FlightHudMath;
import net.critical.flight_display.FlightHudMath.Layout;
import net.critical.flight_display.FlightHudMath.SpeedSample;

// Mojang official mappings are used across the whole matrix (Stonecraft's default
// namespace, matching the sibling critical-orientation mod): Fabric, Forge, and
// NeoForge all resolve Minecraft/LocalPlayer to the same names below. See
// versions/dependencies/*.properties, none of which set yarn_mappings.
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

//? if fabric && <1.20 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
*///?} elif fabric && <1.21 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
*///?} elif fabric {
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
//?} elif neoforge {
/*import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
*///?} elif forge && <1.19 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraftforge.client.gui.ForgeIngameGui;
*///?} elif forge && <1.20 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
*///?} elif forge {
/*import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
*///?}

/**
 * Loader/version-conditional HUD drawing. All layout, pitch, and speed math
 * is delegated to {@link FlightHudMath}, which has no Minecraft-class
 * dependency; this class only fetches loader-specific player/screen state
 * and issues the actual draw calls, matching the original's colors: RED
 * left/right rails and hash marks, RED pitch and speed text.
 */
public final class FlightHudRenderer {

    private static final int RED = 0xFFFF0000;

    private static SpeedSample speedSample = FlightHudMath.initialSpeedSample();

    private FlightHudRenderer() {
    }

    //? if fabric && <1.20 {
    /*public static void onHudRender(PoseStack poseStack, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || !player.isFallFlying()) {
            return;
        }

        int scaledWidth = client.getWindow().getGuiScaledWidth();
        int scaledHeight = client.getWindow().getGuiScaledHeight();
        Layout layout = FlightHudMath.computeLayout(scaledWidth, scaledHeight);

        float pitch = player.getXRot();
        int rawTruncatedPitch = FlightHudMath.truncatedPitch(pitch);
        int displayPitch = FlightHudMath.invertedDisplayPitch(pitch);
        double pitchOffset = FlightHudMath.pitchOffset(layout.distanceBetweenHashes(), rawTruncatedPitch);

        // Entity.level() (a method) doesn't exist before 1.20; use the older getLevel() accessor.
        speedSample = FlightHudMath.sampleSpeed(
                player.getLevel().getGameTime(), player.getX(), player.getY(), player.getZ(), speedSample);

        GuiComponent.fill(poseStack, (int) layout.left(), (int) layout.top(), (int) layout.left() + 1, (int) layout.bottom(), RED);
        GuiComponent.fill(poseStack, (int) layout.right(), (int) layout.top(), (int) layout.right() + 1, (int) layout.bottom(), RED);

        for (double y : FlightHudMath.hashLineYs(layout, pitchOffset)) {
            GuiComponent.fill(poseStack, (int) layout.left(), (int) y, (int) layout.right() + 1, (int) y + 1, RED);
        }

        client.font.draw(poseStack, "Pitch: " + displayPitch, (float) layout.left(), (float) layout.middleHeight() - 20, RED);
        client.font.draw(poseStack, "Speed: " + speedSample.speed(), (float) layout.left(), (float) layout.middleHeight() - 10, RED);
    }
    *///?} elif fabric && <1.21 {
    /*public static void onHudRender(GuiGraphics guiGraphics, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || !player.isFallFlying()) {
            return;
        }

        int scaledWidth = client.getWindow().getGuiScaledWidth();
        int scaledHeight = client.getWindow().getGuiScaledHeight();
        Layout layout = FlightHudMath.computeLayout(scaledWidth, scaledHeight);

        float pitch = player.getXRot();
        int rawTruncatedPitch = FlightHudMath.truncatedPitch(pitch);
        int displayPitch = FlightHudMath.invertedDisplayPitch(pitch);
        double pitchOffset = FlightHudMath.pitchOffset(layout.distanceBetweenHashes(), rawTruncatedPitch);

        speedSample = FlightHudMath.sampleSpeed(
                player.level().getGameTime(), player.getX(), player.getY(), player.getZ(), speedSample);

        guiGraphics.fill((int) layout.left(), (int) layout.top(), (int) layout.left() + 1, (int) layout.bottom(), RED);
        guiGraphics.fill((int) layout.right(), (int) layout.top(), (int) layout.right() + 1, (int) layout.bottom(), RED);

        for (double y : FlightHudMath.hashLineYs(layout, pitchOffset)) {
            guiGraphics.fill((int) layout.left(), (int) y, (int) layout.right() + 1, (int) y + 1, RED);
        }

        guiGraphics.drawString(client.font, "Pitch: " + displayPitch, (int) layout.left(), (int) layout.middleHeight() - 20, RED, false);
        guiGraphics.drawString(client.font, "Speed: " + speedSample.speed(), (int) layout.left(), (int) layout.middleHeight() - 10, RED, false);
    }
    *///?} elif fabric {
    public static void onHudRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || !player.isFallFlying()) {
            return;
        }

        int scaledWidth = client.getWindow().getGuiScaledWidth();
        int scaledHeight = client.getWindow().getGuiScaledHeight();
        Layout layout = FlightHudMath.computeLayout(scaledWidth, scaledHeight);

        float pitch = player.getXRot();
        int rawTruncatedPitch = FlightHudMath.truncatedPitch(pitch);
        int displayPitch = FlightHudMath.invertedDisplayPitch(pitch);
        double pitchOffset = FlightHudMath.pitchOffset(layout.distanceBetweenHashes(), rawTruncatedPitch);

        speedSample = FlightHudMath.sampleSpeed(
                player.level().getGameTime(), player.getX(), player.getY(), player.getZ(), speedSample);

        guiGraphics.fill((int) layout.left(), (int) layout.top(), (int) layout.left() + 1, (int) layout.bottom(), RED);
        guiGraphics.fill((int) layout.right(), (int) layout.top(), (int) layout.right() + 1, (int) layout.bottom(), RED);

        for (double y : FlightHudMath.hashLineYs(layout, pitchOffset)) {
            guiGraphics.fill((int) layout.left(), (int) y, (int) layout.right() + 1, (int) y + 1, RED);
        }

        guiGraphics.drawString(client.font, "Pitch: " + displayPitch, (int) layout.left(), (int) layout.middleHeight() - 20, RED, false);
        guiGraphics.drawString(client.font, "Speed: " + speedSample.speed(), (int) layout.left(), (int) layout.middleHeight() - 10, RED, false);
    }
    //?} elif neoforge {
    /*public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || !player.isFallFlying()) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int scaledWidth = client.getWindow().getGuiScaledWidth();
        int scaledHeight = client.getWindow().getGuiScaledHeight();
        Layout layout = FlightHudMath.computeLayout(scaledWidth, scaledHeight);

        float pitch = player.getXRot();
        int rawTruncatedPitch = FlightHudMath.truncatedPitch(pitch);
        int displayPitch = FlightHudMath.invertedDisplayPitch(pitch);
        double pitchOffset = FlightHudMath.pitchOffset(layout.distanceBetweenHashes(), rawTruncatedPitch);

        speedSample = FlightHudMath.sampleSpeed(
                player.level().getGameTime(), player.getX(), player.getY(), player.getZ(), speedSample);

        guiGraphics.fill((int) layout.left(), (int) layout.top(), (int) layout.left() + 1, (int) layout.bottom(), RED);
        guiGraphics.fill((int) layout.right(), (int) layout.top(), (int) layout.right() + 1, (int) layout.bottom(), RED);

        for (double y : FlightHudMath.hashLineYs(layout, pitchOffset)) {
            guiGraphics.fill((int) layout.left(), (int) y, (int) layout.right() + 1, (int) y + 1, RED);
        }

        guiGraphics.drawString(client.font, "Pitch: " + displayPitch, (int) layout.left(), (int) layout.middleHeight() - 20, RED, false);
        guiGraphics.drawString(client.font, "Speed: " + speedSample.speed(), (int) layout.left(), (int) layout.middleHeight() - 10, RED, false);
    }
    *///?} elif forge && <1.19 {
    /*public static void renderLegacyOverlay(ForgeIngameGui gui, PoseStack poseStack, float partialTick, int width, int height) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || !player.isFallFlying()) {
            return;
        }

        Layout layout = FlightHudMath.computeLayout(width, height);

        float pitch = player.getXRot();
        int rawTruncatedPitch = FlightHudMath.truncatedPitch(pitch);
        int displayPitch = FlightHudMath.invertedDisplayPitch(pitch);
        double pitchOffset = FlightHudMath.pitchOffset(layout.distanceBetweenHashes(), rawTruncatedPitch);

        // Entity.level() (a method) doesn't exist before 1.20; use the older getLevel() accessor.
        speedSample = FlightHudMath.sampleSpeed(
                player.getLevel().getGameTime(), player.getX(), player.getY(), player.getZ(), speedSample);

        GuiComponent.fill(poseStack, (int) layout.left(), (int) layout.top(), (int) layout.left() + 1, (int) layout.bottom(), RED);
        GuiComponent.fill(poseStack, (int) layout.right(), (int) layout.top(), (int) layout.right() + 1, (int) layout.bottom(), RED);

        for (double y : FlightHudMath.hashLineYs(layout, pitchOffset)) {
            GuiComponent.fill(poseStack, (int) layout.left(), (int) y, (int) layout.right() + 1, (int) y + 1, RED);
        }

        client.font.draw(poseStack, "Pitch: " + displayPitch, (float) layout.left(), (float) layout.middleHeight() - 20, RED);
        client.font.draw(poseStack, "Speed: " + speedSample.speed(), (float) layout.left(), (float) layout.middleHeight() - 10, RED);
    }
    *///?} elif forge && <1.20 {
    /*public static void renderOverlay(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || !player.isFallFlying()) {
            return;
        }

        Layout layout = FlightHudMath.computeLayout(screenWidth, screenHeight);

        float pitch = player.getXRot();
        int rawTruncatedPitch = FlightHudMath.truncatedPitch(pitch);
        int displayPitch = FlightHudMath.invertedDisplayPitch(pitch);
        double pitchOffset = FlightHudMath.pitchOffset(layout.distanceBetweenHashes(), rawTruncatedPitch);

        // Entity.level() (a method) doesn't exist before 1.20; use the older getLevel() accessor.
        speedSample = FlightHudMath.sampleSpeed(
                player.getLevel().getGameTime(), player.getX(), player.getY(), player.getZ(), speedSample);

        GuiComponent.fill(poseStack, (int) layout.left(), (int) layout.top(), (int) layout.left() + 1, (int) layout.bottom(), RED);
        GuiComponent.fill(poseStack, (int) layout.right(), (int) layout.top(), (int) layout.right() + 1, (int) layout.bottom(), RED);

        for (double y : FlightHudMath.hashLineYs(layout, pitchOffset)) {
            GuiComponent.fill(poseStack, (int) layout.left(), (int) y, (int) layout.right() + 1, (int) y + 1, RED);
        }

        client.font.draw(poseStack, "Pitch: " + displayPitch, (float) layout.left(), (float) layout.middleHeight() - 20, RED);
        client.font.draw(poseStack, "Speed: " + speedSample.speed(), (float) layout.left(), (float) layout.middleHeight() - 10, RED);
    }
    *///?} elif forge {
    /*public static void renderOverlay(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || !player.isFallFlying()) {
            return;
        }

        Layout layout = FlightHudMath.computeLayout(screenWidth, screenHeight);

        float pitch = player.getXRot();
        int rawTruncatedPitch = FlightHudMath.truncatedPitch(pitch);
        int displayPitch = FlightHudMath.invertedDisplayPitch(pitch);
        double pitchOffset = FlightHudMath.pitchOffset(layout.distanceBetweenHashes(), rawTruncatedPitch);

        speedSample = FlightHudMath.sampleSpeed(
                player.level().getGameTime(), player.getX(), player.getY(), player.getZ(), speedSample);

        guiGraphics.fill((int) layout.left(), (int) layout.top(), (int) layout.left() + 1, (int) layout.bottom(), RED);
        guiGraphics.fill((int) layout.right(), (int) layout.top(), (int) layout.right() + 1, (int) layout.bottom(), RED);

        for (double y : FlightHudMath.hashLineYs(layout, pitchOffset)) {
            guiGraphics.fill((int) layout.left(), (int) y, (int) layout.right() + 1, (int) y + 1, RED);
        }

        guiGraphics.drawString(client.font, "Pitch: " + displayPitch, (int) layout.left(), (int) layout.middleHeight() - 20, RED, false);
        guiGraphics.drawString(client.font, "Speed: " + speedSample.speed(), (int) layout.left(), (int) layout.middleHeight() - 10, RED, false);
    }
    *///?}
}
