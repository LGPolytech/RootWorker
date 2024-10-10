package RootModels;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Metadata class represents metadata information for the RSML2D format.
 */
public class Metadata {
    public List<PropertyDefinition> propertiedef; // List of property definitions
    float version; // Version of the metadata
    String unit; // Unit of measurement
    float resolution; // Resolution of the metadata
    LocalDateTime modifyDate; // Modification date of the metadata
    String dateOfCapture; // Date to use
    String software; // Software information
    String user; // User information
    String fileKey; // File key
    double size; // Size of the metadata
    List<Double> observationHours; // Observation hours
    Map<String, String> image_info; // Image information

    /**
     * Default constructor initializing metadata with default values.
     */
    public Metadata() {
        this.version = 0;
        this.unit = "";
        this.resolution = 0;
        this.modifyDate = LocalDateTime.now();
        this.software = "";
        this.user = "";
        this.fileKey = "";
        this.dateOfCapture = "";
        this.size = 0;
        this.propertiedef = new ArrayList<>();
        this.image_info = new HashMap<>();
    }

    /**
     * Gets the unit of measurement.
     *
     * @return The unit of measurement.
     */
    public String getUnit() {
        return unit;
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
     * Gets the version of the metadata.
     *
     * @return The version of the metadata.
     */
    public float getVersion() {
        return version;
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
     * Gets the modification date of the metadata.
     *
     * @return The modification date of the metadata.
     */
    public LocalDateTime getModifyDate() {
        return modifyDate;
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
     * Gets the resolution of the metadata.
     *
     * @return The resolution of the metadata.
     */
    public float getResolution() {
        return resolution;
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
    public String getdateOfCapture() {
        return dateOfCapture;
    }

    /**
     * Sets the date to use.
     *
     * @param dateOfCapture The date to use.
     */
    public void setdateOfCapture(String dateOfCapture) {
        this.dateOfCapture = dateOfCapture;
    }

    /**
     * Gets the software information.
     *
     * @return The software information.
     */
    public String getSoftware() {
        return software;
    }

    /**
     * Sets the software information.
     *
     * @param software The software information.
     */
    public void setSoftware(String software) {
        this.software = software;
    }

    /**
     * Gets the user information.
     *
     * @return The user information.
     */
    public String getUser() {
        return user;
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
     * Gets the file key.
     *
     * @return The file key.
     */
    public String getFileKey() {
        return fileKey;
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

    public void setObservationHours(List<Double> observationHoursList) {
        this.observationHours = new ArrayList<>(observationHoursList);
    }

    /**
     * Gets the size of the metadata.
     *
     * @return The size of the metadata.
     */
    public double getSize() {
        return size;
    }

    /**
     * Sets the size of the metadata.
     *
     * @param size The size of the metadata.
     */
    public void setSize(double size) {
        this.size = size;
    }

    /**
     * Adds image information.
     *
     * @param key   The key for the image information.
     * @param value The value for the image information.
     */
    public void addImageInfo(String key, String value) {
        this.image_info.put(key, value);
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
