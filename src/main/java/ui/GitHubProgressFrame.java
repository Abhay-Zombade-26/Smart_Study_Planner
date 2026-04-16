package ui;

import model.User;
import model.StudyPlan;
import model.StudyTask;
import dao.StudyPlanDAO;
import dao.StudyTaskDAO;
import service.GitHubCommitChecker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class GitHubProgressFrame extends JPanel {

    private User user;
    private StudyPlanDAO planDAO;
    private StudyTaskDAO taskDAO;
    private GitHubCommitChecker commitChecker;

    // UI Components
    private JLabel planNameLabel;
    private JProgressBar overallProgressBar;
    private JLabel progressPercentageLabel;
    private JLabel completedTasksLabel;
    private JLabel pendingTasksLabel;
    private JLabel missedTasksLabel;
    private JLabel totalTasksLabel;
    private JLabel streakLabel;
    private JLabel lastCommitLabel;
    private JPanel mainContentPanel;
    private JPanel chartsPanel;

    // Color scheme
    private final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private final Color DANGER_COLOR = new Color(239, 68, 68);
    private final Color WARNING_COLOR = new Color(245, 158, 11);
    private final Color BG_LIGHT = new Color(249, 250, 251);
    private final Color BORDER_COLOR = new Color(229, 231, 235);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private final Color TEXT_SECONDARY = new Color(107, 114, 128);

    public GitHubProgressFrame(User user) {
        this.user = user;
        this.planDAO = new StudyPlanDAO();
        this.taskDAO = new StudyTaskDAO();
        this.commitChecker = new GitHubCommitChecker();

        setLayout(new BorderLayout());
        setBackground(BG_LIGHT);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initUI();
        loadProgressData();

        // Auto-refresh every 30 seconds
        new Timer(30000, e -> loadProgressData()).start();
    }

    private void initUI() {
        mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setBackground(BG_LIGHT);

        // Header
        JPanel headerPanel = createHeaderPanel();
        mainContentPanel.add(headerPanel);
        mainContentPanel.add(Box.createVerticalStrut(20));

        // Stats Dashboard
        JPanel statsDashboard = createStatsDashboard();
        mainContentPanel.add(statsDashboard);
        mainContentPanel.add(Box.createVerticalStrut(20));

        // Charts Section
        chartsPanel = createChartsSection();
        mainContentPanel.add(chartsPanel);

        JScrollPane scrollPane = new JScrollPane(mainContentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 25, 20, 25)
        ));

        JPanel leftPanel = new JPanel(new GridLayout(2, 1));
        leftPanel.setBackground(CARD_BG);

        JLabel titleLabel = new JLabel("📊 GitHub Progress Tracker");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Track your study plan progress and commit activity in real-time");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);

        leftPanel.add(titleLabel);
        leftPanel.add(subtitleLabel);

        JButton refreshBtn = new JButton("🔄 Refresh Progress");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        refreshBtn.setBackground(PRIMARY_COLOR);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setBorderPainted(false);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setPreferredSize(new Dimension(160, 40));
        refreshBtn.addActionListener(e -> {
            refreshBtn.setText("⏳ Refreshing...");
            refreshBtn.setEnabled(false);

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    commitChecker.checkAndUpdateAllTasks(user);
                    return null;
                }

                @Override
                protected void done() {
                    loadProgressData();
                    refreshBtn.setText("🔄 Refresh Progress");
                    refreshBtn.setEnabled(true);
                }
            };
            worker.execute();
        });

        refreshBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                refreshBtn.setBackground(PRIMARY_COLOR.darker());
            }
            public void mouseExited(MouseEvent evt) {
                refreshBtn.setBackground(PRIMARY_COLOR);
            }
        });

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(refreshBtn, BorderLayout.EAST);

        return panel;
    }

    private JPanel createStatsDashboard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_LIGHT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JPanel planCard = createPlanCard();
        panel.add(planCard, gbc);

        gbc.gridy = 1;
        JPanel progressCard = createProgressCard();
        panel.add(progressCard, gbc);

        gbc.gridy = 2;
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        JPanel completedCard = createStatCard("✅ Completed Tasks", "0", SUCCESS_COLOR);
        completedTasksLabel = (JLabel) ((JPanel) completedCard.getComponent(1)).getComponent(0);
        panel.add(completedCard, gbc);

        gbc.gridx = 1;
        JPanel pendingCard = createStatCard("⏳ Pending Tasks", "0", WARNING_COLOR);
        pendingTasksLabel = (JLabel) ((JPanel) pendingCard.getComponent(1)).getComponent(0);
        panel.add(pendingCard, gbc);

        gbc.gridy = 3;

        gbc.gridx = 0;
        JPanel missedCard = createStatCard("❌ Missed Tasks", "0", DANGER_COLOR);
        missedTasksLabel = (JLabel) ((JPanel) missedCard.getComponent(1)).getComponent(0);
        panel.add(missedCard, gbc);

        gbc.gridx = 1;
        JPanel totalCard = createStatCard("📋 Total Tasks", "0", PRIMARY_COLOR);
        totalTasksLabel = (JLabel) ((JPanel) totalCard.getComponent(1)).getComponent(0);
        panel.add(totalCard, gbc);

        gbc.gridy = 4;

        gbc.gridx = 0;
        JPanel streakCard = createStatCard("🔥 Current Streak", "0 days", WARNING_COLOR);
        streakLabel = (JLabel) ((JPanel) streakCard.getComponent(1)).getComponent(0);
        panel.add(streakCard, gbc);

        gbc.gridx = 1;
        JPanel lastCommitCard = createStatCard("📅 Last Commit", "Never", new Color(139, 92, 246));
        lastCommitLabel = (JLabel) ((JPanel) lastCommitCard.getComponent(1)).getComponent(0);
        panel.add(lastCommitCard, gbc);

        return panel;
    }

    private JPanel createPlanCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel("Active Study Plan");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_SECONDARY);

        planNameLabel = new JLabel("Loading...");
        planNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        planNameLabel.setForeground(PRIMARY_COLOR);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(planNameLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createProgressCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel("Overall Progress");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_SECONDARY);

        progressPercentageLabel = new JLabel("0%");
        progressPercentageLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        progressPercentageLabel.setForeground(PRIMARY_COLOR);
        progressPercentageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        overallProgressBar = new JProgressBar(0, 100);
        overallProgressBar.setStringPainted(true);
        overallProgressBar.setForeground(PRIMARY_COLOR);
        overallProgressBar.setBackground(new Color(224, 231, 255));
        overallProgressBar.setBorderPainted(false);
        overallProgressBar.setPreferredSize(new Dimension(200, 35));
        overallProgressBar.setFont(new Font("Segoe UI", Font.BOLD, 12));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(progressPercentageLabel, BorderLayout.CENTER);
        card.add(overallProgressBar, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(TEXT_SECONDARY);

        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        valuePanel.setBackground(CARD_BG);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(accentColor);

        valuePanel.add(valueLabel);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valuePanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createChartsSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_LIGHT);
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));

        JPanel piePanel = createPieChartPanel();
        piePanel.setMaximumSize(new Dimension(1200, 320));
        panel.add(piePanel);
        panel.add(Box.createVerticalStrut(20));

        JPanel linePanel = createLineChartPanel();
        linePanel.setMaximumSize(new Dimension(1200, 380));
        panel.add(linePanel);
        panel.add(Box.createVerticalStrut(20));

        JPanel barPanel = createBarChartPanel();
        barPanel.setMaximumSize(new Dimension(1200, 380));
        panel.add(barPanel);

        // Add Export CSV Button at the bottom
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        exportPanel.setBackground(BG_LIGHT);
        exportPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        JButton exportBtn = new JButton("📥 Download CSV Report");
        exportBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        exportBtn.setBackground(SUCCESS_COLOR);
        exportBtn.setForeground(Color.WHITE);
        exportBtn.setBorderPainted(false);
        exportBtn.setFocusPainted(false);
        exportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportBtn.setPreferredSize(new Dimension(200, 40));
        exportBtn.addActionListener(e -> exportDataToCSV());

        exportPanel.add(exportBtn);
        panel.add(exportPanel);

        return panel;
    }

    private JPanel createPieChartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("📊 Task Status Distribution");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        DefaultPieDataset dataset = new DefaultPieDataset();

        Integer activePlanId = user.getActivePlanId();
        if (activePlanId != null) {
            List<StudyTask> tasks = taskDAO.findByGoalId(activePlanId);
            int completed = 0, pending = 0, missed = 0;
            for (StudyTask task : tasks) {
                if ("COMPLETED".equals(task.getStatus())) completed++;
                else if ("MISSED".equals(task.getStatus())) missed++;
                else pending++;
            }
            dataset.setValue("Completed", completed);
            dataset.setValue("Pending", pending);
            dataset.setValue("Missed", missed);
        }

        JFreeChart chart = ChartFactory.createPieChart("", dataset, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Completed", SUCCESS_COLOR);
        plot.setSectionPaint("Pending", WARNING_COLOR);
        plot.setSectionPaint("Missed", DANGER_COLOR);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setShadowPaint(null);
        plot.setLabelGenerator(null);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 250));
        chartPanel.setBorder(null);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLineChartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("📈 Daily Progress Trend (Last 7 Days)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        Integer activePlanId = user.getActivePlanId();
        if (activePlanId != null) {
            List<StudyTask> tasks = taskDAO.findByGoalId(activePlanId);
            Map<LocalDate, Integer> dailyCompleted = new HashMap<>();

            for (StudyTask task : tasks) {
                if ("COMPLETED".equals(task.getStatus())) {
                    dailyCompleted.put(task.getTaskDate(),
                            dailyCompleted.getOrDefault(task.getTaskDate(), 0) + 1);
                }
            }

            LocalDate today = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                int completed = dailyCompleted.getOrDefault(date, 0);
                dataset.addValue(completed, "Tasks Completed", date.format(DateTimeFormatter.ofPattern("MM/dd")));
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "", "Date", "Tasks Completed",
                dataset, PlotOrientation.VERTICAL, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(200, 200, 200));
        plot.setDomainGridlinePaint(new Color(200, 200, 200));

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, PRIMARY_COLOR);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-4, -4, 8, 8));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(900, 300));
        chartPanel.setBorder(null);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBarChartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("📊 Weekly Completion Summary");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        panel.add(titleLabel, BorderLayout.NORTH);

        // Create dataset with proper week labels
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<Integer, Integer> weeklyCompletedData = new LinkedHashMap<>();

        Integer activePlanId = user.getActivePlanId();
        if (activePlanId != null) {
            List<StudyTask> tasks = taskDAO.findByGoalId(activePlanId);

            LocalDate minDate = LocalDate.now();
            LocalDate maxDate = LocalDate.now().minusDays(1);

            for (StudyTask task : tasks) {
                if (task.getTaskDate().isBefore(minDate)) minDate = task.getTaskDate();
                if (task.getTaskDate().isAfter(maxDate)) maxDate = task.getTaskDate();
            }

            LocalDate startOfYear = LocalDate.of(minDate.getYear(), 1, 1);

            for (StudyTask task : tasks) {
                if ("COMPLETED".equals(task.getStatus())) {
                    int week = (int)((task.getTaskDate().toEpochDay() - startOfYear.toEpochDay()) / 7) + 1;
                    weeklyCompletedData.put(week, weeklyCompletedData.getOrDefault(week, 0) + 1);
                }
            }

            for (Map.Entry<Integer, Integer> entry : weeklyCompletedData.entrySet()) {
                dataset.addValue(entry.getValue(), "Completed", "Week " + entry.getKey());
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "", "Week", "Tasks Completed",
                dataset, PlotOrientation.VERTICAL, true, true, false);

        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(200, 200, 200));
        plot.setDomainGridlinePaint(new Color(200, 200, 200));

        // Custom Bar Renderer with gradient colors
        BarRenderer renderer = new BarRenderer() {
            private Color[] gradientColors = {
                    new Color(79, 70, 229),  // Blue
                    new Color(34, 197, 94),   // Green
                    new Color(245, 158, 11),  // Orange
                    new Color(239, 68, 68)    // Red
            };

            @Override
            public Paint getItemPaint(int row, int column) {
                return gradientColors[column % gradientColors.length];
            }
        };

        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);
        renderer.setMaximumBarWidth(0.15);
        renderer.setDefaultItemLabelGenerator(new org.jfree.chart.labels.StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        renderer.setDefaultItemLabelPaint(new Color(51, 51, 51));

        plot.setRenderer(renderer);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(900, 300));
        chartPanel.setBorder(null);

        // Add tooltip support for bar chart using ChartPanel's tooltip
        chartPanel.setToolTipText("");
        chartPanel.addChartMouseListener(new org.jfree.chart.ChartMouseListener() {
            @Override
            public void chartMouseClicked(org.jfree.chart.ChartMouseEvent event) {}

            @Override
            public void chartMouseMoved(org.jfree.chart.ChartMouseEvent event) {
                org.jfree.chart.entity.ChartEntity entity = event.getEntity();
                if (entity instanceof org.jfree.chart.entity.CategoryItemEntity) {
                    org.jfree.chart.entity.CategoryItemEntity item = (org.jfree.chart.entity.CategoryItemEntity) entity;
                    Comparable week = item.getColumnKey();
                    Number value = item.getDataset().getValue(item.getRowKey(), item.getColumnKey());
                    if (value != null) {
                        chartPanel.setToolTipText("<html><body style='width:200px; padding:5px;'>" +
                                "<b>Week:</b> " + week.toString() + "<br>" +
                                "<b>Tasks Completed:</b> " + value.intValue() + "<br>" +
                                "<b>Contribution:</b> " + value.intValue() + " task(s) completed</body></html>");
                    }
                } else {
                    chartPanel.setToolTipText(null);
                }
            }
        });

        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }

    private void exportDataToCSV() {
        try {
            Integer activePlanId = user.getActivePlanId();
            if (activePlanId == null) {
                JOptionPane.showMessageDialog(this, "No active plan selected. Please activate a plan first.", "No Data", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<StudyTask> tasks = taskDAO.findByGoalId(activePlanId);
            if (tasks.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No tasks found for the active plan.", "No Data", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Open file chooser for user to select save location
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save CSV Report");
            fileChooser.setSelectedFile(new File("study_progress_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv"));

            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return; // User cancelled
            }

            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith(".csv")) {
                filePath += ".csv";
            }

            // Create CSV content
            StringBuilder csvContent = new StringBuilder();

            // Sheet 1: Daily Progress Data
            csvContent.append("DAILY PROGRESS DATA\n");
            csvContent.append("Date,Tasks Completed\n");

            Map<LocalDate, Integer> dailyCompleted = new TreeMap<>();
            for (StudyTask task : tasks) {
                if ("COMPLETED".equals(task.getStatus())) {
                    dailyCompleted.put(task.getTaskDate(),
                            dailyCompleted.getOrDefault(task.getTaskDate(), 0) + 1);
                }
            }

            LocalDate today = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                int completed = dailyCompleted.getOrDefault(date, 0);
                csvContent.append(date).append(",").append(completed).append("\n");
            }

            csvContent.append("\n");

            // Sheet 2: Weekly Summary Data
            csvContent.append("WEEKLY SUMMARY DATA\n");
            csvContent.append("Week,Tasks Completed\n");

            LocalDate minDate = LocalDate.now();
            LocalDate maxDate = LocalDate.now().minusDays(1);
            for (StudyTask task : tasks) {
                if (task.getTaskDate().isBefore(minDate)) minDate = task.getTaskDate();
                if (task.getTaskDate().isAfter(maxDate)) maxDate = task.getTaskDate();
            }

            LocalDate startOfYear = LocalDate.of(minDate.getYear(), 1, 1);
            Map<Integer, Integer> weeklyCompleted = new TreeMap<>();

            for (StudyTask task : tasks) {
                if ("COMPLETED".equals(task.getStatus())) {
                    int week = (int)((task.getTaskDate().toEpochDay() - startOfYear.toEpochDay()) / 7) + 1;
                    weeklyCompleted.put(week, weeklyCompleted.getOrDefault(week, 0) + 1);
                }
            }

            for (Map.Entry<Integer, Integer> entry : weeklyCompleted.entrySet()) {
                csvContent.append("Week ").append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
            }

            csvContent.append("\n");

            // Sheet 3: Task Status Distribution
            csvContent.append("TASK STATUS DISTRIBUTION\n");
            csvContent.append("Status,Count\n");

            int completed = 0, pending = 0, missed = 0;
            for (StudyTask task : tasks) {
                if ("COMPLETED".equals(task.getStatus())) completed++;
                else if ("MISSED".equals(task.getStatus())) missed++;
                else pending++;
            }

            csvContent.append("Completed,").append(completed).append("\n");
            csvContent.append("Pending,").append(pending).append("\n");
            csvContent.append("Missed,").append(missed).append("\n");

            csvContent.append("\n");

            // Sheet 4: Overall Stats
            csvContent.append("OVERALL STATISTICS\n");
            csvContent.append("Metric,Value\n");
            csvContent.append("Total Tasks,").append(tasks.size()).append("\n");
            csvContent.append("Completed Tasks,").append(completed).append("\n");
            csvContent.append("Pending Tasks,").append(pending).append("\n");
            csvContent.append("Missed Tasks,").append(missed).append("\n");

            int progress = tasks.size() > 0 ? (completed * 100 / tasks.size()) : 0;
            csvContent.append("Overall Progress (%),").append(progress).append("\n");

            // Calculate streak
            int streak = 0;
            LocalDate checkDate = LocalDate.now().minusDays(1);
            while (true) {
                final LocalDate currentDate = checkDate;
                boolean hasCompletion = tasks.stream()
                        .anyMatch(t -> "COMPLETED".equals(t.getStatus()) && t.getTaskDate().equals(currentDate));
                if (hasCompletion) {
                    streak++;
                    checkDate = checkDate.minusDays(1);
                } else {
                    break;
                }
            }
            csvContent.append("Current Streak (days),").append(streak).append("\n");

            // Save file
            java.io.FileWriter fileWriter = new java.io.FileWriter(filePath);
            fileWriter.write(csvContent.toString());
            fileWriter.close();

            // Ask user if they want to open the file
            int openFile = JOptionPane.showConfirmDialog(this,
                    "✅ Report exported successfully!\n\nFile saved to:\n" + filePath + "\n\nDo you want to open the file?",
                    "Export Complete",
                    JOptionPane.YES_NO_OPTION);

            if (openFile == JOptionPane.YES_OPTION) {
                // Open the file with default application (Excel)
                Desktop.getDesktop().open(new File(filePath));
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error exporting data: " + e.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProgressData() {
        Integer activePlanId = user.getActivePlanId();

        if (activePlanId == null) {
            planNameLabel.setText("No active plan selected");
            totalTasksLabel.setText("0");
            completedTasksLabel.setText("0");
            pendingTasksLabel.setText("0");
            missedTasksLabel.setText("0");
            progressPercentageLabel.setText("0%");
            overallProgressBar.setValue(0);
            streakLabel.setText("0 days");
            lastCommitLabel.setText("Never");
            updateCharts();
            return;
        }

        StudyPlan activePlan = planDAO.findById(activePlanId);
        if (activePlan == null) {
            planNameLabel.setText("Plan not found");
            return;
        }

        String displayName = activePlan.getPlanName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = activePlan.getRepositoryName();
        }
        planNameLabel.setText(displayName != null ? displayName : "Active Plan");

        List<StudyTask> tasks = taskDAO.findByGoalId(activePlanId);
        int totalTasks = tasks.size();
        int completedTasks = 0;
        int pendingTasks = 0;
        int missedTasks = 0;
        int streak = 0;
        LocalDate lastCommitDate = null;

        for (StudyTask task : tasks) {
            if ("COMPLETED".equals(task.getStatus())) {
                completedTasks++;
                if (task.getActualCommits() > 0) {
                    if (lastCommitDate == null || task.getTaskDate().isAfter(lastCommitDate)) {
                        lastCommitDate = task.getTaskDate();
                    }
                }
            } else if ("MISSED".equals(task.getStatus())) {
                missedTasks++;
            } else {
                pendingTasks++;
            }
        }

        int progress = totalTasks > 0 ? (completedTasks * 100 / totalTasks) : 0;

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 30; i++) {
            LocalDate checkDate = today.minusDays(i);
            boolean hasCompletion = false;
            for (StudyTask task : tasks) {
                if ("COMPLETED".equals(task.getStatus()) && task.getTaskDate().equals(checkDate)) {
                    hasCompletion = true;
                    break;
                }
            }
            if (hasCompletion) {
                streak++;
            } else {
                break;
            }
        }

        totalTasksLabel.setText(String.valueOf(totalTasks));
        completedTasksLabel.setText(String.valueOf(completedTasks));
        pendingTasksLabel.setText(String.valueOf(pendingTasks));
        missedTasksLabel.setText(String.valueOf(missedTasks));
        progressPercentageLabel.setText(progress + "%");
        overallProgressBar.setValue(progress);
        overallProgressBar.setString(progress + "% (" + completedTasks + "/" + totalTasks + ")");
        streakLabel.setText(streak + " days");

        if (lastCommitDate != null) {
            long daysAgo = java.time.temporal.ChronoUnit.DAYS.between(lastCommitDate, LocalDate.now());
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

        updateCharts();
    }

    private void updateCharts() {
        if (chartsPanel != null) {
            mainContentPanel.remove(chartsPanel);
        }

        chartsPanel = createChartsSection();
        mainContentPanel.add(chartsPanel);

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    public JPanel getMainPanel() {
        return this;
    }
}
