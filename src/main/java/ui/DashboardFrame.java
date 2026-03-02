package ui;

import model.User;
import model.DailyTask;
import dao.StudyTaskDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardFrame extends JPanel {
    
    private User user;
    private StudyTaskDAO studyTaskDAO;
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JLabel progressLabel;
    private JLabel todayTasksLabel;
    
    public DashboardFrame(User user) {
        this.user = user;
        this.studyTaskDAO = new StudyTaskDAO();
        initUI();
        loadTasks();
    }
    
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Task Table
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Footer with progress
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + user.getName() + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        todayTasksLabel = new JLabel("Loading today's tasks...");
        todayTasksLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        todayTasksLabel.setForeground(new Color(52, 152, 219));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(Color.WHITE);
        textPanel.add(welcomeLabel);
        textPanel.add(todayTasksLabel);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Your Study Tasks"));
        
        String[] columns = {"Date", "Repository/Subject", "Status", "Planned Hours", "Actual Hours"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        taskTable = new JTable(tableModel);
        taskTable.setRowHeight(25);
        
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
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(refreshBtn);
        
        panel.add(progressLabel, BorderLayout.WEST);
        panel.add(btnPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void loadTasks() {
        List<DailyTask> tasks = studyTaskDAO.findByUserId(user.getId());
        tableModel.setRowCount(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int completed = 0;
        int total = tasks.size();
        
        StringBuilder todayTasks = new StringBuilder("Today: ");
        boolean hasToday = false;
        
        for (DailyTask task : tasks) {
            tableModel.addRow(new Object[]{
                task.getTaskDate().format(formatter),
                task.getRepositoryName(),
                task.getStatusEmoji() + " " + task.getStatus(),
                task.getPlannedHours(),
                task.getActualHours()
            });
            
            if (task.isCompleted()) completed++;
            
            if (task.getTaskDate().equals(java.time.LocalDate.now())) {
                if (hasToday) todayTasks.append(", ");
                todayTasks.append(task.getRepositoryName());
                hasToday = true;
            }
        }
        
        if (hasToday) {
            todayTasksLabel.setText(todayTasks.toString());
        } else {
            todayTasksLabel.setText("No tasks scheduled for today");
        }
        
        int progress = total > 0 ? (completed * 100 / total) : 0;
        progressLabel.setText(String.format("Overall Progress: %d%% (%d/%d tasks completed)", 
            progress, completed, total));
    }
    
    public JPanel getMainPanel() {
        return this;
    }
}
