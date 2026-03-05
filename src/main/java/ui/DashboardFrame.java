package ui;

import dao.StudyPlanDAO;
import dao.StudyTaskDAO;
import model.User;
import model.StudyTask;
import service.AnalysisService;

import javax.swing.*;
        import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
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

    private JLabel welcomeLabel;
    private JLabel dateLabel;
    private JLabel totalTasksLabel;
    private JLabel completedTasksLabel;
    private JProgressBar dailyProgressBar;
    private DefaultListModel<String> taskListModel;
    private JList<String> taskList;

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

        // Top welcome section
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        welcomeLabel = new JLabel("Welcome, " + user.getName());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(33, 33, 33));

        dateLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(new Color(100, 116, 139));

        topPanel.add(welcomeLabel, BorderLayout.NORTH);
        topPanel.add(dateLabel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Stats cards panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        // Total Tasks Card
        JPanel totalTasksCard = createStatCard(
                "📋",
                "Total Tasks",
                "0",
                new Color(52, 152, 219)
        );
        totalTasksLabel = (JLabel) ((JPanel)totalTasksCard.getComponent(1)).getComponent(0);

        // Completed Tasks Card
        JPanel completedTasksCard = createStatCard(
                "✅",
                "Completed",
                "0",
                new Color(46, 204, 113)
        );
        completedTasksLabel = (JLabel) ((JPanel)completedTasksCard.getComponent(1)).getComponent(0);

        // Progress Card
        JPanel progressCard = createStatCard(
                "📊",
                "Daily Progress",
                "0%",
                new Color(155, 89, 182)
        );

        statsPanel.add(totalTasksCard);
        statsPanel.add(completedTasksCard);
        statsPanel.add(progressCard);
        add(statsPanel, BorderLayout.CENTER);

        // Progress bar and tasks section
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 20));
        bottomPanel.setBackground(Color.WHITE);

        // Progress bar panel
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBackground(Color.WHITE);
        progressPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                "Daily Progress",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14)
        ));

        dailyProgressBar = new JProgressBar(0, 100);
        dailyProgressBar.setStringPainted(true);
        dailyProgressBar.setForeground(new Color(52, 152, 219));
        dailyProgressBar.setBackground(new Color(226, 232, 240));
        dailyProgressBar.setPreferredSize(new Dimension(200, 30));
        dailyProgressBar.setBorder(new EmptyBorder(10, 10, 10, 10));

        progressPanel.add(dailyProgressBar, BorderLayout.CENTER);

        // Today's tasks list panel
        JPanel tasksPanel = new JPanel(new BorderLayout());
        tasksPanel.setBackground(Color.WHITE);
        tasksPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                "Today's Task List",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14)
        ));

        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        taskList.setBackground(new Color(248, 250, 252));
        taskList.setBorder(new EmptyBorder(10, 10, 10, 10));
        taskList.setCellRenderer(new TaskListCellRenderer());

        JScrollPane taskScrollPane = new JScrollPane(taskList);
        taskScrollPane.setBorder(null);
        taskScrollPane.setPreferredSize(new Dimension(400, 180));

        tasksPanel.add(taskScrollPane, BorderLayout.CENTER);

        bottomPanel.add(progressPanel, BorderLayout.NORTH);
        bottomPanel.add(tasksPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshDashboardData();
        revalidate();
        repaint();
    }

    private JPanel createStatCard(String icon, String label, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 32));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(color);

        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelLabel.setForeground(new Color(100, 116, 139));

        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(labelLabel);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private void refreshDashboardData() {
        List<StudyTask> todayTasks = studyTaskDAO.findTodayTasks(user.getId());
        int totalToday = todayTasks.size();
        int completedToday = (int) todayTasks.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .count();

        totalTasksLabel.setText(String.valueOf(totalToday));
        completedTasksLabel.setText(String.valueOf(completedToday));

        int progress = totalToday > 0 ? (completedToday * 100 / totalToday) : 0;
        dailyProgressBar.setValue(progress);
        dailyProgressBar.setString(progress + "% (" + completedToday + " of " + totalToday + " tasks completed)");

        taskListModel.clear();
        for (StudyTask task : todayTasks) {
            String status = "COMPLETED".equals(task.getStatus()) ? "✅ " : "⬜ ";
            taskListModel.addElement(status + task.getDescription());
        }

        if (todayTasks.isEmpty()) {
            taskListModel.addElement("🎉 No tasks for today! Enjoy your day.");
        }
    }

    // Custom cell renderer for task list
    class TaskListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            String text = value.toString();
            if (text.startsWith("✅")) {
                setForeground(new Color(46, 204, 113));
                setFont(getFont().deriveFont(Font.BOLD));
            } else if (text.startsWith("🎉")) {
                setForeground(new Color(52, 152, 219));
                setFont(getFont().deriveFont(Font.ITALIC));
            } else if (text.startsWith("⬜")) {
                setForeground(Color.BLACK);
            }

            return c;
        }
    }

    public JPanel getMainPanel() {
        return this;
    }
}
