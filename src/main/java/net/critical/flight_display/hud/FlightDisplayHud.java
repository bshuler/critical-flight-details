package net.critical.flight_display.hud;

import com.mojang.blaze3d.systems.RenderSystem;

//? if fabric {
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
//?} else if neoforge {
/*
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.*;
import com.mojang.blaze3d.vertex.*;
*///?}

//? if fabric {
@Environment(EnvType.CLIENT)
//?} else if neoforge {
/*@OnlyIn(Dist.CLIENT)*///?}
public class FlightDisplayHud {
    private static final int RED_COLOR = 0xFFFF0000;
    private static final int GREEN_COLOR = 0xFF00FF00;

    //? if fabric {
    private final MinecraftClient client;
    //?} else if neoforge {
    /*private final Minecraft client;*///?}

    private double lastX = 0;
    private double lastY = 0;
    private double lastZ = 0;
    private long lastTime = 0;
    private int speed = 0;

    //? if fabric {
    public FlightDisplayHud(MinecraftClient client) {
        this.client = client;
    }

    public void render(DrawContext context) {
    //?} else if neoforge {
    /*
    public FlightDisplayHud(Minecraft client) {
        this.client = client;
    }

    public void render(GuiGraphics context) {
    *///?}
        if (client.player == null || client.level == null) {
            return;
        }

        int screenHeight = client.getWindow().getGuiScaledHeight();
        int screenWidth = client.getWindow().getGuiScaledWidth();

        double factor = 3.0;

        double top = screenHeight / factor;
        double left = screenWidth / factor;
        double right = (screenWidth / factor) * (factor - 1);
        double bottom = (screenHeight / factor) * (factor - 1);
        double middleHeight = screenHeight / 2.0;
        double heightOfDisplay = bottom - top;
        int numberOfHashes = 11;
        double distanceBetweenHashes = heightOfDisplay / numberOfHashes;

        // Get player pitch
        float pitch = client.player.getXRot();
        int displayPitch = (int) pitch;
        double pitchOffset = (distanceBetweenHashes / 10) * (displayPitch % 10);

        // Draw pitch text
        String pitchText = String.format("Pitch: %d", (int) (-pitch));
        //? if fabric {
        context.drawText(client.textRenderer, pitchText, (int) left + 10, (int) middleHeight, RED_COLOR, true);
        //?} else if neoforge {
        /*context.drawString(client.font, pitchText, (int) left + 10, (int) middleHeight, RED_COLOR, true);*///?}

        // Draw speed text
        String speedText = String.format("Speed: %d", speed);
        //? if fabric {
        context.drawText(client.textRenderer, speedText, (int) left + 10, (int) bottom, RED_COLOR, true);
        //?} else if neoforge {
        /*context.drawString(client.font, speedText, (int) left + 10, (int) bottom, RED_COLOR, true);*///?}

        // Draw pitch indicator hash marks
        for (double hashY = top; hashY <= bottom + distanceBetweenHashes; hashY += distanceBetweenHashes) {
            double hashYOffset = hashY + pitchOffset;
            if (hashYOffset >= top && hashYOffset <= bottom) {
                drawLine(context, (int) (left - 10), (int) hashYOffset, (int) left, (int) hashYOffset, RED_COLOR);
            }
        }

        // Draw vertical reference lines
        drawLine(context, (int) left, (int) top, (int) left, (int) bottom, RED_COLOR);
        drawLine(context, (int) right, (int) top, (int) right, (int) bottom, GREEN_COLOR);

        // Calculate speed every 10 ticks
        //? if fabric {
        long currentTime = client.world.getTime();
        //?} else if neoforge {
        /*long currentTime = client.level.getGameTime();*///?}

        if (currentTime > lastTime + 10) {
            double dx = client.player.getX() - lastX;
            double dy = client.player.getY() - lastY;
            double dz = client.player.getZ() - lastZ;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            long timeDelta = currentTime - lastTime;

            if (timeDelta > 0) {
                speed = (int) (distance / timeDelta * 20); // Convert to blocks per second
            }

            lastTime = currentTime;
            lastX = client.player.getX();
            lastY = client.player.getY();
            lastZ = client.player.getZ();
        }
    }

    //? if fabric {
    private void drawLine(DrawContext context, int x1, int y1, int x2, int y2, int color) {
    //?} else if neoforge {
    /*private void drawLine(GuiGraphics context, int x1, int y1, int x2, int y2, int color) {*///?}
        // Extract color components
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        if (alpha == 0) {
            alpha = 255;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        //? if fabric {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        buffer.vertex(x1, y1, 0).color(red, green, blue, alpha);
        buffer.vertex(x2, y2, 0).color(red, green, blue, alpha);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        //?} else if neoforge {
        /*
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(x1, y1, 0).setColor(red, green, blue, alpha);
        buffer.addVertex(x2, y2, 0).setColor(red, green, blue, alpha);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        *///?}
        RenderSystem.disableBlend();
    }
}
