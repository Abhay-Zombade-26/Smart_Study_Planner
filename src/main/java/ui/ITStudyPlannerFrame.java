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
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JComboBox<String> repoComboBox;
    private JLabel statusLabel;
    private JTextArea repoDetailsArea;
    private GitHubOAuthService gitHubService;
    private List<Map<String, String>> repositories;
    
    // Color scheme
    private final Color SIDEBAR_BG = new Color(26, 32, 44);
    private final Color SIDEBAR_HOVER = new Color(44, 55, 74);
    private final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private final Color SUCCESS_COLOR = new Color(16, 185, 129);
    private final Color DANGER_COLOR = new Color(239, 68, 68);
    private final Color WARNING_COLOR = new Color(245, 158, 11);
    private final Color BG_LIGHT = new Color(249, 250, 251);
    private final Color BORDER_COLOR = new Color(229, 231, 235);
    
    public ITStudyPlannerFrame(User user) {
        this.user = user;
        this.gitHubService = new GitHubOAuthService();
        this.repositories = new ArrayList<>();
        
        setTitle("Smart Study Planner - " + user.getGithubUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1200, 700));
        
        initUI();
        loadRepositories();
    }
    
    private void initUI() {
        mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BG_LIGHT);
        
        // Create sidebar
        createSidebar();
        
        // Create content area with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_LIGHT);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Add all screens
        contentPanel.add(createGoalSetupPanel(), "GOAL_SETUP");
        contentPanel.add(new DashboardFrame(user).getMainPanel(), "DASHBOARD");
        contentPanel.add(createStudyPlanPanel(), "STUDY_PLAN");
        contentPanel.add(new GitHubProgressFrame(user).getMainPanel(), "GITHUB_PROGRESS");
        contentPanel.add(createGoalsPanel(), "GOALS");
        contentPanel.add(new ProfileFrame(user).getMainPanel(), "PROFILE");
        
        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel);
        
        // Show goal setup first
        cardLayout.show(contentPanel, "GOAL_SETUP");
    }
    
    private void createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(SIDEBAR_BG);
        sidebarPanel.setPreferredSize(new Dimension(280, getHeight()));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
        
        // App logo with gradient effect
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR, w, 0, SUCCESS_COLOR);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        logoPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 20));
        logoPanel.setPreferredSize(new Dimension(280, 100));
        logoPanel.setMaximumSize(new Dimension(280, 100));
        
        JLabel logoLabel = new JLabel("?? Smart Planner");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoLabel.setForeground(Color.WHITE);
        logoPanel.add(logoLabel);
        
        sidebarPanel.add(logoPanel);
        sidebarPanel.add(Box.createVerticalStrut(20));
        
        // User profile card
        JPanel userCard = new JPanel();
        userCard.setLayout(new BoxLayout(userCard, BoxLayout.Y_AXIS));
        userCard.setBackground(new Color(44, 55, 74));
        userCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(55, 65, 81), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        userCard.setMaximumSize(new Dimension(260, 120));
        userCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel avatarLabel = new JLabel("??");
        avatarLabel.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel nameLabel = new JLabel(user.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel roleLabel = new JLabel("IT Student");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(new Color(156, 163, 175));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        userCard.add(avatarLabel);
        userCard.add(Box.createVerticalStrut(10));
        userCard.add(nameLabel);
        userCard.add(Box.createVerticalStrut(5));
        userCard.add(roleLabel);
        
        sidebarPanel.add(userCard);
        sidebarPanel.add(Box.createVerticalStrut(30));
        
        // Navigation buttons with icons
        addNavButton("?? Goal Setup", "GOAL_SETUP", new Color(79, 70, 229));
        addNavButton("?? Dashboard", "DASHBOARD", new Color(16, 185, 129));
        addNavButton("?? My Study Plan", "STUDY_PLAN", new Color(245, 158, 11));
        addNavButton("?? GitHub Progress", "GITHUB_PROGRESS", new Color(139, 92, 246));
        addNavButton("?? Goals", "GOALS", new Color(236, 72, 153));
        addNavButton("?? Profile", "PROFILE", new Color(59, 130, 246));
        
        sidebarPanel.add(Box.createVerticalGlue());
        
        // Logout button
        JButton logoutBtn = new JButton("?? Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(DANGER_COLOR);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(240, 45));
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Logout Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });
        
        sidebarPanel.add(logoutBtn);
        sidebarPanel.add(Box.createVerticalStrut(20));
    }
    
    private void addNavButton(String text, String cardName, Color accentColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(new Color(209, 213, 219));
        button.setBackground(SIDEBAR_BG);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(240, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(44, 55, 74));
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(SIDEBAR_BG);
                button.setForeground(new Color(209, 213, 219));
            }
        });
        
        button.addActionListener(e -> {
            cardLayout.show(contentPanel, cardName);
            // Highlight active button
            for (Component comp : sidebarPanel.getComponents()) {
                if (comp instanceof JButton) {
                    ((JButton) comp).setForeground(new Color(209, 213, 219));
                }
            }
            button.setForeground(accentColor);
        });
        
        sidebarPanel.add(button);
        sidebarPanel.add(Box.createVerticalStrut(5));
    }
    
    private JPanel createGoalSetupPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(30, 30, 30, 30)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 20, 10, 20);
        
        // Header with gradient line
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel headerLabel = new JLabel("Create Your Smart Study Plan");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(new Color(17, 24, 39));
        
        JLabel subHeaderLabel = new JLabel("Select a GitHub repository to generate an AI-powered study plan");
        subHeaderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subHeaderLabel.setForeground(new Color(107, 114, 128));
        
        JSeparator separator = new JSeparator();
        separator.setForeground(PRIMARY_COLOR);
        separator.setPreferredSize(new Dimension(100, 3));
        
        headerPanel.add(headerLabel, BorderLayout.NORTH);
        headerPanel.add(subHeaderLabel, BorderLayout.CENTER);
        headerPanel.add(separator, BorderLayout.SOUTH);
        
        panel.add(headerPanel, gbc);
        panel.add(Box.createVerticalStrut(20), gbc);
        
        // Repository selection card
        JPanel repoCard = new JPanel(new GridBagLayout());
        repoCard.setBackground(new Color(249, 250, 251));
        repoCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 0, 5, 0);
        
        JLabel repoLabel = new JLabel("?? Select Repository");
        repoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        repoCard.add(repoLabel, c);
        
        repoComboBox = new JComboBox<>();
        repoComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        repoComboBox.setPreferredSize(new Dimension(400, 45));
        repoComboBox.setMaximumSize(new Dimension(400, 45));
        repoComboBox.setBackground(Color.WHITE);
        repoComboBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        repoComboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        repoCard.add(repoComboBox, c);
        
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(PRIMARY_COLOR);
        repoCard.add(statusLabel, c);
        
        panel.add(repoCard, gbc);
        panel.add(Box.createVerticalStrut(10), gbc);
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton refreshBtn = createStyledButton("?? Refresh", new Color(107, 114, 128));
        refreshBtn.addActionListener(e -> loadRepositories());
        
        JButton generateBtn = createStyledButton("?? Generate Plan", SUCCESS_COLOR);
        generateBtn.addActionListener(e -> openMultiRepoDialog());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(generateBtn);
        
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(180, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private void openMultiRepoDialog() {
        if (repositories == null || repositories.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No repositories found. Please refresh and try again.",
                "No Repositories",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        MultiRepoSelectionDialog dialog = new MultiRepoSelectionDialog(this, user, repositories);
        dialog.setVisible(true);
    }
    
    private void loadRepositories() {
        statusLabel.setText("? Loading your repositories...");
        repoComboBox.removeAllItems();
        
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
                        statusLabel.setText("? No repositories found. Create some on GitHub first!");
                        statusLabel.setForeground(DANGER_COLOR);
                        repoComboBox.addItem("No repositories");
                    } else {
                        for (Map<String, String> repo : repositories) {
                            repoComboBox.addItem(repo.get("name"));
                        }
                        statusLabel.setText("? Loaded " + repositories.size() + " repositories");
                        statusLabel.setForeground(SUCCESS_COLOR);
                        
                        if (repositories.size() > 0) {
                            repoComboBox.setSelectedIndex(0);
                        }
                    }
                } catch (Exception e) {
                    statusLabel.setText("? Error loading repositories: " + e.getMessage());
                    statusLabel.setForeground(DANGER_COLOR);
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private JPanel createStudyPlanPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(30, 30, 30, 30)
        ));
        
        JLabel placeholder = new JLabel("?? Your study plan will appear here");
        placeholder.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        placeholder.setForeground(new Color(156, 163, 175));
        placeholder.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(placeholder, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createGoalsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(30, 30, 30, 30)
        ));
        
        JLabel placeholder = new JLabel("?? Your goals and milestones will appear here");
        placeholder.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        placeholder.setForeground(new Color(156, 163, 175));
        placeholder.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(placeholder, BorderLayout.CENTER);
        
        return panel;
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
