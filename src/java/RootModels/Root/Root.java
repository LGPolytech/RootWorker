package RootModels.Root;

import RootModels.Plant;
import RootModels.Root.Geometry.Function;
import RootModels.Root.Geometry.Geometry;

import java.util.List;

public class Root {

    public final int order; // The order of the root
    public final Geometry geometry; // The geometry of the root
    protected final Root parent; // The parent root
    protected final String label; // The label of the root
    protected final List<Property> properties; // List of properties associated with the root
    final String poAccession; // The Plant Ontology accession of the root
    final List<Function> functions; // List of functions associated with the root
    final Plant parentPlant;
    public List<Root> children; // List of child roots
    protected String id; // The ID of the root

    public Root(List<Root> children, String id, int order, List<Property> properties, String label, List<Function> functions, String poAccession, Root parent, Geometry geometry, Plant parentPlant) {
        this.children = children;
        this.id = id;
        this.order = order;
        this.properties = properties;
        this.label = label;
        this.functions = functions;
        this.poAccession = poAccession;
        this.parent = parent;
        this.geometry = geometry;
        this.parentPlant = parentPlant;
    }

    /**
     * Getter for the Root id
     *
     * @return the unique id of the root
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the label of the root.
     *
     * @return The label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the Plant Ontology accession of the root.
     *
     * @return The Plant Ontology accession.
     */
    public String getPoAccession() {
        return poAccession;
    }

    /**
     * Gets the order of the root.
     *
     * @return The order.
     */
    public int getOrder() {
        return order;
    }

    /**
     * Gets the list of child roots.
     *
     * @return The list of child roots.
     */
    public List<Root> getChildren() {
        return children;
    }

    /**
     * Gets the list of properties associated with the root.
     *
     * @return The list of properties.
     */
    public List<Property> getProperties() {
        return properties;
    }

    /**
     * Gets the list of functions associated with the root.
     *
     * @return The list of functions.
     */
    public List<Function> getFunctions() {
        return functions;
    }

    /**
     * Gets the geometry of the root.
     *
     * @return The geometry.
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * Gets the parent root.
     *
     * @return The parent root.
     */
    public Root getParent() {
        return parent;
    }

    /**
     * Gets the ID of the parent root.
     *
     * @return The parent ID.
     */
    public String getParentId() {
        return parent == null ? null : parent.getId();
    }

    /**
     * Gets the label of the parent root.
     *
     * @return The parent label.
     */
    public String getParentLabel() {
        return parent == null ? null : parent.getLabel();
    }

    /**
     * Gets the parent plant of the Root
     *
     * @return the parent Plant object
     */
    public Plant getParentPlant() {
        return parentPlant;
    }
}
