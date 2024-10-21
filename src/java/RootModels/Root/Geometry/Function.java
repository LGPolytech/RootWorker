package RootModels.Root.Geometry;

import java.util.ArrayList;
import java.util.List;

/**
 * The Function class represents a function with a name and a list of samples.
 */
public class Function {
    private final String name; // The name of the function
    private final List<Double> samples; // The list of samples

    public Function(String name, List<Double> samples) {
        this.name = name;
        this.samples = samples;
    }

    /**
     * Gets the name of the function.
     *
     * @return The name of the function.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a string representation of the function.
     *
     * @return A string representation of the function.
     */
    @Override
    public String toString() {
        return "Function{" +
                "name='" + name + '\'' +
                ", samples=" + samples +
                '}';
    }
}