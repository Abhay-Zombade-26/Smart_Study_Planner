package ui;

import dao.StudyPlanDAO;
import dao.StudyTaskDAO;
import model.User;
import service.AnalysisService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DashboardFrame extends JPanel {

    private User user;
    private AnalysisService analysisService;
    private StudyPlanDAO studyPlanDAO;
    private StudyTaskDAO studyTaskDAO;

    public DashboardFrame(User user) {
        this.user = user;
        this.analysisService = new AnalysisService();
        this.studyPlanDAO = new StudyPlanDAO();
        this.studyTaskDAO = new StudyTaskDAO();

        initUI();
        loadDashboardData();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
    }

    private void loadDashboardData() {
        removeAll();

        Map<String, Object> stats = analysisService.getDashboardStats(user);

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Stats cards
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        contentPanel.add(createStatsPanel(stats), gbc);

        // Today's tasks
        gbc.gridy = 1;
        contentPanel.add(createTodayTasksPanel(), gbc);

        // Progress and deadline
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        contentPanel.add(createProgressPanel(stats), gbc);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel welcomeLabel = new JLabel("Welcome back, " + user.getName() + " 👋");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(33, 33, 33));

        JLabel dateLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(new Color(100, 116, 139));

        panel.add(welcomeLabel, BorderLayout.NORTH);
        panel.add(dateLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatsPanel(Map<String, Object> stats) {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setBackground(Color.WHITE);

        // Overall Progress
        panel.add(createStatCard(
                "📊",
                "Overall Progress",
                stats.get("overallProgress") + "%",
                new Color(52, 152, 219)
        ));

        // Commit Streak
        panel.add(createStatCard(
                "🔥",
                "Commit Streak",
                stats.get("commitStreak") + " days",
                new Color(46, 204, 113)
        ));

        // Today's Tasks
        panel.add(createStatCard(
                "📝",
                "Today's Tasks",
                stats.get("pendingTodayTasks") + "/" + stats.get("todayTasksCount"),
                new Color(155, 89, 182)
        ));

        // Deadline
        String daysRemaining = stats.get("daysRemaining") != null ?
                stats.get("daysRemaining") + " days" : "No deadline";
        panel.add(createStatCard(
                "⏰",
                "Next Deadline",
                daysRemaining,
                new Color(230, 126, 34)
        ));

        return panel;
    }

    private JPanel createStatCard(String icon, String label, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelLabel.setForeground(new Color(100, 116, 139));
        labelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(iconLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(labelLabel);

        return card;
    }

    private JPanel createTodayTasksPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel("Today's Tasks");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 33, 33));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));

        List<model.StudyTask> tasks = studyTaskDAO.findTodayTasks(user.getId());

        if (tasks.isEmpty()) {
            JLabel emptyLabel = new JLabel("No tasks for today. Enjoy your day! 🎉");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            emptyLabel.setForeground(new Color(148, 163, 184));
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(emptyLabel);
        } else {
            for (model.StudyTask task : tasks) {
                panel.add(createTaskItem(task));
                panel.add(Box.createVerticalStrut(10));
            }
        }

        return panel;
    }

    private JPanel createTaskItem(model.StudyTask task) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(new Color(248, 250, 252));
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel leftPanel = new JPanel(new BorderLayout(10, 0));
        leftPanel.setBackground(new Color(248, 250, 252));

        JCheckBox checkBox = new JCheckBox();
        checkBox.setBackground(new Color(248, 250, 252));
        checkBox.setSelected(task.getStatus().equals("COMPLETED"));
        checkBox.setEnabled(!task.getStatus().equals("COMPLETED"));

        checkBox.addActionListener(e -> {
            if (checkBox.isSelected()) {
                studyTaskDAO.updateStatus(task.getId(), "COMPLETED");
                loadDashboardData();
            }
        });

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(new Color(248, 250, 252));

        JLabel descLabel = new JLabel(task.getDescription());
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setForeground(new Color(33, 33, 33));

        JLabel metaLabel = new JLabel();
        if (task.isRequiredCommit()) {
            metaLabel.setText("🔨 Required commit");
            metaLabel.setForeground(new Color(46, 204, 113));
        } else {
            metaLabel.setText("📖 Study task");
            metaLabel.setForeground(new Color(52, 152, 219));
        }
        metaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        textPanel.add(descLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(metaLabel);

        leftPanel.add(checkBox, BorderLayout.WEST);
        leftPanel.add(textPanel, BorderLayout.CENTER);

        JLabel statusLabel = new JLabel();
        if (task.getStatus().equals("COMPLETED")) {
            statusLabel.setText("✓ Completed");
            statusLabel.setForeground(new Color(46, 204, 113));
        } else if (task.getStatus().equals("MISSED")) {
            statusLabel.setText("⏰ Missed");
            statusLabel.setForeground(new Color(220, 38, 38));
        } else {
            statusLabel.setText("⚡ Pending");
            statusLabel.setForeground(new Color(241, 196, 15));
        }
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        item.add(leftPanel, BorderLayout.CENTER);
        item.add(statusLabel, BorderLayout.EAST);

        return item;
    }

    private JPanel createProgressPanel(Map<String, Object> stats) {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBackground(Color.WHITE);

        // Weekly activity card
        JPanel activityCard = new JPanel();
        activityCard.setLayout(new BoxLayout(activityCard, BoxLayout.Y_AXIS));
        activityCard.setBackground(Color.WHITE);
        activityCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel activityTitle = new JLabel("Weekly Activity Summary");
        activityTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        activityTitle.setForeground(new Color(33, 33, 33));
        activityTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        activityCard.add(activityTitle);
        activityCard.add(Box.createVerticalStrut(15));

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        JPanel daysPanel = new JPanel(new GridLayout(1, 7, 5, 0));
        daysPanel.setBackground(Color.WHITE);

        for (String day : days) {
            JPanel dayCard = new JPanel();
            dayCard.setLayout(new BoxLayout(dayCard, BoxLayout.Y_AXIS));
            dayCard.setBackground(new Color(248, 250, 252));
            dayCard.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true));

            JLabel dayLabel = new JLabel(day);
            dayLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dayLabel.setForeground(new Color(100, 116, 139));
            dayLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel activityIndicator = new JLabel("●");
            activityIndicator.setFont(new Font("Segoe UI", Font.PLAIN, 20));
            activityIndicator.setForeground(Math.random() > 0.3 ? new Color(46, 204, 113) : new Color(226, 232, 240));
            activityIndicator.setAlignmentX(Component.CENTER_ALIGNMENT);

            dayCard.add(Box.createVerticalStrut(10));
            dayCard.add(dayLabel);
            dayCard.add(Box.createVerticalStrut(5));
            dayCard.add(activityIndicator);
            dayCard.add(Box.createVerticalStrut(10));

            daysPanel.add(dayCard);
        }

        activityCard.add(daysPanel);

        // Deadline countdown card
        JPanel deadlineCard = new JPanel();
        deadlineCard.setLayout(new BoxLayout(deadlineCard, BoxLayout.Y_AXIS));
        deadlineCard.setBackground(Color.WHITE);
        deadlineCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel deadlineTitle = new JLabel("Upcoming Deadline");
        deadlineTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        deadlineTitle.setForeground(new Color(33, 33, 33));
        deadlineTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        deadlineCard.add(deadlineTitle);
        deadlineCard.add(Box.createVerticalStrut(15));

        if (stats.get("nearestDeadline") != null) {
            LocalDate deadline = (LocalDate) stats.get("nearestDeadline");
            long daysRemaining = (long) stats.get("daysRemaining");

            JLabel deadlineValueLabel = new JLabel(deadline.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            deadlineValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            deadlineValueLabel.setForeground(new Color(230, 126, 34));
            deadlineValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel daysLabel = new JLabel(daysRemaining + " days remaining");
            daysLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            daysLabel.setForeground(new Color(100, 116, 139));
            daysLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setValue((int) Math.min(100, (30 - daysRemaining) * 100 / 30));
            progressBar.setStringPainted(true);
            progressBar.setForeground(new Color(46, 204, 113));
            progressBar.setBackground(new Color(226, 232, 240));
            progressBar.setBorderPainted(false);
            progressBar.setPreferredSize(new Dimension(200, 15));
            progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);

            deadlineCard.add(deadlineValueLabel);
            deadlineCard.add(Box.createVerticalStrut(10));
            deadlineCard.add(daysLabel);
            deadlineCard.add(Box.createVerticalStrut(15));
            deadlineCard.add(progressBar);
        } else {
            JLabel noDeadlineLabel = new JLabel("No active goals");
            noDeadlineLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            noDeadlineLabel.setForeground(new Color(148, 163, 184));
            noDeadlineLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            deadlineCard.add(noDeadlineLabel);
        }

        panel.add(activityCard);
        panel.add(deadlineCard);

        return panel;
    }

    public JPanel getMainPanel() {
        return this;
    }
}