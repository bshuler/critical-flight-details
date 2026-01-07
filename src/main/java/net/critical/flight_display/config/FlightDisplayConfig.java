package net.critical.flight_display.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration for the Flight Display mod.
 * Uses simple JSON file for cross-loader compatibility.
 */
public class FlightDisplayConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static FlightDisplayConfig INSTANCE;
    private static Path configPath;

    // HUD visibility
    public boolean showPitchIndicator = true;
    public boolean showSpeedDisplay = true;
    public boolean showHorizonLines = true;
    public boolean showAltitudeDisplay = true;
    public boolean showHeadingDisplay = true;

    // HUD position (as fraction of screen: 0.0 = left/top, 1.0 = right/bottom)
    public double hudLeftPosition = 0.333;  // 1/3 from left
    public double hudRightPosition = 0.667; // 2/3 from left

    // Colors (ARGB format)
    public int pitchIndicatorColor = 0xFFFF0000;  // Red
    public int horizonLineColor = 0xFF00FF00;     // Green
    public int textColor = 0xFFFF0000;            // Red
    public int altitudeColor = 0xFFFFFF00;        // Yellow
    public int headingColor = 0xFF00FFFF;         // Cyan

    // Display options
    public boolean showTextShadow = true;
    public int speedUpdateInterval = 10;  // Ticks between speed updates

    // Altitude display options
    public boolean showAltitudeAboveGround = true;  // Show height above ground (AGL)
    public boolean showAltitudeAbsolute = true;     // Show Y coordinate (MSL)

    private FlightDisplayConfig() {}

    public static FlightDisplayConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public static void setConfigPath(Path path) {
        configPath = path;
    }

    public static FlightDisplayConfig load() {
        if (configPath == null) {
            return new FlightDisplayConfig();
        }

        Path configFile = configPath.resolve("flight_display.json");
        if (Files.exists(configFile)) {
            try (Reader reader = Files.newBufferedReader(configFile)) {
                FlightDisplayConfig config = GSON.fromJson(reader, FlightDisplayConfig.class);
                if (config != null) {
                    return config;
                }
            } catch (Exception e) {
                System.err.println("[Flight Display] Failed to load config: " + e.getMessage());
            }
        }

        // Create default config
        FlightDisplayConfig config = new FlightDisplayConfig();
        config.save();
        return config;
    }

    public void save() {
        if (configPath == null) {
            return;
        }

        Path configFile = configPath.resolve("flight_display.json");
        try {
            Files.createDirectories(configPath);
            try (Writer writer = Files.newBufferedWriter(configFile)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            System.err.println("[Flight Display] Failed to save config: " + e.getMessage());
        }
    }

    /**
     * Reload config from file
     */
    public static void reload() {
        INSTANCE = load();
    }
}
