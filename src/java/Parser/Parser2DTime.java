package Parser;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Classe pour le parsing des fichiers RSML 2D avec coordonnées temporelles.
 */
public class Parser2DTime extends RSMLParser<Parser2DTime.PointData> {

    public static void main(String[] args) {
        // Obtenir la liste des chemins de fichiers RSML de l'utilisateur
        HashSet<String> rsmlFiles = ParserUtils.getUserInputPaths();

        if (rsmlFiles.isEmpty()) {
            System.err.println("Aucun fichier RSML fourni.");
            return;
        }

        try {
            Parser2DTime parser = new Parser2DTime();
            // Parser les fichiers RSML et récupérer les données
            List<Map<String, Object>> parsedData = parser.parseRsmlFiles(rsmlFiles);

            // Afficher les données parsées
            parsedData.forEach(System.out::println);
        } catch (Exception e) {
            System.err.println("Une erreur est survenue lors du parsing des fichiers RSML : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected List<List<PointData>> parseGeometry(Element rootElement) {
        NodeList geometryNodes = rootElement.getElementsByTagName("geometry");
        List<List<PointData>> geometry = new ArrayList<>();

        if (geometryNodes.getLength() == 0) {
            return geometry; // Retourner une géométrie vide
        }

        for (int i = 0; i < geometryNodes.getLength(); i++) {
            Element geometryElement = (Element) geometryNodes.item(i);
            NodeList polylineNodes = geometryElement.getElementsByTagName("polyline");

            for (int j = 0; j < polylineNodes.getLength(); j++) {
                Element polylineElement = (Element) polylineNodes.item(j);
                NodeList pointNodes = polylineElement.getElementsByTagName("point");

                List<PointData> polyline = new ArrayList<>();

                for (int k = 0; k < pointNodes.getLength(); k++) {
                    Element pointElement = (Element) pointNodes.item(k);
                    try {
                        double coord_t = Double.parseDouble(pointElement.getAttribute("coord_t"));
                        double coord_th = Double.parseDouble(pointElement.getAttribute("coord_th"));
                        double coord_x = Double.parseDouble(pointElement.getAttribute("coord_x"));
                        double coord_y = Double.parseDouble(pointElement.getAttribute("coord_y"));
                        double diameter = Double.parseDouble(pointElement.getAttribute("diameter"));
                        double vx = Double.parseDouble(pointElement.getAttribute("vx"));
                        double vy = Double.parseDouble(pointElement.getAttribute("vy"));

                        PointData pointData = new PointData(coord_t, coord_th, coord_x, coord_y, diameter, vx, vy);
                        polyline.add(pointData);
                    } catch (NumberFormatException e) {
                        System.err.println("Coordonnées de point invalides dans la racine ID : " + rootElement.getAttribute("ID"));
                    }
                }
                if (!polyline.isEmpty()) {
                    geometry.add(polyline);
                }
            }
        }
        return geometry;
    }

    /**
     * Classe représentant un point avec des attributs supplémentaires.
     */
    public static class PointData {
        public double coord_t;
        public double coord_th;
        public double coord_x;
        public double coord_y;
        public double diameter;
        public double vx;
        public double vy;

        // Constructeur
        public PointData(double coord_t, double coord_th, double coord_x, double coord_y, double diameter, double vx, double vy) {
            this.coord_t = coord_t;
            this.coord_th = coord_th;
            this.coord_x = coord_x;
            this.coord_y = coord_y;
            this.diameter = diameter;
            this.vx = vx;
            this.vy = vy;
        }

        @Override
        public String toString() {
            return String.format("PointData(coord_t=%f, coord_th=%f, coord_x=%f, coord_y=%f, diameter=%f, vx=%f, vy=%f)",
                    coord_t, coord_th, coord_x, coord_y, diameter, vx, vy);
        }
    }
}
