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
    
    public LoginFrame() {
        initUI();
        setupEventListeners();
        // Start server in background thread
        new Thread(this::startOAuthServer).start();
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
        googleLoginBtn.addActionListener(e -> {
            if (serverStarted) {
                String url = new GoogleAuthService().getAuthorizationUrl();
                openBrowser(url);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Starting authentication server... Please try again in 2 seconds.",
                    "Server Starting",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        githubLoginBtn.addActionListener(e -> {
            if (serverStarted) {
                String url = new GitHubAuthService().getAuthorizationUrl();
                openBrowser(url);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Starting authentication server... Please try again in 2 seconds.",
                    "Server Starting",
                    JOptionPane.INFORMATION_MESSAGE);
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
                
                // Always use port 8888
                server = HttpServer.create(new InetSocketAddress("localhost", 8888), 0);
                
                // Google callback
                server.createContext("/callback", new HttpHandler() {
                    @Override
                    public void handle(HttpExchange exchange) throws IOException {
                        handleOAuthCallback(exchange, "GOOGLE");
                    }
                });
                
                // GitHub callback
                server.createContext("/github-callback", new HttpHandler() {
                    @Override
                    public void handle(HttpExchange exchange) throws IOException {
                        handleOAuthCallback(exchange, "GITHUB");
                    }
                });
                
                server.setExecutor(null);
                server.start();
                serverStarted = true;
                
                System.out.println("✅ OAuth server started successfully on port 8888");
                System.out.println("📎 Google callback: http://localhost:8888/callback");
                System.out.println("📎 GitHub callback: http://localhost:8888/github-callback");
                
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
                            Thread.sleep(1000); // Wait for process to die
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
        
        String response = 
            "<html>" +
            "<body style='font-family: sans-serif; text-align: center; padding-top: 50px;'>" +
            "    <h2 style='color: #2ecc71;'>✅ Authentication Successful!</h2>" +
            "    <p>You can close this window and return to the application.</p>" +
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
            SwingUtilities.invokeLater(() -> {
                authenticateUser(provider, authCode);
            });
        }
        
        exchange.close();
    }
    
    private void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // Fallback for systems without Desktop support
                String os = System.getProperty("os.name").toLowerCase();
                Runtime rt = Runtime.getRuntime();
                if (os.contains("win")) {
                    rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
                } else if (os.contains("mac")) {
                    rt.exec("open " + url);
                } else if (os.contains("nix") || os.contains("nux")) {
                    rt.exec("xdg-open " + url);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "<html>Failed to open browser automatically.<br>" +
                "Please visit this URL manually:<br><br>" +
                "<b>" + url + "</b></html>",
                "Open URL Manually",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void authenticateUser(String provider, String code) {
        User user = null;
        AuthService authService;
        
        try {
            if (provider.equals("GOOGLE")) {
                authService = new GoogleAuthService();
                user = authService.authenticate(code);
            } else {
                authService = new GitHubAuthService();
                user = authService.authenticate(code);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Authentication failed: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (user != null) {
            // Stop the server before disposing
            if (server != null) {
                server.stop(0);
                serverStarted = false;
                System.out.println("🛑 OAuth server stopped");
            }
            dispose();
            
            if (provider.equals("GOOGLE")) {
                new NormalStudyPlannerFrame(user).setVisible(true);
            } else {
                new RoleSelectionFrame(user).setVisible(true);
            }
        } else {
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