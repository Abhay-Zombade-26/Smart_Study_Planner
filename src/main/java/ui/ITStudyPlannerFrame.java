package ui;

import model.User;
import service.GitHubOAuthService;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class ITStudyPlannerFrame extends JFrame {
    private User user;
    private JComboBox<String> repoComboBox;
    private JLabel statusLabel;
    private GitHubOAuthService gitHubService;
    
    public ITStudyPlannerFrame(User user) {
        this.user = user;
        this.gitHubService = new GitHubOAuthService();
        initUI();
        loadRepositories();
    }
    
    private void initUI() {
        setTitle("Smart Study Planner - IT Student");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel();
        panel.add(new JLabel("Your GitHub Repositories"));
        
        repoComboBox = new JComboBox<>();
        panel.add(repoComboBox);
        
        statusLabel = new JLabel("Loading...");
        panel.add(statusLabel);
        
        JButton btn = new JButton("Generate");
        btn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Plan generated!"));
        panel.add(btn);
        
        add(panel);
    }
    
    private void loadRepositories() {
        statusLabel.setText("Loading...");
    }
}
