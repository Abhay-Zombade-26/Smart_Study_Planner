package ui;

import dao.UserDAO;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProfileFrame extends JPanel {

    private User user;
    private UserDAO userDAO;

    public ProfileFrame(User user) {
        this.user = user;
        this.userDAO = new UserDAO();

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("👤 Profile Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 33, 33));

        headerPanel.add(titleLabel, BorderLayout.NORTH);

        add(headerPanel, BorderLayout.NORTH);

        // Profile content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Avatar section
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(createAvatarSection(), gbc);

        // Profile form
        gbc.gridy = 1;
        contentPanel.add(createProfileForm(), gbc);

        // Account info
        gbc.gridy = 2;
        contentPanel.add(createAccountInfo(), gbc);

        // Danger zone
        gbc.gridy = 3;
        contentPanel.add(createDangerZone(), gbc);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createAvatarSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(30, 30, 30, 30)
        ));

        // Avatar placeholder
        JLabel avatarLabel = new JLabel("👤");
        avatarLabel.setFont(new Font("Segoe UI", Font.PLAIN, 64));
        avatarLabel.setForeground(new Color(100, 116, 139));
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(user.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setForeground(new Color(33, 33, 33));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel(user.getRole().getDisplayName());
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleLabel.setForeground(new Color(52, 152, 219));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton changeAvatarBtn = new JButton("Change Avatar");
        changeAvatarBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        changeAvatarBtn.setForeground(new Color(33, 33, 33));
        changeAvatarBtn.setBackground(Color.WHITE);
        changeAvatarBtn.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        changeAvatarBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        changeAvatarBtn.setMaximumSize(new Dimension(150, 35));
        changeAvatarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panel.add(avatarLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(roleLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(changeAvatarBtn);

        return panel;
    }

    private JPanel createProfileForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Profile Information");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(33, 33, 33));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // Name field
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel nameLabel = new JLabel("Full Name");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(nameLabel, gbc);

        gbc.gridx = 1;
        JTextField nameField = new JTextField(user.getName(), 20);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameField.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(nameField, gbc);

        // Email field
        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(emailLabel, gbc);

        gbc.gridx = 1;
        JTextField emailField = new JTextField(user.getEmail(), 20);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailField.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        emailField.setEditable(false);
        emailField.setBackground(new Color(248, 250, 252));
        panel.add(emailField, gbc);

        // GitHub username (if IT student)
        if (user.getGithubUsername() != null) {
            gbc.gridy = 3;
            gbc.gridx = 0;
            JLabel githubLabel = new JLabel("GitHub");
            githubLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            panel.add(githubLabel, gbc);

            gbc.gridx = 1;
            JTextField githubField = new JTextField(user.getGithubUsername(), 20);
            githubField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            githubField.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
            githubField.setEditable(false);
            githubField.setBackground(new Color(248, 250, 252));
            panel.add(githubField, gbc);
        }

        // Save button
        gbc.gridy = 4;
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(new Color(52, 152, 219));
        saveBtn.setBorderPainted(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setPreferredSize(new Dimension(150, 40));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        saveBtn.addActionListener(e -> {
            user.setName(nameField.getText());
            userDAO.update(user);
            JOptionPane.showMessageDialog(this, "Profile updated successfully!");
        });

        panel.add(saveBtn, gbc);

        return panel;
    }

    private JPanel createAccountInfo() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Account Information");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(33, 33, 33));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel providerLabel = new JLabel("Connected with");
        providerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(providerLabel, gbc);

        gbc.gridx = 1;
        JLabel providerValue = new JLabel(user.getOauthProvider());
        providerValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        providerValue.setForeground(user.getOauthProvider().equals("GITHUB") ?
                new Color(36, 41, 47) : new Color(66, 133, 244));
        panel.add(providerValue, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel memberLabel = new JLabel("Member since");
        memberLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(memberLabel, gbc);

        gbc.gridx = 1;
        JLabel memberValue = new JLabel("January 2024");
        memberValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(memberValue, gbc);

        return panel;
    }

    private JPanel createDangerZone() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 38, 38), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("⚠️ Danger Zone");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(220, 38, 38));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel deleteLabel = new JLabel("Delete account and all associated data");
        deleteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(deleteLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JButton deleteBtn = new JButton("Delete Account");
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setBackground(new Color(220, 38, 38));
        deleteBtn.setBorderPainted(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setPreferredSize(new Dimension(150, 40));
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete your account?\nThis action cannot be undone.",
                    "Delete Account",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                // Delete account logic here
                JOptionPane.showMessageDialog(this, "Account deleted successfully.");
                new LoginFrame().setVisible(true);
                SwingUtilities.getWindowAncestor(this).dispose();
            }
        });

        panel.add(deleteBtn, gbc);

        return panel;
    }

    public JPanel getMainPanel() {
        return this;
    }
}