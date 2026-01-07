package net.critical.flight_display.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.critical.flight_display.config.FlightDisplayConfig;

//? if fabric {
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
//?} elif neoforge {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
*///?} elif forge {
/*import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
*///?}

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
//? if >=1.20 {
import net.minecraft.client.gui.DrawContext;
//?} else {
/*import net.minecraft.client.util.math.MatrixStack;
*///?}

//? if fabric {
@Environment(EnvType.CLIENT)
//?} else {
/*@OnlyIn(Dist.CLIENT)
*///?}
public class FlightDisplayHud {
    private final MinecraftClient client;

    private double lastX = 0;
    private double lastY = 0;
    private double lastZ = 0;
    private long lastTime = 0;
    private int speed = 0;

    public FlightDisplayHud(MinecraftClient client) {
        this.client = client;
    }

    //? if >=1.20 {
    public void render(DrawContext context) {
    //?} else {
    /*public void render(MatrixStack matrices) {
    *///?}
        if (client.player == null || client.world == null) {
            return;
        }

        int screenHeight = client.getWindow().getScaledHeight();
        int screenWidth = client.getWindow().getScaledWidth();

        // Get config
        FlightDisplayConfig config = FlightDisplayConfig.getInstance();

        // Calculate positions from config
        double left = screenWidth * config.hudLeftPosition;
        double right = screenWidth * config.hudRightPosition;
        double top = screenHeight * config.hudLeftPosition;
        double bottom = screenHeight * config.hudRightPosition;
        double middleHeight = screenHeight / 2.0;
        double heightOfDisplay = bottom - top;
        int numberOfHashes = 11;
        double distanceBetweenHashes = heightOfDisplay / numberOfHashes;

        // Get colors from config
        int pitchColor = config.pitchIndicatorColor;
        int horizonColor = config.horizonLineColor;
        int textColor = config.textColor;
        boolean showShadow = config.showTextShadow;

        // Get player pitch
        float pitch = client.player.getPitch(1.0f);
        int displayPitch = (int) pitch;
        double pitchOffset = (distanceBetweenHashes / 10) * (displayPitch % 10);

        // Draw pitch text (if enabled)
        if (config.showPitchIndicator) {
            String pitchText = String.format("Pitch: %d", (int) (-pitch));
            //? if >=1.20 {
            context.drawText(client.textRenderer, pitchText, (int) left + 10, (int) middleHeight, textColor, showShadow);
            //?} else {
            /*if (showShadow) {
                client.textRenderer.drawWithShadow(matrices, pitchText, (int) left + 10, (int) middleHeight, textColor);
            } else {
                client.textRenderer.draw(matrices, pitchText, (int) left + 10, (int) middleHeight, textColor);
            }
            *///?}
        }

        // Draw speed text (if enabled)
        if (config.showSpeedDisplay) {
            String speedText = String.format("Speed: %d", speed);
            //? if >=1.20 {
            context.drawText(client.textRenderer, speedText, (int) left + 10, (int) bottom, textColor, showShadow);
            //?} else {
            /*if (showShadow) {
                client.textRenderer.drawWithShadow(matrices, speedText, (int) left + 10, (int) bottom, textColor);
            } else {
                client.textRenderer.draw(matrices, speedText, (int) left + 10, (int) bottom, textColor);
            }
            *///?}
        }

        // Draw altitude display (if enabled)
        if (config.showAltitudeDisplay) {
            int altitudeColor = config.altitudeColor;
            int yOffset = 0;

            // Absolute altitude (Y coordinate / MSL)
            if (config.showAltitudeAbsolute) {
                int absoluteAlt = (int) client.player.getY();
                String altText = String.format("Alt: %d", absoluteAlt);
                //? if >=1.20 {
                context.drawText(client.textRenderer, altText, (int) right + 10, (int) middleHeight + yOffset, altitudeColor, showShadow);
                //?} else {
                /*if (showShadow) {
                    client.textRenderer.drawWithShadow(matrices, altText, (int) right + 10, (int) middleHeight + yOffset, altitudeColor);
                } else {
                    client.textRenderer.draw(matrices, altText, (int) right + 10, (int) middleHeight + yOffset, altitudeColor);
                }
                *///?}
                yOffset += 12;
            }

            // Height above ground (AGL)
            if (config.showAltitudeAboveGround) {
                int groundHeight = getGroundHeight();
                int agl = (int) client.player.getY() - groundHeight;
                String aglText = String.format("AGL: %d", agl);
                //? if >=1.20 {
                context.drawText(client.textRenderer, aglText, (int) right + 10, (int) middleHeight + yOffset, altitudeColor, showShadow);
                //?} else {
                /*if (showShadow) {
                    client.textRenderer.drawWithShadow(matrices, aglText, (int) right + 10, (int) middleHeight + yOffset, altitudeColor);
                } else {
                    client.textRenderer.draw(matrices, aglText, (int) right + 10, (int) middleHeight + yOffset, altitudeColor);
                }
                *///?}
            }
        }

        // Draw heading/compass display (if enabled)
        if (config.showHeadingDisplay) {
            int headingColor = config.headingColor;
            float yaw = client.player.getYaw(1.0f);

            // Normalize yaw to 0-360
            float heading = ((yaw % 360) + 360) % 360;
            String direction = getCardinalDirection(heading);
            String headingText = String.format("HDG: %.0f%s %s", heading, "\u00B0", direction);

            //? if >=1.20 {
            context.drawText(client.textRenderer, headingText, (int) right + 10, (int) top, headingColor, showShadow);
            //?} else {
            /*if (showShadow) {
                client.textRenderer.drawWithShadow(matrices, headingText, (int) right + 10, (int) top, headingColor);
            } else {
                client.textRenderer.draw(matrices, headingText, (int) right + 10, (int) top, headingColor);
            }
            *///?}
        }

        // Draw pitch indicator hash marks (if enabled)
        if (config.showPitchIndicator) {
            for (double hashY = top; hashY <= bottom + distanceBetweenHashes; hashY += distanceBetweenHashes) {
                double hashYOffset = hashY + pitchOffset;
                if (hashYOffset >= top && hashYOffset <= bottom) {
                    drawLine((int) (left - 10), (int) hashYOffset, (int) left, (int) hashYOffset, pitchColor);
                }
            }
        }

        // Draw vertical reference lines (if enabled)
        if (config.showHorizonLines) {
            drawLine((int) left, (int) top, (int) left, (int) bottom, pitchColor);
            drawLine((int) right, (int) top, (int) right, (int) bottom, horizonColor);
        }

        // Calculate speed every N ticks (from config)
        long currentTime = client.world.getTime();

        if (currentTime > lastTime + config.speedUpdateInterval) {
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

    /**
     * Get the ground height below the player
     */
    private int getGroundHeight() {
        int playerX = (int) Math.floor(client.player.getX());
        int playerY = (int) Math.floor(client.player.getY());
        int playerZ = (int) Math.floor(client.player.getZ());

        // Search downward for solid ground
        for (int y = playerY; y >= client.world.getBottomY(); y--) {
            if (!client.world.isAir(new net.minecraft.util.math.BlockPos(playerX, y, playerZ))) {
                return y + 1;
            }
        }
        return client.world.getBottomY();
    }

    /**
     * Get cardinal direction from heading
     */
    private String getCardinalDirection(float heading) {
        // Minecraft: 0 = South, 90 = West, 180 = North, 270 = East
        if (heading >= 337.5 || heading < 22.5) return "S";
        if (heading >= 22.5 && heading < 67.5) return "SW";
        if (heading >= 67.5 && heading < 112.5) return "W";
        if (heading >= 112.5 && heading < 157.5) return "NW";
        if (heading >= 157.5 && heading < 202.5) return "N";
        if (heading >= 202.5 && heading < 247.5) return "NE";
        if (heading >= 247.5 && heading < 292.5) return "E";
        if (heading >= 292.5 && heading < 337.5) return "SE";
        return "";
    }

    private void drawLine(int x1, int y1, int x2, int y2, int color) {
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
        //? if >=1.21 {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        buffer.vertex(x1, y1, 0).color(red, green, blue, alpha);
        buffer.vertex(x2, y2, 0).color(red, green, blue, alpha);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        //?} elif >=1.19.3 {
        /*RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        buffer.vertex(x1, y1, 0).color(red, green, blue, alpha).next();
        buffer.vertex(x2, y2, 0).color(red, green, blue, alpha).next();
        Tessellator.getInstance().draw();
        *///?} else {
        /*RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        buffer.vertex(x1, y1, 0).color(red, green, blue, alpha).next();
        buffer.vertex(x2, y2, 0).color(red, green, blue, alpha).next();
        Tessellator.getInstance().draw();
        *///?}

        RenderSystem.disableBlend();
    }
}
