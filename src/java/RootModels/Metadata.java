package RootModels;

import java.time.LocalDateTime;
import java.util.*;

/**
 * The Metadata class represents metadata information for the RSML2D format.
 */
public class Metadata {
    private float version;
    private String unit;
    private float resolution;
    private LocalDateTime modifyDate;
    private TreeSet<LocalDateTime> dateOfCapture;
    private String software;
    private String user;
    private String fileKey;
    private List<Double> observationHours;
    List<PropertyDefinition> propertyDefinitions;
    private Map<String, String> imageInfo;

    public Metadata() {
        this.version = 0;
        this.unit = "";
        this.resolution = 0;
        this.modifyDate = LocalDateTime.now();
        this.dateOfCapture = new TreeSet<>();
        this.software = "";
        this.user = "";
        this.fileKey = "";
        this.observationHours = new ArrayList<>();
        this.propertyDefinitions = new ArrayList<>();
        this.imageInfo = new HashMap<>();
    }

    /**
     * Sets the unit of measurement.
     *
     * @param unit The unit of measurement.
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Sets the version of the metadata.
     *
     * @param version The version of the metadata.
     */
    public void setVersion(float version) {
        this.version = version;
    }

    /**
     * Sets the modification date of the metadata.
     *
     * @param modifyDate The modification date of the metadata.
     */
    public void setModifyDate(LocalDateTime modifyDate) {
        this.modifyDate = modifyDate;
    }

    /**
     * Sets the resolution of the metadata.
     *
     * @param resolution The resolution of the metadata.
     */
    public void setResolution(float resolution) {
        this.resolution = resolution;
    }

    /**
     * Gets the date to use.
     *
     * @return The date to use.
     */
    public TreeSet<LocalDateTime> getdateOfCapture() {
        return dateOfCapture;
    }

    public void setdateOfCapture(TreeSet<LocalDateTime> date) {
         dateOfCapture = date;
    }

    public void adddateOfCapture(LocalDateTime date) {dateOfCapture.add(date);}

    /**
     * Sets the software information.
     *
     * @param software The software information.
     */
    public void setSoftware(String software) {
        this.software = software;
    }

    /**
     * Sets the user information.
     *
     * @param user The user information.
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Sets the file key.
     *
     * @param fileKey The file key.
     */
    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    /**
     * Gets the observation hours.
     *
     * @return The observation hours.
     */
    public List<Double> getObservationHours() {
        return observationHours;
    }

    public TreeSet<LocalDateTime> getDateOfCapture() {
        return dateOfCapture;
    }

    public void addDateOfCapture(LocalDateTime date) {
        this.dateOfCapture.add(date);
    }

    public void setObservationHours(List<Double> observationHoursList) {
        this.observationHours = new ArrayList<>(observationHoursList);
    }

    public void add(Metadata meta) {
        this.adddateOfCapture(meta.dateOfCapture.first());
        if (meta.resolution != this.resolution) {
            System.err.println("Not the same resolution between files !" + "\n\tBefore : " + this.resolution + "\tAfter : " + meta.resolution);
            }
        if (!Objects.equals(meta.unit, this.unit)) {
            System.err.println("Not the same measure unit between files !");
        }
    }

    /**
     * The PropertyDefinition class represents a property definition in the metadata.
     */
    public static class PropertyDefinition {
        public String label; // Label of the property
        public String type; // Type of the property
        public String unit; // Unit of the property

        /**
         * Default constructor initializing property definition with default values.
         */
        public PropertyDefinition() {
            this.label = "";
            this.type = "";
            this.unit = "";
        }

        /**
         * Constructor initializing property definition with provided values.
         *
         * @param label The label of the property.
         * @param type  The type of the property.
         * @param unit  The unit of the property.
         */
        public PropertyDefinition(String label, String type, String unit) {
            this.label = label;
            this.type = type;
            this.unit = unit;
        }
    }
}
