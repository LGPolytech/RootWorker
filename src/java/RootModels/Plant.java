package RootModels;

import RootModels.Root.Root;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * The Plant class represents a plant with a collection of roots.
 */
public class Plant {
    final List<Root> roots; // List of root parsers
    final HashSet<Root> flatSetOfRoots; // Set of root parsers for quick access
    public String id; // ID of the plant
    public String label; // Label of the plant
    public Scene parentScene;

    /**
     * Constructs a Plant object with empty roots and flat set of roots.
     */
    public Plant() {
        this.roots = new ArrayList<>();
        this.flatSetOfRoots = new HashSet<>();
        id = "";
        label = "";
    }

    /**
     * Adds a root to the plant.
     *
     * @param root The root to add.
     */
    public void addRoot(Root root) {
        this.roots.add(root);
        this.flatSetOfRoots.add(root);
    }

    /**
     * Adds a root to the flat set of roots.
     *
     * @param root The root to add.
     */
    public void add2FlatSet(Root root) {
        this.flatSetOfRoots.add(root);
    }

    /**
     * Gets a list of root IDs.
     *
     * @return A list of root IDs.
     */
    public List<String> getListID() {
        List<String> listID = new ArrayList<>();
        for (Root root : flatSetOfRoots) {
            listID.add(root.getId());
        }
        return listID;
    }

    /**
     * Gets a root by its ID.
     *
     * @param id The ID of the root.
     * @return The root with the specified ID, or null if not found.
     */
    public Root getRootByID(String id) {
        for (Root root : flatSetOfRoots) {
            if (root.getId().equals(id)) {
                return root;
            }
        }
        return null;
    }

    /**
     * Gets the list of roots.
     *
     * @return The list of roots.
     */
    public List<Root> getRoots() {
        return roots;
    }

    /**
     * Gets the flat set of roots.
     *
     * @return The flat set of roots.
     */
    public HashSet<Root> getFlatRoots() {
        return flatSetOfRoots;
    }

    /**
     * Gets the list of first-order roots.
     *
     * @return The list of first-order roots.
     */
    public List<Root> getFirstOrderRoots() {
        List<Root> firstOrderRoots = new ArrayList<>();
        for (Root root : roots) {
            if (root.getOrder() == 1) {
                firstOrderRoots.add(root);
            }
        }
        return firstOrderRoots;
    }

    /**
     * Gets the parent scene of the plant
     *
     * @return the parent scene
     */
    public Scene getParentScene() {
        return parentScene;
    }

    /**
     * Returns a string representation of the Plant object.
     *
     * @return A string representation of the Plant object.
     */
    @Override
    public String toString() {
        return "\nPlant{" +
                "roots=" + roots +
                "}\n";
    }
}