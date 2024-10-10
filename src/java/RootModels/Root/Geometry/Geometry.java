package RootModels.Root.Geometry;

import io.github.rocsg.fijiyama.registration.ItkTransform;

public interface Geometry {
    void scale(double scaleFactor);

    double getLengthUntil(double time);

    double getTotalLength();

    void transform(ItkTransform transform);

    void transformBeforeTime(ItkTransform transform, double time);

    void add(Object o);

    @Override
    boolean equals(Object o);
}
