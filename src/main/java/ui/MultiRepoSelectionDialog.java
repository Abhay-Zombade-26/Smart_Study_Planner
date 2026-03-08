package ui;

import model.User;
import service.StudyPlanGenerator;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultiRepoSelectionDialog extends JDialog {
    
    private User user;
    private List<Map<String, String>> repositories;
    private JPanel centerPanel;
    private DefaultTableModel tableModel;
    private JComboBox<String> durationCombo;
    private JSpinner hoursSpinner;
    private JLabel statusLabel;
    private boolean planGenerated = false;
    private StudyPlanGenerator planGenerator;
    
    // Input fields
    private java.util.List<JComboBox<String>> priorityCombos;
    private java.util.List<JTextArea> featureAreas;
    private java.util.List<JComboBox<String>> experienceCombos;
    private java.util.List<JCheckBox> checkBoxes;
    
    // Color scheme
    private final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private final Color SUCCESS_COLOR = new Color(16, 185, 129);
    private final Color DANGER_COLOR = new Color(239, 68, 68);
    private final Color WARNING_COLOR = new Color(245, 158, 11);
    private final Color BG_LIGHT = new Color(249, 250, 251);
    private final Color BORDER_COLOR = new Color(229, 231, 235);
    
    public MultiRepoSelectionDialog(JFrame parent, User user, List<Map<String, String>> repositories) {
        super(parent, "Select Repositories for Study Plan", true);
        this.user = user;
        this.repositories = repositories;
        this.planGenerator = new StudyPlanGenerator();
        this.priorityCombos = new ArrayList<>();
        this.featureAreas = new ArrayList<>();
        this.experienceCombos = new ArrayList<>();
        this.checkBoxes = new ArrayList<>();
        
        setSize(900, 700);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        initUI();
    }
    
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(BG_LIGHT);
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Repository Selection Panel with Details
        centerPanel = createCenterPanel();
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Plan Configuration
        JPanel configPanel = createConfigPanel();
        mainPanel.add(configPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(20, 25, 20, 25)
        ));
        
        JLabel titleLabel = new JLabel("Configure Your Study Plan");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(17, 24, 39));
        
        JLabel subtitleLabel = new JLabel("Set priority, features, and experience level for each repository");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(107, 114, 128));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(Color.WHITE);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        // Headers
        JPanel headerRow = new JPanel(new GridLayout(1, 5, 10, 0));
        headerRow.setBackground(Color.WHITE);
        headerRow.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        headerRow.add(new JLabel("Repository", SwingConstants.LEFT));
        headerRow.add(new JLabel("Priority", SwingConstants.CENTER));
        headerRow.add(new JLabel("Target Features", SwingConstants.CENTER));
        headerRow.add(new JLabel("Experience", SwingConstants.CENTER));
        headerRow.add(new JLabel("Select", SwingConstants.CENTER));
        
        panel.add(headerRow);
        panel.add(Box.createVerticalStrut(10));
        
        // Repository rows
        for (Map<String, String> repo : repositories) {
            panel.add(createRepoRow(repo));
            panel.add(Box.createVerticalStrut(10));
        }
        
        return panel;
    }
    
    private JPanel createRepoRow(Map<String, String> repo) {
        JPanel row = new JPanel(new GridLayout(1, 5, 10, 0));
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        // Repository name
        JLabel repoLabel = new JLabel(repo.get("name"));
        repoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        // Priority combo
        JComboBox<String> priorityCombo = new JComboBox<>(new String[]{"HIGH", "MEDIUM", "LOW"});
        priorityCombo.setPreferredSize(new Dimension(100, 30));
        priorityCombo.setBackground(Color.WHITE);
        priorityCombo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        priorityCombos.add(priorityCombo);
        
        // Features text area
        JTextArea featuresArea = new JTextArea(2, 15);
        featuresArea.setLineWrap(true);
        featuresArea.setWrapStyleWord(true);
        featuresArea.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        featuresArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane featureScroll = new JScrollPane(featuresArea);
        featureScroll.setPreferredSize(new Dimension(200, 50));
        featureAreas.add(featuresArea);
        
        // Experience combo
        JComboBox<String> experienceCombo = new JComboBox<>(new String[]{"BEGINNER", "INTERMEDIATE", "ADVANCED"});
        experienceCombo.setPreferredSize(new Dimension(120, 30));
        experienceCombo.setBackground(Color.WHITE);
        experienceCombo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        experienceCombos.add(experienceCombo);
        
        // Select checkbox
        JCheckBox selectCheck = new JCheckBox();
        selectCheck.setBackground(Color.WHITE);
        checkBoxes.add(selectCheck);
        
        row.add(repoLabel);
        row.add(priorityCombo);
        row.add(featureScroll);
        row.add(experienceCombo);
        row.add(selectCheck);
        
        return row;
    }
    
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Duration
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Study Duration:"), gbc);
        
        gbc.gridx = 1;
        String[] durations = {"1 month", "2 months", "3 months", "6 months"};
        durationCombo = new JComboBox<>(durations);
        durationCombo.setPreferredSize(new Dimension(150, 35));
        durationCombo.setBackground(Color.WHITE);
        durationCombo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(durationCombo, gbc);
        
        // Daily Hours
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Daily Hours:"), gbc);
        
        gbc.gridx = 3;
        SpinnerNumberModel hoursModel = new SpinnerNumberModel(2, 1, 8, 1);
        hoursSpinner = new JSpinner(hoursModel);
        hoursSpinner.setPreferredSize(new Dimension(100, 35));
        panel.add(hoursSpinner, gbc);
        
        // Status Label
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 4;
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(PRIMARY_COLOR);
        panel.add(statusLabel, gbc);
        
        // Generate Button
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JButton generateBtn = new JButton("Generate Smart Plan");
        generateBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        generateBtn.setForeground(Color.WHITE);
        generateBtn.setBackground(SUCCESS_COLOR);
        generateBtn.setFocusPainted(false);
        generateBtn.setPreferredSize(new Dimension(250, 50));
        generateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        generateBtn.addActionListener(e -> generatePlan());
        
        // Hover effect
        generateBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                generateBtn.setBackground(SUCCESS_COLOR.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                generateBtn.setBackground(SUCCESS_COLOR);
            }
        });
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(generateBtn);
        
        panel.add(btnPanel, gbc);
        
        return panel;
    }
    
    private void generatePlan() {
        // Get selected repositories and their configurations
        List<String> selectedRepos = new ArrayList<>();
        List<String> priorities = new ArrayList<>();
        List<String> features = new ArrayList<>();
        List<String> experienceLevels = new ArrayList<>();
        
        // Loop through checkboxes to find selected ones
        for (int i = 0; i < checkBoxes.size(); i++) {
            JCheckBox checkBox = checkBoxes.get(i);
            if (checkBox.isSelected()) {
                // Get repository name from the repositories list
                String repoName = repositories.get(i).get("name");
                selectedRepos.add(repoName);
                
                // Get corresponding inputs
                priorities.add((String) priorityCombos.get(i).getSelectedItem());
                features.add(featureAreas.get(i).getText());
                experienceLevels.add((String) experienceCombos.get(i).getSelectedItem());
            }
        }
        
        if (selectedRepos.isEmpty()) {
            statusLabel.setText("Please select at least one repository");
            statusLabel.setForeground(DANGER_COLOR);
            return;
        }
        
        // Validate features input
        for (int i = 0; i < features.size(); i++) {
            if (features.get(i).trim().isEmpty()) {
                statusLabel.setText("Please enter target features for all selected repositories");
                statusLabel.setForeground(DANGER_COLOR);
                return;
            }
        }
        
        // Get configuration
        int durationMonths = durationCombo.getSelectedIndex() + 1;
        int dailyHours = (Integer) hoursSpinner.getValue();
        
        statusLabel.setText("Generating your smart study plan...");
        statusLabel.setForeground(PRIMARY_COLOR);
        
        // Generate plan in background
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    planGenerator.generatePlan(user, selectedRepos, priorities, features, 
                                             durationMonths, dailyHours, experienceLevels);
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Error: " + e.getMessage());
                        statusLabel.setForeground(DANGER_COLOR);
                    });
                }
                return null;
            }
            
            @Override
            protected void done() {
                statusLabel.setText("Study plan generated successfully!");
                statusLabel.setForeground(SUCCESS_COLOR);
                planGenerated = true;
                
                // Show summary
                StringBuilder summary = new StringBuilder();
                summary.append("Study Plan Generated!\n\n");
                summary.append("Duration: ").append(durationMonths).append(" months\n");
                summary.append("Daily Hours: ").append(dailyHours).append("\n");
                summary.append("Repositories: ").append(selectedRepos.size()).append("\n\n");
                summary.append("Planned Features:\n");
                
                for (int i = 0; i < selectedRepos.size(); i++) {
                    summary.append("? ").append(selectedRepos.get(i)).append(": ")
                           .append(features.get(i)).append("\n");
                }
                
                JOptionPane.showMessageDialog(MultiRepoSelectionDialog.this,
                    summary.toString(),
                    "Plan Generated",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Close dialog
                Timer timer = new Timer(1500, e -> dispose());
                timer.setRepeats(false);
                timer.start();
            }
        };
        worker.execute();
    }
    
    public boolean isPlanGenerated() {
        return planGenerated;
    }
}
