package ui;

import model.User;
import model.StudyPlan;
import service.StudyPlanGenerator;
import service.AIPlanService;
import service.GitHubOAuthService;
import dao.StudyPlanDAO;
import dao.StudyTaskDAO;
import dao.UserDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.Map;

public class MultiRepoSelectionDialog extends JDialog {

    private User user;
    private List<Map<String, String>> repositories;
    private JPanel centerPanel;
    private JComboBox<String> durationCombo;
    private JSpinner hoursSpinner;
    private JLabel statusLabel;
    private boolean planGenerated = false;
    private StudyPlanGenerator planGenerator;
    private AIPlanService aiPlanService;
    private StudyPlanDAO planDAO;
    private StudyTaskDAO taskDAO;
    private UserDAO userDAO;
    private JCheckBox useAICheckbox;
    private JTextField planNameField;
    private JTextArea projectPromptArea;

    private List<JComboBox<String>> priorityCombos;
    private List<JTextArea> featureAreas;
    private List<JComboBox<String>> experienceCombos;
    private List<JCheckBox> checkBoxes;

    private final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private final Color SUCCESS_COLOR = new Color(16, 185, 129);
    private final Color DANGER_COLOR = new Color(239, 68, 68);
    private final Color WARNING_COLOR = new Color(245, 158, 11);
    private final Color BG_LIGHT = new Color(249, 250, 251);
    private final Color BORDER_COLOR = new Color(229, 231, 235);
    private final Color TEXT_SECONDARY = new Color(107, 114, 128);

    public MultiRepoSelectionDialog(JFrame parent, User user, List<Map<String, String>> repositories) {
        super(parent, "Create Study Plan", true);
        this.user = user;
        this.repositories = repositories;
        this.planGenerator = new StudyPlanGenerator();
        this.aiPlanService = new AIPlanService();
        this.planDAO = new StudyPlanDAO();
        this.taskDAO = new StudyTaskDAO();
        this.userDAO = new UserDAO();
        this.priorityCombos = new ArrayList<>();
        this.featureAreas = new ArrayList<>();
        this.experienceCombos = new ArrayList<>();
        this.checkBoxes = new ArrayList<>();

        setSize(950, 850);
        setLocationRelativeTo(parent);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(BG_LIGHT);

        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        centerPanel = createCenterPanel();
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

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

        JLabel titleLabel = new JLabel("Create Your Study Plan");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(17, 24, 39));

        JLabel subtitleLabel = new JLabel("Describe your project idea - AI will design the structure and daily tasks");
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

        JPanel ideaPanel = new JPanel(new BorderLayout());
        ideaPanel.setBackground(Color.WHITE);
        ideaPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                "📝 Project Idea",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                PRIMARY_COLOR
        ));

        JPanel ideaContentPanel = new JPanel(new BorderLayout());
        ideaContentPanel.setBackground(Color.WHITE);
        ideaContentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel ideaLabel = new JLabel("Describe your project idea in detail:");
        ideaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ideaLabel.setForeground(TEXT_SECONDARY);

        projectPromptArea = new JTextArea(6, 40);
        projectPromptArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        projectPromptArea.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        projectPromptArea.setLineWrap(true);
        projectPromptArea.setWrapStyleWord(true);
        projectPromptArea.setText("Example: I want to build a movie ticket booking system with:\n" +
                "- User authentication (login/signup)\n" +
                "- Movie listing with filters\n" +
                "- Seat selection and booking\n" +
                "- Payment integration\n" +
                "- Booking history\n" +
                "Tech Stack: React, Node.js, MongoDB");

        JScrollPane ideaScroll = new JScrollPane(projectPromptArea);
        ideaScroll.setPreferredSize(new Dimension(800, 120));

        ideaContentPanel.add(ideaLabel, BorderLayout.NORTH);
        ideaContentPanel.add(ideaScroll, BorderLayout.CENTER);
        ideaPanel.add(ideaContentPanel);

        panel.add(ideaPanel);
        panel.add(Box.createVerticalStrut(15));

        JPanel repoHeader = new JPanel(new GridLayout(1, 5, 10, 0));
        repoHeader.setBackground(Color.WHITE);
        repoHeader.setBorder(new EmptyBorder(10, 0, 10, 0));
        repoHeader.add(new JLabel("Repository", SwingConstants.LEFT));
        repoHeader.add(new JLabel("Priority", SwingConstants.CENTER));
        repoHeader.add(new JLabel("Target Features", SwingConstants.CENTER));
        repoHeader.add(new JLabel("Experience", SwingConstants.CENTER));
        repoHeader.add(new JLabel("Select", SwingConstants.CENTER));

        panel.add(repoHeader);
        panel.add(Box.createVerticalStrut(5));

        for (Map<String, String> repo : repositories) {
            panel.add(createRepoRow(repo));
            panel.add(Box.createVerticalStrut(10));
        }

        return panel;
    }

    private JPanel createRepoRow(Map<String, String> repo) {
        JPanel row = new JPanel(new GridLayout(1, 5, 10, 0));
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel repoLabel = new JLabel("<html><b>" + repo.get("name") + "</b><br>" +
                "<font color='gray' size='-2'>" + repo.get("description") + "</font></html>");
        repoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JComboBox<String> priorityCombo = new JComboBox<>(new String[]{"HIGH", "MEDIUM", "LOW"});
        priorityCombo.setPreferredSize(new Dimension(100, 35));
        priorityCombo.setBackground(Color.WHITE);
        priorityCombo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        priorityCombos.add(priorityCombo);

        JTextArea featuresArea = new JTextArea(2, 15);
        featuresArea.setLineWrap(true);
        featuresArea.setWrapStyleWord(true);
        featuresArea.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        featuresArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane featureScroll = new JScrollPane(featuresArea);
        featureScroll.setPreferredSize(new Dimension(200, 50));
        featureAreas.add(featuresArea);

        JComboBox<String> experienceCombo = new JComboBox<>(new String[]{"BEGINNER", "INTERMEDIATE", "ADVANCED"});
        experienceCombo.setPreferredSize(new Dimension(120, 35));
        experienceCombo.setBackground(Color.WHITE);
        experienceCombo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        experienceCombos.add(experienceCombo);

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

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Plan Name:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        planNameField = new JTextField(20);
        planNameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        planNameField.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(planNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Study Duration:"), gbc);

        gbc.gridx = 1;
        String[] durations = {"1 month", "2 months", "3 months", "6 months"};
        durationCombo = new JComboBox<>(durations);
        durationCombo.setPreferredSize(new Dimension(150, 35));
        panel.add(durationCombo, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Daily Hours:"), gbc);

        gbc.gridx = 3;
        SpinnerNumberModel hoursModel = new SpinnerNumberModel(2, 1, 8, 1);
        hoursSpinner = new JSpinner(hoursModel);
        hoursSpinner.setPreferredSize(new Dimension(100, 35));
        panel.add(hoursSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 4;
        useAICheckbox = new JCheckBox("✨ Use AI Smart Plan (Recommended) - Converts your idea into executable tasks");
        useAICheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        useAICheckbox.setForeground(PRIMARY_COLOR);
        useAICheckbox.setSelected(AIPlanService.isAIAvailable());
        if (!AIPlanService.isAIAvailable()) {
            useAICheckbox.setEnabled(false);
            useAICheckbox.setToolTipText("AI not available. Please configure API key in config.properties");
        }
        panel.add(useAICheckbox, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(PRIMARY_COLOR);
        panel.add(statusLabel, gbc);

        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;

        JButton generateBtn = new JButton("Generate Smart Plan");
        generateBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        generateBtn.setForeground(Color.WHITE);
        generateBtn.setBackground(SUCCESS_COLOR);
        generateBtn.setFocusPainted(false);
        generateBtn.setPreferredSize(new Dimension(250, 50));
        generateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        generateBtn.addActionListener(e -> {
            String planName = planNameField.getText().trim();
            if (planName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a plan name", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String projectIdea = projectPromptArea.getText().trim();
            if (projectIdea.isEmpty() || projectIdea.equals("Example: I want to build a movie ticket booking system with:\n" +
                    "- User authentication (login/signup)\n" +
                    "- Movie listing with filters\n" +
                    "- Seat selection and booking\n" +
                    "- Payment integration\n" +
                    "- Booking history\n" +
                    "Tech Stack: React, Node.js, MongoDB")) {
                JOptionPane.showMessageDialog(this, "Please describe your project idea", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            generatePlan(planName, projectIdea);
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(generateBtn);

        panel.add(btnPanel, gbc);

        return panel;
    }

    private void generatePlan(String planName, String projectIdea) {
        List<String> selectedRepos = new ArrayList<>();
        List<String> priorities = new ArrayList<>();
        List<String> features = new ArrayList<>();
        List<String> experienceLevels = new ArrayList<>();

        for (int i = 0; i < checkBoxes.size(); i++) {
            JCheckBox checkBox = checkBoxes.get(i);
            if (checkBox.isSelected()) {
                String repoName = repositories.get(i).get("name");
                selectedRepos.add(repoName);
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

        int durationMonths = durationCombo.getSelectedIndex() + 1;
        int dailyHours = (Integer) hoursSpinner.getValue();
        LocalDate endDate = LocalDate.now().plusMonths(durationMonths);

        statusLabel.setText("Generating your smart study plan...");
        statusLabel.setForeground(PRIMARY_COLOR);

        JDialog progressDialog = new JDialog(this, "Generating Plan", true);
        progressDialog.setLayout(new BorderLayout());
        progressDialog.setSize(550, 400);
        progressDialog.setLocationRelativeTo(this);

        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        progressPanel.setBackground(Color.WHITE);

        JLabel progressLabel = new JLabel("Creating your study plan...");
        progressLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        progressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(450, 20));

        JTextArea stepArea = new JTextArea(10, 45);
        stepArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        stepArea.setForeground(new Color(107, 114, 128));
        stepArea.setBackground(Color.WHITE);
        stepArea.setEditable(false);
        stepArea.setLineWrap(true);
        stepArea.setWrapStyleWord(true);
        JScrollPane stepScroll = new JScrollPane(stepArea);
        stepScroll.setPreferredSize(new Dimension(500, 250));
        stepScroll.setBorder(null);

        progressPanel.add(progressLabel);
        progressPanel.add(Box.createVerticalStrut(15));
        progressPanel.add(progressBar);
        progressPanel.add(Box.createVerticalStrut(15));
        progressPanel.add(stepScroll);

        progressDialog.add(progressPanel);

        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    boolean useAI = useAICheckbox.isSelected() && AIPlanService.isAIAvailable();

                    publish("📋 Plan Name: " + planName);
                    publish("📝 Project Idea: " + (projectIdea.length() > 100 ? projectIdea.substring(0, 100) + "..." : projectIdea));
                    publish("📦 Repositories: " + selectedRepos.size() + " selected");
                    publish("⚙️ Using " + (useAI ? "AI" : "Manual") + " generation");

                    if (useAI) {
                        publish("🤖 AI is designing your project structure...");
                        publish("📂 Creating file structure based on your idea...");
                        publish("📅 Generating day-wise tasks...");

                        String firstRepo = selectedRepos.get(0);

                        AIPlanService.RepositoryAnalysis analysis = aiPlanService.analyzeRepository(user.getAccessToken(), firstRepo);
                        analysis.projectIdea = projectIdea;
                        analysis.userPrompt = projectIdea;
                        analysis.isEmpty = true;

                        publish("🏗️ Designing system architecture...");
                        publish("📁 Creating file structure...");

                        StudyPlan plan = aiPlanService.generatePlanFromIdea(user, analysis, endDate, dailyHours, projectIdea);

                        if (plan != null) {
                            plan.setRole("IT");
                            plan.setLoginType("GITHUB");
                            planDAO.update(plan);

                            publish("✅ Plan created successfully! ID: " + plan.getId());
                            publish("📁 File structure created based on your idea");
                            publish("📝 Daily tasks generated for " + durationMonths + " months");

                            userDAO.updateActivePlan(user.getId(), plan.getId());
                            user.setActivePlanId(plan.getId());
                        } else {
                            publish("⚠️ AI plan failed, using manual fallback...");
                            fallbackManualPlan(selectedRepos, priorities, features, experienceLevels,
                                    durationMonths, dailyHours, planName);
                        }
                    } else {
                        publish("📝 Using manual plan generation...");
                        fallbackManualPlan(selectedRepos, priorities, features, experienceLevels,
                                durationMonths, dailyHours, planName);
                    }

                    publish("✅ All plans generated successfully!");

                } catch (Exception e) {
                    e.printStackTrace();
                    publish("❌ Error: " + e.getMessage());
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Error: " + e.getMessage());
                        statusLabel.setForeground(DANGER_COLOR);
                    });
                }
                return null;
            }

            private void fallbackManualPlan(List<String> repos, List<String> priorities,
                                            List<String> features, List<String> experienceLevels,
                                            int durationMonths, int dailyHours, String planName) {
                try {
                    planGenerator.generatePlan(user, repos, priorities, features,
                            durationMonths, dailyHours, experienceLevels);

                    List<StudyPlan> plans = planDAO.findByUserIdAndRole(user.getId(), "IT", "GITHUB");
                    if (!plans.isEmpty()) {
                        StudyPlan plan = plans.get(0);
                        plan.setPlanName(planName);
                        plan.setRole("IT");
                        plan.setLoginType("GITHUB");
                        planDAO.update(plan);

                        userDAO.updateActivePlan(user.getId(), plan.getId());
                        user.setActivePlanId(plan.getId());
                        publish("✅ Manual plan created successfully!");
                    } else {
                        publish("❌ Failed to create manual plan");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    publish("❌ Error creating manual plan: " + e.getMessage());
                }
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    stepArea.append(message + "\n");
                    stepArea.setCaretPosition(stepArea.getDocument().getLength());
                }
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                statusLabel.setText("Study plan generated successfully!");
                statusLabel.setForeground(SUCCESS_COLOR);
                planGenerated = true;

                StringBuilder summary = new StringBuilder();
                summary.append("✅ Study Plan Created!\n\n");
                summary.append("Plan Name: ").append(planName).append("\n");
                summary.append("Duration: ").append(durationMonths).append(" months\n");
                summary.append("Daily Hours: ").append(dailyHours).append("\n");
                summary.append("Repositories (").append(selectedRepos.size()).append("):\n");
                for (String repo : selectedRepos) {
                    summary.append("  • ").append(repo).append("\n");
                }
                summary.append("\n📂 AI has designed a complete file structure based on your idea.\n");
                summary.append("📝 Daily tasks will appear in 'My Study Plan' section.\n");
                summary.append("🔍 Progress is tracked via GitHub commits.");

                JOptionPane.showMessageDialog(MultiRepoSelectionDialog.this,
                        summary.toString(),
                        "Plan Created",
                        JOptionPane.INFORMATION_MESSAGE);

                javax.swing.Timer timer = new javax.swing.Timer(2000, e -> dispose());
                timer.setRepeats(false);
                timer.start();
            }
        };

        worker.execute();
        progressDialog.setVisible(true);
    }

    public boolean isPlanGenerated() {
        return planGenerated;
    }
}