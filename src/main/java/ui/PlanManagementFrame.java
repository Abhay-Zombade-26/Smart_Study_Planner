package ui;

import model.User;
import model.StudyPlan;
import model.StudyTask;
import model.Topic;
import dao.StudyPlanDAO;
import dao.StudyTaskDAO;
import dao.TopicDAO;
import service.NormalPlanGenerator;
import service.TopicWeightCalculator;
import service.PlanRescheduler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PlanManagementFrame extends JFrame {

    private User user;
    private StudyPlan plan;
    private StudyPlanDAO studyPlanDAO;
    private StudyTaskDAO studyTaskDAO;
    private TopicDAO topicDAO;
    private NormalPlanGenerator planGenerator;
    private TopicWeightCalculator weightCalculator;
    private PlanRescheduler planRescheduler;

    // UI Components
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

    // Colors
    private final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private final Color DANGER_COLOR = new Color(239, 68, 68);
    private final Color WARNING_COLOR = new Color(245, 158, 11);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private final Color BORDER_COLOR = new Color(229, 231, 235);

    public PlanManagementFrame(User user, StudyPlan plan) {
        this.user = user;
        this.plan = plan;
        this.studyPlanDAO = new StudyPlanDAO();
        this.studyTaskDAO = new StudyTaskDAO();
        this.topicDAO = new TopicDAO();
        this.planGenerator = new NormalPlanGenerator();
        this.weightCalculator = new TopicWeightCalculator();
        this.planRescheduler = new PlanRescheduler();

        initUI();
        loadPlanData();
        loadTopics();
        loadTasks();
        checkMissedTasks();
    }

    private void initUI() {
        setTitle("Manage Study Plan - ID: " + plan.getId());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(CARD_BG);

        // Top Panel - Plan Info & Controls
        JPanel topPanel = createTopPanel();

        // Center Panel - Split: Topics (left) and Tasks (right)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.4);
        splitPane.setBorder(null);
        splitPane.setBackground(CARD_BG);

        // Left: Topics panel
        JPanel topicsPanel = createTopicsPanel();
        // Right: Tasks panel
        JPanel tasksPanel = createTasksPanel();

        splitPane.setLeftComponent(topicsPanel);
        splitPane.setRightComponent(tasksPanel);

        // Bottom Panel - Action Buttons
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

        // Plan ID
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Plan ID:"), gbc);
        gbc.gridx = 1;
        planIdLabel = new JLabel();
        planIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(planIdLabel, gbc);

        // Subjects
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Subjects:"), gbc);
        gbc.gridx = 1;
        subjectsLabel = new JLabel();
        subjectsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(subjectsLabel, gbc);

        // Exam Date
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Exam Date:"), gbc);
        gbc.gridx = 1;
        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy"));
        dateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateSpinner.setPreferredSize(new Dimension(150, 30));
        panel.add(dateSpinner, gbc);

        // Hours per Day
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Hours per Day:"), gbc);
        gbc.gridx = 1;
        hoursSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 12, 1));
        hoursSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hoursSpinner.setPreferredSize(new Dimension(100, 30));
        panel.add(hoursSpinner, gbc);

        // Difficulty
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Difficulty:"), gbc);
        gbc.gridx = 1;
        difficultyCombo = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        difficultyCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        difficultyCombo.setPreferredSize(new Dimension(120, 30));
        panel.add(difficultyCombo, gbc);

        // Stats
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Progress:"), gbc);
        gbc.gridx = 1;
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statsLabel.setForeground(PRIMARY_COLOR);
        panel.add(statsLabel, gbc);

        // Missed tasks indicator
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

        // RESCHEDULE BUTTON
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
        List<StudyTask> tasks = studyTaskDAO.findByGoalId(plan.getId());
        int completed = 0;
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        for (StudyTask task : tasks) {
            String status;
            if ("COMPLETED".equals(task.getStatus())) {
                status = "✅ Completed";
                completed++;
            } else if (task.getTaskDate().isBefore(today) && !"COMPLETED".equals(task.getStatus())) {
                status = "❌ Missed";
            } else {
                status = "⏳ Pending";
            }
            tableModel.addRow(new Object[]{
                    task.getTaskDate().format(fmt),
                    task.getDescription(),
                    status
            });
        }
        int total = tasks.size();
        int progress = total > 0 ? (completed * 100 / total) : 0;
        statsLabel.setText(completed + "/" + total + " (" + progress + "%)");
    }

    private void checkMissedTasks() {
        List<StudyTask> missedTasks = studyTaskDAO.findMissedTasks(plan.getId());
        int missedCount = missedTasks.size();
        missedTasksLabel.setText(String.valueOf(missedCount));

        if (missedCount > 0) {
            missedTasksLabel.setForeground(DANGER_COLOR);
        } else {
            missedTasksLabel.setForeground(SUCCESS_COLOR);
        }
    }

    private void rescheduleMissedTasks() {
        // Refresh plan data first to get latest daily hours
        plan = studyPlanDAO.findById(plan.getId());

        List<StudyTask> missedTasks = studyTaskDAO.findMissedTasks(plan.getId());

        if (missedTasks.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No missed tasks to reschedule! Great job! 🎉",
                    "No Missed Tasks",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Show current plan info
        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), plan.getDeadline());

        int confirm = JOptionPane.showConfirmDialog(this,
                "You have " + missedTasks.size() + " missed task(s).\n" +
                        "Current daily hours: " + plan.getDailyHours() + "\n" +
                        "Days remaining: " + daysRemaining + "\n\n" +
                        "These will be distributed evenly across your remaining days.\n" +
                        "Continue?",
                "Reschedule Missed Tasks",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        // Use the PlanRescheduler
        PlanRescheduler.RescheduleResult result = planRescheduler.rescheduleMissedTasks(plan.getId(), missedTasks);

        if (result.success) {
            // Show distribution summary
            StringBuilder message = new StringBuilder("✅ " + result.message + "\n\nNew distribution:\n");
            if (result.newDistribution != null) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd");
                for (Map.Entry<LocalDate, Integer> entry : result.newDistribution.entrySet()) {
                    message.append("• ").append(entry.getKey().format(fmt))
                            .append(": ").append(entry.getValue()).append(" tasks\n");
                }
            }

            JOptionPane.showMessageDialog(this,
                    message.toString(),
                    "Rescheduling Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            // Refresh the view
            loadTasks();
            checkMissedTasks();
        } else {
            if (result.message.contains("increase daily hours")) {
                JOptionPane.showMessageDialog(this,
                        "❌ " + result.message + "\n\n" +
                                "To fix this:\n" +
                                "1. Click 'Update Plan' button\n" +
                                "2. Increase the 'Hours per Day' value\n" +
                                "3. Click 'Update Plan' to save\n" +
                                "4. Try rescheduling again",
                        "Need More Hours",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ " + result.message,
                        "Rescheduling Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
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

        AddTopicDialog dialog = new AddTopicDialog(this, plan.getId(), subjectsArray);
        dialog.setVisible(true);
        if (dialog.isSucceeded()) {
            loadTopics();
        }
    }

    private void generateTasks() {
        List<Topic> topics = topicDAO.findByPlanId(plan.getId());
        if (topics.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add topics first.", "No Topics", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "This will replace all existing tasks. Continue?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        studyTaskDAO.deleteByGoalId(plan.getId());
        List<StudyTask> newTasks = planGenerator.generateTasksFromTopics(plan);
        studyTaskDAO.saveAll(newTasks);

        JOptionPane.showMessageDialog(this, "Tasks generated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        loadTasks();
        checkMissedTasks();
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

            plan.setDeadline(newDeadline);
            plan.setDailyHours(newHours);
            plan.setDifficulty(difficultyEnum);
            studyPlanDAO.update(plan);

            JOptionPane.showMessageDialog(this, "Plan updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadPlanData();

            // After updating, check if missed tasks can now be rescheduled
            checkMissedTasks();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePlan() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this plan? All topics and tasks will be lost.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            studyTaskDAO.deleteByGoalId(plan.getId());
            topicDAO.deleteByPlanId(plan.getId());
            studyPlanDAO.deleteById(plan.getId());
            JOptionPane.showMessageDialog(this, "Plan deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}