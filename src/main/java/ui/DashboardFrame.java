package ui;

import model.User;
import dao.StudyPlanDAO;
import dao.StudyTaskDAO;
import dao.GoalDAO;
import service.GitHubCommitChecker;
import model.StudyPlan;
import model.StudyTask;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardFrame extends JPanel {

    private User user;
    private StudyTaskDAO studyTaskDAO;
    private GoalDAO goalDAO;
    private StudyPlanDAO studyPlanDAO;

    private JLabel totalTasksLabel;
    private JLabel completedTasksLabel;
    private JLabel progressPercentageLabel;
    private JProgressBar dailyProgressBar;
    private DefaultListModel<String> taskListModel;
    private JList<String> taskList;

    // Overall progress components
    private JLabel overallTotalLabel;
    private JLabel overallCompletedLabel;
    private JProgressBar overallProgressBar;

    // Colors
    private final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private final Color WARNING_COLOR = new Color(245, 158, 11);
    private final Color DANGER_COLOR = new Color(239, 68, 68);
    private final Color BG_LIGHT = new Color(249, 250, 251);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private final Color BORDER_COLOR = new Color(229, 231, 235);

    public DashboardFrame(User user) {
        this.user = user;
        this.studyTaskDAO = new StudyTaskDAO();
        this.goalDAO = new GoalDAO();
        this.studyPlanDAO = new StudyPlanDAO();

        setLayout(new BorderLayout());
        setBackground(BG_LIGHT);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initUI();
        refreshDashboard();
    }

    private void initUI() {
        // Main content panel that will be scrollable
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_LIGHT);

        // Welcome Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BG);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 10, 20)
        ));

        JLabel headerTitle = new JLabel("Dashboard");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerTitle.setForeground(TEXT_PRIMARY);

        JLabel welcomeLabel = new JLabel("Welcome back, " + user.getName() + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        welcomeLabel.setForeground(TEXT_SECONDARY);

        JLabel dateLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(TEXT_SECONDARY);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(CARD_BG);
        titlePanel.add(headerTitle, BorderLayout.NORTH);
        titlePanel.add(welcomeLabel, BorderLayout.SOUTH);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);

        contentPanel.add(headerPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        // Active Plan Info
        JPanel activePlanPanel = createActivePlanPanel();
        contentPanel.add(activePlanPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Stats Cards Grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 3, 20, 0));
        statsGrid.setBackground(BG_LIGHT);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        statsGrid.setBorder(new EmptyBorder(0, 20, 0, 20));

        statsGrid.add(createStatsCard("📋", "Today's Tasks", "0"));
        statsGrid.add(createStatsCard("✅", "Completed Today", "0"));
        statsGrid.add(createStatsCard("📊", "Today's Progress", "0%"));

        contentPanel.add(statsGrid);
        contentPanel.add(Box.createVerticalStrut(20));

        // Daily Progress Card
        JPanel progressCard = new JPanel(new BorderLayout());
        progressCard.setBackground(CARD_BG);
        progressCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        progressCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel progressTitle = new JLabel("Today's Progress");
        progressTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        progressTitle.setForeground(TEXT_PRIMARY);

        progressPercentageLabel = new JLabel("0%");
        progressPercentageLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        progressPercentageLabel.setForeground(PRIMARY_COLOR);
        progressPercentageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        dailyProgressBar = new JProgressBar(0, 100);
        dailyProgressBar.setStringPainted(true);
        dailyProgressBar.setForeground(PRIMARY_COLOR);
        dailyProgressBar.setBackground(new Color(224, 231, 255));
        dailyProgressBar.setBorderPainted(false);
        dailyProgressBar.setPreferredSize(new Dimension(200, 25));

        JPanel progressContent = new JPanel(new BorderLayout(0, 15));
        progressContent.setBackground(CARD_BG);
        progressContent.add(progressPercentageLabel, BorderLayout.NORTH);
        progressContent.add(dailyProgressBar, BorderLayout.CENTER);

        progressCard.add(progressTitle, BorderLayout.NORTH);
        progressCard.add(progressContent, BorderLayout.CENTER);

        contentPanel.add(progressCard);
        contentPanel.add(Box.createVerticalStrut(20));

        // Today's Tasks Card
        JPanel tasksCard = new JPanel(new BorderLayout());
        tasksCard.setBackground(CARD_BG);
        tasksCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        tasksCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JPanel tasksHeader = new JPanel(new BorderLayout());
        tasksHeader.setBackground(CARD_BG);

        JLabel tasksTitle = new JLabel("Today's Tasks");
        tasksTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
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
        taskScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        taskScroll.setPreferredSize(new Dimension(300, 200));

        tasksCard.add(tasksHeader, BorderLayout.NORTH);
        tasksCard.add(taskScroll, BorderLayout.CENTER);

        contentPanel.add(tasksCard);
        contentPanel.add(Box.createVerticalStrut(20));

        // Overall Progress Card
        JPanel overallCard = new JPanel(new BorderLayout());
        overallCard.setBackground(CARD_BG);
        overallCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        overallCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JLabel overallTitle = new JLabel("Overall Plan Progress");
        overallTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        overallTitle.setForeground(TEXT_PRIMARY);

        JPanel overallStatsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        overallStatsPanel.setBackground(CARD_BG);

        // Completed panel
        JPanel completedPanel = new JPanel();
        completedPanel.setLayout(new BoxLayout(completedPanel, BoxLayout.Y_AXIS));
        completedPanel.setBackground(CARD_BG);

        overallCompletedLabel = new JLabel("0");
        overallCompletedLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        overallCompletedLabel.setForeground(SUCCESS_COLOR);
        overallCompletedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel completedLabel = new JLabel("Completed");
        completedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        completedLabel.setForeground(TEXT_SECONDARY);
        completedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        completedPanel.add(overallCompletedLabel);
        completedPanel.add(completedLabel);

        // Total panel
        JPanel totalPanel = new JPanel();
        totalPanel.setLayout(new BoxLayout(totalPanel, BoxLayout.Y_AXIS));
        totalPanel.setBackground(CARD_BG);

        overallTotalLabel = new JLabel("0");
        overallTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        overallTotalLabel.setForeground(PRIMARY_COLOR);
        overallTotalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel totalLabel = new JLabel("Total Tasks");
        totalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        totalLabel.setForeground(TEXT_SECONDARY);
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        totalPanel.add(overallTotalLabel);
        totalPanel.add(totalLabel);

        overallStatsPanel.add(completedPanel);
        overallStatsPanel.add(totalPanel);

        overallProgressBar = new JProgressBar(0, 100);
        overallProgressBar.setStringPainted(true);
        overallProgressBar.setForeground(PRIMARY_COLOR);
        overallProgressBar.setBackground(new Color(224, 231, 255));
        overallProgressBar.setBorderPainted(false);
        overallProgressBar.setPreferredSize(new Dimension(200, 20));

        overallCard.add(overallTitle, BorderLayout.NORTH);
        overallCard.add(overallStatsPanel, BorderLayout.CENTER);
        overallCard.add(overallProgressBar, BorderLayout.SOUTH);

        contentPanel.add(overallCard);
        contentPanel.add(Box.createVerticalStrut(50));

        // Add to scroll pane
        JScrollPane mainScrollPane = new JScrollPane(contentPanel);
        mainScrollPane.setBorder(null);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(mainScrollPane, BorderLayout.CENTER);
    }

    private JPanel createActivePlanPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(10, 20, 10, 20)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel activeLabel = new JLabel("Active Plan: ");
        activeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        activeLabel.setForeground(TEXT_PRIMARY);
        panel.add(activeLabel);

        JLabel planNameLabel = new JLabel();
        planNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        planNameLabel.setForeground(PRIMARY_COLOR);

        Integer activePlanId = user.getActivePlanId();
        if (activePlanId != null) {
            StudyPlan plan = studyPlanDAO.findById(activePlanId);
            if (plan != null) {
                String displayName = plan.getSubjects() != null ? plan.getSubjects() : "Plan " + activePlanId;
                planNameLabel.setText(displayName);
            } else {
                planNameLabel.setText("None");
            }
        } else {
            planNameLabel.setText("None (select a plan)");
        }
        panel.add(planNameLabel);

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

        // Store references based on label
        if (label.equals("Today's Tasks")) {
            totalTasksLabel = valueLabel;
        } else if (label.equals("Completed Today")) {
            completedTasksLabel = valueLabel;
        } else if (label.equals("Today's Progress")) {
            progressPercentageLabel = valueLabel;
        }

        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    public void refreshDashboard() {
        System.out.println("\n=== REFRESHING DASHBOARD ===");

        Integer activePlanId = user.getActivePlanId();

        if (activePlanId == null) {
            // No active plan – show empty state
            if (totalTasksLabel != null) totalTasksLabel.setText("0");
            if (completedTasksLabel != null) completedTasksLabel.setText("0");
            if (progressPercentageLabel != null) progressPercentageLabel.setText("0%");
            if (dailyProgressBar != null) {
                dailyProgressBar.setValue(0);
                dailyProgressBar.setString("0% (0/0)");
            }
            taskListModel.clear();
            taskListModel.addElement("🎯 No active plan selected. Choose a plan from 'View My Plans'.");
            if (overallTotalLabel != null) overallTotalLabel.setText("0");
            if (overallCompletedLabel != null) overallCompletedLabel.setText("0");
            if (overallProgressBar != null) {
                overallProgressBar.setValue(0);
                overallProgressBar.setString("0%");
            }
            revalidate();
            repaint();
            return;
        }

        // Get tasks for active plan
        List<StudyTask> allPlanTasks = studyTaskDAO.findByGoalId(activePlanId);
        int totalPlanTasks = allPlanTasks.size();
        int completedPlanTasks = (int) allPlanTasks.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus())).count();
        int overallProgress = totalPlanTasks > 0 ? (completedPlanTasks * 100 / totalPlanTasks) : 0;

        // Update overall stats
        if (overallTotalLabel != null) overallTotalLabel.setText(String.valueOf(totalPlanTasks));
        if (overallCompletedLabel != null) overallCompletedLabel.setText(String.valueOf(completedPlanTasks));
        if (overallProgressBar != null) {
            overallProgressBar.setValue(overallProgress);
            overallProgressBar.setString(overallProgress + "% (" + completedPlanTasks + "/" + totalPlanTasks + ")");
        }

        // Get today's tasks
        LocalDate today = LocalDate.now();
        List<StudyTask> todayTasks = allPlanTasks.stream()
                .filter(t -> t.getTaskDate() != null && t.getTaskDate().equals(today))
                .toList();
        int totalToday = todayTasks.size();
        int completedToday = (int) todayTasks.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus())).count();
        int todayProgress = totalToday > 0 ? (completedToday * 100 / totalToday) : 0;

        if (totalTasksLabel != null) totalTasksLabel.setText(String.valueOf(totalToday));
        if (completedTasksLabel != null) completedTasksLabel.setText(String.valueOf(completedToday));
        if (progressPercentageLabel != null) progressPercentageLabel.setText(todayProgress + "%");
        if (dailyProgressBar != null) {
            dailyProgressBar.setValue(todayProgress);
            dailyProgressBar.setString(todayProgress + "% (" + completedToday + "/" + totalToday + ")");
        }

        // Update task list
        taskListModel.clear();
        for (StudyTask task : todayTasks) {
            String status = "COMPLETED".equals(task.getStatus()) ? "✅ " : "⬜ ";
            String desc = task.getDescription() != null ? task.getDescription() : "Task";
            taskListModel.addElement(status + desc);
        }
        if (todayTasks.isEmpty()) {
            taskListModel.addElement("🎉 No tasks for today! Check your study plan.");
        }

        revalidate();
        repaint();
    }

    // Add this method to DashboardFrame.java (after refreshDashboard)
    public void refresh() {
        refreshDashboard();
    }

    // Custom cell renderer for task list
    class TaskListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (c instanceof JLabel) {
                JLabel label = (JLabel) c;
                String text = value.toString();

                if (text.startsWith("✅")) {
                    label.setForeground(SUCCESS_COLOR);
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                } else if (text.startsWith("🎉") || text.startsWith("🎯")) {
                    label.setForeground(PRIMARY_COLOR);
                    label.setFont(label.getFont().deriveFont(Font.ITALIC));
                } else if (text.startsWith("⬜")) {
                    label.setForeground(TEXT_PRIMARY);
                }

                label.setBorder(new EmptyBorder(8, 10, 8, 10));
            }

            return c;
        }
    }

    public JPanel getMainPanel() {
        return this;
    }
}