package ui;

import model.User;
import enums.UserRole;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RoleSelectionFrame extends JFrame {

    private User user;
    private JPanel mainPanel;

    public RoleSelectionFrame(User user) {
        this.user = user;
        initUI();
    }

    private void initUI() {
        setTitle("Select Your Path");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);

        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 236, 240), 1, true),
                new EmptyBorder(40, 50, 40, 50)
        ));

        // Welcome header
        JLabel welcomeLabel = new JLabel("Welcome, " + user.getName() + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeLabel.setForeground(new Color(33, 33, 33));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel questionLabel = new JLabel("How would you like to use Smart Study Planner?");
        questionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        questionLabel.setForeground(new Color(71, 85, 105));
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(welcomeLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(questionLabel);
        contentPanel.add(Box.createVerticalStrut(40));

        // Role cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        cardsPanel.setBackground(Color.WHITE);
        cardsPanel.setMaximumSize(new Dimension(600, 200));

        // Normal Student Card
        JPanel normalCard = createRoleCard(
                "🎓 Normal Student",
                new String[]{
                        "• Manual study plan creation",
                        "• Subject-based scheduling",
                        "• Basic progress tracking",
                        "• Simple timetable generation"
                },
                new Color(52, 152, 219)
        );

        normalCard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                user.setRole(UserRole.NORMAL);
                new NormalStudyPlannerFrame(user).setVisible(true);
                dispose();
            }
        });

        // IT Student Card
        JPanel itCard = createRoleCard(
                "💻 IT Student",
                new String[]{
                        "• GitHub repository integration",
                        "• Commit-based progress tracking",
                        "• Adaptive smart planning",
                        "• Deadline-driven AI schedule"
                },
                new Color(46, 204, 113)
        );

        itCard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                user.setRole(UserRole.IT);
                new ITStudyPlannerFrame(user).setVisible(true);
                dispose();
            }
        });

        cardsPanel.add(normalCard);
        cardsPanel.add(itCard);

        contentPanel.add(cardsPanel);
        contentPanel.add(Box.createVerticalStrut(30));

        JLabel noteLabel = new JLabel("You can always change this later in settings");
        noteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        noteLabel.setForeground(new Color(148, 163, 184));
        noteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(noteLabel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(contentPanel, gbc);

        add(mainPanel);
    }

    private JPanel createRoleCard(String title, String[] features, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(accentColor);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(15));

        for (String feature : features) {
            JLabel featureLabel = new JLabel(feature);
            featureLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            featureLabel.setForeground(new Color(71, 85, 105));
            featureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(featureLabel);
            card.add(Box.createVerticalStrut(5));
        }

        return card;
    }
}