package Parser;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Classe pour le parsing des fichiers RSML 2D.
 */
public class Parser2D extends RSMLParser<Point2D.Double> {

    public static void main(String[] args) {
        // Obtenir la liste des chemins de fichiers RSML de l'utilisateur
        HashSet<String> rsmlFiles = ParserUtils.getUserInputPaths();

        if (rsmlFiles.isEmpty()) {
            System.err.println("Aucun fichier RSML fourni.");
            return;
        }

        try {
            Parser2D parser = new Parser2D();
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
    protected List<List<Point2D.Double>> parseGeometry(Element rootElement) {
        NodeList geometryNodes = rootElement.getElementsByTagName("geometry");
        List<List<Point2D.Double>> geometry = new ArrayList<>();

        if (geometryNodes.getLength() == 0) {
            return geometry; // Retourner une géométrie vide
        }

        for (int i = 0; i < geometryNodes.getLength(); i++) {
            Element geometryElement = (Element) geometryNodes.item(i);
            NodeList polylineNodes = geometryElement.getElementsByTagName("polyline");

            for (int j = 0; j < polylineNodes.getLength(); j++) {
                Element polylineElement = (Element) polylineNodes.item(j);
                NodeList pointNodes = polylineElement.getElementsByTagName("point");

                List<Point2D.Double> polyline = new ArrayList<>();

                for (int k = 0; k < pointNodes.getLength(); k++) {
                    Element pointElement = (Element) pointNodes.item(k);
                    try {
                        double x = Double.parseDouble(pointElement.getAttribute("x"));
                        double y = Double.parseDouble(pointElement.getAttribute("y"));
                        polyline.add(new Point2D.Double(x, y));
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
}
