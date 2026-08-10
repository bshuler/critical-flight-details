package net.critical.flight_display;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure, loader-agnostic HUD layout, pitch-ladder, and speed math extracted
 * verbatim from the original 1.16-era {@code FlightDisplayHud.draw()}. No
 * Minecraft-class dependency, so the same code runs unmodified on every
 * loader/version subproject and is fully unit-testable.
 *
 * <p>The original code's numeric quirks are preserved intentionally and are
 * NOT bugs to "fix" during this port:
 * <ul>
 *   <li>the pitch shown in the text readout is truncated to {@code int}
 *       <em>then</em> negated ({@code (int) pitch * -1}), while the pitch
 *       used to scroll the ladder is the raw truncated value (not negated);</li>
 *   <li>the speed formula is {@code (dx*dx + dy*dy + dz*dz) * 0.5} - not a
 *       real Euclidean distance (no square root) - sampled only once every
 *       10+ world ticks.</li>
 * </ul>
 */
public final class FlightHudMath {

    /** Number of horizontal tick marks drawn down the pitch ladder. */
    public static final int NUMBER_OF_HASHES = 11;

    /**
     * The screen is divided into thirds; the ladder occupies the middle
     * third vertically and spans the left/right thirds horizontally.
     */
    private static final double FACTOR = 3;

    private FlightHudMath() {
    }

    /**
     * @param top                   y of the top of the pitch ladder
     * @param left                  x of the left rail (and left edge of hash marks)
     * @param right                 x of the right rail
     * @param bottom                y of the bottom of the pitch ladder
     * @param middleHeight          vertical screen center, used for the pitch text row
     * @param heightOfDisplay       {@code bottom - top}
     * @param distanceBetweenHashes vertical spacing between hash marks
     */
    public record Layout(
            double top,
            double left,
            double right,
            double bottom,
            double middleHeight,
            double heightOfDisplay,
            double distanceBetweenHashes
    ) {
    }

    /**
     * Reproduces the original layout math exactly: the screen is split into
     * thirds ({@code factor = 3}); the ladder's left/right rails sit at the
     * 1/3 and 2/3 x-marks, and its top/bottom sit at the 1/3 and 2/3 y-marks.
     */
    public static Layout computeLayout(int scaledWidth, int scaledHeight) {
        double top = scaledHeight / FACTOR;
        double left = scaledWidth / FACTOR;
        double right = (scaledWidth / FACTOR) * (FACTOR - 1);
        double bottom = (scaledHeight / FACTOR) * (FACTOR - 1);
        double middleHeight = scaledHeight / 2.0;
        double heightOfDisplay = bottom - top;
        double distanceBetweenHashes = heightOfDisplay / NUMBER_OF_HASHES;

        return new Layout(top, left, right, bottom, middleHeight, heightOfDisplay, distanceBetweenHashes);
    }

    /**
     * Raw camera pitch truncated to {@code int} - used (un-negated) for the
     * ladder scroll offset.
     */
    public static int truncatedPitch(float pitch) {
        return (int) pitch;
    }

    /**
     * Pitch as shown in the on-screen text readout: truncated to {@code int}
     * THEN negated - {@code (int) pitch * -1} in the original source. Kept
     * as its own method (rather than inlined as {@code -truncatedPitch(...)})
     * so the port's intent is explicit and independently testable.
     */
    public static int invertedDisplayPitch(float pitch) {
        return (int) pitch * -1;
    }

    /**
     * Vertical offset applied to every hash mark to make the ladder appear
     * to scroll as pitch changes. Uses the RAW truncated pitch (not the
     * negated display pitch) - this asymmetry is preserved from the
     * original.
     */
    public static double pitchOffset(double distanceBetweenHashes, int rawTruncatedPitch) {
        return (distanceBetweenHashes / 10) * (rawTruncatedPitch % 10);
    }

    /**
     * The y-coordinates (after the pitch offset is applied) of every hash
     * mark that falls within {@code [top, bottom]}, in the same order the
     * original {@code for} loop produced them.
     */
    public static double[] hashLineYs(Layout layout, double pitchOffset) {
        List<Double> ys = new ArrayList<>();
        double distance = layout.distanceBetweenHashes();

        for (double hashY = layout.top(); hashY <= layout.bottom() + distance; hashY += distance) {
            double hashYOffset = hashY + pitchOffset;
            if (hashYOffset >= layout.top() && hashYOffset <= layout.bottom()) {
                ys.add(hashYOffset);
            }
        }

        double[] result = new double[ys.size()];
        for (int i = 0; i < ys.size(); i++) {
            result[i] = ys.get(i);
        }
        return result;
    }

    /**
     * Result of a speed sample: the (possibly-unchanged) speed and tracking
     * state to carry into the next frame.
     */
    public record SpeedSample(int speed, long lastTime, double lastX, double lastY, double lastZ) {
    }

    /** The initial/idle speed-sample state, matching the original's zero-initialized fields. */
    public static SpeedSample initialSpeedSample() {
        return new SpeedSample(0, 0L, 0.0, 0.0, 0.0);
    }

    /**
     * Re-derives the original speed sampling: only recomputes once
     * {@code worldTime > previous.lastTime() + 10}; otherwise returns
     * {@code previous} unchanged (including its stale speed value - the
     * original never reset speed to 0 between samples).
     *
     * <p>The distance formula is intentionally
     * {@code (dx*dx + dy*dy + dz*dz) * 0.5} - not a real Euclidean distance
     * (no square root) - and the elapsed-tick divisor is
     * {@code worldTime - previous.lastTime()}. Both are preserved verbatim
     * from the original.
     */
    public static SpeedSample sampleSpeed(long worldTime, double x, double y, double z, SpeedSample previous) {
        if (worldTime <= previous.lastTime() + 10) {
            return previous;
        }

        double dx = x - previous.lastX();
        double dy = y - previous.lastY();
        double dz = z - previous.lastZ();
        double distance = (dx * dx + dy * dy + dz * dz) * 0.5;
        int speed = (int) (distance / (worldTime - previous.lastTime()));

        return new SpeedSample(speed, worldTime, x, y, z);
    }
}
