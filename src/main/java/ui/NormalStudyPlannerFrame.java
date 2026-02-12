package ui;

import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class NormalStudyPlannerFrame extends JFrame {

    private User user;
    private JPanel mainPanel;

    public NormalStudyPlannerFrame(User user) {
        this.user = user;
        initUI();
    }

    private void initUI() {
        setTitle("Smart Study Planner - Normal Student");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Simple welcome panel for normal students
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.setBorder(new EmptyBorder(50, 50, 50, 50));

        JLabel welcomeLabel = new JLabel("Welcome to Smart Study Planner");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(new Color(33, 33, 33));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel("Hello, " + user.getName() + "!");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        nameLabel.setForeground(new Color(71, 85, 105));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel modeLabel = new JLabel("Normal Student Mode");
        modeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        modeLabel.setForeground(new Color(52, 152, 219));
        modeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        modeLabel.setBorder(new EmptyBorder(10, 0, 30, 0));

        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setMaximumSize(new Dimension(600, 150));
        infoPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        addInfoCard(infoPanel, "📚", "Create Study Plan", "Set up your manual study schedule");
        addInfoCard(infoPanel, "📊", "Track Progress", "Monitor your daily study tasks");
        addInfoCard(infoPanel, "🎯", "Set Goals", "Define your academic objectives");
        addInfoCard(infoPanel, "📝", "Take Notes", "Record important learning points");

        welcomePanel.add(welcomeLabel);
        welcomePanel.add(Box.createVerticalStrut(20));
        welcomePanel.add(nameLabel);
        welcomePanel.add(Box.createVerticalStrut(10));
        welcomePanel.add(modeLabel);
        welcomePanel.add(Box.createVerticalStrut(40));
        welcomePanel.add(infoPanel);

        mainPanel.add(welcomePanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void addInfoCard(JPanel panel, String icon, String title, String desc) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(33, 33, 33));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(100, 116, 139));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(iconLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(descLabel);

        panel.add(card);
    }
}