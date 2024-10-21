package RootModels;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import RootModels.Root.Root;

public class RootModel {
    public final TreeMap<LocalDateTime, RootModelEntry> dataByDate;

    public RootModel(TreeMap<LocalDateTime, RootModelEntry> dataByDate) {
        this.dataByDate = dataByDate;
    }

    public TreeMap<LocalDateTime, RootModelEntry> getDataByDate() {
        return dataByDate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RootModel composed of:\n");
        for (Map.Entry<LocalDateTime, RootModelEntry> entry : dataByDate.entrySet()) {
            sb.append("\tDate: ").append(entry.getKey()).append("\n");
            sb.append("\tScene: ").append(entry.getValue().scene).append("\n");
            sb.append("\tMetadata: ").append(entry.getValue().metadata).append("\n");
            sb.append("\tNumber of roots: ").append(entry.getValue().flatRootList.size()).append("\n");
        }
        return sb.toString();
    }

    static class RootModelEntry {
        public final Scene scene;
        public final Metadata metadata;
        public final List<Root> flatRootList;

        public RootModelEntry(Scene scene, Metadata metadata, List<Root> flatRootList) {
            this.scene = scene;
            this.metadata = metadata;
            this.flatRootList = flatRootList;
        }
    }
}

