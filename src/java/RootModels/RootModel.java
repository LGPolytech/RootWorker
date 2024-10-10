package RootModels;

import RootModels.Root.Root;
import org.apache.commons.collections.map.HashedMap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class RootModel {
    public final List<Root> rootList; // List of root parsers
    public final HashSet<Double> hoursCorrespondingToTimePoints; // Strictly increasing set of double values representing hours
    final Map<Scene, Plant> sceneAndPlants;
    Metadata metadata;
    private float dpi;
    private float pixelSize;

    public RootModel(List<Root> roots, Map<Scene, Plant> sceneAndPlants, Metadata metadata) {
        this.rootList = roots;
        this.sceneAndPlants = sceneAndPlants;
        this.metadata = metadata;
        this.hoursCorrespondingToTimePoints = new HashSet<>();
    }

    public HashSet<Double> getHoursCorrespondingToTimePoints() {
        return hoursCorrespondingToTimePoints;
    }

    public Map<Scene, Plant> getSceneAndPlants() {
        return sceneAndPlants;
    }

    public List<Root> getRootList() {
        return rootList;
    }

    /**
     * Gets the metadata associated with the original RSML file
     *
     * @return A Metadata object
     */
    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        if (hoursCorrespondingToTimePoints.isEmpty()) {
            return "RootModel composed of :\n" + "\t" + rootList.size() + " roots\n\tTaken at time : "+metadata.getdateOfCapture();
        } else {
            return "RootModel composed of :\n" + "\t" + rootList.size() + " roots\n\t" + "That grows in a range of " ;
        }
    }
}
