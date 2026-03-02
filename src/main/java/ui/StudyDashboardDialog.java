package ui;

import model.User;
import model.DailyTask;
import dao.StudyTaskDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudyDashboardDialog extends JDialog {
    
    private User user;
    private StudyTaskDAO taskDAO;
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JLabel todayTaskLabel;
    private JLabel progressLabel;
    
    public StudyDashboardDialog(JFrame parent, User user) {
        super(parent, "Study Dashboard", true);
        this.user = user;
        this.taskDAO = new StudyTaskDAO();
        
        setSize(800, 600);
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
        
        // Progress summary
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("?? Your Study Dashboard");
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
        taskTable.setRowHeight(25);
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        taskTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        taskTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        taskTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        
        JScrollPane scrollPane = new JScrollPane(taskTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        progressLabel = new JLabel("Overall Progress: 0%");
        progressLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JButton refreshBtn = new JButton("?? Refresh");
        refreshBtn.addActionListener(e -> loadTasks());
        
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(refreshBtn);
        btnPanel.add(closeBtn);
        
        panel.add(progressLabel, BorderLayout.WEST);
        panel.add(btnPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void loadTasks() {
        List<DailyTask> tasks = taskDAO.findByUserId(user.getId());
        tableModel.setRowCount(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        int completed = 0;
        int total = tasks.size();
        
        StringBuilder todayTasks = new StringBuilder("Today: ");
        boolean hasTodayTask = false;
        
        for (DailyTask task : tasks) {
            tableModel.addRow(new Object[]{
                task.getTaskDate().format(formatter),
                task.getRepositoryName(),
                task.getStatusEmoji() + " " + task.getStatus(),
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
}

