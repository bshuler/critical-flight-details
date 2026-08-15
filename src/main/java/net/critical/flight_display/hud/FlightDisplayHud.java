package net.critical.flight_display.hud;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.gl.ShaderProgramKeys;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.RenderLayer;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import net.minecraft.util.Colors;
import java.awt.Color;
import java.lang.Math;


@Environment(EnvType.CLIENT)
public class FlightDisplayHud implements Drawable {
    private final MinecraftClient client;
    private final TextRenderer fontRenderer;
    private ClientPlayerEntity player;
    private double last_x=0;
    private double last_y=0;
    private double last_z=0;
    private long last_time=0;
    private int speed=0;

    public FlightDisplayHud(MinecraftClient client) {
        this.client = client;
        this.fontRenderer = client.textRenderer;
    }

    public void draw(DrawContext context) {

        int height = client.getWindow().getScaledHeight();
        int width = client.getWindow().getScaledWidth();

        double factor = 3;

        int zLevel = 1;
        double top = height/factor;
        double left = width/factor;
        double right = (width/factor) * (factor-1);
        double bottom = (height/factor) * (factor-1);
        double middle_height = height/2.0;
        double height_of_display = bottom - top;
        int number_of_hashes = 11;
        double distance_between_hashes = height_of_display / number_of_hashes;

        this.player = this.client.player;

        float pitch = this.player.getPitch(0);
        int display_pitch = (int) pitch;
        double pitch_offset = (distance_between_hashes / 10) * (display_pitch % 10);

        int lineHeight = this.fontRenderer.fontHeight + 2;

        context.drawText(this.client.textRenderer, String.format("Pitch: %s", (float) this.player.getPitch(0)*-1), (int) left+10, (int) middle_height, Color.RED.getRGB(), false);
        context.drawText(this.client.textRenderer, String.format("Speed: %s", (float) this.speed ), (int) left+10, (int) bottom, Color.RED.getRGB(), false);

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        for(double hash_y = top; hash_y <= bottom + distance_between_hashes; hash_y = hash_y + distance_between_hashes) {
            double hash_y_offset = hash_y + pitch_offset;
            if (hash_y_offset >= top) {
                if (hash_y_offset <= bottom) {
                    context.drawHorizontalLine(
                        (int)(left - 10),
                        (int)left,
                        (int)(hash_y + pitch_offset),
                        Colors.RED);
                }
            }
        }

//        this.fontRenderer.draw( String.format("%s %s %s",  (int) this.last_x, (int) this.last_y, (int) this.last_z), (float) left+10, (float) bottom+lineHeight, Color.RED.getRGB());
//        this.fontRenderer.draw( String.format("%s %s %s",  (int) client.player.getX(), (int) client.player.getY(), (int) client.player.getZ()), (float) left+10, (float) bottom+(lineHeight*2), Color.RED.getRGB());
//        this.fontRenderer.draw( String.format("%s",  client.world.getTime()), (float) left+10, (float) bottom+(lineHeight*3), Color.RED.getRGB());

         context.drawVerticalLine(
            (int)left,
            (int)top,
            (int) bottom,
            Colors.RED);
        context.drawVerticalLine(
            (int)right,
            (int)top,
            (int)bottom,
            Colors.GREEN);

        if (client.world.getTime() > this.last_time+10) {
            double distance = (Math.pow(client.player.getX() -  this.last_x, 2) + Math.pow(client.player.getY() -  this.last_y, 2) + Math.pow(client.player.getZ() -  this.last_z, 2)) * 0.5;
            this.speed = (int) (distance/(client.world.getTime() - this.last_time));

            this.last_time = client.world.getTime();
            this.last_x = client.player.getX();
            this.last_y = client.player.getY();
            this.last_z = client.player.getZ();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                MinecraftClient minecraftClient = MinecraftClient.getInstance();

        if (minecraftClient.player != null) {
            if (minecraftClient.player.isGliding()) {
                this.draw(context);
            }
        }
    }
}