package ui;

import model.User;
import model.DailyTask;
import dao.StudyTaskDAO;
import dao.GoalDAO;
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

public class DashboardFrame extends JPanel {
    
    private User user;
    private StudyTaskDAO studyTaskDAO;
    private GoalDAO goalDAO;
    private JPanel statsPanel;
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JLabel progressLabel;
    private JLabel todayTasksLabel;
    private JLabel completedLabel;
    private JLabel streakLabel;
    
    // Color scheme
    private final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private final Color SUCCESS_COLOR = new Color(16, 185, 129);
    private final Color DANGER_COLOR = new Color(239, 68, 68);
    private final Color WARNING_COLOR = new Color(245, 158, 11);
    private final Color BG_LIGHT = new Color(249, 250, 251);
    private final Color BORDER_COLOR = new Color(229, 231, 235);
    private final Color CARD_BG = Color.WHITE;
    
    public DashboardFrame(User user) {
        this.user = user;
        this.studyTaskDAO = new StudyTaskDAO();
        this.goalDAO = new GoalDAO();
        setLayout(new BorderLayout(0, 20));
        setBackground(BG_LIGHT);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initUI();
        loadTasks();
    }
    
    private void initUI() {
        // Header with welcome message
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Stats cards
        statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.CENTER);
        
        // Tasks table
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
        
        JLabel welcomeLabel = new JLabel("Welcome back, " + user.getName() + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(17, 24, 39));
        
        JLabel dateLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(new Color(107, 114, 128));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(CARD_BG);
        textPanel.add(welcomeLabel);
        textPanel.add(dateLabel);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setBackground(BG_LIGHT);
        panel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        // Today's tasks card
        JPanel todayCard = createStatCard("?? Today's Tasks", "0", new Color(59, 130, 246));
        todayTasksLabel = (JLabel) ((JPanel) todayCard.getComponent(1)).getComponent(0);
        
        // Completed tasks card
        JPanel completedCard = createStatCard("? Completed", "0", SUCCESS_COLOR);
        completedLabel = (JLabel) ((JPanel) completedCard.getComponent(1)).getComponent(0);
        
        // Progress card
        JPanel progressCard = createStatCard("?? Progress", "0%", WARNING_COLOR);
        progressLabel = (JLabel) ((JPanel) progressCard.getComponent(1)).getComponent(0);
        
        // Streak card
        JPanel streakCard = createStatCard("?? Streak", "0 days", PRIMARY_COLOR);
        streakLabel = (JLabel) ((JPanel) streakCard.getComponent(1)).getComponent(0);
        
        panel.add(todayCard);
        panel.add(completedCard);
        panel.add(progressCard);
        panel.add(streakCard);
        
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
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
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
        
        JLabel titleLabel = new JLabel("Recent Tasks");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(17, 24, 39));
        
        // Create table
        String[] columns = {"Date", "Repository", "Status", "Planned", "Actual", "Commits"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        taskTable = new JTable(tableModel);
        taskTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        taskTable.setRowHeight(40);
        taskTable.setShowGrid(true);
        taskTable.setGridColor(BORDER_COLOR);
        taskTable.setSelectionBackground(new Color(239, 246, 255));
        taskTable.setRowSelectionAllowed(false);
        
        // Table header styling
        JTableHeader header = taskTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(243, 244, 246));
        header.setForeground(new Color(55, 65, 81));
        header.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        // Custom cell renderer for status
        taskTable.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());
        
        // Column widths
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        taskTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        taskTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        taskTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        
        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CARD_BG);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(CARD_BG);
        topPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(CARD_BG);
        
        JButton refreshBtn = new JButton("?? Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.setBackground(CARD_BG);
        refreshBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> loadTasks());
        
        JButton deletePlanBtn = new JButton("??? Delete Plan");
        deletePlanBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deletePlanBtn.setBackground(DANGER_COLOR);
        deletePlanBtn.setForeground(Color.WHITE);
        deletePlanBtn.setFocusPainted(false);
        deletePlanBtn.setBorder(BorderFactory.createLineBorder(DANGER_COLOR.darker()));
        deletePlanBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deletePlanBtn.addActionListener(e -> deletePlan());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(deletePlanBtn);
        
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
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
            badge.setBorder(new EmptyBorder(5, 12, 5, 12));
            badge.setOpaque(true);
            
            String text = value.toString();
            
            // Check based on the actual status string, not just emojis
            if (text.contains("?") || text.contains("COMPLETED")) {
                badge.setText("? Completed");
                badge.setForeground(Color.WHITE);
                badge.setBackground(SUCCESS_COLOR);
            } else if (text.contains("?") || text.contains("MISSED")) {
                badge.setText("? Missed");
                badge.setForeground(Color.WHITE);
                badge.setBackground(DANGER_COLOR);
            } else if (text.contains("?") || text.contains("PENDING") || text.contains("??")) {
                if (text.contains("??")) {
                    badge.setText("?? Overdue");
                } else {
                    badge.setText("? Pending");
                }
                badge.setForeground(Color.WHITE);
                badge.setBackground(WARNING_COLOR);
            } else {
                badge.setText("?? " + value.toString());
                badge.setForeground(Color.WHITE);
                badge.setBackground(new Color(107, 114, 128));
            }
            
            badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(badge.getBackground().darker(), 1, true),
                new EmptyBorder(5, 12, 5, 12)
            ));
            
            panel.add(badge);
            return panel;
        }
    }
    
    private void loadTasks() {
        // Check GitHub commits first
        checkGitHubCommits();
        
        List<DailyTask> tasks = studyTaskDAO.findByUserId(user.getId());
        tableModel.setRowCount(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        int completed = 0;
        int total = tasks.size();
        int streak = 0;
        int todayCount = 0;
        
        // Sort tasks by date (most recent first for display)
        tasks.sort((t1, t2) -> t2.getTaskDate().compareTo(t1.getTaskDate()));
        
        for (DailyTask task : tasks) {
            // Only show recent tasks (last 30 days)
            if (task.getTaskDate().isAfter(today.minusDays(30))) {
                tableModel.addRow(new Object[]{
                    task.getTaskDate().format(formatter),
                    task.getRepositoryName(),
                    task.getStatusEmoji() + " " + task.getStatus(),
                    task.getPlannedHours() + "h",
                    task.getActualHours() + "h",
                    task.getActualCommits() + "/" + task.getPlannedCommits()
                });
            }
            
            if (task.isCompleted()) {
                completed++;
                if (task.getTaskDate().equals(today.minusDays(streak))) {
                    streak++;
                }
            }
            
            if (task.getTaskDate().equals(today)) {
                todayCount++;
            }
        }
        
        // Update stat cards
        todayTasksLabel.setText(String.valueOf(todayCount));
        completedLabel.setText(String.valueOf(completed));
        streakLabel.setText(streak + " days");
        
        int progress = total > 0 ? (completed * 100 / total) : 0;
        progressLabel.setText(progress + "%");
    }
    
    private void checkGitHubCommits() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                GitHubCommitChecker checker = new GitHubCommitChecker();
                checker.checkAndUpdateAllTasks(user);
                return null;
            }
            
            @Override
            protected void done() {
                refreshDisplay();
            }
        };
        worker.execute();
    }
    
    private void refreshDisplay() {
        List<DailyTask> tasks = studyTaskDAO.findByUserId(user.getId());
        tableModel.setRowCount(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        int completed = 0;
        int total = tasks.size();
        int todayCount = 0;
        
        // Sort tasks by date (most recent first for display)
        tasks.sort((t1, t2) -> t2.getTaskDate().compareTo(t1.getTaskDate()));
        
        for (DailyTask task : tasks) {
            if (task.getTaskDate().isAfter(today.minusDays(30))) {
                tableModel.addRow(new Object[]{
                    task.getTaskDate().format(formatter),
                    task.getRepositoryName(),
                    task.getStatusEmoji() + " " + task.getStatus(),
                    task.getPlannedHours() + "h",
                    task.getActualHours() + "h",
                    task.getActualCommits() + "/" + task.getPlannedCommits()
                });
            }
            
            if (task.isCompleted()) completed++;
            if (task.getTaskDate().equals(today)) todayCount++;
        }
        
        todayTasksLabel.setText(String.valueOf(todayCount));
        completedLabel.setText(String.valueOf(completed));
        
        int progress = total > 0 ? (completed * 100 / total) : 0;
        progressLabel.setText(progress + "%");
    }
    
    private void deletePlan() {
        // Ask for confirmation
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete ALL your study plans?\n\n" +
            "This will permanently remove:\n" +
            "? All your goals\n" +
            "? All your daily tasks\n" +
            "? All progress data\n\n" +
            "This action cannot be undone!",
            "Delete Plan",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Delete in background
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    // Delete all tasks for user
                    studyTaskDAO.deleteByUserId(user.getId());
                    
                    // Delete all goals for user
                    goalDAO.deleteByUserId(user.getId());
                    
                    return null;
                }
                
                @Override
                protected void done() {
                    JOptionPane.showMessageDialog(DashboardFrame.this,
                        "? All plans deleted successfully!",
                        "Plan Deleted",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    // Refresh the display
                    loadTasks();
                }
            };
            worker.execute();
        }
    }
    
    public JPanel getMainPanel() {
        return this;
    }
}
