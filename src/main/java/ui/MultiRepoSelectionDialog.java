package ui;

import model.User;
import service.StudyPlanGenerator;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultiRepoSelectionDialog extends JDialog {
    
    private User user;
    private List<Map<String, String>> repositories;
    private JTable repoTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> durationCombo;
    private JSpinner hoursSpinner;
    private JLabel statusLabel;
    private boolean planGenerated = false;
    private StudyPlanGenerator planGenerator;
    
    public MultiRepoSelectionDialog(JFrame parent, User user, List<Map<String, String>> repositories) {
        super(parent, "Select Repositories for Study Plan", true);
        this.user = user;
        this.repositories = repositories;
        this.planGenerator = new StudyPlanGenerator();
        
        setSize(700, 600);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        initUI();
    }
    
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Repository Selection Table
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
        // Plan Configuration
        JPanel configPanel = createConfigPanel();
        mainPanel.add(configPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("?? Select Repositories for Your Study Plan");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        JLabel subtitleLabel = new JLabel("Choose one or more repositories to include in your rotating study schedule");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(Color.WHITE);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Your Repositories"));
        
        // Create table model
        String[] columns = {"Select", "Repository", "Private", "Last Updated"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }
        };
        
        // Add repositories to table
        for (Map<String, String> repo : repositories) {
            tableModel.addRow(new Object[]{
                false,
                repo.get("name"),
                repo.get("private"),
                repo.get("updated_at").substring(0, 10)
            });
        }
        
        repoTable = new JTable(tableModel);
        repoTable.setRowHeight(25);
        repoTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        repoTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        repoTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        repoTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        
        JScrollPane scrollPane = new JScrollPane(repoTable);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        
        // Select All / Deselect All buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton selectAllBtn = new JButton("Select All");
        selectAllBtn.addActionListener(e -> selectAll(true));
        
        JButton deselectAllBtn = new JButton("Deselect All");
        deselectAllBtn.addActionListener(e -> selectAll(false));
        
        buttonPanel.add(selectAllBtn);
        buttonPanel.add(deselectAllBtn);
        
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Plan Configuration"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Duration
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Study Duration:"), gbc);
        
        gbc.gridx = 1;
        String[] durations = {"1 month", "2 months", "3 months", "6 months"};
        durationCombo = new JComboBox<>(durations);
        durationCombo.setPreferredSize(new Dimension(150, 30));
        panel.add(durationCombo, gbc);
        
        // Daily Hours
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Daily Study Hours:"), gbc);
        
        gbc.gridx = 1;
        SpinnerNumberModel hoursModel = new SpinnerNumberModel(2, 1, 8, 1);
        hoursSpinner = new JSpinner(hoursModel);
        hoursSpinner.setPreferredSize(new Dimension(150, 30));
        panel.add(hoursSpinner, gbc);
        
        // Status Label
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(statusLabel, gbc);
        
        // Generate Button
        gbc.gridy = 3;
        JButton generateBtn = new JButton("?? Generate Study Plan");
        generateBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        generateBtn.setBackground(new Color(46, 204, 113));
        generateBtn.setForeground(Color.WHITE);
        generateBtn.setFocusPainted(false);
        generateBtn.setPreferredSize(new Dimension(200, 40));
        generateBtn.addActionListener(e -> generatePlan());
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(generateBtn);
        
        gbc.gridy = 4;
        panel.add(btnPanel, gbc);
        
        return panel;
    }
    
    private void selectAll(boolean select) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(select, i, 0);
        }
    }
    
    private void generatePlan() {
        // Get selected repositories
        List<String> selectedRepos = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean selected = (Boolean) tableModel.getValueAt(i, 0);
            if (selected) {
                selectedRepos.add((String) tableModel.getValueAt(i, 1));
            }
        }
        
        if (selectedRepos.isEmpty()) {
            statusLabel.setText("? Please select at least one repository");
            statusLabel.setForeground(new Color(231, 76, 60));
            return;
        }
        
        // Get configuration
        int durationMonths = durationCombo.getSelectedIndex() + 1;
        int dailyHours = (Integer) hoursSpinner.getValue();
        
        statusLabel.setText("? Generating your study plan...");
        statusLabel.setForeground(new Color(52, 152, 219));
        
        // Generate plan in background
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                planGenerator.generatePlan(user, selectedRepos, durationMonths, dailyHours);
                return null;
            }
            
            @Override
            protected void done() {
                statusLabel.setText("? Study plan generated successfully!");
                statusLabel.setForeground(new Color(46, 204, 113));
                planGenerated = true;
                
                // Show summary
                String message = String.format(
                    "?? Study Plan Generated!\n\n" +
                    "Duration: %d months\n" +
                    "Daily Hours: %d\n" +
                    "Repositories: %d\n\n" +
                    "Your daily tasks have been created. Check the Dashboard to view them.",
                    durationMonths, dailyHours, selectedRepos.size()
                );
                
                JOptionPane.showMessageDialog(MultiRepoSelectionDialog.this,
                    message,
                    "Plan Generated",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Close dialog
                Timer timer = new Timer(1500, e -> dispose());
                timer.setRepeats(false);
                timer.start();
            }
        };
        worker.execute();
    }
    
    public boolean isPlanGenerated() {
        return planGenerated;
    }
}

