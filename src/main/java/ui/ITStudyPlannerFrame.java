package ui;

import model.User;
import model.StudyTask;
import model.StudyPlan;
import service.GitHubOAuthService;
import service.GitHubCommitChecker;
import dao.StudyTaskDAO;
import dao.GoalDAO;
import dao.StudyPlanDAO;
import dao.UserDAO;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import java.net.URL;

public class ITStudyPlannerFrame extends JFrame {

    private User user;
    private JPanel mainPanel;
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JComboBox<String> repoComboBox;
    private JLabel statusLabel;
    private GitHubOAuthService gitHubService;
    private List<Map<String, String>> repositories;
    private StudyTaskDAO taskDAO;
    private GoalDAO goalDAO;
    private StudyPlanDAO planDAO;
    private UserDAO userDAO;
    private Integer currentActivePlanId;
    private JLabel activePlanLabel;
    private GitHubCommitChecker commitChecker;
    private String currentVisiblePanel;

    private static final String PANEL_GOAL_SETUP = "GOAL_SETUP";
    private static final String PANEL_DASHBOARD = "DASHBOARD";
    private static final String PANEL_STUDY_PLAN = "STUDY_PLAN";
    private static final String PANEL_GITHUB_PROGRESS = "GITHUB_PROGRESS";
    private static final String PANEL_VIEW_PLANS = "VIEW_PLANS";
    private static final String PANEL_PROFILE = "PROFILE";
    private static final String PANEL_ABOUT = "ABOUT";

    private JPanel dashboardPanel;
    private JPanel studyPlanPanel;
    private JPanel gitHubProgressPanel;
    private JPanel viewPlansPanel;

    private final Color SIDEBAR_BG = new Color(26, 32, 44);
    private final Color SIDEBAR_HOVER = new Color(44, 55, 74);
    private final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private final Color SUCCESS_COLOR = new Color(16, 185, 129);
    private final Color DANGER_COLOR = new Color(239, 68, 68);
    private final Color WARNING_COLOR = new Color(245, 158, 11);
    private final Color BG_LIGHT = new Color(249, 250, 251);
    private final Color BORDER_COLOR = new Color(229, 231, 235);
    private final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private final Color CARD_BG = Color.WHITE;

    public ITStudyPlannerFrame(User user) {
        this.user = user;
        this.gitHubService = new GitHubOAuthService();
        this.repositories = new ArrayList<>();
        this.taskDAO = new StudyTaskDAO();
        this.goalDAO = new GoalDAO();
        this.planDAO = new StudyPlanDAO();
        this.userDAO = new UserDAO();
        this.commitChecker = new GitHubCommitChecker();
        this.currentActivePlanId = user.getActivePlanId();
        this.currentVisiblePanel = PANEL_GOAL_SETUP;

        System.out.println("=== ITStudyPlannerFrame Constructor ===");
        System.out.println("User: " + user.getEmail());
        System.out.println("User ID: " + user.getId());
        System.out.println("Active Plan ID: " + currentActivePlanId);

        setTitle("Smart Study Planner - " + user.getGithubUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1200, 700));

        try {
            initUI();
            loadRepositories();
            System.out.println("ITStudyPlannerFrame initialized successfully");
            setVisible(true);
            toFront();
            requestFocus();
        } catch (Exception e) {
            System.err.println("Error initializing: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading dashboard: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initUI() {
        mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BG_LIGHT);

        createSidebar();

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_LIGHT);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        dashboardPanel = new DashboardFrame(user).getMainPanel();
        studyPlanPanel = createStudyPlanPanel();
        gitHubProgressPanel = createGitHubProgressPanel();
        viewPlansPanel = createViewPlansPanel();

        contentPanel.add(createGoalSetupPanel(), PANEL_GOAL_SETUP);
        contentPanel.add(dashboardPanel, PANEL_DASHBOARD);
        contentPanel.add(studyPlanPanel, PANEL_STUDY_PLAN);
        contentPanel.add(gitHubProgressPanel, PANEL_GITHUB_PROGRESS);
        contentPanel.add(viewPlansPanel, PANEL_VIEW_PLANS);
        contentPanel.add(createProfilePanel(), PANEL_PROFILE);
        contentPanel.add(createAboutPanel(), PANEL_ABOUT);

        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
        cardLayout.show(contentPanel, PANEL_GOAL_SETUP);
        currentVisiblePanel = PANEL_GOAL_SETUP;
    }

    private void createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(SIDEBAR_BG);
        sidebarPanel.setPreferredSize(new Dimension(280, getHeight()));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR, getWidth(), 0, SUCCESS_COLOR);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        logoPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 20));
        logoPanel.setPreferredSize(new Dimension(280, 100));
        logoPanel.setMaximumSize(new Dimension(280, 100));

        JLabel logoLabel = new JLabel("Smart Planner");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoLabel.setForeground(Color.WHITE);
        logoPanel.add(logoLabel);

        sidebarPanel.add(logoPanel);
        sidebarPanel.add(Box.createVerticalStrut(20));

        JPanel userCard = new JPanel();
        userCard.setLayout(new BoxLayout(userCard, BoxLayout.Y_AXIS));
        userCard.setBackground(new Color(44, 55, 74));
        userCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 65, 81), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));
        userCard.setMaximumSize(new Dimension(260, 120));
        userCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(user.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel("IT Student");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(new Color(156, 163, 175));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        userCard.add(nameLabel);
        userCard.add(Box.createVerticalStrut(5));
        userCard.add(roleLabel);

        sidebarPanel.add(userCard);
        sidebarPanel.add(Box.createVerticalStrut(30));

        addNavButton("Goal Setup", PANEL_GOAL_SETUP, PRIMARY_COLOR);
        addNavButton("Dashboard", PANEL_DASHBOARD, SUCCESS_COLOR);
        addNavButton("My Study Plan", PANEL_STUDY_PLAN, WARNING_COLOR);
        addNavButton("GitHub Progress", PANEL_GITHUB_PROGRESS, new Color(139, 92, 246));
        addNavButton("View My Plans", PANEL_VIEW_PLANS, new Color(236, 72, 153));
        addNavButton("Profile", PANEL_PROFILE, new Color(59, 130, 246));
        addNavButton("About", PANEL_ABOUT, new Color(139, 92, 246));

        sidebarPanel.add(Box.createVerticalGlue());

        JPanel activePlanPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        activePlanPanel.setBackground(SIDEBAR_BG);
        activePlanPanel.setMaximumSize(new Dimension(260, 40));

        JLabel activeLabel = new JLabel("Active Plan: ");
        activeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        activeLabel.setForeground(Color.WHITE);

        activePlanLabel = new JLabel(getFormattedActivePlanName());
        activePlanLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        activePlanLabel.setForeground(SUCCESS_COLOR);

        activePlanPanel.add(activeLabel);
        activePlanPanel.add(activePlanLabel);

        sidebarPanel.add(activePlanPanel);
        sidebarPanel.add(Box.createVerticalStrut(10));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(DANGER_COLOR);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(240, 45));
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?",
                    "Logout Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });

        sidebarPanel.add(logoutBtn);
        sidebarPanel.add(Box.createVerticalStrut(20));
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel headerLabel = new JLabel("Profile");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(TEXT_PRIMARY);
        headerLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(headerLabel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(25, 0));
        contentPanel.setBackground(Color.WHITE);

        JPanel avatarPanel = new JPanel();
        avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.Y_AXIS));
        avatarPanel.setBackground(CARD_BG);
        avatarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(40, 40, 40, 40)
        ));

        JLabel avatarLabel = new JLabel();
        avatarLabel.setFont(new Font("Segoe UI", Font.PLAIN, 80));
        avatarLabel.setForeground(PRIMARY_COLOR);
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            try {
                URL url = new URL(user.getAvatarUrl());
                java.awt.Image image = ImageIO.read(url);
                if (image != null) {
                    java.awt.Image scaledImage = image.getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
                    ImageIcon icon = new ImageIcon(scaledImage);
                    avatarLabel.setIcon(icon);
                    avatarLabel.setText("");
                } else {
                    avatarLabel.setText("🐙");
                    avatarLabel.setFont(new Font("Segoe UI", Font.PLAIN, 80));
                }
            } catch (Exception e) {
                avatarLabel.setText("🐙");
                avatarLabel.setFont(new Font("Segoe UI", Font.PLAIN, 80));
            }
        } else {
            avatarLabel.setText("🐙");
            avatarLabel.setFont(new Font("Segoe UI", Font.PLAIN, 80));
        }

        JLabel nameLabel = new JLabel(user.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameLabel.setForeground(TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel("💻 IT Student");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleLabel.setForeground(PRIMARY_COLOR);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        avatarPanel.add(avatarLabel);
        avatarPanel.add(Box.createVerticalStrut(15));
        avatarPanel.add(nameLabel);
        avatarPanel.add(Box.createVerticalStrut(5));
        avatarPanel.add(roleLabel);

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(CARD_BG);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(30, 30, 30, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0;
        infoPanel.add(createInfoLabel("📧 Email:"), gbc);
        gbc.gridx = 1;
        JLabel emailLabel = new JLabel(user.getEmail());
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoPanel.add(emailLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        infoPanel.add(createInfoLabel("🔐 Login Provider:"), gbc);
        gbc.gridx = 1;
        JLabel providerLabel = new JLabel("GitHub");
        providerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        providerLabel.setForeground(PRIMARY_COLOR);
        infoPanel.add(providerLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        infoPanel.add(createInfoLabel("🐙 GitHub Username:"), gbc);
        gbc.gridx = 1;
        JLabel githubLabel = new JLabel(user.getGithubUsername() != null ? user.getGithubUsername() : "Not linked");
        githubLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        githubLabel.setForeground(SUCCESS_COLOR);
        infoPanel.add(githubLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        infoPanel.add(createInfoLabel("📅 Member Since:"), gbc);
        gbc.gridx = 1;
        JLabel memberLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        memberLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoPanel.add(memberLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        infoPanel.add(createInfoLabel("📚 Active Plans:"), gbc);
        gbc.gridx = 1;
        List<StudyPlan> plans = planDAO.findByUserIdAndRole(user.getId(), "IT", "GITHUB");
        JLabel plansCountLabel = new JLabel(String.valueOf(plans.size()));
        plansCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        plansCountLabel.setForeground(SUCCESS_COLOR);
        infoPanel.add(plansCountLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        infoPanel.add(createInfoLabel("📊 Total Commits:"), gbc);
        gbc.gridx = 1;
        int totalCommits = 0;
        for (StudyPlan plan : plans) {
            List<StudyTask> tasks = taskDAO.findByGoalId(plan.getId());
            totalCommits += tasks.stream().mapToInt(StudyTask::getActualCommits).sum();
        }
        JLabel commitsLabel = new JLabel(String.valueOf(totalCommits));
        commitsLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        commitsLabel.setForeground(SUCCESS_COLOR);
        infoPanel.add(commitsLabel, gbc);

        contentPanel.add(avatarPanel, BorderLayout.WEST);
        contentPanel.add(infoPanel, BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private JPanel createAboutPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel headerLabel = new JLabel("About Smart Study Planner");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(TEXT_PRIMARY);
        headerLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(headerLabel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(CARD_BG);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(30, 30, 30, 30)
        ));

        JLabel appIcon = new JLabel("📚");
        appIcon.setFont(new Font("Segoe UI", Font.PLAIN, 64));
        appIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(appIcon);
        contentPanel.add(Box.createVerticalStrut(10));

        JLabel appName = new JLabel("Smart Study Planner");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 24));
        appName.setForeground(PRIMARY_COLOR);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(appName);
        contentPanel.add(Box.createVerticalStrut(5));

        JLabel versionLabel = new JLabel("Version 2.0.0 - IT Edition");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(TEXT_SECONDARY);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(versionLabel);
        contentPanel.add(Box.createVerticalStrut(20));

        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(500, 2));
        separator.setForeground(BORDER_COLOR);
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(separator);
        contentPanel.add(Box.createVerticalStrut(20));

        JTextArea descriptionArea = new JTextArea(
                "Smart Study Planner is a comprehensive productivity tool designed to help students manage their studies effectively.\n\n" +
                        "✨ Key Features:\n" +
                        "• Dual Authentication: Login with Google (Normal Student) or GitHub (IT Student)\n" +
                        "• Personalized Study Plans\n" +
                        "• Smart Task Generation\n" +
                        "• Progress Tracking & Streaks\n" +
                        "• GitHub Integration for IT Students\n" +
                        "• AI-Powered Project Planning\n\n" +
                        "© 2026 Smart Study Planner. All rights reserved.\n" +
                        "Made with ❤️ for students worldwide."
        );
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descriptionArea.setEditable(false);
        descriptionArea.setBackground(CARD_BG);
        descriptionArea.setForeground(TEXT_SECONDARY);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        descriptionArea.setBorder(new EmptyBorder(10, 20, 10, 20));

        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(800, 450));
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(scrollPane);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private String getFormattedActivePlanName() {
        if (currentActivePlanId != null) {
            StudyPlan plan = planDAO.findById(currentActivePlanId);
            if (plan != null) {
                String name = plan.getPlanName();
                if (name != null && !name.isEmpty()) {
                    return name;
                }
                String repo = plan.getRepositoryName();
                if (repo != null && !repo.isEmpty()) {
                    return repo;
                }
                return "Plan " + plan.getId();
            }
        }
        return "None";
    }

    private void addNavButton(String text, String cardName, Color accentColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(new Color(209, 213, 219));
        button.setBackground(SIDEBAR_BG);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(240, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(SIDEBAR_HOVER);
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(SIDEBAR_BG);
                button.setForeground(new Color(209, 213, 219));
            }
        });

        button.addActionListener(e -> {
            cardLayout.show(contentPanel, cardName);
            currentVisiblePanel = cardName;
            for (Component comp : sidebarPanel.getComponents()) {
                if (comp instanceof JButton) {
                    ((JButton) comp).setForeground(new Color(209, 213, 219));
                }
            }
            button.setForeground(accentColor);
            refreshCurrentPanel(cardName);
        });

        sidebarPanel.add(button);
        sidebarPanel.add(Box.createVerticalStrut(5));
    }

    private void refreshCurrentPanel(String panelName) {
        switch (panelName) {
            case PANEL_STUDY_PLAN:
                refreshStudyPlanPanel();
                break;
            case PANEL_GITHUB_PROGRESS:
                refreshGitHubProgressPanel();
                break;
            case PANEL_VIEW_PLANS:
                refreshViewPlansPanel();
                break;
            case PANEL_DASHBOARD:
                refreshDashboardPanel();
                break;
        }
    }

    private void refreshStudyPlanPanel() {
        JPanel newPanel = createStudyPlanPanel();
        contentPanel.remove(studyPlanPanel);
        contentPanel.add(newPanel, PANEL_STUDY_PLAN);
        studyPlanPanel = newPanel;
        if (PANEL_STUDY_PLAN.equals(currentVisiblePanel)) {
            cardLayout.show(contentPanel, PANEL_STUDY_PLAN);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void refreshGitHubProgressPanel() {
        JPanel newPanel = createGitHubProgressPanel();
        contentPanel.remove(gitHubProgressPanel);
        contentPanel.add(newPanel, PANEL_GITHUB_PROGRESS);
        gitHubProgressPanel = newPanel;
        if (PANEL_GITHUB_PROGRESS.equals(currentVisiblePanel)) {
            cardLayout.show(contentPanel, PANEL_GITHUB_PROGRESS);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void refreshViewPlansPanel() {
        JPanel newPanel = createViewPlansPanel();
        contentPanel.remove(viewPlansPanel);
        contentPanel.add(newPanel, PANEL_VIEW_PLANS);
        viewPlansPanel = newPanel;
        if (PANEL_VIEW_PLANS.equals(currentVisiblePanel)) {
            cardLayout.show(contentPanel, PANEL_VIEW_PLANS);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void refreshDashboardPanel() {
        JPanel newPanel = new DashboardFrame(user).getMainPanel();
        contentPanel.remove(dashboardPanel);
        contentPanel.add(newPanel, PANEL_DASHBOARD);
        dashboardPanel = newPanel;
        if (PANEL_DASHBOARD.equals(currentVisiblePanel)) {
            cardLayout.show(contentPanel, PANEL_DASHBOARD);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createGoalSetupPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(30, 30, 30, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 20, 10, 20);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel headerLabel = new JLabel("Create Your Smart Study Plan");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(new Color(17, 24, 39));

        JLabel subHeaderLabel = new JLabel("Describe your project idea - AI will design the structure and daily tasks");
        subHeaderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subHeaderLabel.setForeground(new Color(107, 114, 128));

        JSeparator separator = new JSeparator();
        separator.setForeground(PRIMARY_COLOR);
        separator.setPreferredSize(new Dimension(100, 3));

        headerPanel.add(headerLabel, BorderLayout.NORTH);
        headerPanel.add(subHeaderLabel, BorderLayout.CENTER);
        headerPanel.add(separator, BorderLayout.SOUTH);

        panel.add(headerPanel, gbc);
        panel.add(Box.createVerticalStrut(20), gbc);

        JPanel repoCard = new JPanel(new GridBagLayout());
        repoCard.setBackground(new Color(249, 250, 251));
        repoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 0, 5, 0);

        JLabel repoLabel = new JLabel("Select Repository");
        repoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        repoCard.add(repoLabel, c);

        repoComboBox = new JComboBox<>();
        repoComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        repoComboBox.setPreferredSize(new Dimension(400, 45));
        repoComboBox.setMaximumSize(new Dimension(400, 45));
        repoComboBox.setBackground(Color.WHITE);
        repoComboBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        repoComboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        repoCard.add(repoComboBox, c);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(PRIMARY_COLOR);
        repoCard.add(statusLabel, c);

        panel.add(repoCard, gbc);
        panel.add(Box.createVerticalStrut(10), gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton refreshBtn = createStyledButton("Refresh", new Color(107, 114, 128));
        refreshBtn.addActionListener(e -> loadRepositories());

        JButton generateBtn = createStyledButton("Generate Plan", SUCCESS_COLOR);
        generateBtn.addActionListener(e -> openMultiRepoDialog());

        JButton viewPlansBtn = createStyledButton("View My Plans", PRIMARY_COLOR);
        viewPlansBtn.addActionListener(e -> {
            cardLayout.show(contentPanel, PANEL_VIEW_PLANS);
            currentVisiblePanel = PANEL_VIEW_PLANS;
            refreshViewPlansPanel();
        });

        buttonPanel.add(refreshBtn);
        buttonPanel.add(generateBtn);
        buttonPanel.add(viewPlansBtn);

        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private void loadRepositories() {
        statusLabel.setText("Loading your repositories...");
        repoComboBox.removeAllItems();

        SwingWorker<List<Map<String, String>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, String>> doInBackground() {
                String token = user.getAccessToken();
                if (token == null || token.isEmpty()) return new ArrayList<>();
                return gitHubService.getRepositories(token);
            }

            @Override
            protected void done() {
                try {
                    repositories = get();
                    if (repositories == null || repositories.isEmpty()) {
                        statusLabel.setText("No repositories found. Create some on GitHub first!");
                        statusLabel.setForeground(DANGER_COLOR);
                        repoComboBox.addItem("No repositories");
                    } else {
                        for (Map<String, String> repo : repositories) {
                            repoComboBox.addItem(repo.get("name"));
                        }
                        statusLabel.setText("Loaded " + repositories.size() + " repositories");
                        statusLabel.setForeground(SUCCESS_COLOR);
                        if (repositories.size() > 0) repoComboBox.setSelectedIndex(0);
                    }
                } catch (Exception e) {
                    statusLabel.setText("Error: " + e.getMessage());
                    statusLabel.setForeground(DANGER_COLOR);
                }
            }
        };
        worker.execute();
    }

    private void openMultiRepoDialog() {
        if (repositories == null || repositories.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No repositories found. Please refresh and try again.",
                    "No Repositories", JOptionPane.WARNING_MESSAGE);
            return;
        }
        MultiRepoSelectionDialog dialog = new MultiRepoSelectionDialog(this, user, repositories);
        dialog.setVisible(true);

        refreshViewPlansPanel();
        refreshStudyPlanPanel();
        refreshGitHubProgressPanel();
        refreshDashboardPanel();

        currentActivePlanId = user.getActivePlanId();
        updateActivePlanIndicator();
    }

    private void updateActivePlanIndicator() {
        if (activePlanLabel != null) {
            String planName = getFormattedActivePlanName();
            activePlanLabel.setText(planName);
        }
    }

    private JPanel createStudyPlanPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(30, 30, 30, 30)
        ));

        JLabel titleLabel = new JLabel("My Study Plan", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        String[] columns = {"Day", "Date", "Task", "Status", "Commits"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable planTable = new JTable(model) {
            @Override
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int row = rowAtPoint(p);
                int col = columnAtPoint(p);

                if (col == 2 && row >= 0) {
                    Object value = getValueAt(row, col);
                    if (value != null) {
                        String taskText = value.toString();
                        return "<html><body style='width:350px; padding:10px; font-family:Segoe UI;'>" +
                                "<b>📝 Task Details:</b><br><br>" +
                                taskText + "</body></html>";
                    }
                }
                return null;
            }
        };

        planTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        planTable.setRowHeight(50);
        planTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        planTable.getTableHeader().setBackground(new Color(249, 250, 251));
        planTable.setShowGrid(true);
        planTable.setGridColor(BORDER_COLOR);
        planTable.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());

        // Set custom renderer for Task column to truncate and show tooltip
        planTable.getColumnModel().getColumn(2).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JLabel) {
                    JLabel label = (JLabel) c;
                    String text = value != null ? value.toString() : "";
                    // Set tooltip on the renderer
                    label.setToolTipText("<html><body style='width:350px; padding:10px;'>" + text + "</body></html>");
                    // Truncate long text with ellipsis
                    if (text.length() > 50) {
                        label.setText(text.substring(0, 47) + "...");
                    } else {
                        label.setText(text);
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(planTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        loadActivePlanTasks(model);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton refreshBtn = new JButton("🔄 Check Commits");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setForeground(Color.WHITE);  // White text
        refreshBtn.setBackground(PRIMARY_COLOR); // Blue background
        refreshBtn.setBorderPainted(false);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setPreferredSize(new Dimension(140, 35));
        refreshBtn.addActionListener(e -> {
            JDialog loadingDialog = new JDialog(this, "Checking Commits", true);
            loadingDialog.setSize(300, 100);
            loadingDialog.setLocationRelativeTo(this);
            JLabel loadingLabel = new JLabel("Checking GitHub commits...", SwingConstants.CENTER);
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            loadingDialog.setLayout(new BorderLayout());
            loadingDialog.add(loadingLabel, BorderLayout.CENTER);
            loadingDialog.add(progressBar, BorderLayout.SOUTH);

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    commitChecker.checkAndUpdateAllTasks(user);
                    return null;
                }

                @Override
                protected void done() {
                    loadingDialog.dispose();
                    refreshStudyPlanPanel();
                    refreshGitHubProgressPanel();
                    refreshDashboardPanel();
                    JOptionPane.showMessageDialog(ITStudyPlannerFrame.this,
                            "Commit check completed! Task status updated.",
                            "Refresh Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            };
            worker.execute();
            loadingDialog.setVisible(true);
        });

        JButton deletePlanBtn = new JButton("🗑️ Delete Current Plan");
        deletePlanBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deletePlanBtn.setForeground(Color.WHITE);  // White text
        deletePlanBtn.setBackground(DANGER_COLOR); // Red background
        deletePlanBtn.setBorderPainted(false);
        deletePlanBtn.setFocusPainted(false);
        deletePlanBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deletePlanBtn.setPreferredSize(new Dimension(150, 35));
        deletePlanBtn.addActionListener(e -> deleteCurrentPlan());

        buttonPanel.add(refreshBtn);
        buttonPanel.add(deletePlanBtn);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadActivePlanTasks(DefaultTableModel model) {
        try {
            if (currentActivePlanId == null) {
                model.addRow(new Object[]{"-", "-", "No active plan. Select a plan from 'View My Plans'", "-", "-"});
                return;
            }

            StudyPlan plan = planDAO.findById(currentActivePlanId);
            if (plan == null) {
                model.addRow(new Object[]{"-", "-", "Plan not found", "-", "-"});
                return;
            }

            List<StudyTask> tasks = taskDAO.findByGoalId(currentActivePlanId);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");

            System.out.println("Loading " + tasks.size() + " tasks for plan " + currentActivePlanId);

            if (tasks.isEmpty()) {
                model.addRow(new Object[]{"-", "-", "No tasks found for this plan. Generate tasks first.", "-", "-"});
            } else {
                int day = 1;
                for (StudyTask task : tasks) {
                    String status = "COMPLETED".equals(task.getStatus()) ? "✅ Completed" :
                            "MISSED".equals(task.getStatus()) ? "❌ Missed" : "⏳ Pending";

                    // Ensure planned_commits is at least 1 for display
                    int plannedCommits = task.getPlannedCommits();
                    if (plannedCommits == 0) {
                        plannedCommits = 1;
                    }
                    String commitInfo = task.getActualCommits() + "/" + plannedCommits;

                    model.addRow(new Object[]{
                            "Day " + day,
                            task.getTaskDate().format(fmt),
                            task.getDescription(),
                            status,
                            commitInfo
                    });
                    day++;
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading tasks: " + e.getMessage());
            model.addRow(new Object[]{"-", "-", "Error loading tasks: " + e.getMessage(), "-", "-"});
        }
    }

    private JPanel createGitHubProgressPanel() {
        return new GitHubProgressFrame(user).getMainPanel();
    }

    private JPanel createViewPlansPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(30, 30, 30, 30)
        ));

        JLabel titleLabel = new JLabel("My Study Plans", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

        String[] columns = {"Plan ID", "Plan Name", "Repositories", "Type", "Deadline", "Progress", "Status", "Action"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 7;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Integer.class;
                }
                return String.class;
            }
        };

        JTable plansTable = new JTable(model);
        plansTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        plansTable.setRowHeight(60);
        plansTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        plansTable.getTableHeader().setBackground(new Color(249, 250, 251));
        plansTable.setShowGrid(true);
        plansTable.setGridColor(BORDER_COLOR);

        // Simple renderer for Repositories column with tooltip
        plansTable.getColumnModel().getColumn(2).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JLabel) {
                    JLabel label = (JLabel) c;
                    String fullText = value != null ? value.toString() : "";

                    // Set tooltip with full text
                    label.setToolTipText(fullText);

                    // Truncate display
                    if (fullText.length() > 30) {
                        label.setText(fullText.substring(0, 27) + "...");
                    } else {
                        label.setText(fullText);
                    }
                }
                return c;
            }
        });

        plansTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        plansTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(plansTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        loadPlans(model);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadPlans(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            List<StudyPlan> plans = planDAO.findByUserIdAndRole(user.getId(), "IT", "GITHUB");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");

            System.out.println("Found " + plans.size() + " IT plans for user " + user.getId());

            for (StudyPlan plan : plans) {
                List<StudyTask> tasks = taskDAO.findByGoalId(plan.getId());
                int totalTasks = tasks.size();
                int completedTasks = (int) tasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
                int progress = totalTasks > 0 ? (completedTasks * 100 / totalTasks) : 0;

                String status = (currentActivePlanId != null && currentActivePlanId == plan.getId()) ? "ACTIVE" : "Inactive";
                String type = plan.isAiGenerated() ? "✨ AI" : "📝 Manual";
                String action = (currentActivePlanId != null && currentActivePlanId == plan.getId()) ? "Deactivate" : "Activate";

                // Store full repository name (no truncation)
                String repositories = plan.getRepositoryName() != null ? plan.getRepositoryName() : "N/A";
                String planName = plan.getPlanName() != null ? plan.getPlanName() : "Plan " + plan.getId();
                String deadline = plan.getDeadline() != null ? plan.getDeadline().format(fmt) : "Not set";

                model.addRow(new Object[]{
                        plan.getId(),
                        planName,
                        repositories,  // Full repository name
                        type,
                        deadline,
                        progress + "%",
                        status,
                        action
                });
            }
            if (model.getRowCount() == 0) {
                model.addRow(new Object[]{"-", "No plans found", "-", "-", "-", "-", "-", "-"});
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addRow(new Object[]{"-", "Error loading plans", "-", "-", "-", "-", "-", "-"});
        }
    }

    private void deleteCurrentPlan() {
        if (currentActivePlanId == null) {
            JOptionPane.showMessageDialog(this, "No active plan to delete.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the current active plan?\nThis will delete all associated tasks!",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                taskDAO.deleteTasksByGoalId(currentActivePlanId);
                planDAO.deleteById(currentActivePlanId);
                userDAO.updateActivePlan(user.getId(), null);
                user.setActivePlanId(null);
                currentActivePlanId = null;

                JOptionPane.showMessageDialog(this, "Plan deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                refreshStudyPlanPanel();
                refreshViewPlansPanel();
                refreshDashboardPanel();
                refreshGitHubProgressPanel();
                updateActivePlanIndicator();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting plan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            if ("Activate".equals(value)) {
                setBackground(SUCCESS_COLOR);
                setForeground(Color.WHITE);
            } else if ("Deactivate".equals(value)) {
                setBackground(WARNING_COLOR);
                setForeground(Color.WHITE);
            }
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int selectedPlanId;
        private String currentAction;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            selectedPlanId = (int) table.getValueAt(row, 0);
            currentAction = (String) value;
            label = currentAction;
            button.setText(label);

            if ("Activate".equals(currentAction)) {
                button.setBackground(SUCCESS_COLOR);
                button.setForeground(Color.WHITE);
            } else if ("Deactivate".equals(currentAction)) {
                button.setBackground(WARNING_COLOR);
                button.setForeground(Color.WHITE);
            }

            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                if ("Activate".equals(currentAction)) {
                    userDAO.updateActivePlan(user.getId(), selectedPlanId);
                    user.setActivePlanId(selectedPlanId);
                    currentActivePlanId = selectedPlanId;
                    JOptionPane.showMessageDialog(button, "Plan " + selectedPlanId + " activated!");
                } else if ("Deactivate".equals(currentAction)) {
                    userDAO.updateActivePlan(user.getId(), null);
                    user.setActivePlanId(null);
                    currentActivePlanId = null;
                    JOptionPane.showMessageDialog(button, "Plan deactivated.");
                }

                refreshViewPlansPanel();
                refreshStudyPlanPanel();
                refreshGitHubProgressPanel();
                refreshDashboardPanel();
                updateActivePlanIndicator();
            }
            isPushed = false;
            return label;
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            String status = value.toString();
            if (status.contains("✅")) {
                setForeground(SUCCESS_COLOR);
                setFont(getFont().deriveFont(Font.BOLD));
            } else if (status.contains("❌")) {
                setForeground(DANGER_COLOR);
                setFont(getFont().deriveFont(Font.BOLD));
            } else if (status.contains("⏳")) {
                setForeground(WARNING_COLOR);
                setFont(getFont().deriveFont(Font.BOLD));
            }
            setHorizontalAlignment(JLabel.CENTER);
            return c;
        }
    }
}