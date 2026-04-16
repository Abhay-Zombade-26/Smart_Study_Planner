package ui;

import dao.StudyPlanDAO;
import model.User;
import service.GitHubService;
import service.ITPlanGenerator;
import service.PlanStrategyService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ITStudyPlannerFrame extends JFrame {

    private User user;
    private JPanel mainPanel;
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    private JComboBox<String> repoComboBox;
    private JSpinner deadlineSpinner;
    private JComboBox<Integer> hoursComboBox;
    private JComboBox<String> difficultyComboBox;
    private JButton generatePlanBtn;
    private JLabel statusLabel;

    private GitHubService gitHubService;
    private PlanStrategyService planGenerator;
    private StudyPlanDAO studyPlanDAO;

    private List<Map<String, String>> repositories;

    public ITStudyPlannerFrame(User user) {
        this.user = user;
        this.gitHubService = new GitHubService(user.getAccessToken());
        this.planGenerator = new ITPlanGenerator();
        this.studyPlanDAO = new StudyPlanDAO();

        initUI();
        loadRepositories();
    }

    private void initUI() {
        setTitle("Smart Study Planner - IT Student");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        // Main layout with sidebar
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 250));

        // Create sidebar
        createSidebar();

        // Create content area with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add all screens
        contentPanel.add(createGoalSetupPanel(), "GOAL_SETUP");
        contentPanel.add(createDashboardPanel(), "DASHBOARD");
        contentPanel.add(createStudyPlanPanel(), "STUDY_PLAN");
        contentPanel.add(createGitHubProgressPanel(), "GITHUB_PROGRESS");
        contentPanel.add(createGoalsPanel(), "GOALS");
        contentPanel.add(createProfilePanel(), "PROFILE");

        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);

        // Show goal setup first
        cardLayout.show(contentPanel, "GOAL_SETUP");
    }

    private void createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(33, 33, 33));
        sidebarPanel.setPreferredSize(new Dimension(250, getHeight()));
        sidebarPanel.setBorder(new EmptyBorder(20, 15, 20, 15));

        // App logo
        JLabel logoLabel = new JLabel("📘 Smart Planner");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoLabel.setBorder(new EmptyBorder(0, 10, 20, 0));

        sidebarPanel.add(logoLabel);
        sidebarPanel.add(Box.createVerticalStrut(20));

        // User info
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setBackground(new Color(45, 45, 45));
        userInfoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));
        userInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        userInfoPanel.setMaximumSize(new Dimension(220, 80));

        JLabel nameLabel = new JLabel(user.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLabel = new JLabel("IT Student");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(new Color(160, 174, 192));
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        userInfoPanel.add(nameLabel);
        userInfoPanel.add(Box.createVerticalStrut(5));
        userInfoPanel.add(roleLabel);

        sidebarPanel.add(userInfoPanel);
        sidebarPanel.add(Box.createVerticalStrut(30));

        // Navigation buttons
        addNavButton("🎯 Goal Setup", "GOAL_SETUP");
        addNavButton("📊 Dashboard", "DASHBOARD");
        addNavButton("📋 My Study Plan", "STUDY_PLAN");
        addNavButton("📈 GitHub Progress", "GITHUB_PROGRESS");
        addNavButton("🎨 Goals", "GOALS");
        addNavButton("👤 Profile", "PROFILE");

        sidebarPanel.add(Box.createVerticalGlue());

        // Logout button
        JButton logoutBtn = new JButton("🚪 Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(new Color(220, 38, 38));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(220, 40));
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to logout?",
                    "Logout",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });

        sidebarPanel.add(logoutBtn);
    }

    private void addNavButton(String text, String cardName) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(new Color(226, 232, 240));
        button.setBackground(new Color(33, 33, 33));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(220, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(10, 15, 10, 15));

        button.addActionListener(e -> cardLayout.show(contentPanel, cardName));

        sidebarPanel.add(button);
        sidebarPanel.add(Box.createVerticalStrut(5));
    }

    private JPanel createGoalSetupPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 20, 10, 20);

        // Header
        JLabel headerLabel = new JLabel("Create Your Smart Study Plan");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(33, 33, 33));

        JLabel subHeaderLabel = new JLabel("Select a GitHub repository to generate an AI-powered study plan");
        subHeaderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subHeaderLabel.setForeground(new Color(100, 116, 139));

        panel.add(headerLabel, gbc);
        panel.add(subHeaderLabel, gbc);

        // Repository selection
        panel.add(Box.createVerticalStrut(20), gbc);

        JLabel repoLabel = new JLabel("Select Repository");
        repoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(repoLabel, gbc);

        repoComboBox = new JComboBox<>();
        repoComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        repoComboBox.setPreferredSize(new Dimension(400, 40));
        repoComboBox.setMaximumSize(new Dimension(400, 40));
        repoComboBox.setBackground(Color.WHITE);
        repoComboBox.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        repoComboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.add(repoComboBox, gbc);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 116, 139));
        panel.add(statusLabel, gbc);

        // Deadline configuration
        panel.add(Box.createVerticalStrut(20), gbc);

        JLabel deadlineLabel = new JLabel("Target Deadline");
        deadlineLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(deadlineLabel, gbc);

        SpinnerDateModel dateModel = new SpinnerDateModel();
        deadlineSpinner = new JSpinner(dateModel);
        deadlineSpinner.setEditor(new JSpinner.DateEditor(deadlineSpinner, "yyyy-MM-dd"));
        deadlineSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        deadlineSpinner.setPreferredSize(new Dimension(400, 40));
        deadlineSpinner.setMaximumSize(new Dimension(400, 40));
        panel.add(deadlineSpinner, gbc);

        // Daily hours
        panel.add(Box.createVerticalStrut(20), gbc);

        JLabel hoursLabel = new JLabel("Daily Study Hours");
        hoursLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(hoursLabel, gbc);

        Integer[] hours = {1, 2, 3, 4, 5, 6, 7, 8};
        hoursComboBox = new JComboBox<>(hours);
        hoursComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hoursComboBox.setPreferredSize(new Dimension(400, 40));
        hoursComboBox.setMaximumSize(new Dimension(400, 40));
        hoursComboBox.setSelectedItem(2);
        panel.add(hoursComboBox, gbc);

        // Difficulty
        panel.add(Box.createVerticalStrut(20), gbc);

        JLabel difficultyLabel = new JLabel("Difficulty Preference");
        difficultyLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(difficultyLabel, gbc);

        String[] difficulties = {"Easy", "Moderate", "Hard"};
        difficultyComboBox = new JComboBox<>(difficulties);
        difficultyComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        difficultyComboBox.setPreferredSize(new Dimension(400, 40));
        difficultyComboBox.setMaximumSize(new Dimension(400, 40));
        difficultyComboBox.setSelectedItem("Moderate");
        panel.add(difficultyComboBox, gbc);

        // Generate button
        panel.add(Box.createVerticalStrut(30), gbc);

        generatePlanBtn = new JButton("Generate Smart Plan 🚀");
        generatePlanBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        generatePlanBtn.setForeground(Color.WHITE);
        generatePlanBtn.setBackground(new Color(46, 204, 113));
        generatePlanBtn.setBorderPainted(false);
        generatePlanBtn.setFocusPainted(false);
        generatePlanBtn.setPreferredSize(new Dimension(400, 50));
        generatePlanBtn.setMaximumSize(new Dimension(400, 50));
        generatePlanBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        generatePlanBtn.addActionListener(e -> generateStudyPlan());
        panel.add(generatePlanBtn, gbc);

        return panel;
    }

    private void loadRepositories() {
        statusLabel.setText("Loading repositories...");

        SwingWorker<List<Map<String, String>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, String>> doInBackground() throws Exception {
                return gitHubService.getRepositories();
            }

            @Override
            protected void done() {
                try {
                    repositories = get();
                    repoComboBox.removeAllItems();

                    if (repositories.isEmpty()) {
                        statusLabel.setText("No repositories found. Create one on GitHub first.");
                    } else {
                        for (Map<String, String> repo : repositories) {
                            repoComboBox.addItem(repo.get("name"));
                        }
                        statusLabel.setText("✓ " + repositories.size() + " repositories loaded");
                        statusLabel.setForeground(new Color(46, 204, 113));
                    }
                } catch (Exception e) {
                    statusLabel.setText("Failed to load repositories");
                    statusLabel.setForeground(new Color(220, 38, 38));
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    private void generateStudyPlan() {
        String selectedRepo = (String) repoComboBox.getSelectedItem();

        if (selectedRepo == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a repository first.",
                    "No Repository Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate deadline = ((java.util.Date) deadlineSpinner.getValue()).toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();

        int dailyHours = (Integer) hoursComboBox.getSelectedItem();
        String difficulty = (String) difficultyComboBox.getSelectedItem();

        // Check if deadline is valid
        if (deadline.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this,
                    "Deadline must be in the future.",
                    "Invalid Deadline",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Generate plan
        model.StudyPlan plan = planGenerator.generatePlan(
                user, selectedRepo, deadline, dailyHours, difficulty.toUpperCase()
        );

        if (plan != null) {
            JOptionPane.showMessageDialog(this,
                    "Smart plan generated successfully! 🎉\n" +
                            "You have " + java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), deadline) +
                            " days to complete your goal.",
                    "Plan Generated",
                    JOptionPane.INFORMATION_MESSAGE);

            // Switch to dashboard
            cardLayout.show(contentPanel, "DASHBOARD");
        }
    }

    private JPanel createDashboardPanel() {
        return new DashboardFrame(user).getMainPanel();
    }

    private JPanel createStudyPlanPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel placeholder = new JLabel("📋 Your study plan will appear here");
        placeholder.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        placeholder.setForeground(new Color(148, 163, 184));
        placeholder.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(placeholder, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createGitHubProgressPanel() {
        return new GitHubProgressFrame(user).getMainPanel();
    }

    private JPanel createGoalsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel placeholder = new JLabel("🎯 Your goals and milestones will appear here");
        placeholder.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        placeholder.setForeground(new Color(148, 163, 184));
        placeholder.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(placeholder, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createProfilePanel() {
        return new ProfileFrame(user).getMainPanel();
    }
}