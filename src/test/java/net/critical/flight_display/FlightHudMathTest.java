package net.critical.flight_display;

import net.critical.flight_display.FlightHudMath.Layout;
import net.critical.flight_display.FlightHudMath.SpeedSample;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class FlightHudMathTest {

    private static final double DELTA = 1e-9;

    @Test
    void computeLayoutSplitsScreenIntoThirds() {
        Layout layout = FlightHudMath.computeLayout(300, 300);

        assertEquals(100.0, layout.top(), DELTA);
        assertEquals(100.0, layout.left(), DELTA);
        assertEquals(200.0, layout.right(), DELTA);
        assertEquals(200.0, layout.bottom(), DELTA);
        assertEquals(150.0, layout.middleHeight(), DELTA);
        assertEquals(100.0, layout.heightOfDisplay(), DELTA);
        assertEquals(100.0 / FlightHudMath.NUMBER_OF_HASHES, layout.distanceBetweenHashes(), DELTA);
    }

    @Test
    void computeLayoutHandlesNonSquareScreens() {
        Layout layout = FlightHudMath.computeLayout(1920, 1080);

        assertEquals(1080.0 / 3, layout.top(), DELTA);
        assertEquals(1920.0 / 3, layout.left(), DELTA);
        assertEquals((1920.0 / 3) * 2, layout.right(), DELTA);
        assertEquals((1080.0 / 3) * 2, layout.bottom(), DELTA);
        assertEquals(1080.0 / 2, layout.middleHeight(), DELTA);
    }

    @Test
    void truncatedPitchTruncatesTowardZero() {
        assertEquals(5, FlightHudMath.truncatedPitch(5.9f));
        assertEquals(-5, FlightHudMath.truncatedPitch(-5.9f));
        assertEquals(0, FlightHudMath.truncatedPitch(0.4f));
    }

    @Test
    void invertedDisplayPitchNegatesTheTruncatedValue() {
        assertEquals(-5, FlightHudMath.invertedDisplayPitch(5.9f));
        assertEquals(5, FlightHudMath.invertedDisplayPitch(-5.9f));
        assertEquals(0, FlightHudMath.invertedDisplayPitch(0.4f));
    }

    @Test
    void pitchOffsetMatchesOriginalFormula() {
        // (distanceBetweenHashes / 10) * (rawTruncatedPitch % 10)
        assertEquals(77.0, FlightHudMath.pitchOffset(110.0, 7), DELTA);
    }

    @Test
    void pitchOffsetPreservesJavaNegativeModuloBehavior() {
        // Java's % keeps the sign of the dividend: -7 % 10 == -7, not 3.
        // This asymmetric-looking result is preserved verbatim from the
        // original and must not be "fixed" to a mathematical modulo.
        assertEquals(-77.0, FlightHudMath.pitchOffset(110.0, -7), DELTA);
    }

    @Test
    void hashLineYsProducesElevenMarksWithNoOffset() {
        Layout layout = FlightHudMath.computeLayout(300, 300);

        double[] ys = FlightHudMath.hashLineYs(layout, 0.0);

        assertEquals(FlightHudMath.NUMBER_OF_HASHES, ys.length);
        double[] expected = new double[FlightHudMath.NUMBER_OF_HASHES];
        for (int i = 0; i < expected.length; i++) {
            expected[i] = layout.top() + i * layout.distanceBetweenHashes();
        }
        assertArrayEquals(expected, ys, DELTA);
    }

    @Test
    void hashLineYsClipsMarksThatFallOutsideTopAndBottom() {
        Layout layout = new Layout(0, 0, 0, 100, 0, 100, 10);

        // Every offset mark is clipped to [top, bottom]; a +5 offset pushes
        // the mark generated at hashY=100 to 105, which is out of range and
        // must be dropped (one fewer mark than the zero-offset case).
        double[] ys = FlightHudMath.hashLineYs(layout, 5.0);

        assertEquals(10, ys.length);
        for (double y : ys) {
            assertEquals(true, y >= layout.top() && y <= layout.bottom());
        }
    }

    @Test
    void initialSpeedSampleIsZeroed() {
        SpeedSample initial = FlightHudMath.initialSpeedSample();

        assertEquals(0, initial.speed());
        assertEquals(0L, initial.lastTime());
        assertEquals(0.0, initial.lastX(), DELTA);
        assertEquals(0.0, initial.lastY(), DELTA);
        assertEquals(0.0, initial.lastZ(), DELTA);
    }

    @Test
    void sampleSpeedDoesNothingBeforeTenTicksElapse() {
        SpeedSample previous = FlightHudMath.initialSpeedSample();

        SpeedSample result = FlightHudMath.sampleSpeed(5, 100, 100, 100, previous);

        assertSame(previous, result);
    }

    @Test
    void sampleSpeedDoesNothingExactlyAtTheTenTickBoundary() {
        SpeedSample previous = FlightHudMath.initialSpeedSample();

        // worldTime > lastTime + 10 is required; == 10 must not trigger a sample.
        SpeedSample result = FlightHudMath.sampleSpeed(10, 100, 100, 100, previous);

        assertSame(previous, result);
    }

    @Test
    void sampleSpeedUsesSumOfSquaresTimesOneHalfWithNoSquareRoot() {
        SpeedSample previous = FlightHudMath.initialSpeedSample();

        SpeedSample result = FlightHudMath.sampleSpeed(11, 3, 4, 5, previous);

        // distance = (3*3 + 4*4 + 5*5) * 0.5 = 25.0; elapsed = 11 - 0 = 11
        // speed = (int) (25.0 / 11) = 2
        assertEquals(2, result.speed());
        assertEquals(11L, result.lastTime());
        assertEquals(3.0, result.lastX(), DELTA);
        assertEquals(4.0, result.lastY(), DELTA);
        assertEquals(5.0, result.lastZ(), DELTA);
    }

    @Test
    void sampleSpeedChainsFromThePreviousPositionNotTheOrigin() {
        SpeedSample first = FlightHudMath.sampleSpeed(11, 3, 4, 5, FlightHudMath.initialSpeedSample());

        // Not enough ticks have passed yet - unchanged.
        SpeedSample unchanged = FlightHudMath.sampleSpeed(15, 999, 999, 999, first);
        assertSame(first, unchanged);

        // Now enough ticks have passed; distance is measured from `first`'s
        // position (3,4,5), not from the world origin.
        SpeedSample second = FlightHudMath.sampleSpeed(25, 6, 8, 10, first);

        // dx=3, dy=4, dz=5 -> distance = (9+16+25)*0.5 = 25.0; elapsed = 25-11 = 14
        // speed = (int) (25.0 / 14) = 1
        assertEquals(1, second.speed());
        assertEquals(25L, second.lastTime());
        assertEquals(6.0, second.lastX(), DELTA);
        assertEquals(8.0, second.lastY(), DELTA);
        assertEquals(10.0, second.lastZ(), DELTA);
    }

    @Test
    void sampleSpeedKeepsStaleSpeedValueWhenNotEnoughTicksElapsed() {
        SpeedSample first = FlightHudMath.sampleSpeed(11, 3, 4, 5, FlightHudMath.initialSpeedSample());
        assertEquals(2, first.speed());

        SpeedSample stillStale = FlightHudMath.sampleSpeed(20, 1000, 1000, 1000, first);

        // The original never resets speed to 0 between samples - it just
        // keeps showing the last computed value until the next sample.
        assertEquals(2, stillStale.speed());
    }
}
