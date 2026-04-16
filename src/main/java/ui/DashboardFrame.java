package ui;

import dao.StudyPlanDAO;
import dao.StudyTaskDAO;
import dao.UserDAO;
import model.User;
import model.StudyPlan;
import model.StudyTask;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardFrame extends JPanel {

    private User user;
    private StudyTaskDAO studyTaskDAO;
    private StudyPlanDAO studyPlanDAO;

    private JLabel welcomeLabel;
    private JLabel dateLabel;
    private JLabel progressPercentageLabel;
    private JProgressBar dailyProgressBar;
    private JLabel streakLabel; // 🔥 New streak label

    // Colors
    private final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private final Color WARNING_COLOR = new Color(245, 158, 11);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private final Color BORDER_COLOR = new Color(229, 231, 235);

    public DashboardFrame(User user) {
        this.user = user;
        this.studyTaskDAO = new StudyTaskDAO();
        this.studyPlanDAO = new StudyPlanDAO();

        initUI();
        refreshDashboard();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Main content panel (scrollable)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        // Welcome Header
        JPanel headerPanel = createHeaderPanel();
        contentPanel.add(headerPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        // Active Plan Info
        JPanel activePlanPanel = createActivePlanPanel();
        contentPanel.add(activePlanPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        // Streak Panel 🔥
        JPanel streakPanel = createStreakPanel();
        contentPanel.add(streakPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Daily Progress Card
        JPanel progressCard = createDailyProgressCard();
        contentPanel.add(progressCard);
        contentPanel.add(Box.createVerticalStrut(50));

        // Scroll pane
        JScrollPane mainScrollPane = new JScrollPane(contentPanel);
        mainScrollPane.setBorder(null);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(mainScrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        headerPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

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

        return headerPanel;
    }

    private JPanel createActivePlanPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
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
                String displayName = plan.getSubjects() != null ? plan.getSubjects() : plan.getSubjectName();
                planNameLabel.setText(displayName != null ? displayName : "Plan " + activePlanId);
            } else {
                planNameLabel.setText("Plan " + activePlanId + " (not found)");
            }
        } else {
            planNameLabel.setText("None (select a plan)");
        }
        panel.add(planNameLabel);

        return panel;
    }

    // 🔥 NEW: Streak panel
    private JPanel createStreakPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(10, 20, 10, 20)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        streakLabel = new JLabel("🔥 Current Streak: 0 days");
        streakLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        streakLabel.setForeground(WARNING_COLOR);
        panel.add(streakLabel);

        return panel;
    }

    private JPanel createDailyProgressCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel title = new JLabel("Daily Progress");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_PRIMARY);

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

        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setBackground(CARD_BG);
        content.add(progressPercentageLabel, BorderLayout.NORTH);
        content.add(dailyProgressBar, BorderLayout.CENTER);

        card.add(title, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    public void refreshDashboard() {
        System.out.println("\n=== REFRESHING DASHBOARD ===");

        // Get latest active plan from database
        UserDAO userDAO = new UserDAO();
        User latestUser = userDAO.findById(user.getId());
        if (latestUser != null) {
            user.setActivePlanId(latestUser.getActivePlanId());
        }

        Integer activePlanId = user.getActivePlanId();

        if (activePlanId == null) {
            // No active plan – show empty state
            progressPercentageLabel.setText("0%");
            dailyProgressBar.setValue(0);
            dailyProgressBar.setString("0% (0/0)");
            streakLabel.setText("🔥 No active plan selected");
            revalidate();
            repaint();
            return;
        }

        // Get today's tasks for active plan (just for progress calculation)
        List<StudyTask> todayTasks = studyTaskDAO.findTodayTasksByPlan(activePlanId);

        int totalToday = todayTasks.size();
        int completedToday = (int) todayTasks.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .count();
        int todayProgress = totalToday > 0 ? (completedToday * 100 / totalToday) : 0;

        // Update UI
        progressPercentageLabel.setText(todayProgress + "%");
        dailyProgressBar.setValue(todayProgress);
        dailyProgressBar.setString(todayProgress + "% (" + completedToday + "/" + totalToday + ")");

        // Update streak
        int streak = studyTaskDAO.calculateStreak(activePlanId);
        streakLabel.setText("🔥 Current Streak: " + streak + " day" + (streak != 1 ? "s" : ""));

        revalidate();
        repaint();
    }

    public void refresh() {
        refreshDashboard();
    }

    public JPanel getMainPanel() {
        return this;
    }
}