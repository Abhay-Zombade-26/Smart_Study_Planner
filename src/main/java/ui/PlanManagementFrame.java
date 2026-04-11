package ui;

import model.User;
import model.StudyPlan;
import model.StudyTask;
import model.Topic;
import dao.StudyPlanDAO;
import dao.StudyTaskDAO;
import dao.TopicDAO;
import dao.UserDAO;
import service.NormalPlanGenerator;
import service.TopicWeightCalculator;
import service.PlanRescheduler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PlanManagementFrame extends JDialog {

    private User user;
    private StudyPlan plan;
    private NormalStudyPlannerFrame parentFrame;
    private StudyPlanDAO studyPlanDAO;
    private StudyTaskDAO studyTaskDAO;
    private TopicDAO topicDAO;
    private UserDAO userDAO;
    private NormalPlanGenerator planGenerator;
    private TopicWeightCalculator weightCalculator;

    private JLabel planIdLabel;
    private JLabel subjectsLabel;
    private JSpinner dateSpinner;
    private JSpinner hoursSpinner;
    private JComboBox<String> difficultyCombo;
    private JTable tasksTable;
    private DefaultTableModel tableModel;
    private JLabel statsLabel;
    private DefaultListModel<String> topicListModel;
    private JList<String> topicList;
    private JLabel missedTasksLabel;

    private final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private final Color DANGER_COLOR = new Color(239, 68, 68);
    private final Color WARNING_COLOR = new Color(245, 158, 11);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private final Color BORDER_COLOR = new Color(229, 231, 235);

    public PlanManagementFrame(User user, StudyPlan plan) {
        this(null, user, plan);
    }

    public PlanManagementFrame(NormalStudyPlannerFrame parent, User user, StudyPlan plan) {
        super(parent, "Manage Study Plan - ID: " + plan.getId(), true);
        this.user = user;
        this.plan = plan;
        this.parentFrame = parent;
        this.studyPlanDAO = new StudyPlanDAO();
        this.studyTaskDAO = new StudyTaskDAO();
        this.topicDAO = new TopicDAO();
        this.userDAO = new UserDAO();
        this.planGenerator = new NormalPlanGenerator();
        this.weightCalculator = new TopicWeightCalculator();

        initUI();
        loadPlanData();
        loadTopics();
        loadTasks();
        checkMissedTasks();

        setSize(1000, 750);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(CARD_BG);

        JPanel topPanel = createTopPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.4);
        splitPane.setBorder(null);
        splitPane.setBackground(CARD_BG);

        JPanel topicsPanel = createTopicsPanel();
        JPanel tasksPanel = createTasksPanel();

        splitPane.setLeftComponent(topicsPanel);
        splitPane.setRightComponent(tasksPanel);

        JPanel bottomPanel = createBottomPanel();

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Plan ID:"), gbc);
        gbc.gridx = 1;
        planIdLabel = new JLabel();
        planIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(planIdLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Subjects:"), gbc);
        gbc.gridx = 1;
        subjectsLabel = new JLabel();
        subjectsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(subjectsLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Exam Date:"), gbc);
        gbc.gridx = 1;
        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy"));
        dateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateSpinner.setPreferredSize(new Dimension(150, 30));
        panel.add(dateSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Hours per Day:"), gbc);
        gbc.gridx = 1;
        hoursSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 12, 1));
        hoursSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hoursSpinner.setPreferredSize(new Dimension(100, 30));
        panel.add(hoursSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Difficulty:"), gbc);
        gbc.gridx = 1;
        difficultyCombo = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        difficultyCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        difficultyCombo.setPreferredSize(new Dimension(120, 30));
        panel.add(difficultyCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Progress:"), gbc);
        gbc.gridx = 1;
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statsLabel.setForeground(PRIMARY_COLOR);
        panel.add(statsLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("Missed Tasks:"), gbc);
        gbc.gridx = 1;
        missedTasksLabel = new JLabel("0");
        missedTasksLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        missedTasksLabel.setForeground(DANGER_COLOR);
        panel.add(missedTasksLabel, gbc);

        return panel;
    }

    private JPanel createTopicsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel title = new JLabel("Topics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        topicListModel = new DefaultListModel<>();
        topicList = new JList<>(topicListModel);
        topicList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        topicList.setBackground(CARD_BG);
        topicList.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        JScrollPane scrollPane = new JScrollPane(topicList);
        scrollPane.setPreferredSize(new Dimension(300, 300));

        JButton addTopicBtn = new JButton("Add Topic");
        addTopicBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addTopicBtn.setForeground(Color.WHITE);
        addTopicBtn.setBackground(PRIMARY_COLOR);
        addTopicBtn.setBorderPainted(false);
        addTopicBtn.addActionListener(e -> openAddTopicDialog());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_BG);
        buttonPanel.add(addTopicBtn);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTasksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel title = new JLabel("Tasks");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        String[] columns = {"Date", "Description", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tasksTable = new JTable(tableModel);
        tasksTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tasksTable.setRowHeight(30);
        tasksTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tasksTable.getTableHeader().setBackground(new Color(249, 250, 251));
        tasksTable.setShowGrid(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tasksTable.getColumnCount(); i++) {
            tasksTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(tasksTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.setPreferredSize(new Dimension(400, 300));

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(CARD_BG);

        JButton generateTasksBtn = createStyledButton("Generate Tasks", SUCCESS_COLOR);
        generateTasksBtn.addActionListener(e -> generateTasks());

        JButton updatePlanBtn = createStyledButton("Update Plan", new Color(52, 152, 219));
        updatePlanBtn.addActionListener(e -> updatePlan());

        JButton deletePlanBtn = createStyledButton("Delete Plan", DANGER_COLOR);
        deletePlanBtn.addActionListener(e -> deletePlan());

        JButton rescheduleBtn = createStyledButton("🔄 Reschedule Missed", WARNING_COLOR);
        rescheduleBtn.addActionListener(e -> rescheduleMissedTasks());
        rescheduleBtn.setToolTipText("Distribute missed tasks across remaining days");

        JButton refreshBtn = createStyledButton("Refresh", PRIMARY_COLOR);
        refreshBtn.addActionListener(e -> {
            loadTopics();
            loadTasks();
            checkMissedTasks();
        });

        JButton closeBtn = createStyledButton("Close", new Color(100, 116, 139));
        closeBtn.addActionListener(e -> dispose());

        panel.add(generateTasksBtn);
        panel.add(updatePlanBtn);
        panel.add(deletePlanBtn);
        panel.add(rescheduleBtn);
        panel.add(refreshBtn);
        panel.add(closeBtn);

        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 35));
        return button;
    }

    private void loadPlanData() {
        plan = studyPlanDAO.findById(plan.getId());
        if (plan == null) {
            JOptionPane.showMessageDialog(this, "Plan not found!", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        planIdLabel.setText(String.valueOf(plan.getId()));
        String displaySubjects = plan.getSubjects() != null ? plan.getSubjects().replace(",", ", ") : plan.getSubjectName();
        subjectsLabel.setText(displaySubjects != null ? displaySubjects : "N/A");
        dateSpinner.setValue(Date.from(plan.getDeadline().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        hoursSpinner.setValue(plan.getDailyHours());
        difficultyCombo.setSelectedItem(capitalize(plan.getDifficulty()));
    }

    private void loadTopics() {
        topicListModel.clear();
        List<Topic> topics = topicDAO.findByPlanId(plan.getId());
        for (Topic topic : topics) {
            topicListModel.addElement(String.format("[%s] %s (diff=%d, size=%d, weight=%.1f)",
                    topic.getSubject(), topic.getName(), topic.getDifficulty(), topic.getSize(), topic.getWeight()));
        }
    }

    private void loadTasks() {
        tableModel.setRowCount(0);

        plan = studyPlanDAO.findById(plan.getId());
        if (plan == null) {
            System.err.println("Plan not found!");
            return;
        }

        List<StudyTask> tasks = studyTaskDAO.findByGoalId(plan.getId());
        int completed = 0;
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        System.out.println("=== LOADING TASKS ===");
        System.out.println("Plan ID: " + plan.getId());
        System.out.println("Found " + tasks.size() + " tasks for plan " + plan.getId());

        if (tasks.isEmpty()) {
            tableModel.addRow(new Object[]{"-", "No tasks generated yet. Click 'Generate Tasks' after adding topics.", "-"});
            System.out.println("No tasks found!");
        } else {
            for (StudyTask task : tasks) {
                String status;
                String statusText = task.getStatus();

                if ("COMPLETED".equals(statusText)) {
                    status = "✅ Completed";
                    completed++;
                } else if ("RESCHEDULED".equals(statusText)) {
                    status = "🔄 Rescheduled";
                } else if (task.getTaskDate().isBefore(today) && !"COMPLETED".equals(statusText) && !"RESCHEDULED".equals(statusText)) {
                    status = "❌ Missed";
                } else {
                    status = "⏳ Pending";
                }

                System.out.println("  Task: " + task.getTaskDate() + " - " + task.getDescription() + " [" + statusText + "]");

                tableModel.addRow(new Object[]{
                        task.getTaskDate().format(fmt),
                        task.getDescription(),
                        status
                });
            }
        }

        int total = tasks.size();
        int pendingAndRescheduled = 0;
        for (StudyTask task : tasks) {
            String s = task.getStatus();
            if (!"COMPLETED".equals(s) && !"RESCHEDULED".equals(s)) {
                pendingAndRescheduled++;
            }
        }

        int progress = total > 0 ? (completed * 100 / total) : 0;
        statsLabel.setText(completed + "/" + total + " (" + progress + "%)");

        System.out.println("Total tasks: " + total + ", Completed: " + completed + ", Progress: " + progress + "%");
        System.out.println("=========================");
    }

    private void generateTasks() {
        List<Topic> topics = topicDAO.findByPlanId(plan.getId());
        if (topics.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add topics first.", "No Topics", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "This will replace all pending tasks. Completed tasks will be preserved.\nContinue?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Get existing tasks
        List<StudyTask> allTasks = studyTaskDAO.findByGoalId(plan.getId());

        // Separate completed tasks (preserve these)
        List<StudyTask> completedTasks = new ArrayList<>();
        List<StudyTask> otherTasks = new ArrayList<>();

        for (StudyTask task : allTasks) {
            if ("COMPLETED".equals(task.getStatus())) {
                completedTasks.add(task);
            } else {
                otherTasks.add(task);
            }
        }

        System.out.println("\n=== GENERATING TASKS ===");
        System.out.println("Plan ID: " + plan.getId());
        System.out.println("Completed tasks preserved: " + completedTasks.size());
        System.out.println("Pending/Missed tasks to regenerate: " + otherTasks.size());
        System.out.println("Topics found: " + topics.size());

        // Delete only pending/missed tasks (not completed)
        for (StudyTask task : otherTasks) {
            studyTaskDAO.deleteTaskById(task.getId());
        }
        System.out.println("Deleted " + otherTasks.size() + " pending/missed tasks");

        // Generate new tasks
        List<StudyTask> newTasks = planGenerator.generateTasksFromTopics(plan);
        System.out.println("Generated " + newTasks.size() + " new tasks");

        if (newTasks.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No tasks were generated. Please check if you have topics added and deadline is in the future.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            // Restore the pending tasks if generation failed?
            // But they're already deleted, so we need to handle this
            return;
        }

        // Save new tasks
        studyTaskDAO.saveAll(newTasks);
        System.out.println("✅ Saved " + newTasks.size() + " new tasks to database");

        JOptionPane.showMessageDialog(this,
                "Tasks generated successfully!\n" +
                        "Preserved: " + completedTasks.size() + " completed tasks\n" +
                        "New tasks: " + newTasks.size() + "\n" +
                        "Total tasks: " + (completedTasks.size() + newTasks.size()),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

        // Refresh the tasks display
        loadTasks();
        checkMissedTasks();

        // Refresh parent dashboard and study plan
        if (parentFrame != null) {
            parentFrame.refreshDashboardFromPlans();
            parentFrame.refreshStudyPlan();
        }
        System.out.println("=== TASKS GENERATION COMPLETE ===\n");
    }

    private void checkMissedTasks() {
        List<StudyTask> allTasks = studyTaskDAO.findByGoalId(plan.getId());
        LocalDate today = LocalDate.now();

        int missedCount = 0;
        for (StudyTask task : allTasks) {
            String status = task.getStatus();
            // Only count as missed if status is PENDING and date is in past
            if (!"COMPLETED".equals(status) && !"RESCHEDULED".equals(status)) {
                if (task.getTaskDate().isBefore(today)) {
                    missedCount++;
                }
            }
        }

        missedTasksLabel.setText(String.valueOf(missedCount));

        if (missedCount > 0) {
            missedTasksLabel.setForeground(DANGER_COLOR);
        } else {
            missedTasksLabel.setForeground(SUCCESS_COLOR);
        }

        System.out.println("Missed tasks count: " + missedCount);
    }

    private void rescheduleMissedTasks() {
        // Refresh plan data first
        plan = studyPlanDAO.findById(plan.getId());

        if (plan == null) {
            JOptionPane.showMessageDialog(this, "Plan not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<StudyTask> missedTasks = studyTaskDAO.findMissedTasks(plan.getId());

        if (missedTasks.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "🎉 No missed tasks to reschedule! Great job!",
                    "No Missed Tasks",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Show current plan info
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), plan.getDeadline()) + 1;
        int maxTasksPerDay = plan.getDailyHours();

        String message = String.format(
                "📊 Reschedule Summary:\n\n" +
                        "• Missed tasks: %d\n" +
                        "• Days remaining: %d\n" +
                        "• Max tasks/day: %d\n\n" +
                        "Missed tasks will be marked as 'Rescheduled' and\n" +
                        "new copies will be created on future dates.\n\n" +
                        "Continue?",
                missedTasks.size(),
                daysRemaining,
                maxTasksPerDay
        );

        int confirm = JOptionPane.showConfirmDialog(this,
                message,
                "Reschedule Tasks",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        // Show processing indicator
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            PlanRescheduler planRescheduler = new PlanRescheduler();
            PlanRescheduler.RescheduleResult result = planRescheduler.rescheduleMissedTasks(plan.getId(), missedTasks);

            if (result.success) {
                JOptionPane.showMessageDialog(this,
                        result.message,
                        "✅ Rescheduling Successful",
                        JOptionPane.INFORMATION_MESSAGE);

                // CRITICAL: Refresh everything
                loadPlanData();
                loadTopics();
                loadTasks();
                checkMissedTasks();

                if (parentFrame != null) {
                    parentFrame.refreshDashboardFromPlans();
                    parentFrame.refreshStudyPlan();
                }

                // Force UI repaint
                repaint();
                revalidate();
            } else {
                JOptionPane.showMessageDialog(this,
                        result.message,
                        "❌ Rescheduling Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void openAddTopicDialog() {
        String subjectsStr = plan.getSubjects();
        if (subjectsStr == null || subjectsStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No subjects defined for this plan.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String[] subjectsArray = subjectsStr.split(",");
        for (int i = 0; i < subjectsArray.length; i++) {
            subjectsArray[i] = subjectsArray[i].trim();
        }

        Window window = SwingUtilities.getWindowAncestor(this);
        Frame parentFrame = (window instanceof Frame) ? (Frame) window : null;

        AddTopicDialog dialog = new AddTopicDialog(parentFrame, plan.getId(), subjectsArray);
        dialog.setVisible(true);
        if (dialog.isSucceeded()) {
            loadTopics();
        }
    }

    private void updatePlan() {
        try {
            Date selected = (Date) dateSpinner.getValue();
            LocalDate newDeadline = selected.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int newHours = (Integer) hoursSpinner.getValue();

            String selectedDifficulty = (String) difficultyCombo.getSelectedItem();
            String difficultyEnum;
            if ("Easy".equals(selectedDifficulty)) {
                difficultyEnum = "EASY";
            } else if ("Medium".equals(selectedDifficulty)) {
                difficultyEnum = "MODERATE";
            } else {
                difficultyEnum = "HARD";
            }

            if (newDeadline.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Exam date must be in the future!", "Invalid Date", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate oldDeadline = plan.getDeadline();
            int oldHours = plan.getDailyHours();
            String oldDifficulty = plan.getDifficulty();

            // Check if anything actually changed
            boolean deadlineChanged = !oldDeadline.equals(newDeadline);
            boolean hoursChanged = oldHours != newHours;
            boolean difficultyChanged = !oldDifficulty.equals(difficultyEnum);

            if (!deadlineChanged && !hoursChanged && !difficultyChanged) {
                JOptionPane.showMessageDialog(this, "No changes detected.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Get all tasks
            List<StudyTask> allTasks = studyTaskDAO.findByGoalId(plan.getId());

            // Separate completed and pending tasks
            List<StudyTask> completedTasks = new ArrayList<>();
            List<StudyTask> pendingTasks = new ArrayList<>();

            for (StudyTask task : allTasks) {
                if ("COMPLETED".equals(task.getStatus())) {
                    completedTasks.add(task);
                } else {
                    pendingTasks.add(task);
                }
            }

            System.out.println("\n=== UPDATING PLAN ===");
            System.out.println("Plan ID: " + plan.getId());
            System.out.println("Old deadline: " + oldDeadline);
            System.out.println("New deadline: " + newDeadline);
            System.out.println("Old hours: " + oldHours);
            System.out.println("New hours: " + newHours);
            System.out.println("Completed tasks: " + completedTasks.size());
            System.out.println("Pending tasks: " + pendingTasks.size());

            // Calculate capacity with new settings
            long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), newDeadline) + 1;
            long totalCapacity = daysRemaining * newHours;
            int totalPendingTasks = pendingTasks.size();

            String message;
            boolean needReschedule = false;

            if (deadlineChanged || hoursChanged) {
                // Check if pending tasks fit in new capacity
                if (totalPendingTasks > totalCapacity) {
                    int extra = totalPendingTasks - (int) totalCapacity;
                    int confirm = JOptionPane.showConfirmDialog(this,
                            String.format("⚠️ Warning: New settings don't have enough capacity!\n\n" +
                                            "• Pending tasks: %d\n" +
                                            "• New capacity: %d slots\n" +
                                            "• Shortage: %d tasks\n\n" +
                                            "The extra tasks will be scheduled on the last day.\n" +
                                            "Consider increasing daily hours or extending deadline.\n\n" +
                                            "Continue anyway?",
                                    totalPendingTasks, totalCapacity, extra),
                            "Capacity Warning",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (confirm != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                message = String.format("Plan settings will be updated:\n\n" +
                                "• Deadline: %s → %s\n" +
                                "• Daily Hours: %d → %d\n" +
                                "• Difficulty: %s → %s\n\n" +
                                "Pending tasks will be rescheduled to fit new settings.\n" +
                                "✅ Completed tasks (%d) will be preserved.\n\n" +
                                "Continue?",
                        oldDeadline, newDeadline,
                        oldHours, newHours,
                        oldDifficulty, difficultyEnum,
                        completedTasks.size());
                needReschedule = true;
            } else {
                message = String.format("Update plan difficulty?\n\n" +
                                "• Difficulty: %s → %s\n\n" +
                                "No rescheduling needed.\n" +
                                "Continue?",
                        oldDifficulty, difficultyEnum);
            }

            int confirm = JOptionPane.showConfirmDialog(this, message, "Confirm Update",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            // Update plan in database
            plan.setDeadline(newDeadline);
            plan.setDailyHours(newHours);
            plan.setDifficulty(difficultyEnum);
            studyPlanDAO.update(plan);

            // Reschedule pending tasks if deadline or hours changed
            if (needReschedule && !pendingTasks.isEmpty()) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                try {
                    // Delete old pending tasks
                    for (StudyTask task : pendingTasks) {
                        studyTaskDAO.deleteTaskById(task.getId());
                    }
                    System.out.println("Deleted " + pendingTasks.size() + " old pending tasks");

                    // Redistribute pending tasks across new date range
                    Map<LocalDate, List<StudyTask>> distribution = redistributeTasks(
                            pendingTasks, LocalDate.now(), (int) daysRemaining, newHours);

                    // Save redistributed tasks
                    int savedCount = 0;
                    for (Map.Entry<LocalDate, List<StudyTask>> entry : distribution.entrySet()) {
                        for (StudyTask task : entry.getValue()) {
                            task.setTaskDate(entry.getKey());
                            task.setStatus("PENDING");
                            studyTaskDAO.save(task);
                            savedCount++;
                        }
                    }

                    System.out.println("✅ Rescheduled " + savedCount + " tasks with new settings");
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }

            // Show success message
            String successMsg = "✅ Plan updated successfully!\n\n";
            if (needReschedule && !pendingTasks.isEmpty()) {
                successMsg += String.format("• %d tasks rescheduled\n", pendingTasks.size());
            }
            successMsg += String.format("• %d completed tasks preserved", completedTasks.size());

            JOptionPane.showMessageDialog(this, successMsg, "Success", JOptionPane.INFORMATION_MESSAGE);

            // Refresh UI
            loadPlanData();
            loadTasks();
            checkMissedTasks();

            if (parentFrame != null) {
                parentFrame.refreshDashboardFromPlans();
                parentFrame.refreshStudyPlan();
            }

            System.out.println("=== PLAN UPDATE COMPLETE ===\n");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error updating plan: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Redistributes tasks evenly across available days
     */
    private Map<LocalDate, List<StudyTask>> redistributeTasks(
            List<StudyTask> tasks, LocalDate startDate, int daysRemaining, int maxPerDay) {

        Map<LocalDate, List<StudyTask>> distribution = new TreeMap<>();

        // Initialize all days with empty lists
        for (int i = 0; i < daysRemaining; i++) {
            distribution.put(startDate.plusDays(i), new ArrayList<>());
        }

        // Sort tasks by original date (oldest first)
        tasks.sort(Comparator.comparing(StudyTask::getTaskDate));

        // Round-robin distribution
        int dayIndex = 0;
        for (StudyTask task : tasks) {
            // Find next available day that hasn't reached max capacity
            int attempts = 0;
            boolean placed = false;

            while (attempts < daysRemaining) {
                LocalDate date = startDate.plusDays(dayIndex);
                List<StudyTask> dayTasks = distribution.get(date);

                if (dayTasks.size() < maxPerDay) {
                    dayTasks.add(task);
                    placed = true;
                    break;
                }

                dayIndex = (dayIndex + 1) % daysRemaining;
                attempts++;
            }

            // If all days are full, add to the day with least tasks
            if (!placed) {
                LocalDate minDay = distribution.entrySet().stream()
                        .min(Comparator.comparingInt(e -> e.getValue().size()))
                        .map(Map.Entry::getKey)
                        .orElse(startDate.plusDays(daysRemaining - 1));
                distribution.get(minDay).add(task);
            }

            dayIndex = (dayIndex + 1) % daysRemaining;
        }

        // Print distribution for debugging
        System.out.println("\nNew task distribution:");
        for (Map.Entry<LocalDate, List<StudyTask>> entry : distribution.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue().size() + " tasks");
            }
        }

        return distribution;
    }

    private void deletePlan() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this plan? All topics and tasks will be lost.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            studyTaskDAO.deleteByGoalId(plan.getId());
            topicDAO.deleteByPlanId(plan.getId());
            studyPlanDAO.deleteById(plan.getId());

            if (user.getActivePlanId() != null && user.getActivePlanId() == plan.getId()) {
                userDAO.updateActivePlan(user.getId(), null);
                user.setActivePlanId(null);
                if (parentFrame != null) {
                    parentFrame.refreshDashboardFromPlans();
                    parentFrame.refreshStudyPlan();
                }
            }

            JOptionPane.showMessageDialog(this, "Plan deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}