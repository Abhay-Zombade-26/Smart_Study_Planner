package ui;

import model.User;
import service.GitHubOAuthService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class ITStudyPlannerFrame extends JFrame {
    
    private User user;
    private JPanel mainPanel;
    private JComboBox<String> repoComboBox;
    private JLabel statusLabel;
    private JTextArea repoDetailsArea;
    private GitHubOAuthService gitHubService;
    private List<Map<String, String>> repositories;
    
    public ITStudyPlannerFrame(User user) {
        this.user = user;
        this.gitHubService = new GitHubOAuthService();
        this.repositories = new ArrayList<>();
        
        setTitle("Smart Study Planner - IT Student: " + user.getGithubUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        initUI();
        loadRepositories();
    }
    
    private void initUI() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 247, 250));
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Center Panel with repository list
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(new JScrollPane(centerPanel), BorderLayout.CENTER);
        
        // Bottom Panel with actions
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + user.getName() + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JLabel githubLabel = new JLabel("GitHub: @" + user.getGithubUsername());
        githubLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        githubLabel.setForeground(new Color(52, 152, 219));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(Color.WHITE);
        textPanel.add(welcomeLabel);
        textPanel.add(githubLabel);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Repository selection panel
        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectPanel.setBackground(Color.WHITE);
        
        JLabel repoLabel = new JLabel("Select Repository:");
        repoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        repoComboBox = new JComboBox<>();
        repoComboBox.setPreferredSize(new Dimension(400, 35));
        repoComboBox.addActionListener(e -> showRepoDetails());
        
        selectPanel.add(repoLabel);
        selectPanel.add(repoComboBox);
        
        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(52, 152, 219));
        
        // Repository details area
        repoDetailsArea = new JTextArea(15, 50);
        repoDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        repoDetailsArea.setEditable(false);
        repoDetailsArea.setBackground(new Color(248, 249, 250));
        repoDetailsArea.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        
        JScrollPane scrollPane = new JScrollPane(repoDetailsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Repository Details"));
        
        panel.add(selectPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(Color.WHITE);
        
        JButton refreshBtn = new JButton("?? Refresh Repositories");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        refreshBtn.setBackground(new Color(52, 152, 219));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> loadRepositories());
        
        JButton generatePlanBtn = new JButton("?? Generate Study Plan");
        generatePlanBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        generatePlanBtn.setBackground(new Color(46, 204, 113));
        generatePlanBtn.setForeground(Color.WHITE);
        generatePlanBtn.setFocusPainted(false);
        generatePlanBtn.addActionListener(e -> generateStudyPlan());
        
        JButton logoutBtn = new JButton("?? Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutBtn.setBackground(new Color(231, 76, 60));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> logout());
        
        panel.add(refreshBtn);
        panel.add(generatePlanBtn);
        panel.add(logoutBtn);
        
        return panel;
    }
    
    private void loadRepositories() {
        statusLabel.setText("Loading your repositories...");
        repoComboBox.removeAllItems();
        repoDetailsArea.setText("");
        
        SwingWorker<List<Map<String, String>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, String>> doInBackground() {
                String token = user.getAccessToken();
                if (token == null || token.isEmpty()) {
                    return new ArrayList<>();
                }
                return gitHubService.getRepositories(token);
            }
            
            @Override
            protected void done() {
                try {
                    repositories = get();
                    if (repositories == null || repositories.isEmpty()) {
                        statusLabel.setText("No repositories found. Create some on GitHub first!");
                        repoComboBox.addItem("No repositories");
                    } else {
                        for (Map<String, String> repo : repositories) {
                            repoComboBox.addItem(repo.get("name"));
                        }
                        statusLabel.setText("? Loaded " + repositories.size() + " repositories");
                        
                        if (repositories.size() > 0) {
                            repoComboBox.setSelectedIndex(0);
                        }
                    }
                } catch (Exception e) {
                    statusLabel.setText("? Error loading repositories: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void showRepoDetails() {
        int selectedIndex = repoComboBox.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < repositories.size()) {
            Map<String, String> repo = repositories.get(selectedIndex);
            
            StringBuilder details = new StringBuilder();
            details.append("?? Repository: ").append(repo.get("name")).append("\n");
            details.append("?? URL: ").append(repo.get("html_url")).append("\n");
            details.append("?? Description: ").append(repo.get("description")).append("\n");
            details.append("?? Private: ").append(repo.get("private")).append("\n");
            details.append("?? Last Updated: ").append(repo.get("updated_at")).append("\n");
            
            repoDetailsArea.setText(details.toString());
        }
    }
    
    private void generateStudyPlan() {
        String selectedRepo = (String) repoComboBox.getSelectedItem();
        if (selectedRepo == null || selectedRepo.equals("No repositories")) {
            JOptionPane.showMessageDialog(this,
                "Please select a valid repository first.",
                "No Repository",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JOptionPane.showMessageDialog(this,
            "Study plan will be generated for: " + selectedRepo + "\n\n" +
            "This feature is coming soon!",
            "Study Plan",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Logout",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
