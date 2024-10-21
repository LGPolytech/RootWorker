package RootModels.Root.Geometry;

import io.github.rocsg.fijiyama.registration.ItkTransform;

import java.awt.geom.Point2D;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Représente une polyligne 2D, définie comme une liste de points dans un plan 2D.
 * Cette classe fournit des méthodes pour effectuer des opérations géométriques sur la polyligne.
 */
public class Polyline2D implements Geometry {
    // Liste des points représentant la polyligne
    public final List<Point2D> polyline;
    public final LocalDateTime dateOfCapture;

    /**
     * Constructeur pour Polyline2D.
     *
     * @param polyline      Une liste d'objets Point2D qui définissent la polyligne.
     * @param dateOfCapture Date de capture associée à la polyligne.
     */
    public Polyline2D(List<Point2D> polyline, LocalDateTime dateOfCapture) {
        this.polyline = polyline;
        this.dateOfCapture = dateOfCapture;
    }

    /**
     * Met à l'échelle la polyligne par un facteur donné (entre 0 et 1).
     *
     * @param scaleFactor Le facteur par lequel mettre à l'échelle la polyligne.
     */
    @Override
    public void scale(double scaleFactor) {
        polyline.forEach(point -> {
            double newX = point.getX() * scaleFactor;
            double newY = point.getY() * scaleFactor;
            point.setLocation(newX, newY);
        });
    }

    /**
     * Calcule la longueur totale de la polyligne jusqu'à un temps donné.
     *
     * @param time Non utilisé pour une polyligne 2D.
     * @return La longueur totale de la polyligne.
     */
    @Override
    public double getLengthUntil(double time) {
        return getTotalLength();
    }

    /**
     * Calcule la longueur totale de la polyligne.
     *
     * @return La longueur totale de la polyligne.
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
     * Applique une transformation donnée à la polyligne.
     *
     * @param transform Un objet ItkTransform représentant la transformation à appliquer.
     */
    @Override
    public void transform(ItkTransform transform) {
        polyline.forEach(point -> {
            double[] transformedPoint = transform.transformPoint(new double[]{point.getX(), point.getY(), 0});
            point.setLocation(transformedPoint[0], transformedPoint[1]);
        });
    }

    /**
     * Applique une transformation à la polyligne avant un temps spécifié.
     * Pour une polyligne 2D, elle appelle simplement la méthode transform.
     *
     * @param transform Un objet ItkTransform représentant la transformation à appliquer.
     * @param time      Non utilisé ici.
     */
    @Override
    public void transformBeforeTime(ItkTransform transform, double time) {
        transform(transform);
    }

    /**
     * Ajoute un point, une autre polyligne, une liste de points ou une représentation de chaîne d'un point à la polyligne.
     *
     * @param o L'objet à ajouter.
     */
    @Override
    public void add(Object o) {
        if (o instanceof Point2D) {
            polyline.add((Point2D) o);
        } else if (o instanceof Polyline2D) {
            polyline.addAll(((Polyline2D) o).getPolyline());
        } else if (o instanceof List) {
            List<?> list = (List<?>) o;
            for (Object item : list) {
                if (item instanceof Point2D) {
                    polyline.add((Point2D) item);
                } else if (item instanceof String) {
                    String str = (String) item;
                    Pattern pattern = Pattern.compile("[\\[(](-?\\d+(\\.\\d+)?),\\s*(-?\\d+(\\.\\d+)?)[])]");
                    Matcher matcher = pattern.matcher(str);
                    if (matcher.matches()) {
                        double x = Double.parseDouble(matcher.group(1));
                        double y = Double.parseDouble(matcher.group(3));
                        polyline.add(new Point2D.Double(x, y));
                    } else {
                        throw new IllegalArgumentException("La chaîne doit contenir deux valeurs numériques entre parenthèses ou crochets");
                    }
                } else {
                    throw new IllegalArgumentException("La liste contient des éléments non Point2D");
                }
            }
        } else if (o instanceof String) {
            String[] coordinates = ((String) o).split(",");
            if (coordinates.length == 2) {
                try {
                    double x = Double.parseDouble(coordinates[0].trim());
                    double y = Double.parseDouble(coordinates[1].trim());
                    polyline.add(new Point2D.Double(x, y));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Format de chaîne invalide pour Point2D: " + o);
                }
            } else {
                throw new IllegalArgumentException("La chaîne doit être au format 'x,y'");
            }
        } else {
            throw new IllegalArgumentException("Type non pris en charge: " + o.getClass().getName());
        }
    }

    /**
     * Obtient la liste des points qui forment la polyligne.
     *
     * @return Une liste d'objets Point2D représentant la polyligne.
     */
    public List<Point2D> getPolyline() {
        return polyline;
    }

    /**
     * Vérifie si cette polyligne est égale à un autre objet.
     *
     * @param o L'objet à comparer.
     * @return True si l'objet est une Polyline2D avec les mêmes points, false sinon.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Polyline2D that = (Polyline2D) o;
        return this.polyline.equals(that.polyline);
    }
}
