package model;

import java.util.ArrayList;
import java.util.List;

public class FeatureTemplate {
    private String featureName;
    private List<TaskTemplate> subTasks;
    private double baseHours;

    public FeatureTemplate(String featureName, double baseHours) {
        this.featureName = featureName;
        this.baseHours = baseHours;
        this.subTasks = new ArrayList<>();
    }

    public static class TaskTemplate {
        private String description;
        private String[] possibleFiles;
        private double weight; // 0.1 to 1.0
        private String layer; // UI, BACKEND, DATABASE, etc.

        public TaskTemplate(String description, String[] possibleFiles, double weight, String layer) {
            this.description = description;
            this.possibleFiles = possibleFiles;
            this.weight = weight;
            this.layer = layer;
        }

        public String getDescription() { return description; }
        public String[] getPossibleFiles() { return possibleFiles; }
        public double getWeight() { return weight; }
        public String getLayer() { return layer; }
    }

    public void addSubTask(TaskTemplate task) {
        subTasks.add(task);
    }

    public List<TaskTemplate> getSubTasks() { return subTasks; }
    public String getFeatureName() { return featureName; }
    public double getBaseHours() { return baseHours; }
}