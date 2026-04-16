package ui;

import model.Topic;
import dao.TopicDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddTopicDialog extends JDialog {
    private int planId;
    private String[] subjects;
    private JComboBox<String> subjectCombo;
    private JTextField nameField;
    private JComboBox<Integer> difficultyCombo;
    private JTextField sizeField;
    private JButton addButton;
    private JButton cancelButton;
    private Topic addedTopic;
    private boolean succeeded;

    public AddTopicDialog(Frame owner, int planId, String[] subjects) {
        super(owner, "Add New Topic", true);
        this.planId = planId;
        this.subjects = subjects;

        // Debug print
        System.out.println("AddTopicDialog created with " + (subjects != null ? subjects.length : 0) + " subjects:");
        if (subjects != null) {
            for (String s : subjects) {
                System.out.println("  - " + s);
            }
        }

        initUI();
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(400, 300);
        setResizable(false);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Subject dropdown
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Subject:"), gbc);
        gbc.gridx = 1;

        // Check if subjects array is valid
        if (subjects == null || subjects.length == 0 || (subjects.length == 1 && (subjects[0] == null || subjects[0].isEmpty()))) {
            subjectCombo = new JComboBox<>(new String[]{"General"});
            subjectCombo.setEnabled(false);
        } else {
            subjectCombo = new JComboBox<>(subjects);
        }
        subjectCombo.setSelectedIndex(0);
        formPanel.add(subjectCombo, gbc);

        // Topic Name
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Topic Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(15);
        formPanel.add(nameField, gbc);

        // Difficulty (1-5)
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Difficulty (1-5):"), gbc);
        gbc.gridx = 1;
        difficultyCombo = new JComboBox<>(new Integer[]{1,2,3,4,5});
        difficultyCombo.setSelectedIndex(2);
        formPanel.add(difficultyCombo, gbc);

        // Size
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Size (subtopics/hours):"), gbc);
        gbc.gridx = 1;
        sizeField = new JTextField("1", 15);
        formPanel.add(sizeField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        addButton = new JButton("Add Topic");
        cancelButton = new JButton("Cancel");

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTopic();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                succeeded = false;
                dispose();
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addTopic() {
        String subject = (String) subjectCombo.getSelectedItem();
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter topic name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int difficulty = (Integer) difficultyCombo.getSelectedItem();

        int size;
        try {
            String sizeText = sizeField.getText().trim();
            if (sizeText.isEmpty()) sizeText = "1";
            size = Integer.parseInt(sizeText);
            if (size <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Size must be a positive integer.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        System.out.println("Adding topic: Subject=" + subject + ", Name=" + name +
                ", Difficulty=" + difficulty + ", Size=" + size + ", PlanId=" + planId);

        // Create Topic object with subject
        Topic topic = new Topic(planId, subject, name, difficulty, size);

        // Save to database via TopicDAO
        TopicDAO topicDAO = new TopicDAO();
        Topic saved = topicDAO.save(topic);
        if (saved != null) {
            addedTopic = saved;
            succeeded = true;
            JOptionPane.showMessageDialog(this, "Topic added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save topic to database.\nPlease check database connection.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public Topic getAddedTopic() {
        return addedTopic;
    }
}