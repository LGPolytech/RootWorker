package RootModels.Root.Geometry;

import io.github.rocsg.fijiyama.registration.ItkTransform;

import java.awt.geom.Point2D;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a 2D polyline, defined as a list of points in a 2D plane.
 * This class provides methods to perform geometric operations on the polyline.
 */
public class Polyline2D implements Geometry {
    // List of points representing the polyline
    public final List<Point2D> polyline;
    public final LocalDateTime dateOfCapture;

    public Polyline2D() {
        this.polyline = new ArrayList<>();
        dateOfCapture = LocalDateTime.now();
    }

    /**
     * Constructor for Polyline2D.
     *
     * @param polyline A list of Point2D objects that define the polyline.
     */
    public Polyline2D(List<Point2D> polyline) {
        this.polyline = polyline;
        dateOfCapture = LocalDateTime.now();
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
     * Calculates the length of the polyline until a given time
     *
     * @param time No use for 2D polyline.
     * @return For a 2D polyline it returns the total length of the polyline.
     */
    @Override
    public double getLengthUntil(double time) {
        return getTotalLength();
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
            Point2D p1 = polyline.get(i - 1);
            Point2D p2 = polyline.get(i);
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
     * Applies a transformation to the polyline before a specified time. For 2D polyline, it calls the transform method.
     *
     * @param transform An ItkTransform object representing the transformation to apply.
     * @param time      Not used here.
     */
    @Override
    public void transformBeforeTime(ItkTransform transform, double time) {
        transform(transform);
    }

    /**
     * Adds a point, another polyline, a list of points, or a string representation of a point to the polyline.
     *
     * @param o The object to add, which can be a Point2D, another Polyline2D, a list of Point2D, or a string representation of a point (e.g., "x,y").
     */
    @Override
    public void add(Object o) {
        if (o instanceof Point2D) {
            // Add a single Point2D to the polyline
            polyline.add((Point2D) o);
        } else if (o instanceof Polyline2D) {
            // Add all points from another Polyline2D to this polyline
            polyline.addAll(((Polyline2D) o).getPolyline());
        } else if (o instanceof List) {
            // Add a list of Point2D objects to the polyline
            List<?> list = (List<?>) o;
            for (Object item : list) {
                if (item instanceof Point2D) {
                    polyline.add((Point2D) item);
                } else if (item instanceof String) {
                    // Check if the string contains two numbers inside brackets or parentheses
                    String str = (String) item;
                    Pattern pattern = Pattern.compile("[\\[(](-?\\d+(\\.\\d+)?),\\s*(-?\\d+(\\.\\d+)?)[])]");
                    Matcher matcher = pattern.matcher(str);
                    if (matcher.matches()) {
                        double x = Double.parseDouble(matcher.group(1));
                        double y = Double.parseDouble(matcher.group(3));
                        polyline.add(new Point2D.Double(x, y));
                    } else {
                        throw new IllegalArgumentException("String must contain two numeric values enclosed in brackets or parentheses");
                    }
                } else {
                    throw new IllegalArgumentException("List contains non-Point2D elements");
                }
            }
        } else if (o instanceof String) {
            // Parse a string representation of a point and add it to the polyline
            String[] coordinates = ((String) o).split(",");
            if (coordinates.length == 2) {
                try {
                    double x = Double.parseDouble(coordinates[0].trim());
                    double y = Double.parseDouble(coordinates[1].trim());
                    polyline.add(new Point2D.Double(x, y));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid string format for Point2D: " + o);
                }
            } else {
                throw new IllegalArgumentException("String must be in the format 'x,y'");
            }
        } else {
            throw new IllegalArgumentException("Unsupported type: " + o.getClass().getName());
        }
    }

    /**
     * Gets the list of points that form the polyline.
     *
     * @return A list of Point2D objects representing the polyline.
     */
    public List<Point2D> getPolyline() {
        return polyline;
    }

    /**
     * Checks if this polyline is equal to another object.
     *
     * @param o The object to compare with.
     * @return True if the object is a Polyline2D with the same points and associated times, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Polyline2D that = (Polyline2D) o;
        if (this.polyline.size() != that.polyline.size()) return false;
        for (int i = 0; i < this.polyline.size(); i++) {
            if (this.polyline.get(i).equals(that.polyline.get(i))) return false;
        }
        return true;
    }
}