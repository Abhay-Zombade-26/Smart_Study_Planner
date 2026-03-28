package ui;

import model.User;
import model.StudyPlan;
import model.StudyTask;
import service.NormalPlanGenerator;
import dao.StudyPlanDAO;
import dao.StudyTaskDAO;
import dao.UserDAO;
import dao.TopicDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
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

    // Plan generation components (subjects)
    private JCheckBox marathiCheckbox, hindiCheckbox, englishCheckbox, physicsCheckbox;
    private JCheckBox chemistryCheckbox, biologyCheckbox, historyCheckbox, geographyCheckbox;
    private JTextField customSubjectField;
    private JSpinner hoursSpinner;
    private JSpinner dateSpinner;
    private JComboBox<String> difficultyCombo;

    // Today's Tasks components (in Study Plan panel)
    private DefaultListModel<String> taskListModel;
    private JList<String> taskList;
    private JPanel tasksCard;

    // Profile components
    private JLabel profileNameLabel;
    private JLabel profileEmailLabel;
    private JLabel profileRoleLabel;
    private JLabel profileProviderLabel;
    private JLabel profileAvatarLabel;

    private StudyPlanDAO studyPlanDAO;
    private StudyTaskDAO studyTaskDAO;
    private TopicDAO topicDAO;
    private NormalPlanGenerator planGenerator;
    private StudyPlan currentPlan;

    // Colors
    private final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private final Color WARNING_COLOR = new Color(245, 158, 11);
    private final Color DANGER_COLOR = new Color(239, 68, 68);
    private final Color MISSED_COLOR = new Color(220, 38, 38);
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
        this.topicDAO = new TopicDAO();
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

    public void refreshDashboardFromPlans() {
        Component comp = contentPanel.getComponent(0);
        if (comp instanceof DashboardFrame) {
            ((DashboardFrame) comp).refreshDashboard();
        }
        refreshStudyPlan();
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

        contentPanel.add(new DashboardFrame(user), "DASHBOARD");
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

            if (cardName.equals("DASHBOARD")) {
                Component comp = contentPanel.getComponent(0);
                if (comp instanceof DashboardFrame) {
                    ((DashboardFrame) comp).refreshDashboard();
                }
            } else if (cardName.equals("STUDY_PLAN")) {
                refreshStudyPlan();
            } else if (cardName.equals("PROFILE")) {
                refreshProfile();
            }
        });

        sidebarPanel.add(button);
        sidebarPanel.add(Box.createVerticalStrut(8));
    }

    private void checkAndHandleMissedTasks() {
        Integer activePlanId = user.getActivePlanId();
        if (activePlanId == null) return;

        List<StudyTask> missedTasks = studyTaskDAO.findMissedTasks(activePlanId);

        if (!missedTasks.isEmpty()) {
            int response = JOptionPane.showConfirmDialog(this,
                    "❌ You have " + missedTasks.size() + " missed task(s) from previous days.\n" +
                            "Would you like to open the plan manager to reschedule them?",
                    "Missed Tasks Detected",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (response == JOptionPane.YES_OPTION) {
                StudyPlan plan = studyPlanDAO.findById(activePlanId);
                if (plan != null) {
                    PlanManagementFrame managementFrame = new PlanManagementFrame(this, user, plan);
                    managementFrame.setVisible(true);
                    managementFrame.toFront();
                }
            }
        }
    }

    private JPanel createStudyPlanPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 25));
        panel.setBackground(Color.WHITE);

        JLabel headerLabel = new JLabel("Study Plan Generator");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(TEXT_PRIMARY);
        panel.add(headerLabel, BorderLayout.NORTH);

        // MAIN CONTENT PANEL - Scrollable
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(Color.WHITE);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        // Top section - Subject selection and config
        JPanel topSection = new JPanel(new GridLayout(1, 2, 25, 0));
        topSection.setBackground(Color.WHITE);
        topSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));

        // Left panel - Subject selection
        JPanel leftPanel = createSubjectSelectionPanel();
        // Right panel - config
        JPanel rightPanel = createConfigPanel();

        topSection.add(leftPanel);
        topSection.add(rightPanel);

        contentPanel.add(topSection);
        contentPanel.add(Box.createVerticalStrut(25));

        // TODAY'S TASKS SECTION
        tasksCard = createTodayTasksPanel();
        contentPanel.add(tasksCard);
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

    private JPanel createTodayTasksPanel() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BG);

        JLabel title = new JLabel("Today's Tasks");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        taskList.setBackground(CARD_BG);
        taskList.setSelectionBackground(new Color(224, 231, 255));
        taskList.setBorder(new EmptyBorder(10, 0, 0, 0));
        taskList.setCellRenderer(new TaskListCellRenderer());

        taskList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = taskList.locationToIndex(evt.getPoint());
                    if (index >= 0) {
                        String taskStr = taskListModel.get(index);
                        if (!taskStr.startsWith("🎉") && !taskStr.startsWith("🎯") && !taskStr.startsWith("❌") && !taskStr.contains("---")) {
                            Integer activePlanId = user.getActivePlanId();
                            if (activePlanId != null) {
                                List<StudyTask> tasks = studyTaskDAO.findTodayTasksByPlan(activePlanId);
                                if (index < tasks.size()) {
                                    StudyTask task = tasks.get(index);
                                    String newStatus = "COMPLETED".equals(task.getStatus()) ? "PENDING" : "COMPLETED";
                                    studyTaskDAO.updateStatus(task.getId(), newStatus);
                                    refreshStudyPlan();
                                    refreshDashboardFromPlans();
                                }
                            }
                        }
                    }
                }
            }
        });

        JScrollPane taskScroll = new JScrollPane(taskList);
        taskScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        taskScroll.setPreferredSize(new Dimension(300, 200));

        card.add(header, BorderLayout.NORTH);
        card.add(taskScroll, BorderLayout.CENTER);

        return card;
    }

    private JPanel createSubjectSelectionPanel() {
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

        JPanel checkPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        checkPanel.setBackground(CARD_BG);

        marathiCheckbox = new JCheckBox("Marathi");
        hindiCheckbox = new JCheckBox("Hindi");
        englishCheckbox = new JCheckBox("English");
        physicsCheckbox = new JCheckBox("Physics");
        chemistryCheckbox = new JCheckBox("Chemistry");
        biologyCheckbox = new JCheckBox("Biology");
        historyCheckbox = new JCheckBox("History");
        geographyCheckbox = new JCheckBox("Geography");

        checkPanel.add(marathiCheckbox);
        checkPanel.add(hindiCheckbox);
        checkPanel.add(englishCheckbox);
        checkPanel.add(physicsCheckbox);
        checkPanel.add(chemistryCheckbox);
        checkPanel.add(biologyCheckbox);
        checkPanel.add(historyCheckbox);
        checkPanel.add(geographyCheckbox);

        JPanel customPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        customPanel.setBackground(CARD_BG);
        customPanel.add(new JLabel("Other:"));
        customSubjectField = new JTextField(20);
        customPanel.add(customSubjectField);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(CARD_BG);
        centerPanel.add(checkPanel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(customPanel);

        panel.add(title, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

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

        gbc.gridx = 0; gbc.gridy = 0;
        configPanel.add(createStyledLabel("Hours per Day:"), gbc);
        gbc.gridx = 1;
        hoursSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 12, 1));
        hoursSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hoursSpinner.setPreferredSize(new Dimension(120, 35));
        configPanel.add(hoursSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        configPanel.add(createStyledLabel("Exam Date:"), gbc);
        gbc.gridx = 1;
        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy"));
        dateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateSpinner.setPreferredSize(new Dimension(150, 35));
        configPanel.add(dateSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        configPanel.add(createStyledLabel("Difficulty:"), gbc);
        gbc.gridx = 1;
        difficultyCombo = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        difficultyCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        difficultyCombo.setPreferredSize(new Dimension(150, 35));
        configPanel.add(difficultyCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JButton createPlanBtn = createStyledButton("Create Plan", PRIMARY_COLOR);
        createPlanBtn.addActionListener(e -> createPlan());
        configPanel.add(createPlanBtn, gbc);

        gbc.gridy = 4;
        JButton viewPlansBtn = createStyledButton("View My Plans", SUCCESS_COLOR);
        viewPlansBtn.addActionListener(e -> openPlanList());
        configPanel.add(viewPlansBtn, gbc);

        panel.add(title, BorderLayout.NORTH);
        panel.add(configPanel, BorderLayout.CENTER);

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

        JPanel avatarPanel = new JPanel();
        avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.Y_AXIS));
        avatarPanel.setBackground(CARD_BG);
        avatarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(40, 40, 40, 40)
        ));

        String initial = user.getName() != null && !user.getName().isEmpty()
                ? user.getName().substring(0, 1).toUpperCase()
                : "👤";

        profileAvatarLabel = new JLabel(initial);
        profileAvatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 80));
        profileAvatarLabel.setForeground(PRIMARY_COLOR);
        profileAvatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        profileNameLabel = new JLabel(user.getName());
        profileNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        profileNameLabel.setForeground(TEXT_PRIMARY);
        profileNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        profileRoleLabel = new JLabel("🎓 Normal Student");
        profileRoleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        profileRoleLabel.setForeground(PRIMARY_COLOR);
        profileRoleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        avatarPanel.add(profileAvatarLabel);
        avatarPanel.add(Box.createVerticalStrut(15));
        avatarPanel.add(profileNameLabel);
        avatarPanel.add(Box.createVerticalStrut(5));
        avatarPanel.add(profileRoleLabel);

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
        profileEmailLabel = new JLabel(user.getEmail());
        profileEmailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoPanel.add(profileEmailLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        infoPanel.add(createInfoLabel("🔐 Login Provider:"), gbc);
        gbc.gridx = 1;
        profileProviderLabel = new JLabel("Google");
        profileProviderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        profileProviderLabel.setForeground(PRIMARY_COLOR);
        infoPanel.add(profileProviderLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        infoPanel.add(createInfoLabel("📅 Member Since:"), gbc);
        gbc.gridx = 1;
        JLabel memberLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        memberLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoPanel.add(memberLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        infoPanel.add(createInfoLabel("📚 Active Plans:"), gbc);
        gbc.gridx = 1;
        List<StudyPlan> plans = studyPlanDAO.findByUserIdAndRole(user.getId(), "NORMAL", "GOOGLE");
        JLabel plansCountLabel = new JLabel(String.valueOf(plans.size()));
        plansCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        plansCountLabel.setForeground(SUCCESS_COLOR);
        infoPanel.add(plansCountLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        infoPanel.add(createInfoLabel("✅ Tasks Completed:"), gbc);
        gbc.gridx = 1;
        int totalCompleted = 0;
        for (StudyPlan plan : plans) {
            List<StudyTask> tasks = studyTaskDAO.findByGoalId(plan.getId());
            totalCompleted += (int) tasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
        }
        JLabel completedLabel = new JLabel(String.valueOf(totalCompleted));
        completedLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        completedLabel.setForeground(SUCCESS_COLOR);
        infoPanel.add(completedLabel, gbc);

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

        JLabel versionLabel = new JLabel("Version 2.0.0");
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
                "Smart Study Planner is a comprehensive productivity tool designed to help students manage their studies effectively. " +
                        "With intelligent planning, progress tracking, and personalized study schedules, you can achieve your academic goals with ease.\n\n" +

                        "✨ Key Features:\n" +
                        "• Dual Authentication: Login with Google (Normal Student) or GitHub (IT Student)\n" +
                        "• Personalized Study Plans: Create custom study plans based on your subjects and exam dates\n" +
                        "• Topic-Based Learning: Break down subjects into manageable topics with weighted importance\n" +
                        "• Smart Task Generation: Automatically generate daily study tasks based on your plan\n" +
                        "• Progress Tracking: Monitor your daily and overall progress with visual indicators\n" +
                        "• Streak System: Build and maintain study streaks to stay motivated\n" +
                        "• Reschedule Missed Tasks: Intelligently redistribute missed tasks across remaining days\n" +
                        "• GitHub Integration: For IT students, track commits and project progress\n" +
                        "• Modern UI: Clean, intuitive interface with beautiful design\n\n" +

                        "🎯 How It Works:\n" +
                        "1. Choose your role: Normal Student (Google) or IT Student (GitHub)\n" +
                        "2. Create a study plan with your subjects and exam date\n" +
                        "3. Add topics to each subject with difficulty levels\n" +
                        "4. Generate tasks automatically distributed across your available time\n" +
                        "5. Complete tasks daily to build your streak\n" +
                        "6. Track your progress and adjust your plan as needed\n\n" +

                        "💡 Tips for Success:\n" +
                        "• Start with smaller goals and gradually increase your daily hours\n" +
                        "• Use the reschedule feature if you fall behind\n" +
                        "• Focus on completing daily tasks to maintain your streak\n" +
                        "• Review your progress regularly and adjust your plan\n\n" +

                        "📊 Statistics:\n" +
                        "• Average user completes 85% of their tasks\n" +
                        "• Users who maintain a 7-day streak are 3x more likely to reach their goals\n" +
                        "• 1000+ students using Smart Study Planner worldwide\n\n" +

                        "© 2024 Smart Study Planner. All rights reserved.\n" +
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
        scrollPane.setPreferredSize(new Dimension(800, 400));
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(scrollPane);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
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
        List<StudyPlan> plans = studyPlanDAO.findByUserIdAndRole(user.getId(), "NORMAL", "GOOGLE");
        System.out.println("Found " + plans.size() + " NORMAL plans for user " + user.getId());
        for (StudyPlan p : plans) {
            System.out.println("  Plan ID: " + p.getId() + ", Subjects: " + p.getSubjects() + ", Role: " + p.getRole());
        }
        if (!plans.isEmpty()) {
            currentPlan = plans.get(0);
        }
    }

    private void refreshDashboard() {
        System.out.println("Dashboard refresh triggered");
    }

    public void refreshStudyPlan() {
        System.out.println("\n=== REFRESHING STUDY PLAN ===");

        checkAndHandleMissedTasks();

        taskListModel.clear();

        Integer activePlanId = user.getActivePlanId();
        System.out.println("Active Plan ID from user: " + activePlanId);

        if (activePlanId != null) {
            StudyPlan plan = studyPlanDAO.findById(activePlanId);
            if (plan != null && "NORMAL".equals(plan.getRole()) && "GOOGLE".equals(plan.getLoginType())) {

                List<StudyTask> todayTasks = studyTaskDAO.findTodayTasksByPlan(activePlanId);
                System.out.println("Today's tasks found: " + todayTasks.size());

                List<StudyTask> missedTasks = studyTaskDAO.findMissedTasks(activePlanId);

                for (StudyTask task : todayTasks) {
                    String status = "COMPLETED".equals(task.getStatus()) ? "✅ " : "⬜ ";
                    taskListModel.addElement(status + task.getDescription());
                    System.out.println("  Task: " + task.getDescription() + " - " + task.getStatus());
                }

                if (!missedTasks.isEmpty()) {
                    taskListModel.addElement("--- Missed Tasks (previous days) ---");
                    for (StudyTask task : missedTasks) {
                        taskListModel.addElement("❌ " + task.getDescription() + " (" + task.getTaskDate() + ")");
                    }
                }

                if (todayTasks.isEmpty() && missedTasks.isEmpty()) {
                    taskListModel.addElement("🎉 No tasks for today! Create a plan and generate tasks.");
                }
            } else {
                taskListModel.addElement("🎯 No active NORMAL plan selected. Choose a plan from 'View My Plans'.");
            }
        } else {
            taskListModel.addElement("🎯 No active plan selected. Choose a plan from 'View My Plans'.");
        }

        tasksCard.revalidate();
        tasksCard.repaint();
    }

    private void refreshProfile() {
        System.out.println("\n=== REFRESHING PROFILE ===");
        if (profileNameLabel != null) {
            profileNameLabel.setText(user.getName());
        }
        if (profileEmailLabel != null) {
            profileEmailLabel.setText(user.getEmail());
        }
        if (profileProviderLabel != null) {
            profileProviderLabel.setText("Google");
        }
        if (profileRoleLabel != null) {
            profileRoleLabel.setText("🎓 Normal Student");
        }
        if (profileAvatarLabel != null) {
            String initial = user.getName() != null && !user.getName().isEmpty()
                    ? user.getName().substring(0, 1).toUpperCase()
                    : "👤";
            profileAvatarLabel.setText(initial);
        }
    }

    private void createPlan() {
        List<String> selectedSubjects = new ArrayList<>();
        if (marathiCheckbox.isSelected()) selectedSubjects.add("Marathi");
        if (hindiCheckbox.isSelected()) selectedSubjects.add("Hindi");
        if (englishCheckbox.isSelected()) selectedSubjects.add("English");
        if (physicsCheckbox.isSelected()) selectedSubjects.add("Physics");
        if (chemistryCheckbox.isSelected()) selectedSubjects.add("Chemistry");
        if (biologyCheckbox.isSelected()) selectedSubjects.add("Biology");
        if (historyCheckbox.isSelected()) selectedSubjects.add("History");
        if (geographyCheckbox.isSelected()) selectedSubjects.add("Geography");

        String custom = customSubjectField.getText().trim();
        if (!custom.isEmpty()) {
            selectedSubjects.add(custom);
        }

        if (selectedSubjects.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one subject or enter a custom subject.",
                    "No Subjects",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String subjectsStr = String.join(",", selectedSubjects);
        System.out.println("Selected subjects: " + subjectsStr);

        try {
            int dailyHours = (Integer) hoursSpinner.getValue();
            java.util.Date selected = (java.util.Date) dateSpinner.getValue();
            LocalDate examDate = selected.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            String difficulty = (String) difficultyCombo.getSelectedItem();
            String difficultyEnum;
            if ("Easy".equals(difficulty)) {
                difficultyEnum = "EASY";
            } else if ("Medium".equals(difficulty)) {
                difficultyEnum = "MODERATE";
            } else {
                difficultyEnum = "HARD";
            }

            if (examDate.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this,
                        "Exam date must be in the future!",
                        "Invalid Date",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (user.getId() == 0) {
                UserDAO userDAO = new UserDAO();
                User dbUser = userDAO.findByEmail(user.getEmail());
                if (dbUser != null) {
                    user.setId(dbUser.getId());
                } else {
                    JOptionPane.showMessageDialog(this,
                            "User not found in database. Please log in again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            StudyPlan plan = new StudyPlan();
            plan.setUserId(user.getId());
            plan.setPlanName(subjectsStr);
            plan.setSubjects(subjectsStr);
            plan.setSubjectName(subjectsStr);
            plan.setDeadline(examDate);
            plan.setDailyHours(dailyHours);
            plan.setDifficulty(difficultyEnum);
            plan.setCompletionPercentage(0);
            plan.setAiGenerated(false);
            plan.setRole("NORMAL");
            plan.setLoginType("GOOGLE");

            System.out.println("Creating NORMAL plan with subjects: " + subjectsStr);
            System.out.println("Role: " + plan.getRole());
            System.out.println("LoginType: " + plan.getLoginType());

            StudyPlan savedPlan = studyPlanDAO.save(plan);

            if (savedPlan != null) {
                JOptionPane.showMessageDialog(this,
                        "✅ Plan created successfully!\nSubjects: " + subjectsStr + "\nNow you can add topics.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                SwingUtilities.invokeLater(() -> {
                    new PlanManagementFrame(this, user, savedPlan).setVisible(true);
                });

                cardLayout.show(contentPanel, "DASHBOARD");
                refreshDashboard();
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Failed to create plan. Check database connection.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Exception",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openPlanList() {
        SwingUtilities.invokeLater(() -> {
            new StudyPlanListFrame(this, user).setVisible(true);
        });
    }

    private void setupEventListeners() {
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

    public void refreshAll() {
        refreshDashboardFromPlans();
        refreshStudyPlan();
    }

    class TaskListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            String text = value.toString();
            if (text.startsWith("✅")) {
                setForeground(SUCCESS_COLOR);
                setFont(getFont().deriveFont(Font.BOLD));
            } else if (text.startsWith("❌")) {
                setForeground(MISSED_COLOR);
                setFont(getFont().deriveFont(Font.BOLD));
            } else if (text.startsWith("🎉") || text.startsWith("🎯")) {
                setForeground(PRIMARY_COLOR);
                setFont(getFont().deriveFont(Font.ITALIC));
            } else if (text.startsWith("⬜")) {
                setForeground(TEXT_PRIMARY);
                setFont(getFont().deriveFont(Font.PLAIN));
            } else if (text.contains("---")) {
                setForeground(TEXT_SECONDARY);
                setFont(getFont().deriveFont(Font.BOLD));
            }

            setBorder(new EmptyBorder(8, 10, 8, 10));

            return c;
        }
    }
}