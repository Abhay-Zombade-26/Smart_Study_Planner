package ui;

import dao.GitHubActivityDAO;
import model.GitHubActivity;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class GitHubProgressFrame extends JPanel {

    private User user;
    private GitHubActivityDAO gitHubActivityDAO;
    private JTable progressTable;
    private DefaultTableModel tableModel;

    public GitHubProgressFrame(User user) {
        this.user = user;
        this.gitHubActivityDAO = new GitHubActivityDAO();

        initUI();
        loadProgressData();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("📈 GitHub Progress Tracker");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 33, 33));

        JLabel subtitleLabel = new JLabel("Track your commit activity and repository progress");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 116, 139));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // Table
        createProgressTable();
        JScrollPane scrollPane = new JScrollPane(progressTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void createProgressTable() {
        String[] columns = {"Repository", "Progress", "Last Commit", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        progressTable = new JTable(tableModel);
        progressTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        progressTable.setRowHeight(50);
        progressTable.setShowGrid(true);
        progressTable.setGridColor(new Color(226, 232, 240));
        progressTable.setSelectionBackground(new Color(239, 246, 255));
        progressTable.setRowSelectionAllowed(false);

        // Table header styling
        JTableHeader header = progressTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(33, 33, 33));
        header.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        // Custom cell renderers
        progressTable.getColumnModel().getColumn(0).setCellRenderer(new RepositoryCellRenderer());
        progressTable.getColumnModel().getColumn(1).setCellRenderer(new ProgressCellRenderer());
        progressTable.getColumnModel().getColumn(2).setCellRenderer(new DateCellRenderer());
        progressTable.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());

        // Column widths
        progressTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        progressTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        progressTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        progressTable.getColumnModel().getColumn(3).setPreferredWidth(100);
    }

    private void loadProgressData() {
        tableModel.setRowCount(0);

        List<GitHubActivity> activities = gitHubActivityDAO.findByUserId(user.getId());

        if (activities.isEmpty()) {
            // Add sample data for demonstration
            addSampleData();
        } else {
            for (GitHubActivity activity : activities) {
                int progress = calculateProgress(activity);
                String lastCommit = activity.getLastCommitDate() != null ?
                        activity.getLastCommitDate().toString() : "Never";
                String status = activity.getStatus();

                tableModel.addRow(new Object[]{
                        activity.getRepoName(),
                        progress,
                        lastCommit,
                        status
                });
            }
        }
    }

    private void addSampleData() {
        tableModel.addRow(new Object[]{"user/project-alpha", 75, "2024-01-15", "Green"});
        tableModel.addRow(new Object[]{"user/project-beta", 45, "2024-01-14", "Yellow"});
        tableModel.addRow(new Object[]{"user/project-gamma", 20, "2024-01-10", "Red"});
        tableModel.addRow(new Object[]{"user/project-delta", 90, "2024-01-15", "Green"});
    }

    private int calculateProgress(GitHubActivity activity) {
        if (activity.getCommitCount() == 0) return 0;
        return Math.min(100, activity.getCommitCount());
    }

    // Custom cell renderers
    class RepositoryCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(new EmptyBorder(10, 15, 10, 15));
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(new Color(33, 33, 33));

            // Add repository icon
            setText("📦 " + value.toString());

            return c;
        }
    }

    class ProgressCellRenderer extends DefaultTableCellRenderer {
        private JProgressBar progressBar;

        public ProgressCellRenderer() {
            progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            progressBar.setBorderPainted(false);
            progressBar.setFont(new Font("Segoe UI", Font.BOLD, 12));
            progressBar.setPreferredSize(new Dimension(100, 25));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            int progress = Integer.parseInt(value.toString());
            progressBar.setValue(progress);

            if (progress >= 70) {
                progressBar.setForeground(new Color(46, 204, 113));
            } else if (progress >= 30) {
                progressBar.setForeground(new Color(241, 196, 15));
            } else {
                progressBar.setForeground(new Color(220, 38, 38));
            }

            progressBar.setString(progress + "%");
            progressBar.setBackground(new Color(226, 232, 240));

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
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
                if (date.equals("Never")) {
                    setForeground(new Color(148, 163, 184));
                } else {
                    setForeground(new Color(33, 33, 33));
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
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            JLabel badge = new JLabel();
            badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
            badge.setBorder(new EmptyBorder(5, 15, 5, 15));
            badge.setOpaque(true);

            String status = value.toString();
            switch (status) {
                case "Green":
                    badge.setText("● Active");
                    badge.setForeground(Color.WHITE);
                    badge.setBackground(new Color(46, 204, 113));
                    break;
                case "Yellow":
                    badge.setText("● Delayed");
                    badge.setForeground(new Color(33, 33, 33));
                    badge.setBackground(new Color(241, 196, 15));
                    break;
                case "Red":
                    badge.setText("● Missed");
                    badge.setForeground(Color.WHITE);
                    badge.setBackground(new Color(220, 38, 38));
                    break;
                default:
                    badge.setText("● Unknown");
                    badge.setForeground(Color.WHITE);
                    badge.setBackground(new Color(148, 163, 184));
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