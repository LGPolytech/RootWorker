package Parser;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Classe abstraite pour le parsing des fichiers RSML.
 *
 * @param <T> Type générique pour représenter les données de points.
 */
public abstract class RSMLParser<T> {

    // Liste des formats de date potentiels
    protected static final List<DateTimeFormatter> DATE_FORMATS = Arrays.asList(
            DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss"),
            DateTimeFormatter.ofPattern("dd_MM_yyyy"),
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
     * @param filePaths Ensemble des chemins des fichiers RSML.
     * @return Liste de maps contenant les données parsées de chaque fichier RSML.
     * @throws Exception si une erreur survient durant le parsing.
     */
    public List<Map<String, Object>> parseRsmlFiles(Set<String> filePaths) throws Exception {
        List<Map<String, Object>> parsedFiles = new ArrayList<>();
        for (String filePath : filePaths) {
            Map<String, Object> parsedData = safeParseRsmlFile(filePath);
            if (parsedData != null) {
                parsedFiles.add(parsedData);
            }
        }
        return parsedFiles;
    }

    /**
     * Méthode auxiliaire pour gérer les exceptions lors du parsing d'un fichier RSML.
     *
     * @param filePath Chemin du fichier RSML.
     * @return Map contenant les données parsées ou null en cas d'erreur.
     */
    private Map<String, Object> safeParseRsmlFile(String filePath) {
        try {
            return parseRsmlFile(filePath);
        } catch (Exception e) {
            System.err.println("Erreur lors du parsing du fichier: " + filePath);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse un seul fichier RSML.
     *
     * @param filePath Chemin vers le fichier RSML.
     * @return Map contenant les données parsées du fichier RSML, ou null si le parsing échoue.
     * @throws Exception si une erreur survient durant le parsing.
     */
    public Map<String, Object> parseRsmlFile(String filePath) throws Exception {
        Document doc = parseXmlFile(filePath);

        // Extraire la date la plus ancienne à utiliser
        LocalDateTime dateToUse = extractEarliestDate(doc, filePath);

        // Parser les métadonnées et inclure dateToUse
        Map<String, Object> metadata = parseMetadata(doc);
        metadata.put("dateToUse", dateToUse);

        // Vérifier s'il y a au moins une scène
        NodeList sceneNodes = doc.getElementsByTagName("scene");
        if (sceneNodes.getLength() == 0) {
            System.err.println("Aucune scène trouvée dans le fichier RSML: " + filePath);
            return null;
        }

        // Liste pour collecter toutes les racines
        List<Map<String, Object>> flatRoots = new ArrayList<>();

        // Parser les scènes et collecter les racines
        List<Map<String, Object>> scenes = parseScenes(doc, flatRoots, dateToUse);

        // Vérifier s'il y a au moins une plante et une racine avec une géométrie
        if (scenes.isEmpty() || flatRoots.isEmpty()) {
            System.err.println("Aucune plante ou racine valide trouvée dans le fichier RSML: " + filePath);
            return null;
        }

        boolean hasValidRoot = false;
        for (Map<String, Object> root : flatRoots) {
            List<List<T>> geometry = (List<List<T>>) root.get("geometry");
            if (geometry != null && !geometry.isEmpty()) {
                for (List<T> polyline : geometry) {
                    if (!polyline.isEmpty()) {
                        hasValidRoot = true;
                        break;
                    }
                }
            }
            if (hasValidRoot) {
                break;
            }
        }

        if (!hasValidRoot) {
            System.err.println("Aucune racine valide avec une géométrie trouvée dans le fichier RSML: " + filePath);
            return null;
        }

        for (Map<String, Object> root : flatRoots) {
            root.put("date", dateToUse);
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
     * @throws Exception Si une erreur survient durant le parsing.
     */
    private Document parseXmlFile(String filePath) throws Exception {
        File inputFile = new File(filePath);
        if (!inputFile.exists()) {
            throw new FileNotFoundException("Fichier RSML introuvable: " + filePath);
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
        Node metadataNode = doc.getElementsByTagName("metadata").item(0);
        if (metadataNode == null) {
            System.err.println("Élément metadata introuvable dans le fichier RSML.");
            return Collections.emptyMap();
        }

        Element metadataElement = (Element) metadataNode;
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("version", getTextContent(metadataElement, "version").orElse("-1"));
        metadata.put("unit", getTextContent(metadataElement, "unit").orElse(""));
        metadata.put("resolution", getTextContent(metadataElement, "resolution").orElse("-1"));
        metadata.put("lastModified", parseDate(getTextContent(metadataElement, "last-modified").orElse("today")));
        metadata.put("software", getTextContent(metadataElement, "software").orElse(""));
        metadata.put("user", getTextContent(metadataElement, "user").orElse(""));
        metadata.put("fileKey", getTextContent(metadataElement, "file-key").orElse(""));

        Optional<String> obsHoursOptional = getTextContent(metadataElement, "observation-hours");
        if (obsHoursOptional.isPresent()) {
            String obsHours = obsHoursOptional.get();
            List<Double> observationHours = new ArrayList<>();
            observationHours.add(0d);
            String[] hoursArray = obsHours.split(",");
            for (String hourStr : hoursArray) {
                Optional<Double> hourOpt = parseDouble(hourStr.trim());
                hourOpt.ifPresent(observationHours::add);
            }
            Collections.sort(observationHours);
            metadata.put("observationHours", observationHours);
        }

        // Extraire les définitions de propriétés
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

        metadata.put("propertyDefinitions", propertyDefinitions);

        return metadata;
    }

    /**
     * Parse une définition de propriété.
     *
     * @param propDefElement Élément représentant une définition de propriété.
     * @return Map contenant la définition de la propriété.
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
     * @param dateToUse Date à utiliser pour la capture (dans le cas 2D).
     * @return Liste de maps représentant les scènes.
     */
    private List<Map<String, Object>> parseScenes(Document doc, List<Map<String, Object>> flatRoots, LocalDateTime dateToUse) {
        NodeList sceneNodes = doc.getElementsByTagName("scene");
        List<Map<String, Object>> scenes = new ArrayList<>();
        for (int i = 0; i < sceneNodes.getLength(); i++) {
            Element sceneElement = (Element) sceneNodes.item(i);
            Map<String, Object> scene = parseScene(sceneElement, flatRoots, dateToUse);
            scenes.add(scene);
        }
        return scenes;
    }

    /**
     * Parse une scène individuelle.
     *
     * @param sceneElement Élément représentant une scène.
     * @param flatRoots    Liste pour collecter toutes les racines.
     * @param dateToUse    Date à utiliser pour la capture (dans le cas 2D).
     * @return Map contenant les données de la scène.
     */
    private Map<String, Object> parseScene(Element sceneElement, List<Map<String, Object>> flatRoots, LocalDateTime dateToUse) {
        Map<String, Object> scene = new HashMap<>();
        scene.put("plants", parsePlants(sceneElement, flatRoots, dateToUse));
        return scene;
    }

    /**
     * Parse les plantes d'une scène.
     *
     * @param sceneElement Élément représentant une scène.
     * @param flatRoots    Liste pour collecter toutes les racines.
     * @param dateToUse    Date à utiliser pour la capture (dans le cas 2D).
     * @return Liste de maps des données des plantes.
     */
    private List<Map<String, Object>> parsePlants(Element sceneElement, List<Map<String, Object>> flatRoots, LocalDateTime dateToUse) {
        NodeList plantNodes = sceneElement.getElementsByTagName("plant");
        List<Map<String, Object>> plants = new ArrayList<>();
        for (int i = 0; i < plantNodes.getLength(); i++) {
            Element plantElement = (Element) plantNodes.item(i);
            Map<String, Object> plant = parsePlant(plantElement, flatRoots, dateToUse);
            plants.add(plant);
        }
        return plants;
    }

    /**
     * Parse une plante individuelle.
     *
     * @param plantElement Élément représentant une plante.
     * @param flatRoots    Liste pour collecter toutes les racines.
     * @param dateToUse    Date à utiliser pour la capture (dans le cas 2D).
     * @return Map contenant les données de la plante.
     */
    private Map<String, Object> parsePlant(Element plantElement, List<Map<String, Object>> flatRoots, LocalDateTime dateToUse) {
        Map<String, Object> plant = new HashMap<>();
        plant.put("roots", parseRoots(plantElement, flatRoots, dateToUse));
        return plant;
    }

    /**
     * Parse les racines d'un élément parent.
     *
     * @param parentElement Élément contenant des éléments racine (plante ou racine).
     * @param flatRoots     Liste pour collecter toutes les racines.
     * @param dateToUse     Date à utiliser pour la capture (dans le cas 2D).
     * @return Liste de maps représentant les racines.
     */
    private List<Map<String, Object>> parseRoots(Element parentElement, List<Map<String, Object>> flatRoots, LocalDateTime dateToUse) {
        NodeList rootNodes = parentElement.getElementsByTagName("root");
        List<Map<String, Object>> roots = new ArrayList<>();
        for (int i = 0; i < rootNodes.getLength(); i++) {
            Element rootElement = (Element) rootNodes.item(i);
            if (rootElement.getParentNode().equals(parentElement)) {
                Map<String, Object> root = parseRoot(rootElement, flatRoots, dateToUse);
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
     * @param dateToUse   Date à utiliser pour la capture (dans le cas 2D).
     * @return Map contenant les données de la racine.
     */
    private Map<String, Object> parseRoot(Element rootElement, List<Map<String, Object>> flatRoots, LocalDateTime dateToUse) {
        Map<String, Object> root = new HashMap<>();
        root.put("ID", rootElement.getAttribute("ID"));
        root.put("label", rootElement.getAttribute("label"));
        root.put("po:accession", rootElement.getAttribute("po:accession"));

        // Parser les propriétés
        Optional<Map<String, Double>> propertiesOpt = parseProperties(rootElement);
        if (propertiesOpt.isPresent()) {
            root.put("properties", propertiesOpt.get());
        }

        // Parser la géométrie
        List<List<T>> geometry = parseGeometry(rootElement, dateToUse);
        if (geometry.isEmpty()) {
            System.err.println("Aucune géométrie trouvée pour la racine ID: " + rootElement.getAttribute("ID"));
            return root; // Ignorer les racines sans géométrie
        }
        root.put("geometry", geometry);

        // Parser les fonctions
        Optional<Map<String, List<Double>>> functionsOpt = parseFunctions(rootElement);
        if (functionsOpt.isPresent()) {
            root.put("functions", functionsOpt.get());
        }

        // Parser les annotations
        Optional<List<Map<String, String>>> annotationsOpt = parseAnnotations(rootElement);
        if (annotationsOpt.isPresent()) {
            root.put("annotations", annotationsOpt.get());
        }

        // Parser les racines enfants
        List<Map<String, Object>> childRoots = parseRoots(rootElement, flatRoots, dateToUse);
        if (!childRoots.isEmpty()) {
            root.put("childRoots", childRoots);
        }

        // Assigner l'ordre de la racine (e.g., primaire, secondaire)
        root.put("order", getRootOrder(rootElement));

        // Ajouter la racine à la liste flatRoots
        flatRoots.add(root);

        return root;
    }

    /**
     * Parse les propriétés d'un élément racine.
     *
     * @param rootElement Élément représentant une racine.
     * @return Optional contenant une Map des propriétés.
     */
    private Optional<Map<String, Double>> parseProperties(Element rootElement) {
        NodeList propertiesList = rootElement.getElementsByTagName("properties");
        if (propertiesList.getLength() == 0) {
            return Optional.empty();
        }

        Element propertiesElement = (Element) propertiesList.item(0);
        NodeList propertyNodes = propertiesElement.getChildNodes();

        Map<String, Double> properties = new HashMap<>();
        for (int i = 0; i < propertyNodes.getLength(); i++) {
            Node node = propertyNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Optional<Double> valueOpt = parseDouble(node.getTextContent());
                if (valueOpt.isPresent()) {
                    properties.put(node.getNodeName(), valueOpt.get());
                } else {
                    System.err.println("Valeur de propriété invalide pour " + node.getNodeName() + " dans la racine ID: " + rootElement.getAttribute("ID"));
                }
            }
        }
        return properties.isEmpty() ? Optional.empty() : Optional.of(properties);
    }

    /**
     * Méthode abstraite pour parser la géométrie d'un élément racine.
     *
     * @param rootElement Élément représentant une racine.
     * @param dateToUse   Date à utiliser pour la capture (dans le cas 2D).
     * @return Liste de polylignes, où chaque polyligne est une liste d'objets de type T.
     */
    protected abstract List<List<T>> parseGeometry(Element rootElement, LocalDateTime dateToUse);

    /**
     * Parse les fonctions d'un élément racine.
     *
     * @param rootElement Élément représentant une racine.
     * @return Optional contenant une Map des fonctions.
     */
    private Optional<Map<String, List<Double>>> parseFunctions(Element rootElement) {
        NodeList functionNodes = rootElement.getElementsByTagName("function");
        if (functionNodes.getLength() == 0) {
            return Optional.empty();
        }

        Map<String, List<Double>> functions = new HashMap<>();
        for (int i = 0; i < functionNodes.getLength(); i++) {
            Element functionElement = (Element) functionNodes.item(i);
            String functionName = functionElement.getAttribute("name");
            NodeList sampleNodes = functionElement.getElementsByTagName("sample");

            List<Double> samples = new ArrayList<>();
            for (int j = 0; j < sampleNodes.getLength(); j++) {
                String sampleText = sampleNodes.item(j).getTextContent();
                Optional<Double> sampleValueOpt = parseDouble(sampleText);
                if (sampleValueOpt.isPresent()) {
                    samples.add(sampleValueOpt.get());
                } else {
                    System.err.println("Valeur d'échantillon invalide dans la fonction " + functionName + " de la racine ID: " + rootElement.getAttribute("ID"));
                }
            }
            if (!samples.isEmpty()) {
                functions.put(functionName, samples);
            }
        }
        return functions.isEmpty() ? Optional.empty() : Optional.of(functions);
    }

    /**
     * Parse les annotations d'un élément racine.
     *
     * @param rootElement Élément représentant une racine.
     * @return Optional contenant une liste de maps représentant les annotations.
     */
    private Optional<List<Map<String, String>>> parseAnnotations(Element rootElement) {
        NodeList annotationNodes = rootElement.getElementsByTagName("annotation");
        if (annotationNodes.getLength() == 0) {
            return Optional.empty();
        }

        List<Map<String, String>> annotations = new ArrayList<>();
        for (int i = 0; i < annotationNodes.getLength(); i++) {
            Element annotationElement = (Element) annotationNodes.item(i);
            Map<String, String> annotation = new HashMap<>();
            annotation.put("name", annotationElement.getAttribute("name"));

            NodeList childNodes = annotationElement.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node node = childNodes.item(j);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    annotation.put(node.getNodeName(), node.getTextContent());
                }
            }
            if (!annotation.isEmpty()) {
                annotations.add(annotation);
            }
        }
        return annotations.isEmpty() ? Optional.empty() : Optional.of(annotations);
    }

    /**
     * Obtient le contenu textuel d'un élément enfant d'un élément parent.
     *
     * @param parent  Élément parent.
     * @param tagName Nom de la balise enfant.
     * @return Optional contenant le contenu textuel de l'élément enfant, ou vide si non trouvé.
     */
    private Optional<String> getTextContent(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList == null || nodeList.getLength() == 0) {
            return Optional.empty();
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node != null && node.getParentNode().equals(parent)) {
                return Optional.ofNullable(node.getTextContent());
            }
        }
        return Optional.empty();
    }

    /**
     * Parse une chaîne de caractères en un objet LocalDateTime.
     *
     * @param dateStr Chaîne de date à parser.
     * @return Objet LocalDateTime représentant la date, ou null si le parsing échoue.
     */
    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        if ("today".equalsIgnoreCase(dateStr)) {
            return LocalDateTime.now();
        }
        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                TemporalAccessor temporalAccessor = formatter.parse(dateStr);
                if (temporalAccessor.isSupported(ChronoField.HOUR_OF_DAY)) {
                    return LocalDateTime.from(temporalAccessor);
                } else {
                    return LocalDate.from(temporalAccessor).atStartOfDay();
                }
            } catch (Exception e) {
                // Ignorer et essayer le prochain format
            }
        }
        return null; // Échec du parsing
    }

    /**
     * Extrait la date la plus ancienne trouvée dans le document et le nom du fichier.
     *
     * @param doc      Le Document XML à analyser.
     * @param filePath Le chemin vers le fichier.
     * @return La première LocalDateTime trouvée, ou la date et l'heure actuelles si aucune n'est trouvée.
     */
    private LocalDateTime extractEarliestDate(Document doc, String filePath) {
        String fileName = new File(filePath).getName();

        // Collecter toutes les chaînes potentielles de date
        Set<String> dateStrings = new HashSet<>();
        dateStrings.add(fileName);

        // Ajouter les éléments de métadonnées
        NodeList metadataNodes = doc.getElementsByTagName("metadata");
        if (metadataNodes.getLength() > 0) {
            Element metadataElement = (Element) metadataNodes.item(0);
            NodeList childNodes = metadataElement.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    dateStrings.add(node.getTextContent());
                }
            }
        }

        // Scanner tout le document pour les chaînes ressemblant à des dates
        NodeList allNodes = doc.getElementsByTagName("*");
        for (int i = 0; i < allNodes.getLength(); i++) {
            Node node = allNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                // Ajouter le contenu textuel
                dateStrings.add(element.getTextContent());
                // Ajouter les attributs
                NamedNodeMap attributes = element.getAttributes();
                for (int j = 0; j < attributes.getLength(); j++) {
                    Node attr = attributes.item(j);
                    dateStrings.add(attr.getNodeValue());
                }
            }
        }

        // Extraire et parser les chaînes de date
        List<LocalDateTime> dates = new ArrayList<>();
        for (String text : dateStrings) {
            List<String> extractedDates = extractDateStrings(text);
            for (String dateStr : extractedDates) {
                LocalDateTime date = parseDate(dateStr);
                if (date != null) {
                    dates.add(date);
                }
            }
        }

        if (dates.isEmpty()) {
            System.err.println("Aucune date trouvée dans le document. Utilisation de la date et l'heure actuelles.");
            return LocalDateTime.now();
        }

        // Trouver la date la plus ancienne
        LocalDateTime earliestDate = dates.get(0);
        for (LocalDateTime date : dates) {
            if (date.isBefore(earliestDate)) {
                earliestDate = date;
            }
        }
        return earliestDate;
    }

    /**
     * Extrait les sous-chaînes ressemblant à des dates d'un texte donné en utilisant des motifs regex.
     *
     * @param text Le texte à analyser pour les chaînes de date.
     * @return Liste des sous-chaînes ressemblant à des dates.
     */
    private List<String> extractDateStrings(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        String[] datePatterns = new String[]{
                "\\d{2}_\\d{2}_\\d{4}",          // ex: 24_05_2018
                "\\b\\d{4}-\\d{2}-\\d{2}\\b",    // ex: 2018-05-24
                "\\b\\d{4}/\\d{2}/\\d{2}\\b",    // ex: 2018/05/24
                "\\b\\d{2}/\\d{2}/\\d{4}\\b",    // ex: 24/05/2018
                "\\b\\d{8}\\b",                  // ex: 20180524
                "\\b\\d{4}_\\d{2}_\\d{2}\\b",    // ex: 2018_05_24
                // Ajouter d'autres motifs si nécessaire
        };

        List<String> dateStrings = new ArrayList<>();
        for (String patternStr : datePatterns) {
            Pattern pattern = Pattern.compile(patternStr);
            java.util.regex.Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                dateStrings.add(matcher.group());
            }
        }
        return dateStrings;
    }

    /**
     * Détermine l'ordre d'une racine en fonction de sa position dans la hiérarchie.
     *
     * @param rootElement Élément représentant une racine.
     * @return Entier représentant l'ordre de la racine (e.g., 1 pour racine primaire).
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

    /**
     * Parse une chaîne en Double de manière sécurisée.
     *
     * @param str Chaîne à parser.
     * @return Optional contenant le Double si le parsing réussit.
     */
    private Optional<Double> parseDouble(String str) {
        try {
            return Optional.of(Double.parseDouble(str));
        } catch (NumberFormatException e) {
            System.err.println("Valeur numérique invalide: " + str);
            return Optional.empty();
        }
    }
}
