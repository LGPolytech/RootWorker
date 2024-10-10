package RootModels.Root.Geometry;

import io.github.rocsg.fijiyama.registration.ItkTransform;

import java.awt.geom.Point2D;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a 2D+t polyline, defined as a list of points in a 2D plane with an associated time component.
 * This class provides methods to perform geometric operations on the polyline over time.
 */
public class Polyline2DplusT implements Geometry {
    // List of points representing the polyline over time
    public final List<Point2DWithTime> polyline;
    public final LocalDateTime dateOfCapture;

    /**
     * Constructor for Polyline2DplusT.
     *
     * @param polyline A list of Point2DWithTime objects that define the polyline.
     */
    public Polyline2DplusT(List<Point2DWithTime> polyline, LocalDateTime dateOfCapture) {
        this.polyline = polyline;
        this.dateOfCapture = dateOfCapture;
    }

    /**
     * Scales the polyline by a given factor (between 0 and 1).
     *
     * @param scaleFactor The factor by which to scale the polyline. Values greater than 1 will enlarge the polyline, while values between 0 and 1 will shrink it.
     */
    @Override
    public void scale(double scaleFactor) {
        // For each point in the polyline, scale its coordinates relative to the origin (0,0)
        polyline.forEach(point -> {
            double newX = point.getX() * scaleFactor;
            double newY = point.getY() * scaleFactor;
            point.setLocation(newX, newY);
        });
    }

    /**
     * Calculates the length of the polyline until a given time.
     *
     * @param time The time until which the length of the polyline should be calculated.
     * @return The length of the polyline up to the specified time.
     */
    @Override
    public double getLengthUntil(double time) {
        double totalLength = 0.0;
        for (int i = 1; i < polyline.size(); i++) {
            Point2DWithTime p1 = polyline.get(i - 1);
            Point2DWithTime p2 = polyline.get(i);
            if (p2.getTime() > time) {
                break;
            }
            totalLength += p1.distance(p2);
        }
        return totalLength;
    }

    /**
     * Calculates the total length of the polyline.
     *
     * @return The total length of the polyline, i.e., the sum of all the Euclidean distances between consecutive points in the list.
     */
    @Override
    public double getTotalLength() {
        double totalLength = 0.0;
        for (int i = 1; i < polyline.size(); i++) {
            Point2DWithTime p1 = polyline.get(i - 1);
            Point2DWithTime p2 = polyline.get(i);
            totalLength += p1.distance(p2);
        }
        return totalLength;
    }

    /**
     * Applies a given transformation to the polyline.
     *
     * @param transform An ItkTransform object that represents the transformation to be applied to each point in the polyline.
     */
    @Override
    public void transform(ItkTransform transform) {
        polyline.forEach(point -> {
            double[] transformedPoint = transform.transformPoint(new double[]{point.getX(), point.getY(), 0});
            point.setLocation(transformedPoint[0], transformedPoint[1]);
        });
    }

    /**
     * Applies a transformation to the polyline before a specified time.
     *
     * @param transform An ItkTransform object representing the transformation to apply.
     * @param time      The time before which the transformation should be applied.
     */
    @Override
    public void transformBeforeTime(ItkTransform transform, double time) {
        polyline.stream()
                .filter(point -> point.getTime() <= time)
                .forEach(point -> {
                    double[] transformedPoint = transform.transformPoint(new double[]{point.getX(), point.getY(), 0});
                    point.setLocation(transformedPoint[0], transformedPoint[1]);
                });
    }

    /**
     * Adds a point, another polyline, a list of points, or a string representation of a point to the polyline.
     *
     * @param o The object to add, which can be a Point2DWithTime, another Polyline2DplusT, a list of Point2DWithTime, or a string representation of a point (e.g., "x,y,time").
     */
    @Override
    public void add(Object o) {
        if (o instanceof Point2DWithTime) {
            // Add a single Point2DWithTime to the polyline
            polyline.add((Point2DWithTime) o);
        } else if (o instanceof Polyline2DplusT) {
            // Add all points from another Polyline2DplusT to this polyline
            polyline.addAll(((Polyline2DplusT) o).getPolyline());
        } else if (o instanceof List) {
            // Add a list of Point2DWithTime objects to the polyline
            List<?> list = (List<?>) o;
            for (Object item : list) {
                if (item instanceof Point2DWithTime) {
                    polyline.add((Point2DWithTime) item);
                } else if (item instanceof String) {
                    // Check if the string contains two numbers inside brackets or parentheses
                    String str = (String) item;
                    Pattern pattern = Pattern.compile("[\\[(](-?\\d+(\\.\\d+)?),\\s*(-?\\d+(\\.\\d+)?),\\s*(-?\\d+(\\.\\d+)?)[])]");
                    Matcher matcher = pattern.matcher(str);
                    if (matcher.matches()) {
                        double x = Double.parseDouble(matcher.group(1));
                        double y = Double.parseDouble(matcher.group(3));
                        double time = Double.parseDouble(matcher.group(5));
                        polyline.add(new Point2DWithTime(x, y, time, time));
                    } else {
                        throw new IllegalArgumentException("String must contain two numeric values and a time value enclosed in brackets or parentheses");
                    }
                } else {
                    throw new IllegalArgumentException("List contains non-Point2DWithTime elements");
                }
            }
        } else if (o instanceof String) {
            // Parse a string representation of a point and add it to the polyline
            String[] coordinates = ((String) o).split(",");
            if (coordinates.length == 3) {
                try {
                    double x = Double.parseDouble(coordinates[0].trim());
                    double y = Double.parseDouble(coordinates[1].trim());
                    double time = Double.parseDouble(coordinates[2].trim());
                    polyline.add(new Point2DWithTime(x, y, time, time));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid string format for Point2DWithTime: " + o);
                }
            } else {
                throw new IllegalArgumentException("String must be in the format 'x,y,time'");
            }
        } else {
            throw new IllegalArgumentException("Unsupported type: " + o.getClass().getName());
        }
    }

    /**
     * Gets the list of points that form the polyline.
     *
     * @return A list of Point2DWithTime objects representing the polyline.
     */
    public List<Point2DWithTime> getPolyline() {
        return polyline;
    }

    /**
     * Checks if this polyline is equal to another object.
     *
     * @param o The object to compare with.
     * @return True if the object is a Polyline2DplusT with the same points and associated times, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Polyline2DplusT that = (Polyline2DplusT) o;
        if (this.polyline.size() != that.polyline.size()) return false;
        for (int i = 0; i < this.polyline.size(); i++) {
            Point2DWithTime p1 = this.polyline.get(i);
            Point2DWithTime p2 = that.polyline.get(i);
            if (!p1.equals(p2) || this.polyline.get(i).getX() != that.polyline.get(i).getX() || this.polyline.get(i).getY() != that.polyline.get(i).getY()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Inner class representing a point in 2D space with associated time components.
     */
    public static class Point2DWithTime extends Point2D.Double {
        private final double time;
        private final double timeHour;

        /**
         * Constructor for Point2DWithTime.
         *
         * @param x        The x-coordinate of the point.
         * @param y        The y-coordinate of the point.
         * @param time     The time associated with this point.
         * @param timeHour The hour value associated with this point.
         */
        public Point2DWithTime(double x, double y, double time, double timeHour) {
            super(x, y);
            this.time = time;
            this.timeHour = timeHour;
        }

        /**
         * Gets the time associated with this point.
         *
         * @return The time value.
         */
        public double getTime() {
            return time;
        }

        /**
         * Gets the hour value associated with this point.
         *
         * @return The hour value.
         */
        public double getTimeHour() {
            return timeHour;
        }

        /**
         * Checks if this point is equal to another object.
         *
         * @param o The object to compare with.
         * @return True if the object is a Point2DWithTime with the same coordinates and time components, false otherwise.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point2DWithTime that = (Point2DWithTime) o;
            return Double.distance(that.x, that.y, x, y) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getX(), getY(), time, timeHour);
        }
    }
}