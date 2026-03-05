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

    private StudyPlanDAO studyPlanDAO;
    private StudyTaskDAO studyTaskDAO;
    private NormalPlanGenerator planGenerator;
    private StudyPlan currentPlan;

    public NormalStudyPlannerFrame(User user) {
        this.user = user;
        this.studyPlanDAO = new StudyPlanDAO();
        this.studyTaskDAO = new StudyTaskDAO();
        this.planGenerator = new NormalPlanGenerator();

        // FIX: Ensure user has ID from database
        fixUserId();

        initUI();
        loadUserData();
        setupEventListeners();
    }

    /**
     * FIX: This method ensures the user object has the correct database ID
     */
    private void fixUserId() {
        System.out.println("\n=== USER ID FIX ===");
        System.out.println("User email: " + user.getEmail());
        System.out.println("Current user ID: " + user.getId());

        if (user.getId() == 0) {
            System.out.println("⚠️ User ID is 0, fetching from database...");
            UserDAO userDAO = new UserDAO();

            // Try to find user by email
            User dbUser = userDAO.findByEmail(user.getEmail());
            if (dbUser != null) {
                user.setId(dbUser.getId());
                System.out.println("✅ Fixed user ID to: " + user.getId());
            } else {
                System.err.println("❌ Could not find user in database!");

                // Try to save the user again
                User savedUser = userDAO.save(user);
                if (savedUser != null) {
                    user.setId(savedUser.getId());
                    System.out.println("✅ User saved and ID set to: " + user.getId());
                } else {
                    System.err.println("❌ Failed to save user!");
                }
            }
        } else {
            System.out.println("✅ User ID is already set: " + user.getId());
        }
        System.out.println("==================\n");
    }

    private void initUI() {
        setTitle("Smart Study Planner - Normal Student");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 250));

        createSidebar();

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

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
        sidebarPanel.setBackground(new Color(33, 33, 33));
        sidebarPanel.setPreferredSize(new Dimension(250, getHeight()));
        sidebarPanel.setBorder(new EmptyBorder(20, 15, 20, 15));

        JLabel logoLabel = new JLabel("📚 Smart Planner");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(logoLabel);
        sidebarPanel.add(Box.createVerticalStrut(20));

        // User info
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setBackground(new Color(45, 45, 45));
        userInfoPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        userInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        userInfoPanel.setMaximumSize(new Dimension(220, 100));

        JLabel nameLabel = new JLabel("Welcome, " + user.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(Color.WHITE);

        JLabel roleLabel = new JLabel("Normal Student");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(new Color(52, 152, 219));

        JLabel providerLabel = new JLabel("via " + user.getOauthProvider());
        providerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        providerLabel.setForeground(new Color(160, 174, 192));

        userInfoPanel.add(nameLabel);
        userInfoPanel.add(Box.createVerticalStrut(5));
        userInfoPanel.add(roleLabel);
        userInfoPanel.add(Box.createVerticalStrut(3));
        userInfoPanel.add(providerLabel);

        sidebarPanel.add(userInfoPanel);
        sidebarPanel.add(Box.createVerticalStrut(30));

        // Navigation
        JLabel navHeader = new JLabel("NAVIGATION");
        navHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        navHeader.setForeground(new Color(160, 174, 192));
        navHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(navHeader);
        sidebarPanel.add(Box.createVerticalStrut(10));

        addNavButton("📊 Dashboard", "DASHBOARD");
        addNavButton("📋 Study Plan", "STUDY_PLAN");
        addNavButton("👤 Profile", "PROFILE");
        addNavButton("ℹ️ About App", "ABOUT");

        sidebarPanel.add(Box.createVerticalGlue());

        JButton logoutBtn = new JButton("🚪 Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(new Color(220, 38, 38));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(220, 40));
        logoutBtn.addActionListener(e -> logout());

        sidebarPanel.add(logoutBtn);
    }

    private void addNavButton(String text, String cardName) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(new Color(226, 232, 240));
        button.setBackground(new Color(33, 33, 33));
        button.setBorderPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(220, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> {
            cardLayout.show(contentPanel, cardName);
            if (cardName.equals("DASHBOARD")) refreshDashboard();
            else if (cardName.equals("STUDY_PLAN")) refreshStudyPlan();
        });
        sidebarPanel.add(button);
        sidebarPanel.add(Box.createVerticalStrut(5));
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Welcome section
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        welcomeLabel = new JLabel("Welcome, " + user.getName());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        dateLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(new Color(100, 116, 139));

        topPanel.add(welcomeLabel, BorderLayout.NORTH);
        topPanel.add(dateLabel, BorderLayout.SOUTH);
        panel.add(topPanel, BorderLayout.NORTH);

        // Stats cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        JPanel totalCard = createStatCard("📋", "Total Tasks", "0", new Color(52, 152, 219));
        totalTasksLabel = (JLabel) ((JPanel)totalCard.getComponent(1)).getComponent(0);

        JPanel completedCard = createStatCard("✅", "Completed", "0", new Color(46, 204, 113));
        completedTasksLabel = (JLabel) ((JPanel)completedCard.getComponent(1)).getComponent(0);

        JPanel progressCard = createStatCard("📊", "Daily Progress", "0%", new Color(155, 89, 182));

        statsPanel.add(totalCard);
        statsPanel.add(completedCard);
        statsPanel.add(progressCard);
        panel.add(statsPanel, BorderLayout.CENTER);

        // Progress and tasks
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 20));
        bottomPanel.setBackground(Color.WHITE);

        // Progress bar
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBackground(Color.WHITE);
        progressPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                "Daily Progress",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14)
        ));

        dailyProgressBar = new JProgressBar(0, 100);
        dailyProgressBar.setStringPainted(true);
        dailyProgressBar.setForeground(new Color(52, 152, 219));
        dailyProgressBar.setPreferredSize(new Dimension(200, 25));
        progressPanel.add(dailyProgressBar, BorderLayout.CENTER);

        // Today's tasks
        JPanel tasksPanel = new JPanel(new BorderLayout());
        tasksPanel.setBackground(Color.WHITE);
        tasksPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                "Today's Task List",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14)
        ));

        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        taskList.setBackground(new Color(248, 250, 252));

        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setPreferredSize(new Dimension(400, 150));
        tasksPanel.add(scrollPane, BorderLayout.CENTER);

        bottomPanel.add(progressPanel, BorderLayout.NORTH);
        bottomPanel.add(tasksPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatCard(String icon, String label, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 32));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(color);

        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelLabel.setForeground(new Color(100, 116, 139));

        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(labelLabel);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createStudyPlanPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Normal Student Study Planner");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Main content
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        contentPanel.setBackground(Color.WHITE);

        // Subject selection
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                "Select Subjects",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16)
        ));

        JPanel subjectsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        subjectsPanel.setBackground(Color.WHITE);
        subjectsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        mathCheckbox = new JCheckBox("Mathematics", true);
        scienceCheckbox = new JCheckBox("Science", true);
        englishCheckbox = new JCheckBox("English", true);
        historyCheckbox = new JCheckBox("History");
        geographyCheckbox = new JCheckBox("Geography");
        economicsCheckbox = new JCheckBox("Economics");
        physicsCheckbox = new JCheckBox("Physics");
        chemistryCheckbox = new JCheckBox("Chemistry");

        subjectsPanel.add(mathCheckbox);
        subjectsPanel.add(scienceCheckbox);
        subjectsPanel.add(englishCheckbox);
        subjectsPanel.add(historyCheckbox);
        subjectsPanel.add(geographyCheckbox);
        subjectsPanel.add(economicsCheckbox);
        subjectsPanel.add(physicsCheckbox);
        subjectsPanel.add(chemistryCheckbox);

        leftPanel.add(subjectsPanel, BorderLayout.CENTER);

        // Configuration
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                "Study Plan Configuration",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16)
        ));

        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBackground(Color.WHITE);
        configPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0;
        configPanel.add(new JLabel("Hours per Day:"), gbc);
        gbc.gridx = 1;
        hoursSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 12, 1));
        configPanel.add(hoursSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        configPanel.add(new JLabel("Exam Date:"), gbc);
        gbc.gridx = 1;
        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy"));
        configPanel.add(dateSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        configPanel.add(new JLabel("Difficulty:"), gbc);
        gbc.gridx = 1;
        difficultyCombo = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        configPanel.add(difficultyCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JButton generateBtn = new JButton("Generate Study Plan");
        generateBtn.setBackground(new Color(52, 152, 219));
        generateBtn.setForeground(Color.WHITE);
        generateBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        generateBtn.addActionListener(e -> generateStudyPlan());
        configPanel.add(generateBtn, gbc);

        gbc.gridy = 4;
        JButton viewBtn = new JButton("View My Plan");
        viewBtn.setBackground(new Color(46, 204, 113));
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        viewBtn.addActionListener(e -> showPlanTable());
        configPanel.add(viewBtn, gbc);

        rightPanel.add(configPanel, BorderLayout.CENTER);

        contentPanel.add(leftPanel);
        contentPanel.add(rightPanel);
        panel.add(contentPanel, BorderLayout.CENTER);

        // Plan table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                "Your Study Plan",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16)
        ));

        String[] columns = {"Date", "Subject", "Time", "Status"};
        planTableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        planTable = new JTable(planTableModel);
        planTable.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(planTable);
        scrollPane.setPreferredSize(new Dimension(800, 150));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(tablePanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel("Profile settings will appear here");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAboutPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel("About information will appear here");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);
        return panel;
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
        dailyProgressBar.setString(progress + "% (" + completedToday + " of " + totalToday + ")");

        taskListModel.clear();
        for (StudyTask task : todayTasks) {
            String status = "COMPLETED".equals(task.getStatus()) ? "✅ " : "⬜ ";
            taskListModel.addElement(status + task.getDescription());
        }
        if (todayTasks.isEmpty()) {
            taskListModel.addElement("🎉 No tasks for today!");
        }
    }

    private void refreshStudyPlan() {
        if (currentPlan != null) showPlanTable();
    }

    private void generateStudyPlan() {
        System.out.println("\n=== GENERATE PLAN BUTTON CLICKED ===");
        System.out.println("Current user ID: " + user.getId());
        System.out.println("User email: " + user.getEmail());

        List<String> subjects = new ArrayList<>();
        if (mathCheckbox.isSelected()) subjects.add("Mathematics");
        if (scienceCheckbox.isSelected()) subjects.add("Science");
        if (englishCheckbox.isSelected()) subjects.add("English");
        if (historyCheckbox.isSelected()) subjects.add("History");
        if (geographyCheckbox.isSelected()) subjects.add("Geography");
        if (economicsCheckbox.isSelected()) subjects.add("Economics");
        if (physicsCheckbox.isSelected()) subjects.add("Physics");
        if (chemistryCheckbox.isSelected()) subjects.add("Chemistry");

        System.out.println("Selected subjects: " + subjects);

        if (subjects.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select at least one subject");
            return;
        }

        try {
            int dailyHours = (Integer) hoursSpinner.getValue();
            System.out.println("Daily hours: " + dailyHours);

            java.util.Date selected = (java.util.Date) dateSpinner.getValue();
            LocalDate examDate = selected.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            System.out.println("Exam date: " + examDate);

            String difficulty = (String) difficultyCombo.getSelectedItem();
            System.out.println("Difficulty: " + difficulty);

            if (examDate.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Exam date must be in the future!");
                return;
            }

            // Double-check user ID before generating plan
            if (user.getId() == 0) {
                System.err.println("❌ User ID is still 0! Fixing again...");
                UserDAO userDAO = new UserDAO();
                User dbUser = userDAO.findByEmail(user.getEmail());
                if (dbUser != null) {
                    user.setId(dbUser.getId());
                    System.out.println("✅ Fixed user ID to: " + user.getId());
                } else {
                    JOptionPane.showMessageDialog(this,
                            "User not found in database. Please login again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            currentPlan = planGenerator.generatePlan(
                    user, String.join(",", subjects), examDate, dailyHours, difficulty.toUpperCase()
            );

            System.out.println("Plan generated: " + (currentPlan != null ? "SUCCESS" : "FAILED"));

            if (currentPlan != null) {
                JOptionPane.showMessageDialog(this, "Study plan generated successfully!");
                showPlanTable();
                refreshDashboard();
                cardLayout.show(contentPanel, "DASHBOARD");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to generate plan. Check database connection.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showPlanTable() {
        if (currentPlan == null) {
            JOptionPane.showMessageDialog(this, "No plan found");
            return;
        }

        planTableModel.setRowCount(0);
        List<StudyTask> tasks = studyTaskDAO.findByGoalId(currentPlan.getId());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE");

        for (StudyTask task : tasks) {
            String status = "Pending";
            if ("COMPLETED".equals(task.getStatus())) status = "✅ Completed";
            else if ("MISSED".equals(task.getStatus())) status = "❌ Missed";

            planTableModel.addRow(new Object[]{
                    task.getTaskDate().format(fmt),
                    task.getDescription().replace("Study ", ""),
                    "1.3h",
                    status
            });
        }
    }

    private void setupEventListeners() {
        taskList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = taskList.locationToIndex(evt.getPoint());
                    if (index >= 0) {
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
        });
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
}
