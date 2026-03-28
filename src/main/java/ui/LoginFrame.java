package ui;

import model.User;
import service.GitHubOAuthService;
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
    private HttpServer server;
    private boolean serverStarted = false;
    private JLabel statusLabel;

    public LoginFrame() {
        initUI();
        setupEventListeners();
        new Thread(this::startOAuthServer).start();
    }

    private void initUI() {
        setTitle("Smart Study Planner - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        panel.setPreferredSize(new Dimension(400, 500));
        panel.setMaximumSize(new Dimension(400, 500));

        JLabel titleLabel = new JLabel("📚 Smart Study Planner");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 33, 33));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Choose your login method");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 116, 139));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalStrut(20));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(subtitleLabel);
        panel.add(Box.createVerticalStrut(40));

        statusLabel = new JLabel("⚡ Initializing...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 116, 139));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(20));

        // Google Login Button
        googleLoginBtn = createOAuthButton(
                "Continue with Google",
                new Color(66, 133, 244)
        );
        googleLoginBtn.setToolTipText("Login with Google - For regular students");
        panel.add(googleLoginBtn);
        panel.add(Box.createVerticalStrut(15));

        JLabel orLabel = new JLabel("or");
        orLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        orLabel.setForeground(new Color(148, 163, 184));
        orLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(orLabel);
        panel.add(Box.createVerticalStrut(15));

        // GitHub Login Button
        githubLoginBtn = createOAuthButton(
                "Continue with GitHub",
                new Color(36, 41, 47)
        );
        githubLoginBtn.setToolTipText("Login with GitHub - For IT students");
        panel.add(githubLoginBtn);

        panel.add(Box.createVerticalStrut(20));

        JLabel footerLabel = new JLabel("© 2024 Smart Study Planner");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(new Color(148, 163, 184));
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(footerLabel);

        return panel;
    }

    private JButton createOAuthButton(String text, Color bgColor) {
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

    private void setupEventListeners() {
        googleLoginBtn.addActionListener(e -> {
            if (serverStarted) {
                try {
                    statusLabel.setText("Opening Google login...");
                    GoogleAuthService googleService = new GoogleAuthService();
                    String url = googleService.getAuthorizationUrl();
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this,
                            "Failed to initialize Google login: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                statusLabel.setText("Server starting... please wait");
            }
        });

        githubLoginBtn.addActionListener(e -> {
            if (serverStarted) {
                try {
                    statusLabel.setText("Opening GitHub login...");
                    GitHubOAuthService gitHubService = new GitHubOAuthService();
                    String url = gitHubService.getAuthorizationUrl();
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this,
                            "Failed to initialize GitHub login: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                statusLabel.setText("Server starting... please wait");
            }
        });
    }

    private void startOAuthServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(8888), 0);

            // Store reference as final for lambda
            final HttpServer currentServer = server;

            // GitHub callback (uses /callback)
            currentServer.createContext("/callback", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    handleGitHubCallback(exchange);
                }
            });

            // Google callback (uses /google-callback)
            currentServer.createContext("/google-callback", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    handleGoogleCallback(exchange);
                }
            });

            currentServer.setExecutor(null);
            currentServer.start();
            serverStarted = true;

            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("✅ Server ready - Click to login");
                statusLabel.setForeground(new Color(46, 204, 113));
                System.out.println("✅ OAuth server started on http://localhost:8888");
                System.out.println("   GitHub callback: http://localhost:8888/callback");
                System.out.println("   Google callback: http://localhost:8888/google-callback");
            });

        } catch (IOException e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("❌ Failed to start server");
            });
        }
    }

    private void handleGitHubCallback(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String code = extractCode(query);

        sendSuccessResponse(exchange);

        if (code != null) {
            final String authCode = code;
            final HttpServer currentServer = server;

            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    SwingUtilities.invokeLater(() -> authenticateGitHub(authCode, currentServer));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void handleGoogleCallback(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String code = extractCode(query);

        sendSuccessResponse(exchange);

        if (code != null) {
            final String authCode = code;
            final HttpServer currentServer = server;

            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    SwingUtilities.invokeLater(() -> authenticateGoogle(authCode, currentServer));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private String extractCode(String query) {
        if (query != null && query.contains("code=")) {
            return query.split("code=")[1].split("&")[0];
        }
        return null;
    }

    private void sendSuccessResponse(HttpExchange exchange) throws IOException {
        String response = "<html><body style='text-align:center;padding-top:50px;font-family:Arial;'>" +
                "<h2 style='color:#2ecc71;'>✅ Authentication Successful!</h2>" +
                "<p>You can close this window and return to the application.</p>" +
                "</body></html>";

        exchange.getResponseHeaders().set("Content-Type", "text/html");
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
        exchange.close();
    }

    private void authenticateGitHub(String code, HttpServer currentServer) {
        try {
            statusLabel.setText("Authenticating with GitHub...");
            System.out.println("🔑 GitHub code: " + code);

            GitHubOAuthService gitHubService = new GitHubOAuthService();
            User user = gitHubService.authenticate(code);

            if (user != null) {
                handleSuccessfulLogin(user, "GITHUB", currentServer);
            } else {
                showAuthenticationFailed();
            }
        } catch (Exception e) {
            showAuthenticationError(e);
        }
    }

    private void authenticateGoogle(String code, HttpServer currentServer) {
        try {
            statusLabel.setText("Authenticating with Google...");
            System.out.println("🔑 Google code: " + code);

            GoogleAuthService googleService = new GoogleAuthService();
            User user = googleService.authenticate(code);

            if (user != null) {
                handleSuccessfulLogin(user, "GOOGLE", currentServer);
            } else {
                showAuthenticationFailed();
            }
        } catch (Exception e) {
            showAuthenticationError(e);
        }
    }

    private void handleSuccessfulLogin(User user, String provider, HttpServer currentServer) {
        statusLabel.setText("✅ Authentication successful!");
        System.out.println("✅ User authenticated: " + user.getEmail());

        if (currentServer != null) {
            currentServer.stop(0);
            serverStarted = false;
        }

        dispose();

        if ("GOOGLE".equals(provider)) {
            System.out.println("➡️ Redirecting to Normal Student UI");
            SwingUtilities.invokeLater(() -> new NormalStudyPlannerFrame(user).setVisible(true));
        } else {
            System.out.println("➡️ Redirecting to Role Selection");
            SwingUtilities.invokeLater(() -> new RoleSelectionFrame(user).setVisible(true));
        }
    }

    private void showAuthenticationFailed() {
        statusLabel.setText("❌ Authentication failed");
        JOptionPane.showMessageDialog(this,
                "Authentication failed. Check console for details.",
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showAuthenticationError(Exception e) {
        statusLabel.setText("❌ Error: " + e.getMessage());
        e.printStackTrace();
        JOptionPane.showMessageDialog(this,
                "Error during authentication: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void dispose() {
        if (server != null) {
            server.stop(0);
            serverStarted = false;
            System.out.println("🛑 OAuth server stopped");
        }
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}