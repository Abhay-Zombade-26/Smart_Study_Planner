package ui;

import model.User;
import model.DailyTask;
import model.Goal;
import dao.StudyTaskDAO;
import dao.GoalDAO;
import db.DBConnection;
import service.GitHubCommitChecker;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudyDashboardDialog extends JDialog {
    
    private User user;
    private StudyTaskDAO taskDAO;
    private GoalDAO goalDAO;
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JLabel todayTaskLabel;
    private JLabel progressLabel;
    
    // Color scheme
    private final Color SUCCESS_COLOR = new Color(16, 185, 129);
    private final Color DANGER_COLOR = new Color(239, 68, 68);
    private final Color WARNING_COLOR = new Color(245, 158, 11);
    private final Color CARD_BG = Color.WHITE;
    private final Color BORDER_COLOR = new Color(229, 231, 235);
    
    public StudyDashboardDialog(JFrame parent, User user) {
        super(parent, "Study Dashboard", true);
        this.user = user;
        this.taskDAO = new StudyTaskDAO();
        this.goalDAO = new GoalDAO();
        
        setSize(900, 650);
        setLocationRelativeTo(parent);
        
        initUI();
        loadTasks();
    }
    
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Header with today's focus
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Task table
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
        // Progress summary and buttons
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("Your Study Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        todayTaskLabel = new JLabel("Loading today's tasks...");
        todayTaskLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        todayTaskLabel.setForeground(new Color(52, 152, 219));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(Color.WHITE);
        textPanel.add(titleLabel);
        textPanel.add(todayTaskLabel);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Your Study Schedule"));
        
        String[] columns = {"Date", "Repository", "Status", "Planned Hours", "Actual Hours", "Commits"};
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
        
        // Custom cell renderer for status
        taskTable.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());
        
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        taskTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        taskTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        taskTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        
        JScrollPane scrollPane = new JScrollPane(taskTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            
            JLabel badge = new JLabel();
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setBorder(new EmptyBorder(5, 12, 5, 12));
            badge.setOpaque(true);
            
            String text = value.toString();
            
            if (text.contains("COMPLETED")) {
                badge.setText("Completed");
                badge.setForeground(Color.WHITE);
                badge.setBackground(SUCCESS_COLOR);
            } else if (text.contains("MISSED")) {
                badge.setText("Missed");
                badge.setForeground(Color.WHITE);
                badge.setBackground(DANGER_COLOR);
            } else if (text.contains("PENDING")) {
                badge.setText("Pending");
                badge.setForeground(Color.WHITE);
                badge.setBackground(WARNING_COLOR);
            } else {
                badge.setText(text);
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
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        progressLabel = new JLabel("Overall Progress: 0%");
        progressLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.setBackground(Color.WHITE);
        refreshBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> loadTasks());
        
        // DELETE PLAN BUTTON
        JButton deletePlanBtn = new JButton("Delete Plan");
        deletePlanBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deletePlanBtn.setBackground(DANGER_COLOR);
        deletePlanBtn.setForeground(Color.WHITE);
        deletePlanBtn.setFocusPainted(false);
        deletePlanBtn.setBorder(BorderFactory.createLineBorder(DANGER_COLOR.darker()));
        deletePlanBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deletePlanBtn.addActionListener(e -> deletePlan());
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.setBackground(Color.WHITE);
        closeBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(deletePlanBtn);
        buttonPanel.add(closeBtn);
        
        panel.add(progressLabel, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void loadTasks() {
        checkGitHubCommits();
        
        List<DailyTask> tasks = taskDAO.findByUserId(user.getId());
        tableModel.setRowCount(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        int completed = 0;
        int total = tasks.size();
        
        tasks.sort((t1, t2) -> t2.getTaskDate().compareTo(t1.getTaskDate()));
        
        StringBuilder todayTasks = new StringBuilder("Today: ");
        boolean hasTodayTask = false;
        
        for (DailyTask task : tasks) {
            tableModel.addRow(new Object[]{
                task.getTaskDate().format(formatter),
                task.getRepositoryName(),
                task.getStatus(),
                task.getPlannedHours(),
                task.getActualHours(),
                task.getActualCommits() + "/" + task.getPlannedCommits()
            });
            
            if (task.isCompleted()) completed++;
            
            if (task.getTaskDate().equals(today)) {
                if (hasTodayTask) todayTasks.append(", ");
                todayTasks.append(task.getRepositoryName());
                hasTodayTask = true;
            }
        }
        
        if (hasTodayTask) {
            todayTaskLabel.setText(todayTasks.toString());
        } else {
            todayTaskLabel.setText("No tasks scheduled for today");
        }
        
        int progress = total > 0 ? (completed * 100 / total) : 0;
        progressLabel.setText(String.format("Overall Progress: %d%% (%d/%d tasks completed)", 
            progress, completed, total));
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
        List<DailyTask> tasks = taskDAO.findByUserId(user.getId());
        tableModel.setRowCount(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int completed = 0;
        int total = tasks.size();
        
        tasks.sort((t1, t2) -> t2.getTaskDate().compareTo(t1.getTaskDate()));
        
        for (DailyTask task : tasks) {
            tableModel.addRow(new Object[]{
                task.getTaskDate().format(formatter),
                task.getRepositoryName(),
                task.getStatus(),
                task.getPlannedHours(),
                task.getActualHours(),
                task.getActualCommits() + "/" + task.getPlannedCommits()
            });
            
            if (task.isCompleted()) completed++;
        }
        
        int progress = total > 0 ? (completed * 100 / total) : 0;
        progressLabel.setText(String.format("Overall Progress: %d%% (%d/%d tasks completed)", 
            progress, completed, total));
    }
    
    private void deletePlan() {
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
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    taskDAO.deleteByUserId(user.getId());
                    goalDAO.deleteByUserId(user.getId());
                    return null;
                }
                
                @Override
                protected void done() {
                    JOptionPane.showMessageDialog(StudyDashboardDialog.this,
                        "All plans deleted successfully!",
                        "Plan Deleted",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadTasks();
                }
            };
            worker.execute();
        }
    }
}
