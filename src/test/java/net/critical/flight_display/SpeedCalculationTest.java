package net.critical.flight_display;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for flight speed calculation logic.
 * These tests verify the mathematical calculations used in the HUD
 * without requiring Minecraft to be running.
 */
@DisplayName("Speed Calculation Tests")
class SpeedCalculationTest {

    /**
     * Calculates 3D Euclidean distance between two points.
     * This mirrors the calculation used in FlightDisplayHud.
     */
    private double calculateDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Converts distance traveled over ticks to blocks per second.
     * Minecraft runs at 20 ticks per second.
     */
    private int calculateSpeed(double distance, long ticks) {
        if (ticks <= 0) return 0;
        return (int) (distance / ticks * 20);
    }

    @Test
    @DisplayName("Distance calculation for stationary player should be zero")
    void testZeroDistance() {
        double distance = calculateDistance(100, 64, 200, 100, 64, 200);
        assertEquals(0.0, distance, 0.001);
    }

    @Test
    @DisplayName("Distance calculation for horizontal movement")
    void testHorizontalDistance() {
        // Moving 10 blocks on X axis
        double distance = calculateDistance(0, 64, 0, 10, 64, 0);
        assertEquals(10.0, distance, 0.001);
    }

    @Test
    @DisplayName("Distance calculation for vertical movement")
    void testVerticalDistance() {
        // Moving 10 blocks on Y axis (up/down)
        double distance = calculateDistance(0, 64, 0, 0, 74, 0);
        assertEquals(10.0, distance, 0.001);
    }

    @Test
    @DisplayName("Distance calculation for diagonal movement")
    void testDiagonalDistance() {
        // Moving in 3D space: 3-4-5 triangle extended to 3D
        // sqrt(3^2 + 4^2 + 0^2) = 5
        double distance = calculateDistance(0, 0, 0, 3, 4, 0);
        assertEquals(5.0, distance, 0.001);
    }

    @Test
    @DisplayName("Distance calculation for 3D diagonal movement")
    void test3DDiagonalDistance() {
        // sqrt(1^2 + 1^2 + 1^2) = sqrt(3) ≈ 1.732
        double distance = calculateDistance(0, 0, 0, 1, 1, 1);
        assertEquals(Math.sqrt(3), distance, 0.001);
    }

    @ParameterizedTest
    @DisplayName("Speed calculation for various distances and tick counts")
    @CsvSource({
        "20.0, 10, 40",   // 20 blocks in 10 ticks = 40 blocks/sec
        "100.0, 20, 100", // 100 blocks in 20 ticks (1 sec) = 100 blocks/sec
        "10.0, 10, 20",   // 10 blocks in 10 ticks = 20 blocks/sec
        "0.0, 10, 0",     // No movement = 0 speed
        "50.0, 100, 10"   // 50 blocks in 100 ticks (5 sec) = 10 blocks/sec
    })
    void testSpeedCalculation(double distance, long ticks, int expectedSpeed) {
        int speed = calculateSpeed(distance, ticks);
        assertEquals(expectedSpeed, speed);
    }

    @Test
    @DisplayName("Speed calculation handles zero ticks gracefully")
    void testSpeedWithZeroTicks() {
        int speed = calculateSpeed(100.0, 0);
        assertEquals(0, speed);
    }

    @Test
    @DisplayName("Typical Elytra gliding speed calculation")
    void testTypicalElytraSpeed() {
        // Elytra cruising speed is roughly 30-40 blocks/sec
        // Simulating 35 blocks traveled in 20 ticks (1 second)
        double distance = 35.0;
        long ticks = 20;
        int speed = calculateSpeed(distance, ticks);
        assertEquals(35, speed);
    }

    @Test
    @DisplayName("Maximum Elytra speed with firework boost")
    void testFireworkBoostSpeed() {
        // Firework-boosted Elytra can reach ~100+ blocks/sec
        // Simulating 120 blocks traveled in 20 ticks
        double distance = 120.0;
        long ticks = 20;
        int speed = calculateSpeed(distance, ticks);
        assertEquals(120, speed);
    }
}
