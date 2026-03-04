package ui;

import model.User;
import model.Goal;
import model.DailyTask;
import dao.GoalDAO;
import dao.StudyTaskDAO;
import service.GitHubCommitChecker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class GitHubProgressFrame extends JPanel {

    private User user;
    private GoalDAO goalDAO;
    private StudyTaskDAO taskDAO;
    private GitHubCommitChecker commitChecker;
    private JTable progressTable;
    private DefaultTableModel tableModel;
    private JLabel totalCommitsLabel;
    private JLabel activeReposLabel;
    private JLabel streakLabel;
    private JLabel lastCommitLabel;
    
    // Color scheme
    private final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private final Color SUCCESS_COLOR = new Color(16, 185, 129);
    private final Color DANGER_COLOR = new Color(239, 68, 68);
    private final Color WARNING_COLOR = new Color(245, 158, 11);
    private final Color BG_LIGHT = new Color(249, 250, 251);
    private final Color BORDER_COLOR = new Color(229, 231, 235);
    private final Color CARD_BG = Color.WHITE;

    public GitHubProgressFrame(User user) {
        this.user = user;
        this.goalDAO = new GoalDAO();
        this.taskDAO = new StudyTaskDAO();
        this.commitChecker = new GitHubCommitChecker();

        setLayout(new BorderLayout(0, 20));
        setBackground(BG_LIGHT);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initUI();
        loadProgressData();
    }

    private void initUI() {
        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Stats cards
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.CENTER);

        // Table
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(20, 25, 20, 25)
        ));

        JLabel titleLabel = new JLabel("?? GitHub Progress Tracker");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(17, 24, 39));

        JLabel subtitleLabel = new JLabel("Track your study plan progress and commit activity");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(107, 114, 128));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(CARD_BG);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);

        panel.add(textPanel, BorderLayout.WEST);

        return panel;
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setBackground(BG_LIGHT);
        panel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        // Total Commits card
        JPanel commitsCard = createStatCard("?? Total Commits", "0", PRIMARY_COLOR);
        totalCommitsLabel = (JLabel) ((JPanel) commitsCard.getComponent(1)).getComponent(0);
        
        // Active Repos card
        JPanel reposCard = createStatCard("?? Active Repos", "0", SUCCESS_COLOR);
        activeReposLabel = (JLabel) ((JPanel) reposCard.getComponent(1)).getComponent(0);
        
        // Streak card
        JPanel streakCard = createStatCard("?? Current Streak", "0 days", WARNING_COLOR);
        streakLabel = (JLabel) ((JPanel) streakCard.getComponent(1)).getComponent(0);
        
        // Last Commit card
        JPanel lastCommitCard = createStatCard("?? Last Commit", "Never", new Color(139, 92, 246));
        lastCommitLabel = (JLabel) ((JPanel) lastCommitCard.getComponent(1)).getComponent(0);
        
        panel.add(commitsCard);
        panel.add(reposCard);
        panel.add(streakCard);
        panel.add(lastCommitCard);
        
        return panel;
    }
    
    private JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(107, 114, 128));
        
        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        valuePanel.setBackground(CARD_BG);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(accentColor);
        
        valuePanel.add(valueLabel);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valuePanel, BorderLayout.CENTER);
        
        return card;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel("Repository Progress");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(17, 24, 39));

        // Create table
        String[] columns = {"Repository", "Progress", "Last Commit", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        progressTable = new JTable(tableModel);
        progressTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        progressTable.setRowHeight(60);
        progressTable.setShowGrid(true);
        progressTable.setGridColor(BORDER_COLOR);
        progressTable.setSelectionBackground(new Color(239, 246, 255));
        progressTable.setRowSelectionAllowed(false);

        // Table header styling
        JTableHeader header = progressTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(243, 244, 246));
        header.setForeground(new Color(55, 65, 81));
        header.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        // Custom cell renderers
        progressTable.getColumnModel().getColumn(0).setCellRenderer(new RepositoryCellRenderer());
        progressTable.getColumnModel().getColumn(1).setCellRenderer(new ProgressCellRenderer());
        progressTable.getColumnModel().getColumn(2).setCellRenderer(new DateCellRenderer());
        progressTable.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());

        // Column widths
        progressTable.getColumnModel().getColumn(0).setPreferredWidth(300);
        progressTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        progressTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        progressTable.getColumnModel().getColumn(3).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(progressTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CARD_BG);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(CARD_BG);
        topPanel.add(titleLabel, BorderLayout.WEST);

        JButton refreshBtn = new JButton("?? Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.setBackground(CARD_BG);
        refreshBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> loadProgressData());

        topPanel.add(refreshBtn, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadProgressData() {
        // First, check GitHub for latest commits
        commitChecker.checkAndUpdateAllTasks(user);
        
        tableModel.setRowCount(0);

        // Get all goals for the user
        List<Goal> goals = goalDAO.findByUserId(user.getId());
        
        // Get all tasks for the user
        List<DailyTask> allTasks = taskDAO.findByUserId(user.getId());
        
        // Group tasks by repository
        Map<String, List<DailyTask>> tasksByRepo = new HashMap<>();
        for (DailyTask task : allTasks) {
            tasksByRepo.computeIfAbsent(task.getRepositoryName(), k -> new ArrayList<>()).add(task);
        }

        int totalCommits = 0;
        int activeRepos = 0;
        LocalDate latestCommit = null;
        int currentStreak = 0;

        // Add data for each goal/repository
        for (Goal goal : goals) {
            String repoName = goal.getRepositoryName();
            List<DailyTask> repoTasks = tasksByRepo.getOrDefault(repoName, new ArrayList<>());
            
            // Calculate progress for this repository
            int totalTasks = repoTasks.size();
            int completedTasks = 0;
            LocalDate lastCommitDate = null;
            
            for (DailyTask task : repoTasks) {
                if (task.isCompleted()) {
                    completedTasks++;
                }
                if (task.getActualCommits() > 0) {
                    totalCommits += task.getActualCommits();
                    if (lastCommitDate == null || task.getTaskDate().isAfter(lastCommitDate)) {
                        lastCommitDate = task.getTaskDate();
                    }
                }
            }
            
            // Calculate progress percentage
            int progress = totalTasks > 0 ? (completedTasks * 100 / totalTasks) : 0;
            
            // Determine status
            String status = determineStatus(repoTasks);
            if (status.equals("Green")) {
                activeRepos++;
            }
            
            // Track latest commit
            if (lastCommitDate != null) {
                if (latestCommit == null || lastCommitDate.isAfter(latestCommit)) {
                    latestCommit = lastCommitDate;
                }
            }
            
            // Add row to table
            tableModel.addRow(new Object[]{
                repoName,
                progress,
                lastCommitDate != null ? formatDate(lastCommitDate) : "Never",
                status
            });
        }
        
        // Calculate streak from tasks
        currentStreak = calculateStreak(allTasks);
        
        // Update stats
        totalCommitsLabel.setText(String.valueOf(totalCommits));
        activeReposLabel.setText(String.valueOf(activeRepos));
        streakLabel.setText("?? " + currentStreak + " days");
        
        if (latestCommit != null) {
            long daysAgo = java.time.temporal.ChronoUnit.DAYS.between(latestCommit, LocalDate.now());
            if (daysAgo == 0) {
                lastCommitLabel.setText("Today");
            } else if (daysAgo == 1) {
                lastCommitLabel.setText("Yesterday");
            } else {
                lastCommitLabel.setText(daysAgo + " days ago");
            }
        } else {
            lastCommitLabel.setText("Never");
        }
    }
    
    private String determineStatus(List<DailyTask> tasks) {
        if (tasks.isEmpty()) return "No activity";
        
        LocalDate today = LocalDate.now();
        boolean hasRecent = false;
        boolean hasDelayed = false;
        
        for (DailyTask task : tasks) {
            if (task.isCompleted() && task.getTaskDate().equals(today)) {
                return "Green"; // Active - committed today
            }
            if (task.isCompleted() && task.getTaskDate().equals(today.minusDays(1))) {
                hasRecent = true;
            }
            if (task.isMissed() && task.getTaskDate().isAfter(today.minusDays(3))) {
                hasDelayed = true;
            }
        }
        
        if (hasRecent) return "Yellow"; // Delayed - last commit yesterday
        if (hasDelayed) return "Red"; // Missed - missed recent tasks
        return "No activity";
    }

    private int calculateStreak(List<DailyTask> tasks) {
        int streak = 0;
        LocalDate today = LocalDate.now();
        
        for (int i = 0; i < 30; i++) { // Check last 30 days
            LocalDate checkDate = today.minusDays(i);
            boolean committed = false;
            
            for (DailyTask task : tasks) {
                if (task.isCompleted() && task.getTaskDate().equals(checkDate)) {
                    committed = true;
                    break;
                }
            }
            
            if (committed) {
                streak++;
            } else {
                break; // Streak broken
            }
        }
        
        return streak;
    }

    private String formatDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.equals(today)) return "Today";
        if (date.equals(today.minusDays(1))) return "Yesterday";
        
        long daysAgo = java.time.temporal.ChronoUnit.DAYS.between(date, today);
        if (daysAgo < 7) return daysAgo + " days ago";
        
        return date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }

    // Custom cell renderers
    class RepositoryCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(new EmptyBorder(10, 15, 10, 15));
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(new Color(17, 24, 39));
            setText("?? " + value.toString());
            return c;
        }
    }

    class ProgressCellRenderer extends DefaultTableCellRenderer {
        private JProgressBar progressBar;

        public ProgressCellRenderer() {
            progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            progressBar.setBorderPainted(false);
            progressBar.setFont(new Font("Segoe UI", Font.BOLD, 11));
            progressBar.setPreferredSize(new Dimension(120, 20));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            int progress = Integer.parseInt(value.toString());
            progressBar.setValue(progress);
            progressBar.setString(progress + "%");

            if (progress >= 70) {
                progressBar.setForeground(SUCCESS_COLOR);
            } else if (progress >= 30) {
                progressBar.setForeground(WARNING_COLOR);
            } else {
                progressBar.setForeground(DANGER_COLOR);
            }

            progressBar.setBackground(new Color(229, 231, 235));

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(isSelected ? table.getSelectionBackground() : CARD_BG);
            panel.add(progressBar);

            return panel;
        }
    }

    class DateCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(new EmptyBorder(10, 15, 10, 15));
            setFont(new Font("Segoe UI", Font.PLAIN, 13));

            if (value != null) {
                String date = value.toString();
                if (date.equals("Today")) {
                    setForeground(SUCCESS_COLOR);
                    setText("? " + date);
                } else if (date.equals("Yesterday")) {
                    setForeground(WARNING_COLOR);
                    setText("? " + date);
                } else if (date.contains("ago")) {
                    setForeground(new Color(107, 114, 128));
                } else {
                    setForeground(new Color(17, 24, 39));
                }
            }

            return c;
        }
    }

    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(isSelected ? table.getSelectionBackground() : CARD_BG);

            JLabel badge = new JLabel();
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setBorder(new EmptyBorder(5, 15, 5, 15));
            badge.setOpaque(true);

            String status = value.toString();
            switch (status) {
                case "Green":
                    badge.setText("? Active");
                    badge.setForeground(Color.WHITE);
                    badge.setBackground(SUCCESS_COLOR);
                    break;
                case "Yellow":
                    badge.setText("? Delayed");
                    badge.setForeground(Color.WHITE);
                    badge.setBackground(WARNING_COLOR);
                    break;
                case "Red":
                    badge.setText("? Missed");
                    badge.setForeground(Color.WHITE);
                    badge.setBackground(DANGER_COLOR);
                    break;
                default:
                    badge.setText("? No activity");
                    badge.setForeground(Color.WHITE);
                    badge.setBackground(new Color(156, 163, 175));
            }

            badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(badge.getBackground().darker(), 1, true),
                    new EmptyBorder(5, 15, 5, 15)
            ));

            panel.add(badge);
            return panel;
        }
    }

    public JPanel getMainPanel() {
        return this;
    }
}
