package ui;

import model.User;
import model.StudyPlan;
import model.StudyTask;
import service.NormalPlanGenerator;
import dao.StudyPlanDAO;
import dao.StudyTaskDAO;
import dao.UserDAO;

import javax.swing.*;
        import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
        import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NormalStudyPlannerFrame extends JFrame {

    private User user;
    private JPanel mainPanel;
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Dashboard components
    private JLabel welcomeLabel;
    private JLabel dateLabel;
    private JLabel totalTasksLabel;
    private JLabel completedTasksLabel;
    private JLabel progressPercentageLabel;
    private JProgressBar dailyProgressBar;
    private DefaultListModel<String> taskListModel;
    private JList<String> taskList;

    // Plan generation components
    private JCheckBox mathCheckbox, scienceCheckbox, englishCheckbox;
    private JCheckBox historyCheckbox, geographyCheckbox, economicsCheckbox;
    private JCheckBox physicsCheckbox, chemistryCheckbox;
    private JSpinner hoursSpinner;
    private JSpinner dateSpinner;
    private JComboBox<String> difficultyCombo;
    private JTable planTable;
    private DefaultTableModel planTableModel;

    // Profile components
    private JLabel profileNameLabel;
    private JLabel profileEmailLabel;
    private JLabel profileRoleLabel;
    private JLabel profileProviderLabel;
    private JLabel profileAvatarLabel;

    private StudyPlanDAO studyPlanDAO;
    private StudyTaskDAO studyTaskDAO;
    private NormalPlanGenerator planGenerator;
    private StudyPlan currentPlan;

    // Colors
    private final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private final Color WARNING_COLOR = new Color(245, 158, 11);
    private final Color DANGER_COLOR = new Color(239, 68, 68);
    private final Color DARK_BG = new Color(17, 24, 39);
    private final Color SIDEBAR_BG = new Color(31, 41, 55);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private final Color BORDER_COLOR = new Color(229, 231, 235);

    public NormalStudyPlannerFrame(User user) {
        this.user = user;
        this.studyPlanDAO = new StudyPlanDAO();
        this.studyTaskDAO = new StudyTaskDAO();
        this.planGenerator = new NormalPlanGenerator();

        fixUserId();
        initUI();
        loadUserData();
        setupEventListeners();
    }

    private void fixUserId() {
        System.out.println("\n=== USER ID FIX ===");
        System.out.println("User email: " + user.getEmail());
        System.out.println("Current user ID: " + user.getId());

        if (user.getId() == 0) {
            System.out.println("⚠️ User ID is 0, fetching from database...");
            UserDAO userDAO = new UserDAO();
            User dbUser = userDAO.findByEmail(user.getEmail());
            if (dbUser != null) {
                user.setId(dbUser.getId());
                System.out.println("✅ Fixed user ID to: " + user.getId());
            } else {
                System.err.println("❌ User not found in database!");
            }
        }
    }

    private void initUI() {
        setTitle("Smart Study Planner - Normal Student");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1100, 600));

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(249, 250, 251));

        createSidebar();

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        contentPanel.add(createDashboardPanel(), "DASHBOARD");
        contentPanel.add(createStudyPlanPanel(), "STUDY_PLAN");
        contentPanel.add(createProfilePanel(), "PROFILE");
        contentPanel.add(createAboutPanel(), "ABOUT");

        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
        cardLayout.show(contentPanel, "DASHBOARD");
    }

    private void createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(SIDEBAR_BG);
        sidebarPanel.setPreferredSize(new Dimension(280, getHeight()));
        sidebarPanel.setBorder(new EmptyBorder(30, 20, 30, 20));

        JLabel logoLabel = new JLabel("📚 Smart Planner");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(logoLabel);
        sidebarPanel.add(Box.createVerticalStrut(30));

        // User Profile Card
        JPanel userCard = new JPanel();
        userCard.setLayout(new BoxLayout(userCard, BoxLayout.Y_AXIS));
        userCard.setBackground(new Color(55, 65, 81));
        userCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(75, 85, 99), 1, true),
                new EmptyBorder(20, 15, 20, 15)
        ));
        userCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        userCard.setMaximumSize(new Dimension(240, 120));

        JLabel nameLabel = new JLabel(user.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel emailLabel = new JLabel(user.getEmail());
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailLabel.setForeground(new Color(209, 213, 219));
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleBadge = new JLabel("🎓 Normal Student");
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        roleBadge.setForeground(PRIMARY_COLOR);
        roleBadge.setBackground(new Color(224, 231, 255));
        roleBadge.setOpaque(true);
        roleBadge.setBorder(new EmptyBorder(5, 10, 5, 10));
        roleBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        userCard.add(nameLabel);
        userCard.add(Box.createVerticalStrut(5));
        userCard.add(emailLabel);
        userCard.add(Box.createVerticalStrut(10));
        userCard.add(roleBadge);

        sidebarPanel.add(userCard);
        sidebarPanel.add(Box.createVerticalStrut(30));

        // Navigation Header
        JLabel navHeader = new JLabel("NAVIGATION");
        navHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        navHeader.setForeground(new Color(156, 163, 175));
        navHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(navHeader);
        sidebarPanel.add(Box.createVerticalStrut(15));

        // Navigation Buttons
        addNavButton("📊 Dashboard", "DASHBOARD", true);
        addNavButton("📋 Study Plan", "STUDY_PLAN", false);
        addNavButton("👤 Profile", "PROFILE", false);
        addNavButton("ℹ️ About App", "ABOUT", false);

        sidebarPanel.add(Box.createVerticalGlue());

        // Logout Button
        JButton logoutBtn = new JButton("🚪 Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(DANGER_COLOR);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(240, 45));
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> logout());

        sidebarPanel.add(logoutBtn);
    }

    private void addNavButton(String text, String cardName, boolean isActive) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(isActive ? PRIMARY_COLOR : new Color(209, 213, 219));
        button.setBackground(isActive ? new Color(55, 65, 81) : SIDEBAR_BG);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(240, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 15, 10, 15));

        button.addActionListener(e -> {
            cardLayout.show(contentPanel, cardName);
            // Update button styles
            for (Component comp : sidebarPanel.getComponents()) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    btn.setForeground(new Color(209, 213, 219));
                    btn.setBackground(SIDEBAR_BG);
                }
            }
            button.setForeground(PRIMARY_COLOR);
            button.setBackground(new Color(55, 65, 81));

            if (cardName.equals("DASHBOARD")) refreshDashboard();
            else if (cardName.equals("STUDY_PLAN")) refreshStudyPlan();
            else if (cardName.equals("PROFILE")) refreshProfile();
        });

        sidebarPanel.add(button);
        sidebarPanel.add(Box.createVerticalStrut(8));
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 25));
        panel.setBackground(Color.WHITE);

        // Welcome Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel headerTitle = new JLabel("Dashboard");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerTitle.setForeground(TEXT_PRIMARY);

        welcomeLabel = new JLabel("Welcome back, " + user.getName() + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        welcomeLabel.setForeground(TEXT_SECONDARY);

        dateLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(TEXT_SECONDARY);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(headerTitle, BorderLayout.NORTH);
        titlePanel.add(welcomeLabel, BorderLayout.SOUTH);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Stats Cards Grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 3, 20, 0));
        statsGrid.setBackground(Color.WHITE);
        statsGrid.setBorder(new EmptyBorder(10, 0, 20, 0));

        statsGrid.add(createStatsCard("📋", "Total Tasks", "0"));
        statsGrid.add(createStatsCard("✅", "Completed", "0"));
        statsGrid.add(createStatsCard("📊", "Progress", "0%"));

        panel.add(statsGrid, BorderLayout.CENTER);

        // Progress and Tasks Section
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomPanel.setBackground(Color.WHITE);

        // Daily Progress Card
        JPanel progressCard = new JPanel(new BorderLayout());
        progressCard.setBackground(CARD_BG);
        progressCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel progressTitle = new JLabel("Daily Progress");
        progressTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        progressTitle.setForeground(TEXT_PRIMARY);

        progressPercentageLabel = new JLabel("0%");
        progressPercentageLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        progressPercentageLabel.setForeground(PRIMARY_COLOR);

        dailyProgressBar = new JProgressBar(0, 100);
        dailyProgressBar.setStringPainted(true);
        dailyProgressBar.setForeground(PRIMARY_COLOR);
        dailyProgressBar.setBackground(new Color(224, 231, 255));
        dailyProgressBar.setBorderPainted(false);
        dailyProgressBar.setPreferredSize(new Dimension(200, 20));

        JPanel progressContent = new JPanel(new BorderLayout(0, 15));
        progressContent.setBackground(CARD_BG);
        progressContent.add(progressPercentageLabel, BorderLayout.NORTH);
        progressContent.add(dailyProgressBar, BorderLayout.CENTER);

        progressCard.add(progressTitle, BorderLayout.NORTH);
        progressCard.add(progressContent, BorderLayout.CENTER);

        // Today's Tasks Card
        JPanel tasksCard = new JPanel(new BorderLayout());
        tasksCard.setBackground(CARD_BG);
        tasksCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JPanel tasksHeader = new JPanel(new BorderLayout());
        tasksHeader.setBackground(CARD_BG);

        JLabel tasksTitle = new JLabel("Today's Tasks");
        tasksTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tasksTitle.setForeground(TEXT_PRIMARY);

        totalTasksLabel = new JLabel("0 tasks");
        totalTasksLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        totalTasksLabel.setForeground(TEXT_SECONDARY);

        tasksHeader.add(tasksTitle, BorderLayout.WEST);
        tasksHeader.add(totalTasksLabel, BorderLayout.EAST);

        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        taskList.setBackground(CARD_BG);
        taskList.setSelectionBackground(new Color(224, 231, 255));
        taskList.setBorder(new EmptyBorder(10, 0, 0, 0));
        taskList.setCellRenderer(new TaskListCellRenderer());

        JScrollPane taskScroll = new JScrollPane(taskList);
        taskScroll.setBorder(null);
        taskScroll.setPreferredSize(new Dimension(300, 200));

        tasksCard.add(tasksHeader, BorderLayout.NORTH);
        tasksCard.add(taskScroll, BorderLayout.CENTER);

        bottomPanel.add(progressCard);
        bottomPanel.add(tasksCard);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatsCard(String icon, String label, String value) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 36));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(CARD_BG);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(PRIMARY_COLOR);

        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        labelLabel.setForeground(TEXT_SECONDARY);

        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(labelLabel);

        if (label.equals("Total Tasks")) {
            totalTasksLabel = valueLabel;
        } else if (label.equals("Completed")) {
            completedTasksLabel = valueLabel;
        } else if (label.equals("Progress")) {
            progressPercentageLabel = valueLabel;
        }

        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createStudyPlanPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 25));
        panel.setBackground(Color.WHITE);

        JLabel headerLabel = new JLabel("Study Plan Generator");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(TEXT_PRIMARY);
        panel.add(headerLabel, BorderLayout.NORTH);

        // MAIN CONTENT PANEL - Yeh scrollable hoga
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(Color.WHITE);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        // Top section - Subject selection and config
        JPanel topSection = new JPanel(new GridLayout(1, 2, 25, 0));
        topSection.setBackground(Color.WHITE);
        topSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

        // Left panel - subjects
        JPanel leftPanel = createSubjectPanel();
        // Right panel - config
        JPanel rightPanel = createConfigPanel();

        topSection.add(leftPanel);
        topSection.add(rightPanel);

        // Bottom section - Plan table
        JPanel tablePanel = createPlanTablePanel();

        contentPanel.add(topSection);
        contentPanel.add(Box.createVerticalStrut(25));
        contentPanel.add(tablePanel);
        contentPanel.add(Box.createVerticalStrut(25));

        // Add to scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        contentWrapper.add(scrollPane, BorderLayout.CENTER);
        panel.add(contentWrapper, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSubjectPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Select Subjects");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel subjectsPanel = new JPanel(new GridLayout(4, 2, 15, 15));
        subjectsPanel.setBackground(CARD_BG);

        mathCheckbox = createStyledCheckbox("Mathematics", true);
        scienceCheckbox = createStyledCheckbox("Science", true);
        englishCheckbox = createStyledCheckbox("English", true);
        historyCheckbox = createStyledCheckbox("History", false);
        geographyCheckbox = createStyledCheckbox("Geography", false);
        economicsCheckbox = createStyledCheckbox("Economics", false);
        physicsCheckbox = createStyledCheckbox("Physics", false);
        chemistryCheckbox = createStyledCheckbox("Chemistry", false);

        subjectsPanel.add(mathCheckbox);
        subjectsPanel.add(scienceCheckbox);
        subjectsPanel.add(englishCheckbox);
        subjectsPanel.add(historyCheckbox);
        subjectsPanel.add(geographyCheckbox);
        subjectsPanel.add(economicsCheckbox);
        subjectsPanel.add(physicsCheckbox);
        subjectsPanel.add(chemistryCheckbox);

        panel.add(title, BorderLayout.NORTH);
        panel.add(subjectsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Configuration");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Hours
        gbc.gridx = 0; gbc.gridy = 0;
        configPanel.add(createStyledLabel("Hours per Day:"), gbc);
        gbc.gridx = 1;
        hoursSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 12, 1));
        hoursSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hoursSpinner.setPreferredSize(new Dimension(120, 35));
        configPanel.add(hoursSpinner, gbc);

        // Date
        gbc.gridx = 0; gbc.gridy = 1;
        configPanel.add(createStyledLabel("Exam Date:"), gbc);
        gbc.gridx = 1;
        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy"));
        dateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateSpinner.setPreferredSize(new Dimension(150, 35));
        configPanel.add(dateSpinner, gbc);

        // Difficulty
        gbc.gridx = 0; gbc.gridy = 2;
        configPanel.add(createStyledLabel("Difficulty:"), gbc);
        gbc.gridx = 1;
        difficultyCombo = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        difficultyCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        difficultyCombo.setPreferredSize(new Dimension(150, 35));
        configPanel.add(difficultyCombo, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JButton generateBtn = createStyledButton("Generate Study Plan", PRIMARY_COLOR);
        generateBtn.addActionListener(e -> generateStudyPlan());
        configPanel.add(generateBtn, gbc);

        gbc.gridy = 4;
        JButton viewBtn = createStyledButton("View My Plan", SUCCESS_COLOR);
        viewBtn.addActionListener(e -> showPlanTable());
        configPanel.add(viewBtn, gbc);

        panel.add(title, BorderLayout.NORTH);
        panel.add(configPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPlanTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Your Study Plan");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 15, 0));

        String[] columns = {"Day", "Subject", "Time", "Status"};
        planTableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        planTable = new JTable(planTableModel);
        planTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        planTable.setRowHeight(35);
        planTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        planTable.getTableHeader().setBackground(new Color(249, 250, 251));
        planTable.setShowGrid(false);
        planTable.setIntercellSpacing(new Dimension(0, 0));

        // Center align columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < planTable.getColumnCount(); i++) {
            planTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(planTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.setPreferredSize(new Dimension(800, 200));

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
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

        // Avatar Section
        JPanel avatarPanel = new JPanel();
        avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.Y_AXIS));
        avatarPanel.setBackground(CARD_BG);
        avatarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(40, 40, 40, 40)
        ));

        profileAvatarLabel = new JLabel("👤");
        profileAvatarLabel.setFont(new Font("Segoe UI", Font.PLAIN, 80));
        profileAvatarLabel.setForeground(PRIMARY_COLOR);
        profileAvatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        profileNameLabel = new JLabel(user.getName());
        profileNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        profileNameLabel.setForeground(TEXT_PRIMARY);
        profileNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        profileRoleLabel = new JLabel("Normal Student");
        profileRoleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        profileRoleLabel.setForeground(PRIMARY_COLOR);
        profileRoleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        avatarPanel.add(profileAvatarLabel);
        avatarPanel.add(Box.createVerticalStrut(15));
        avatarPanel.add(profileNameLabel);
        avatarPanel.add(Box.createVerticalStrut(5));
        avatarPanel.add(profileRoleLabel);

        // Info Section
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(CARD_BG);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(30, 30, 30, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Email
        gbc.gridx = 0; gbc.gridy = 0;
        infoPanel.add(createStyledLabel("Email:"), gbc);
        gbc.gridx = 1;
        profileEmailLabel = new JLabel(user.getEmail());
        profileEmailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoPanel.add(profileEmailLabel, gbc);

        // Provider
        gbc.gridx = 0; gbc.gridy = 1;
        infoPanel.add(createStyledLabel("Login Provider:"), gbc);
        gbc.gridx = 1;
        profileProviderLabel = new JLabel(user.getOauthProvider());
        profileProviderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        profileProviderLabel.setForeground(PRIMARY_COLOR);
        infoPanel.add(profileProviderLabel, gbc);

        // Member Since
        gbc.gridx = 0; gbc.gridy = 2;
        infoPanel.add(createStyledLabel("Member Since:"), gbc);
        gbc.gridx = 1;
        JLabel memberLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        memberLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoPanel.add(memberLabel, gbc);

        contentPanel.add(avatarPanel, BorderLayout.WEST);
        contentPanel.add(infoPanel, BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAboutPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel headerLabel = new JLabel("About");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(TEXT_PRIMARY);
        headerLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(headerLabel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(CARD_BG);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(40, 40, 40, 40)
        ));

        JLabel versionLabel = new JLabel("Smart Study Planner v1.0.0");
        versionLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        versionLabel.setForeground(PRIMARY_COLOR);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea descriptionArea = new JTextArea(
                "A goal-based productivity system for students.\n\n" +
                        "Features:\n" +
                        "• Google Authentication\n" +
                        "• Personalized Study Plans\n" +
                        "• Daily Task Tracking\n" +
                        "• Progress Monitoring\n" +
                        "• Modern, Intuitive UI\n\n" +
                        "© 2026 Smart Study Planner"
        );
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descriptionArea.setEditable(false);
        descriptionArea.setBackground(CARD_BG);
        descriptionArea.setForeground(TEXT_SECONDARY);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        descriptionArea.setBorder(new EmptyBorder(20, 0, 0, 0));

        contentPanel.add(versionLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(descriptionArea);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JCheckBox createStyledCheckbox(String text, boolean selected) {
        JCheckBox checkBox = new JCheckBox(text, selected);
        checkBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        checkBox.setBackground(CARD_BG);
        checkBox.setForeground(TEXT_PRIMARY);
        return checkBox;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(200, 40));
        return button;
    }

    private void loadUserData() {
        List<StudyPlan> plans = studyPlanDAO.findByUserId(user.getId());
        if (!plans.isEmpty()) {
            currentPlan = plans.get(0);
            refreshDashboard();
        }
    }

    private void refreshDashboard() {
        List<StudyTask> todayTasks = studyTaskDAO.findTodayTasks(user.getId());
        int totalToday = todayTasks.size();
        int completedToday = (int) todayTasks.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus())).count();

        totalTasksLabel.setText(String.valueOf(totalToday));
        completedTasksLabel.setText(String.valueOf(completedToday));

        int progress = totalToday > 0 ? (completedToday * 100 / totalToday) : 0;
        dailyProgressBar.setValue(progress);
        progressPercentageLabel.setText(progress + "%");
        dailyProgressBar.setString(progress + "% (" + completedToday + "/" + totalToday + ")");

        taskListModel.clear();
        for (StudyTask task : todayTasks) {
            String status = "COMPLETED".equals(task.getStatus()) ? "✅ " : "⬜ ";
            taskListModel.addElement(status + task.getDescription());
        }
        if (todayTasks.isEmpty()) {
            taskListModel.addElement("🎉 No tasks for today! Enjoy your day.");
        }
    }

    private void refreshStudyPlan() {
        if (currentPlan != null) showPlanTable();
    }
    private void refreshProfile() {
        System.out.println("Refreshing profile...");
        if (profileNameLabel != null) {
            profileNameLabel.setText(user.getName());
        }
        if (profileEmailLabel != null) {
            profileEmailLabel.setText(user.getEmail());
        }
        if (profileProviderLabel != null) {
            profileProviderLabel.setText(user.getOauthProvider());
        }
        if (profileRoleLabel != null) {
            profileRoleLabel.setText("Normal Student");
        }
    }

    private void generateStudyPlan() {
        System.out.println("\n=== GENERATE PLAN STARTED ===");
        System.out.println("User ID: " + user.getId());
        System.out.println("User Email: " + user.getEmail());

        List<String> subjects = new ArrayList<>();
        if (mathCheckbox.isSelected()) subjects.add("Mathematics");
        if (scienceCheckbox.isSelected()) subjects.add("Science");
        if (englishCheckbox.isSelected()) subjects.add("English");
        if (historyCheckbox.isSelected()) subjects.add("History");
        if (geographyCheckbox.isSelected()) subjects.add("Geography");
        if (economicsCheckbox.isSelected()) subjects.add("Economics");
        if (physicsCheckbox.isSelected()) subjects.add("Physics");
        if (chemistryCheckbox.isSelected()) subjects.add("Chemistry");

        System.out.println("Subjects selected: " + subjects);

        if (subjects.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one subject!",
                    "No Subjects Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int dailyHours = (Integer) hoursSpinner.getValue();
            System.out.println("Daily hours: " + dailyHours);

            java.util.Date selected = (java.util.Date) dateSpinner.getValue();
            LocalDate examDate = selected.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
            System.out.println("Exam date: " + examDate);

            String difficulty = (String) difficultyCombo.getSelectedItem();
            System.out.println("Difficulty: " + difficulty);

            if (examDate.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this,
                        "Exam date must be in the future!",
                        "Invalid Date",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Double-check user ID
            if (user.getId() == 0) {
                System.err.println("❌ User ID is 0! Fixing...");
                UserDAO userDAO = new UserDAO();
                User dbUser = userDAO.findByEmail(user.getEmail());
                if (dbUser != null) {
                    user.setId(dbUser.getId());
                    System.out.println("✅ Fixed user ID to: " + user.getId());
                }
            }

            System.out.println("Calling planGenerator.generatePlan()...");
            currentPlan = planGenerator.generatePlan(
                    user,
                    String.join(",", subjects),
                    examDate,
                    dailyHours,
                    difficulty.toUpperCase()
            );

            System.out.println("Plan generated: " + (currentPlan != null ? "SUCCESS" : "FAILED"));

            if (currentPlan != null) {
                System.out.println("Plan ID: " + currentPlan.getId());
                List<StudyTask> tasks = studyTaskDAO.findByGoalId(currentPlan.getId());
                System.out.println("Tasks in DB: " + tasks.size());

                JOptionPane.showMessageDialog(this,
                        "✅ Study plan generated successfully!\n" +
                                subjects.size() + " subjects scheduled until " +
                                examDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                showPlanTable();
                refreshDashboard();

                // Switch to dashboard to show tasks
                cardLayout.show(contentPanel, "DASHBOARD");
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Failed to generate plan. Please check database connection.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            System.err.println("❌ Exception: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Exception",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showPlanTable() {
        if (currentPlan == null) {
            JOptionPane.showMessageDialog(this,
                    "No study plan found. Please generate one first.",
                    "No Plan",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        planTableModel.setRowCount(0);
        List<StudyTask> tasks = studyTaskDAO.findByGoalId(currentPlan.getId());
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd");

        System.out.println("Showing " + tasks.size() + " tasks in table");

        for (StudyTask task : tasks) {
            String dayOfWeek = task.getTaskDate().format(dayFormatter);
            String dateStr = task.getTaskDate().format(dateFormatter);
            String subject = task.getDescription().replace("Study ", "");

            String status;
            if ("COMPLETED".equals(task.getStatus())) {
                status = "✅ Completed";
            } else if ("MISSED".equals(task.getStatus())) {
                status = "❌ Missed";
            } else {
                status = "⏳ Pending";
            }

            planTableModel.addRow(new Object[]{
                    dayOfWeek + " (" + dateStr + ")",
                    subject,
                    "2.0h",
                    status
            });
        }

        // Auto-adjust column widths
        for (int i = 0; i < planTable.getColumnCount(); i++) {
            planTable.getColumnModel().getColumn(i).setPreferredWidth(
                    i == 0 ? 200 : (i == 1 ? 150 : (i == 2 ? 80 : 120))
            );
        }
    }

    private void setupEventListeners() {
        // Double-click on task to toggle completion
        taskList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = taskList.locationToIndex(evt.getPoint());
                    if (index >= 0) {
                        String taskStr = taskListModel.get(index);
                        if (!taskStr.startsWith("🎉")) {
                            List<StudyTask> tasks = studyTaskDAO.findTodayTasks(user.getId());
                            if (index < tasks.size()) {
                                StudyTask task = tasks.get(index);
                                String newStatus = "COMPLETED".equals(task.getStatus()) ? "PENDING" : "COMPLETED";
                                studyTaskDAO.updateStatus(task.getId(), newStatus);
                                refreshDashboard();
                            }
                        }
                    }
                }
            }
        });

        // Add window listener to cleanup on close
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        NormalStudyPlannerFrame.this,
                        "Are you sure you want to exit?",
                        "Exit Confirmation",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    dispose();
                }
            }
        });
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Logout Confirmation",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                new LoginFrame().setVisible(true);
            });
        }
    }

    // Custom cell renderer for task list with better styling
    class TaskListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            String text = value.toString();
            if (text.startsWith("✅")) {
                setForeground(new Color(34, 197, 94)); // Success green
                setFont(getFont().deriveFont(Font.BOLD));
            } else if (text.startsWith("🎉")) {
                setForeground(new Color(79, 70, 229)); // Primary color
                setFont(getFont().deriveFont(Font.ITALIC));
            } else if (text.startsWith("⬜")) {
                setForeground(new Color(75, 85, 99)); // Dark gray
                setFont(getFont().deriveFont(Font.PLAIN));
            }

            // Add padding
            setBorder(new EmptyBorder(8, 10, 8, 10));

            return c;
        }
    }

    // Custom table cell renderer for status column
    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String status = value.toString();
            if (status.contains("✅")) {
                setForeground(new Color(34, 197, 94));
                setFont(getFont().deriveFont(Font.BOLD));
            } else if (status.contains("❌")) {
                setForeground(new Color(239, 68, 68));
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                setForeground(new Color(245, 158, 11));
                setFont(getFont().deriveFont(Font.PLAIN));
            }

            setHorizontalAlignment(JLabel.CENTER);
            return c;
        }
    }
}