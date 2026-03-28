package model;

public class Topic {
    private int id;
    private int planId;
    private String subject;        // Subject this topic belongs to (from the plan's subjects list)
    private String name;
    private int difficulty;         // 1-5 scale
    private int size;               // estimated size or number of subtopics
    private double weight;          // calculated as difficulty * size

    // Default constructor
    public Topic() {}

    // Constructor for new topics (without id)
    public Topic(int planId, String subject, String name, int difficulty, int size) {
        this.planId = planId;
        this.subject = subject;
        this.name = name;
        this.difficulty = difficulty;
        this.size = size;
        this.weight = difficulty * size; // calculate weight
    }

    // Full constructor
    public Topic(int id, int planId, String subject, String name, int difficulty, int size, double weight) {
        this.id = id;
        this.planId = planId;
        this.subject = subject;
        this.name = name;
        this.difficulty = difficulty;
        this.size = size;
        this.weight = weight;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
        recalculateWeight();
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
        recalculateWeight();
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    // Helper method to recalculate weight when difficulty or size changes
    private void recalculateWeight() {
        this.weight = this.difficulty * this.size;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "id=" + id +
                ", planId=" + planId +
                ", subject='" + subject + '\'' +
                ", name='" + name + '\'' +
                ", difficulty=" + difficulty +
                ", size=" + size +
                ", weight=" + weight +
                '}';
    }
}