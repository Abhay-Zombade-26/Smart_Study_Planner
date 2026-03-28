package ui;

import config.AppConfig;
import model.User;
import service.AuthService;
import service.GitHubAuthService;
import service.GoogleAuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
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
    private JLabel githubNoteLabel;

    // Colors
    private final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private final Color SECONDARY_COLOR = new Color(16, 185, 129);
    private final Color GOOGLE_COLOR = new Color(66, 133, 244);
    private final Color GITHUB_COLOR = new Color(36, 41, 47);
    private final Color BG_GRADIENT_START = new Color(245, 247, 250);
    private final Color BG_GRADIENT_END = new Color(255, 255, 255);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private final Color TEXT_SECONDARY = new Color(107, 114, 128);

    public LoginFrame() {
        initUI();
        setupEventListeners();
        new Thread(this::startOAuthServer).start();
    }

    private void initUI() {
        setTitle("Smart Study Planner - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(false);

        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Main panel with gradient background
        mainPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, BG_GRADIENT_START, getWidth(), getHeight(), BG_GRADIENT_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new GridBagLayout());
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
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 236, 240), 1, true),
                new EmptyBorder(50, 80, 50, 80)
        ));
        panel.setPreferredSize(new Dimension(500, 650));
        panel.setMaximumSize(new Dimension(500, 650));

        // App Logo/Icon
        JLabel iconLabel = new JLabel("📚");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 64));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(iconLabel);
        panel.add(Box.createVerticalStrut(10));

        // App Title
        JLabel titleLabel = new JLabel("Smart Study Planner");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));

        // Subtitle
        JLabel subtitleLabel = new JLabel("Goal-Based Learning with GitHub Integration");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(subtitleLabel);
        panel.add(Box.createVerticalStrut(30));

        // Status indicator with animation
        statusLabel = new JLabel("⚡ Initializing server...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 116, 139));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(20));

        // Separator
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(300, 1));
        separator.setForeground(new Color(229, 231, 235));
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(separator);
        panel.add(Box.createVerticalStrut(30));

        // Role selection title
        JLabel roleLabel = new JLabel("Choose Your Login Method");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        roleLabel.setForeground(TEXT_PRIMARY);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(roleLabel);
        panel.add(Box.createVerticalStrut(25));

        // Google Login Button
        googleLoginBtn = createModernButton(
                "Continue with Google",
                GOOGLE_COLOR
        );
        googleLoginBtn.setToolTipText("Login with Google for Normal Student features");
        panel.add(googleLoginBtn);
        panel.add(Box.createVerticalStrut(15));

        // OR Divider
        JPanel orPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        orPanel.setBackground(CARD_BG);
        JSeparator leftSep = new JSeparator();
        leftSep.setPreferredSize(new Dimension(80, 1));
        JLabel orLabel = new JLabel("or");
        orLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        orLabel.setForeground(TEXT_SECONDARY);
        JSeparator rightSep = new JSeparator();
        rightSep.setPreferredSize(new Dimension(80, 1));
        orPanel.add(leftSep);
        orPanel.add(orLabel);
        orPanel.add(rightSep);
        orPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(orPanel);
        panel.add(Box.createVerticalStrut(15));

        // GitHub Login Button
        githubLoginBtn = createModernButton(
                "Continue with GitHub",
                GITHUB_COLOR
        );
        githubLoginBtn.setToolTipText("Login with GitHub for IT Student features");
        panel.add(githubLoginBtn);
        panel.add(Box.createVerticalStrut(10));

        // GitHub note
        githubNoteLabel = new JLabel("💡 Use GitHub login for better experience as an IT Student");
        githubNoteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        githubNoteLabel.setForeground(new Color(245, 158, 11));
        githubNoteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(githubNoteLabel);
        panel.add(Box.createVerticalStrut(30));

        // Features section
        JPanel featuresPanel = new JPanel();
        featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));
        featuresPanel.setBackground(CARD_BG);
        featuresPanel.setAlignmentX(Component.CENTER_ALIGNMENT);


        panel.add(Box.createVerticalStrut(20));

        // Footer
        JLabel footerLabel = new JLabel("© 2026 Smart Study Planner. All rights reserved.");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(new Color(148, 163, 184));
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(footerLabel);

        return panel;
    }

    private JButton createModernButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(320, 50));
        button.setMaximumSize(new Dimension(320, 50));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private void setupEventListeners() {
        googleLoginBtn.addActionListener(e -> {
            if (serverStarted) {
                statusLabel.setText("🔑 Opening Google login...");
                try {
                    GoogleAuthService googleService = new GoogleAuthService();
                    String url = googleService.getAuthorizationUrl();
                    Desktop.getDesktop().browse(new URI(url));
                    statusLabel.setText("✅ Check your browser to continue login");
                } catch (Exception ex) {
                    statusLabel.setText("❌ Error: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this,
                            "Failed to initialize Google login: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                statusLabel.setText("⏳ Server starting... please wait");
            }
        });

        githubLoginBtn.addActionListener(e -> {
            if (serverStarted) {
                statusLabel.setText("🔑 Opening GitHub login...");
                try {
                    GitHubAuthService gitHubService = new GitHubAuthService();
                    String url = gitHubService.getAuthorizationUrl();
                    Desktop.getDesktop().browse(new URI(url));
                    statusLabel.setText("✅ Check your browser to continue login");
                } catch (Exception ex) {
                    statusLabel.setText("❌ Error: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this,
                            "Failed to initialize GitHub login: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                statusLabel.setText("⏳ Server starting... please wait");
            }
        });
    }

    private void startOAuthServer() {
        int maxAttempts = 3;
        int attempt = 0;

        while (attempt < maxAttempts && !serverStarted) {
            try {
                attempt++;
                System.out.println("Attempt " + attempt + " to start server on port 8888...");

                server = HttpServer.create(new InetSocketAddress("localhost", 8888), 0);

                // Google callback
                server.createContext("/google-callback", new HttpHandler() {
                    @Override
                    public void handle(HttpExchange exchange) throws IOException {
                        handleOAuthCallback(exchange, "GOOGLE");
                    }
                });

                // GitHub callback
                server.createContext("/callback", new HttpHandler() {
                    @Override
                    public void handle(HttpExchange exchange) throws IOException {
                        handleOAuthCallback(exchange, "GITHUB");
                    }
                });

                server.setExecutor(null);
                server.start();
                serverStarted = true;

                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("✅ Server ready - Click to login");
                    statusLabel.setForeground(new Color(46, 204, 113));
                    System.out.println("✅ OAuth server started on http://localhost:8888");
                    System.out.println("   GitHub callback: http://localhost:8888/callback");
                    System.out.println("   Google callback: http://localhost:8888/google-callback");
                });

            } catch (BindException e) {
                System.err.println("❌ Port 8888 is already in use (attempt " + attempt + ")");

                // Try to kill the process using port 8888
                try {
                    Process process = Runtime.getRuntime().exec("cmd /c netstat -ano | findstr :8888");
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(process.getInputStream())
                    );
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("LISTENING")) {
                            String[] parts = line.trim().split("\\s+");
                            String pid = parts[parts.length - 1];
                            Runtime.getRuntime().exec("taskkill /F /PID " + pid);
                            System.out.println("✅ Killed process with PID: " + pid);
                            Thread.sleep(1000);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            } catch (IOException e) {
                System.err.println("❌ Failed to start server: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (!serverStarted) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("❌ Server failed to start");
                statusLabel.setForeground(new Color(220, 38, 38));
                JOptionPane.showMessageDialog(this,
                        "Failed to start authentication server after " + maxAttempts + " attempts.\n" +
                                "Please make sure port 8888 is available and try restarting the application.",
                        "Server Error",
                        JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    private void handleOAuthCallback(HttpExchange exchange, String provider) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String code = null;

        if (query != null && query.contains("code=")) {
            code = query.split("code=")[1].split("&")[0];
        }

        String response = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Segoe UI', sans-serif; text-align: center; padding-top: 80px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); margin: 0; }" +
                ".container { background: white; max-width: 500px; margin: 0 auto; padding: 40px; border-radius: 20px; box-shadow: 0 20px 60px rgba(0,0,0,0.3); }" +
                "h2 { color: #2ecc71; margin-bottom: 20px; }" +
                ".success { color: #27ae60; font-size: 48px; margin-bottom: 20px; }" +
                "p { color: #666; line-height: 1.6; }" +
                ".button { display: inline-block; margin-top: 20px; padding: 10px 30px; background: #667eea; color: white; text-decoration: none; border-radius: 5px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='success'>✅</div>" +
                "<h2>Authentication Successful!</h2>" +
                "<p>You have successfully logged in to Smart Study Planner.</p>" +
                "<p>You can close this window and return to the application.</p>" +
                "<p style='font-size: 12px; color: #999;'>The application will automatically continue...</p>" +
                "</div>" +
                "</body>" +
                "</html>";

        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes("UTF-8"));
            os.flush();
        }

        if (code != null) {
            final String authCode = code;
            final HttpServer currentServer = server;
            SwingUtilities.invokeLater(() -> {
                authenticateUser(provider, authCode);
                currentServer.stop(0);
                serverStarted = false;
            });
        }

        exchange.close();
    }

    private void authenticateUser(String provider, String code) {
        User user = null;
        AuthService authService;

        try {
            if (provider.equals("GOOGLE")) {
                System.out.println("\n=== PROCESSING GOOGLE LOGIN ===");
                authService = new GoogleAuthService();
                user = authService.authenticate(code);
                if (user != null) {
                    System.out.println("✅ Google login successful for: " + user.getEmail());
                }
            } else {
                System.out.println("\n=== PROCESSING GITHUB LOGIN ===");
                authService = new GitHubAuthService();
                user = authService.authenticate(code);
                if (user != null) {
                    System.out.println("✅ GitHub login successful for: " + user.getEmail());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Authentication error: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Authentication failed: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (user != null) {
            System.out.println("\n🎉 Login successful! Redirecting...\n");
            dispose();

            if (provider.equals("GOOGLE")) {
                new NormalStudyPlannerFrame(user).setVisible(true);
            } else {
                new RoleSelectionFrame(user).setVisible(true);
            }
        } else {
            System.err.println("❌ Authentication returned null user");
            JOptionPane.showMessageDialog(this,
                    "Authentication failed. Please check your OAuth credentials in config.properties",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
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
}