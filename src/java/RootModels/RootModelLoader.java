package RootModels;

import Parser.Parser2D;
import Parser.Parser2DTime;
import RootModels.Root.Geometry.Function;
import RootModels.Root.Geometry.Geometry;
import RootModels.Root.Geometry.Polyline2D;
import RootModels.Root.Geometry.Polyline2DplusT;
import RootModels.Root.Geometry.Polyline2DplusT.Point2DWithTime;
import RootModels.Root.Property;
import RootModels.Root.Root;

import java.awt.geom.Point2D;
import java.time.LocalDateTime;
import java.util.*;

public class RootModelLoader {

    public static boolean isTemporal = false;

    public static void main(String[] args) throws Exception {
        TreeSet<String> files = new TreeSet<>(Arrays.asList(
                "D:\\loaiu\\MAM5\\Stage\\data\\UC3\\Rootsystemtracker\\Original_Data\\B73_R04_01\\13_05_2018_HA01_R004_h053.rsml",
                "D:\\loaiu\\MAM5\\Stage\\data\\UC3\\Rootsystemtracker\\Original_Data\\B73_R04_01\\14_05_2018_HA01_R004_h004.rsml",
                "D:\\loaiu\\MAM5\\Stage\\data\\UC3\\Rootsystemtracker\\Original_Data\\B73_R04_01\\15_05_2018_HA01_R004_h004.rsml",
                "D:\\loaiu\\MAM5\\Stage\\data\\UC3\\Rootsystemtracker\\Original_Data\\B73_R04_01\\16_05_2018_HA01_R004_h004.rsml",
                "D:\\loaiu\\MAM5\\Stage\\data\\UC3\\Rootsystemtracker\\Original_Data\\B73_R04_01\\17_05_2018_HA01_R004_h004.rsml",
                "D:\\loaiu\\MAM5\\Stage\\data\\UC3\\Rootsystemtracker\\Original_Data\\B73_R04_01\\18_05_2018_HA01_R004_h004.rsml",
                "D:\\loaiu\\MAM5\\Stage\\data\\UC3\\Rootsystemtracker\\Original_Data\\B73_R04_01\\19_05_2018_HA01_R004_h004.rsml",
                "D:\\loaiu\\MAM5\\Stage\\data\\UC3\\Rootsystemtracker\\Original_Data\\B73_R04_01\\20_05_2018_HA01_R004_h004.rsml",
                "D:\\loaiu\\MAM5\\Stage\\data\\UC3\\Rootsystemtracker\\Original_Data\\B73_R04_01\\21_05_2018_HA01_R004_h004.rsml",
                "D:\\loaiu\\MAM5\\Stage\\data\\UC3\\Rootsystemtracker\\Original_Data\\B73_R04_01\\22_05_2018_HA01_R004_h004.rsml",
                "D:\\loaiu\\MAM5\\Stage\\data\\UC3\\Rootsystemtracker\\Original_Data\\B73_R04_01\\23_05_2018_HA01_R004_h004.rsml",
                "D:\\loaiu\\MAM5\\Stage\\data\\UC3\\Rootsystemtracker\\Original_Data\\B73_R04_01\\24_05_2018_HA01_R004_h004.rsml",
                "D:\\loaiu\\MAM5\\Stage\\data\\UC3\\Rootsystemtracker\\Original_Data\\B73_R04_01\\25_05_2018_HA01_R004_h004.rsml"
        ));

        RootModel rm = loadRsmlFiles(files);
        System.out.println(rm);
    }

    public static RootModel loadRsmlFiles(Set<String> rsmlFilePaths) throws Exception {
        boolean isTimeData = isTimeData(rsmlFilePaths);

        List<Map<String, Object>> parsedDataList;
        if (isTimeData) {
            Parser2DTime parser = new Parser2DTime();
            parsedDataList = parser.parseRsmlFiles(new HashSet<>(rsmlFilePaths));
        } else {
            Parser2D parser = new Parser2D();
            parsedDataList = parser.parseRsmlFiles(new HashSet<>(rsmlFilePaths));
        }

        return buildRootModelFromParsedData(parsedDataList, isTimeData);
    }

    private static boolean isTimeData(Set<String> rsmlFilePaths) {
        return isTemporal;
    }

    private static RootModel buildRootModelFromParsedData(List<Map<String, Object>> parsedDataList, boolean isTimeData) {
        TreeMap<LocalDateTime, RootModel.RootModelEntry> dataByDate = new TreeMap<>();

        for (Map<String, Object> parsedData : parsedDataList) {
            Metadata metadata = buildMetadata((Map<String, Object>) parsedData.get("metadata"));

            LocalDateTime dateOfCapture = metadata.getDateOfCapture().first();

            List<Map<String, Object>> scenesData = (List<Map<String, Object>>) parsedData.get("scenes");
            if (scenesData == null || scenesData.isEmpty()) {
                System.err.println("Aucune scène trouvée dans les données parsées.");
                continue; // Passer au fichier suivant
            }

            Scene scene = new Scene();

            List<Root> flatRootList = new ArrayList<>();

            for (Map<String, Object> sceneData : scenesData) {
                List<Map<String, Object>> plantsData = (List<Map<String, Object>>) sceneData.get("plants");
                if (plantsData == null || plantsData.isEmpty()) {
                    System.err.println("Aucune plante trouvée dans la scène.");
                    continue; // Passer à la scène suivante
                }

                for (Map<String, Object> plantData : plantsData) {
                    Plant plant = buildPlant(plantData, scene, isTimeData);
                    scene.addPlant(plant);

                    flatRootList.addAll(plant.getFlatRoots());
                }
            }

            RootModel.RootModelEntry entry = new RootModel.RootModelEntry(scene, metadata, flatRootList);

            dataByDate.put(dateOfCapture, entry);
        }

        return new RootModel(dataByDate);
    }

    /**
     * Construit un objet Metadata à partir des données fournies.
     *
     * @param metadataData Map contenant les données des métadonnées.
     * @return Un objet Metadata.
     */
    private static Metadata buildMetadata(Map<String, Object> metadataData) {
        Metadata metadata = new Metadata();

        metadata.setVersion(Float.parseFloat((String) metadataData.getOrDefault("version", "0")));
        metadata.setUnit((String) metadataData.getOrDefault("unit", ""));
        metadata.setResolution(Float.parseFloat((String) metadataData.getOrDefault("resolution", "0")));
        metadata.setModifyDate((LocalDateTime) metadataData.getOrDefault("lastModified", LocalDateTime.now()));
        metadata.setSoftware((String) metadataData.getOrDefault("software", ""));
        metadata.setUser((String) metadataData.getOrDefault("user", ""));
        metadata.setFileKey((String) metadataData.getOrDefault("fileKey", ""));

        List<Map<String, String>> propertyDefinitionsData = (List<Map<String, String>>) metadataData.getOrDefault("propertyDefinitions", Collections.emptyList());
        List<Metadata.PropertyDefinition> propertyDefinitions = new ArrayList<>();
        for (Map<String, String> propDefData : propertyDefinitionsData) {
            String label = propDefData.getOrDefault("label", "");
            String type = propDefData.getOrDefault("type", "");
            String unit = propDefData.getOrDefault("unit", "");
            Metadata.PropertyDefinition propDef = new Metadata.PropertyDefinition(label, type, unit);
            propertyDefinitions.add(propDef);
        }
        metadata.propertyDefinitions = propertyDefinitions;

        // Ajouter la date de capture
        LocalDateTime dateToUse = (LocalDateTime) metadataData.getOrDefault("dateToUse", LocalDateTime.now());
        metadata.addDateOfCapture(dateToUse);

        // Récupérer 'observationHours' s'il existe
        List<Double> observationHours = (List<Double>) metadataData.get("observationHours");
        if (observationHours != null) {
            metadata.setObservationHours(observationHours);
        }

        return metadata;
    }

    /**
     * Construit un objet Scene à partir des données fournies.
     *
     * @param sceneData  Map contenant les données de la scène.
     * @param isTimeData Indique si les données contiennent des informations temporelles.
     * @return Un objet Scene.
     */
    private static Scene buildScene(Map<String, Object> sceneData, boolean isTimeData) {
        Scene scene = new Scene();

        List<Map<String, Object>> plantsData = (List<Map<String, Object>>) sceneData.get("plants");
        for (Map<String, Object> plantData : plantsData) {
            Plant plant = buildPlant(plantData, scene, isTimeData);
            scene.addPlant(plant);
        }

        return scene;
    }

    /**
     * Construit un objet Plant à partir des données fournies.
     *
     * @param plantData   Map contenant les données de la plante.
     * @param parentScene La scène parente.
     * @param isTimeData  Indique si les données contiennent des informations temporelles.
     * @return Un objet Plant.
     */
    private static Plant buildPlant(Map<String, Object> plantData, Scene parentScene, boolean isTimeData) {
        Plant plant = new Plant();
        plant.parentScene = parentScene;

        List<Map<String, Object>> rootsData = (List<Map<String, Object>>) plantData.get("roots");
        for (Map<String, Object> rootData : rootsData) {
            Root root = buildRoot(rootData, null, plant, isTimeData);
            plant.addRoot(root);
        }

        return plant;
    }

    /**
     * Construit un objet Root à partir des données fournies.
     *
     * @param rootData    Map contenant les données de la racine.
     * @param parentRoot  La racine parente.
     * @param parentPlant La plante parente.
     * @param isTimeData  Indique si les données contiennent des informations temporelles.
     * @return Un objet Root.
     */
    private static Root buildRoot(Map<String, Object> rootData, Root parentRoot, Plant parentPlant, boolean isTimeData) {
        String id = (String) rootData.getOrDefault("ID", "");
        String label = (String) rootData.getOrDefault("label", "");
        String poAccession = (String) rootData.getOrDefault("po:accession", "");
        int order = (int) rootData.getOrDefault("order", 1);

        // Propriétés
        Map<String, Double> propertiesData = (Map<String, Double>) rootData.getOrDefault("properties", Collections.emptyMap());
        List<Property> properties = new ArrayList<>();
        for (Map.Entry<String, Double> entry : propertiesData.entrySet()) {
            Property property = new Property(entry.getKey(), entry.getValue());
            properties.add(property);
        }

        // Fonctions
        Map<String, List<Double>> functionsData = (Map<String, List<Double>>) rootData.getOrDefault("functions", Collections.emptyMap());
        List<Function> functions = new ArrayList<>();
        for (Map.Entry<String, List<Double>> entry : functionsData.entrySet()) {
            Function function = new Function(entry.getKey(), entry.getValue());
            functions.add(function);
        }

        // Géométrie
        List<List<?>> geometryData = (List<List<?>>) rootData.get("geometry");
        Geometry geometry = null;
        if (geometryData != null && !geometryData.isEmpty()) {
            // Déterminer le type de données (2D ou 2D+t)
            if (isTimeData) {
                // Données 2D+t
                List<Point2DWithTime> points = new ArrayList<>();
                for (List<?> polylineData : geometryData) {
                    for (Object pointObj : polylineData) {
                        Parser2DTime.PointData pointData = (Parser2DTime.PointData) pointObj;
                        Point2DWithTime point = new Point2DWithTime(pointData.coord_x, pointData.coord_y, pointData.coord_t, pointData.coord_th);
                        points.add(point);
                    }
                }
                geometry = new Polyline2DplusT(points, LocalDateTime.now());
            } else {
                // Données 2D
                List<Point2D> points = new ArrayList<>();
                for (List<?> polylineData : geometryData) {
                    for (Object pointObj : polylineData) {
                        Point2D.Double pointData = (Point2D.Double) pointObj;
                        points.add(pointData);
                    }
                }
                // Récupérer la date de capture à partir des métadonnées
                LocalDateTime dateOfCapture = (LocalDateTime) rootData.get("date");
                if (dateOfCapture == null) {
                    dateOfCapture = LocalDateTime.now();
                }
                geometry = new Polyline2D(points, dateOfCapture);
            }
        }

        // Créer la racine sans enfants pour commencer
        Root root = new Root(new ArrayList<>(), id, order, properties, label, functions, poAccession, parentRoot, geometry, parentPlant);

        // Racines enfants
        List<Map<String, Object>> childRootsData = (List<Map<String, Object>>) rootData.get("childRoots");
        if (childRootsData != null) {
            for (Map<String, Object> childRootData : childRootsData) {
                Root childRoot = buildRoot(childRootData, root, parentPlant, isTimeData);
                root.children.add(childRoot);
            }
        }

        // Ajouter la racine au flatSet de la plante
        parentPlant.add2FlatSet(root);

        return root;
    }
}
