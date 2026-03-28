package service;

import model.Topic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopicWeightCalculator {

    /**
     * Calculates and sets the weight for each topic (weight = difficulty * size).
     * @param topics list of topics to update (in-place)
     */
    public void calculateWeights(List<Topic> topics) {
        for (Topic topic : topics) {
            double weight = topic.getDifficulty() * topic.getSize();
            topic.setWeight(weight);
            System.out.println("TopicWeightCalculator: " + topic.getName() +
                    " weight = " + weight);
        }
    }

    /**
     * Computes the total weight sum for a list of topics.
     * @param topics list of topics
     * @return sum of weights
     */
    public double getTotalWeight(List<Topic> topics) {
        double total = 0.0;
        for (Topic topic : topics) {
            total += topic.getWeight();
        }
        return total;
    }

    /**
     * Distributes total available hours across topics proportionally to their weights.
     * @param topics list of topics (must have weights already set)
     * @param totalHours total hours available to distribute
     * @return map of topic id -> allocated hours (rounded to nearest tenth)
     */
    public Map<Integer, Double> distributeHours(List<Topic> topics, int totalHours) {
        Map<Integer, Double> allocation = new HashMap<>();
        double totalWeight = getTotalWeight(topics);

        if (totalWeight == 0) {
            // If no weight (shouldn't happen), distribute equally
            System.err.println("TopicWeightCalculator: total weight is zero, distributing equally.");
            double equalHours = Math.round((double) totalHours / topics.size() * 10) / 10.0;
            for (Topic topic : topics) {
                allocation.put(topic.getId(), equalHours);
            }
            return allocation;
        }

        double remainingHours = totalHours;
        double remainingWeight = totalWeight;

        // Allocate one topic at a time to avoid floating point accumulation errors
        for (int i = 0; i < topics.size(); i++) {
            Topic topic = topics.get(i);
            if (i == topics.size() - 1) {
                // Last topic gets whatever is left
                allocation.put(topic.getId(), Math.round(remainingHours * 10) / 10.0);
            } else {
                double allocated = (topic.getWeight() / totalWeight) * totalHours;
                allocated = Math.round(allocated * 10) / 10.0; // round to 0.1
                allocation.put(topic.getId(), allocated);
                remainingHours -= allocated;
                remainingWeight -= topic.getWeight();
            }
        }

        // Log allocation
        System.out.println("TopicWeightCalculator: Hour distribution:");
        for (Map.Entry<Integer, Double> entry : allocation.entrySet()) {
            System.out.println("   Topic ID " + entry.getKey() + ": " + entry.getValue() + " hours");
        }

        return allocation;
    }

    /**
     * Alternative distribution that returns a map of topic name to hours (useful for display).
     */
    public Map<String, Double> distributeHoursByName(List<Topic> topics, int totalHours) {
        Map<String, Double> allocation = new HashMap<>();
        Map<Integer, Double> idAlloc = distributeHours(topics, totalHours);
        for (Topic topic : topics) {
            allocation.put(topic.getName(), idAlloc.get(topic.getId()));
        }
        return allocation;
    }
}