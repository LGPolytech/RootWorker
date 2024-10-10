package Parser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe abstraite pour le parsing des fichiers RSML.
 *
 * @param <T> Type générique pour représenter les données de point.
 */
public abstract class RSMLParser<T> {

    // Liste des formats de date potentiels
    protected static final List<DateTimeFormatter> DATE_FORMATS = Arrays.asList(
            DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
    );

    /**
     * Parse plusieurs fichiers RSML.
     *
     * @param filePaths Liste des chemins de fichiers RSML.
     * @return Liste de maps contenant les données parsées de chaque fichier RSML.
     * @throws Exception si une erreur survient pendant le parsing.
     */
    public List<Map<String, Object>> parseRsmlFiles(HashSet<String> filePaths) throws Exception {
        List<Map<String, Object>> parsedData = new ArrayList<>();
        for (String filePath : filePaths) {
            Map<String, Object> data = parseRsmlFile(filePath);
            if (data != null) {
                parsedData.add(data);
            }
        }
        return parsedData;
    }

    /**
     * Parse un fichier RSML unique.
     *
     * @param filePath Chemin vers le fichier RSML.
     * @return Map contenant les données parsées du fichier RSML, ou null si le parsing échoue.
     * @throws Exception si une erreur survient pendant le parsing.
     */
    public Map<String, Object> parseRsmlFile(String filePath) throws Exception {
        Document doc = parseXmlFile(filePath);

        // Vérifier s'il y a au moins une scène
        NodeList sceneNodes = doc.getElementsByTagName("scene");
        if (sceneNodes.getLength() == 0) {
            System.err.println("Aucune scène trouvée dans le fichier RSML : " + filePath);
            return null;
        }

        // Parser les métadonnées
        Map<String, Object> metadata = parseMetadata(doc);

        // Liste pour collecter toutes les racines
        List<Map<String, Object>> flatRoots = new ArrayList<>();

        // Parser les scènes et collecter les racines
        List<Map<String, Object>> scenes = parseScenes(doc, flatRoots);

        // Vérifier s'il y a au moins une plante et une racine avec une géométrie
        if (scenes.isEmpty() || flatRoots.isEmpty()) {
            System.err.println("Aucune plante ou racine valide trouvée dans le fichier RSML : " + filePath);
            return null;
        }

        boolean hasValidRoot = flatRoots.stream().anyMatch(root -> {
            List<List<T>> geometry = (List<List<T>>) root.get("geometry");
            return geometry != null && !geometry.isEmpty() && geometry.stream().anyMatch(polyline -> !polyline.isEmpty());
        });

        if (!hasValidRoot) {
            System.err.println("Aucune racine valide avec géométrie trouvée dans le fichier RSML : " + filePath);
            return null;
        }

        // Construire le résultat final
        Map<String, Object> result = new HashMap<>();
        result.put("metadata", metadata);
        result.put("scenes", scenes);
        result.put("flatRoots", flatRoots);

        return result;
    }

    /**
     * Parse un fichier XML en un objet Document.
     *
     * @param filePath Chemin vers le fichier XML.
     * @return Objet Document représentant le fichier XML parsé.
     * @throws Exception Si une erreur survient pendant le parsing.
     */
    private Document parseXmlFile(String filePath) throws Exception {
        File inputFile = new File(filePath);
        if (!inputFile.exists()) {
            throw new FileNotFoundException("Fichier RSML non trouvé : " + filePath);
        }
        DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();
        return doc;
    }

    /**
     * Parse les métadonnées du fichier RSML.
     *
     * @param doc Objet Document du fichier RSML.
     * @return Map contenant les métadonnées.
     */
    private Map<String, Object> parseMetadata(Document doc) {
        NodeList metadataNodes = doc.getElementsByTagName("metadata");
        if (metadataNodes.getLength() == 0) {
            System.err.println("Élément 'metadata' non trouvé dans le fichier RSML.");
            return Collections.emptyMap();
        }

        Element metadataElement = (Element) metadataNodes.item(0);

        // Extraire les éléments de métadonnées
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("version", getTextContent(metadataElement, "version").orElse("-1"));
        metadata.put("unit", getTextContent(metadataElement, "unit").orElse(""));
        metadata.put("resolution", getTextContent(metadataElement, "resolution").orElse("-1"));
        metadata.put("lastModified", parseDate(getTextContent(metadataElement, "last-modified").orElse("today")));
        metadata.put("software", getTextContent(metadataElement, "software").orElse(""));
        metadata.put("user", getTextContent(metadataElement, "user").orElse(""));
        metadata.put("fileKey", getTextContent(metadataElement, "file-key").orElse(""));

        Optional<String> observationHoursContent = getTextContent(metadataElement, "observation-hours");
        if (observationHoursContent.isPresent()) {
            String[] hoursArray = observationHoursContent.get().split(",");
            List<Double> observationHours = new ArrayList<>();
            observationHours.add(0d);
            for (String hourStr : hoursArray) {
                try {
                    observationHours.add(Double.parseDouble(hourStr.trim()));
                } catch (NumberFormatException e) {
                    System.err.println("Heure d'observation invalide : " + hourStr);
                }
            }
            observationHours = observationHours.stream().sorted().collect(Collectors.toList());
            metadata.put("observationHours", observationHours);
        }

        // Extraire les 'property-definitions'
        NodeList propDefsNodes = metadataElement.getElementsByTagName("property-definitions");
        List<Map<String, String>> propertyDefinitions = new ArrayList<>();

        if (propDefsNodes.getLength() > 0) {
            Element propDefsElement = (Element) propDefsNodes.item(0);
            NodeList propertyDefs = propDefsElement.getElementsByTagName("property-definition");
            for (int i = 0; i < propertyDefs.getLength(); i++) {
                Element propDefElement = (Element) propertyDefs.item(i);
                Map<String, String> propDef = parsePropertyDefinition(propDefElement);
                propertyDefinitions.add(propDef);
            }
        }

        // Ajouter les définitions de propriétés aux métadonnées
        metadata.put("propertyDefinitions", propertyDefinitions);

        return metadata;
    }

    /**
     * Parse un élément de définition de propriété.
     *
     * @param propDefElement Élément représentant une définition de propriété.
     * @return Map contenant la définition de propriété.
     */
    private Map<String, String> parsePropertyDefinition(Element propDefElement) {
        Map<String, String> propDef = new HashMap<>();
        propDef.put("label", getTextContent(propDefElement, "label").orElse("Unknown"));
        propDef.put("type", getTextContent(propDefElement, "type").orElse("Unknown"));
        propDef.put("unit", getTextContent(propDefElement, "unit").orElse("Unknown"));
        return propDef;
    }

    /**
     * Parse les scènes du fichier RSML.
     *
     * @param doc       Objet Document du fichier RSML.
     * @param flatRoots Liste pour collecter toutes les racines.
     * @return Liste de maps représentant les scènes.
     */
    private List<Map<String, Object>> parseScenes(Document doc, List<Map<String, Object>> flatRoots) {
        NodeList sceneNodes = doc.getElementsByTagName("scene");
        List<Map<String, Object>> scenes = new ArrayList<>();

        for (int i = 0; i < sceneNodes.getLength(); i++) {
            Element sceneElement = (Element) sceneNodes.item(i);
            Map<String, Object> scene = parseScene(sceneElement, flatRoots);
            scenes.add(scene);
        }

        return scenes;
    }

    /**
     * Parse une scène individuelle.
     *
     * @param sceneElement Élément représentant une scène.
     * @param flatRoots    Liste pour collecter toutes les racines.
     * @return Map contenant les données de la scène.
     */
    private Map<String, Object> parseScene(Element sceneElement, List<Map<String, Object>> flatRoots) {
        // Parser les plantes et collecter les racines
        List<Map<String, Object>> plants = parsePlants(sceneElement, flatRoots);

        Map<String, Object> scene = new HashMap<>();
        scene.put("plants", plants);
        return scene;
    }

    /**
     * Parse les plantes au sein d'une scène.
     *
     * @param sceneElement Élément représentant une scène.
     * @param flatRoots    Liste pour collecter toutes les racines.
     * @return Liste de maps de données de plantes.
     */
    private List<Map<String, Object>> parsePlants(Element sceneElement, List<Map<String, Object>> flatRoots) {
        NodeList plantNodes = sceneElement.getElementsByTagName("plant");
        List<Map<String, Object>> plants = new ArrayList<>();

        for (int i = 0; i < plantNodes.getLength(); i++) {
            Element plantElement = (Element) plantNodes.item(i);
            Map<String, Object> plant = parsePlant(plantElement, flatRoots);
            plants.add(plant);
        }
        return plants;
    }

    /**
     * Parse une plante individuelle.
     *
     * @param plantElement Élément représentant une plante.
     * @param flatRoots    Liste pour collecter toutes les racines.
     * @return Map contenant les données de la plante.
     */
    private Map<String, Object> parsePlant(Element plantElement, List<Map<String, Object>> flatRoots) {
        List<Map<String, Object>> roots = parseRoots(plantElement, flatRoots);
        Map<String, Object> plant = new HashMap<>();
        plant.put("roots", roots);
        return plant;
    }

    /**
     * Parse les racines d'un élément parent.
     *
     * @param parentElement Élément contenant des éléments racines (plante ou racine).
     * @param flatRoots     Liste pour collecter toutes les racines.
     * @return Liste de maps représentant les racines.
     */
    private List<Map<String, Object>> parseRoots(Element parentElement, List<Map<String, Object>> flatRoots) {
        NodeList rootNodes = parentElement.getElementsByTagName("root");
        List<Map<String, Object>> roots = new ArrayList<>();

        for (int i = 0; i < rootNodes.getLength(); i++) {
            Element rootElement = (Element) rootNodes.item(i);

            // Considérer uniquement les racines enfant directes pour éviter les doublons
            if (rootElement.getParentNode().equals(parentElement)) {
                Map<String, Object> root = parseRoot(rootElement, flatRoots);
                roots.add(root);
            }
        }
        return roots;
    }

    /**
     * Parse une racine individuelle.
     *
     * @param rootElement Élément représentant une racine.
     * @param flatRoots   Liste pour collecter toutes les racines.
     * @return Map contenant les données de la racine.
     */
    private Map<String, Object> parseRoot(Element rootElement, List<Map<String, Object>> flatRoots) {
        Map<String, Object> root = new HashMap<>();
        root.put("ID", rootElement.getAttribute("ID"));
        root.put("label", rootElement.getAttribute("label"));
        root.put("po:accession", rootElement.getAttribute("po:accession"));

        // Parser les propriétés
        Map<String, Double> properties = parseProperties(rootElement);
        if (!properties.isEmpty()) {
            root.put("properties", properties);
        }

        // Parser la géométrie
        List<List<T>> geometry = parseGeometry(rootElement);
        if (geometry.isEmpty()) {
            System.err.println("Aucune géométrie trouvée pour la racine ID : " + rootElement.getAttribute("ID"));
            return root; // Ignorer les racines sans géométrie
        }
        root.put("geometry", geometry);

        // Parser les fonctions
        Map<String, List<Double>> functions = parseFunctions(rootElement);
        if (!functions.isEmpty()) {
            root.put("functions", functions);
        }

        // Parser les annotations
        List<Map<String, String>> annotations = parseAnnotations(rootElement);
        if (!annotations.isEmpty()) {
            root.put("annotations", annotations);
        }

        // Parser les racines enfant
        List<Map<String, Object>> childRoots = parseRoots(rootElement, flatRoots);
        if (!childRoots.isEmpty()) {
            root.put("childRoots", childRoots);
        }

        // Assigner l'ordre de la racine (ex. primaire, secondaire)
        root.put("order", getRootOrder(rootElement));

        // Ajouter la racine à la liste des flatRoots
        flatRoots.add(root);

        return root;
    }

    /**
     * Parse les propriétés d'un élément racine.
     *
     * @param rootElement Élément représentant une racine.
     * @return Map contenant les propriétés avec des clés String et des valeurs Double.
     */
    private Map<String, Double> parseProperties(Element rootElement) {
        NodeList propertiesList = rootElement.getElementsByTagName("properties");
        if (propertiesList.getLength() == 0) {
            return Collections.emptyMap();
        }

        Element propertiesElement = (Element) propertiesList.item(0);
        NodeList propertyNodes = propertiesElement.getChildNodes();

        Map<String, Double> properties = new HashMap<>();
        for (int i = 0; i < propertyNodes.getLength(); i++) {
            Node node = propertyNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                try {
                    properties.put(node.getNodeName(), Double.parseDouble(node.getTextContent()));
                } catch (NumberFormatException e) {
                    System.err.println("Valeur de propriété invalide pour " + node.getNodeName() + " dans la racine ID : " + rootElement.getAttribute("ID"));
                }
            }
        }
        return properties;
    }

    /**
     * Méthode abstraite pour parser la géométrie d'un élément racine.
     *
     * @param rootElement Élément représentant une racine.
     * @return Liste de polylignes, où chaque polyligne est une liste d'objets de type T.
     */
    protected abstract List<List<T>> parseGeometry(Element rootElement);

    /**
     * Parse les fonctions d'un élément racine.
     *
     * @param rootElement Élément représentant une racine.
     * @return Map des noms de fonctions aux listes de valeurs d'échantillons (Double).
     */
    private Map<String, List<Double>> parseFunctions(Element rootElement) {
        NodeList functionNodes = rootElement.getElementsByTagName("function");
        Map<String, List<Double>> functions = new HashMap<>();

        for (int i = 0; i < functionNodes.getLength(); i++) {
            Element functionElement = (Element) functionNodes.item(i);
            String functionName = functionElement.getAttribute("name");
            NodeList sampleNodes = functionElement.getElementsByTagName("sample");

            List<Double> samples = new ArrayList<>();
            for (int j = 0; j < sampleNodes.getLength(); j++) {
                try {
                    double sampleValue = Double.parseDouble(sampleNodes.item(j).getTextContent());
                    samples.add(sampleValue);
                } catch (NumberFormatException e) {
                    System.err.println("Valeur d'échantillon invalide dans la fonction " + functionName + " de la racine ID : " + rootElement.getAttribute("ID"));
                }
            }
            if (!samples.isEmpty()) {
                functions.put(functionName, samples);
            }
        }
        return functions;
    }

    /**
     * Parse les annotations d'un élément racine.
     *
     * @param rootElement Élément représentant une racine.
     * @return Liste de maps représentant les annotations.
     */
    private List<Map<String, String>> parseAnnotations(Element rootElement) {
        NodeList annotationNodes = rootElement.getElementsByTagName("annotation");
        List<Map<String, String>> annotations = new ArrayList<>();

        for (int i = 0; i < annotationNodes.getLength(); i++) {
            Element annotationElement = (Element) annotationNodes.item(i);
            Map<String, String> annotation = new HashMap<>();
            annotation.put("name", annotationElement.getAttribute("name"));

            NodeList childNodes = annotationElement.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node node = childNodes.item(j);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element) node;
                    annotation.put(childElement.getNodeName(), childElement.getTextContent());
                }
            }
            if (!annotation.isEmpty()) {
                annotations.add(annotation);
            }
        }
        return annotations;
    }

    /**
     * Récupère le contenu textuel d'un élément enfant d'un élément parent.
     *
     * @param parent  Élément parent.
     * @param tagName Nom de la balise enfant.
     * @return Optional contenant le contenu textuel de l'élément enfant, ou vide si non trouvé.
     */
    private Optional<String> getTextContent(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node != null && node.getParentNode().equals(parent)) {
                return Optional.ofNullable(node.getTextContent());
            }
        }
        return Optional.empty();
    }

    /**
     * Parse une chaîne de date en un objet LocalDateTime.
     *
     * @param dateStr Chaîne de date à parser.
     * @return Objet LocalDateTime représentant la date, ou la date et l'heure actuelles si le parsing échoue.
     */
    private LocalDateTime parseDate(String dateStr) {
        if ("today".equalsIgnoreCase(dateStr)) {
            return LocalDateTime.now();
        }
        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                TemporalAccessor temporalAccessor = formatter.parse(dateStr);
                return LocalDateTime.from(temporalAccessor);
            } catch (Exception ignored) {
                // Essayer le prochain formateur
            }
        }
        System.err.println("Échec du parsing de la date : " + dateStr + ". Utilisation de la date et de l'heure actuelles.");
        return LocalDateTime.now();
    }

    /**
     * Détermine l'ordre d'une racine en fonction de sa position dans la hiérarchie.
     *
     * @param rootElement Élément représentant une racine.
     * @return Entier représentant l'ordre de la racine (ex. 1 pour racine primaire).
     */
    private int getRootOrder(Element rootElement) {
        int order = 1;
        Node parent = rootElement.getParentNode();
        while (parent instanceof Element) {
            if ("root".equals(((Element) parent).getTagName())) {
                order++;
            }
            parent = parent.getParentNode();
        }
        return order;
    }
}
