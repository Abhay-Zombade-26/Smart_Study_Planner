package ui;

import config.AppConfig;
import model.User;
import service.AuthService;
import service.GitHubAuthService;
import service.GoogleAuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class LoginFrame extends JFrame {
    
    private JPanel mainPanel;
    private JButton googleLoginBtn;
    private JButton githubLoginBtn;
    
    public LoginFrame() {
        initUI();
        setupEventListeners();
    }
    
    private void initUI() {
        setTitle("Smart Study Planner - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        
        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel centerPanel = createCenterPanel();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        
        mainPanel.add(centerPanel, gbc);
        add(mainPanel);
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 236, 240), 1, true),
            new EmptyBorder(40, 60, 40, 60)
        ));
        panel.setPreferredSize(new Dimension(400, 450));
        panel.setMaximumSize(new Dimension(400, 450));
        
        JLabel titleLabel = new JLabel("📚 Smart Study Planner");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 33, 33));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Goal-Based GitHub Integrated Planner");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 116, 139));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(Box.createVerticalStrut(20));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(subtitleLabel);
        panel.add(Box.createVerticalStrut(40));
        
        JLabel roleLabel = new JLabel("Select your role to continue");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleLabel.setForeground(new Color(71, 85, 105));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(roleLabel);
        panel.add(Box.createVerticalStrut(20));
        
        googleLoginBtn = createOAuthButton(
            "Continue with Google",
            new Color(66, 133, 244),
            "G"
        );
        panel.add(googleLoginBtn);
        panel.add(Box.createVerticalStrut(15));
        
        JLabel orLabel = new JLabel("or");
        orLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        orLabel.setForeground(new Color(148, 163, 184));
        orLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(orLabel);
        panel.add(Box.createVerticalStrut(15));
        
        githubLoginBtn = createOAuthButton(
            "Continue with GitHub",
            new Color(36, 41, 47),
            "GH"
        );
        panel.add(githubLoginBtn);
        
        panel.add(Box.createVerticalStrut(30));
        
        JLabel footerLabel = new JLabel("© 2024 Smart Study Planner");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(new Color(148, 163, 184));
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(footerLabel);
        
        return panel;
    }
    
    private JButton createOAuthButton(String text, Color bgColor, String icon) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(280, 45));
        button.setMaximumSize(new Dimension(280, 45));
        
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 1, true),
            new EmptyBorder(10, 20, 10, 20)
        ));
        
        return button;
    }
    
    private void setupEventListeners() {
        googleLoginBtn.addActionListener(e -> handleGoogleLogin());
        githubLoginBtn.addActionListener(e -> handleGitHubLogin());
    }
    
    private void handleGoogleLogin() {
        String url = new GoogleAuthService().getAuthorizationUrl();
        startOAuthServer("GOOGLE", url);
    }
    
    private void handleGitHubLogin() {
        String url = new GitHubAuthService().getAuthorizationUrl();
        startOAuthServer("GITHUB", url);
    }
    
    private void startOAuthServer(String provider, String authUrl) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private HttpServer server;
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    server = HttpServer.create(new InetSocketAddress(8888), 0);
                    
                    String contextPath = provider.equals("GOOGLE") ? "/callback" : "/github-callback";
                    
                    server.createContext(contextPath, new HttpHandler() {
                        @Override
                        public void handle(HttpExchange exchange) throws IOException {
                            String query = exchange.getRequestURI().getQuery();
                            String code = null;
                            
                            if (query != null && query.contains("code=")) {
                                code = query.split("code=")[1].split("&")[0];
                            }
                            
                            String response = 
                                "<html>" +
                                "<body style='font-family: sans-serif; text-align: center; padding-top: 50px;'>" +
                                "    <h2>Authentication Successful!</h2>" +
                                "    <p>You can close this window and return to the application.</p>" +
                                "</body>" +
                                "</html>";
                            
                            exchange.sendResponseHeaders(200, response.length());
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write(response.getBytes());
                            }
                            
                            if (code != null) {
                                final String authCode = code;
                                final HttpServer currentServer = server;
                                SwingUtilities.invokeLater(() -> {
                                    authenticateUser(provider, authCode);
                                    currentServer.stop(0);
                                });
                            }
                        }
                    });
                    
                    server.setExecutor(null);
                    server.start();
                    
                    // Open browser
                    try {
                        Desktop.getDesktop().browse(new URI(authUrl));
                    } catch (Exception e) {
                        e.printStackTrace();
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(LoginFrame.this,
                                "Failed to open browser. Please visit:\n" + authUrl,
                                "Open URL Manually",
                                JOptionPane.INFORMATION_MESSAGE)
                        );
                    }
                    
                } catch (IOException e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(LoginFrame.this,
                            "Failed to start authentication server",
                            "Error",
                            JOptionPane.ERROR_MESSAGE)
                    );
                }
                return null;
            }
        };
        
        worker.execute();
    }
    
    private void authenticateUser(String provider, String code) {
        User user = null;
        AuthService authService;
        
        if (provider.equals("GOOGLE")) {
            authService = new GoogleAuthService();
            user = authService.authenticate(code);
        } else {
            authService = new GitHubAuthService();
            user = authService.authenticate(code);
        }
        
        if (user != null) {
            dispose();
            if (provider.equals("GOOGLE")) {
                new NormalStudyPlannerFrame(user).setVisible(true);
            } else {
                new RoleSelectionFrame(user).setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "Authentication failed. Please try again.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}