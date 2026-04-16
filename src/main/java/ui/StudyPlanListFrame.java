package ui;

import model.User;
import model.StudyPlan;
import dao.StudyPlanDAO;
import dao.StudyTaskDAO;
import dao.UserDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudyPlanListFrame extends JFrame {

    private NormalStudyPlannerFrame parentFrame;   // reference to main frame (ADD THIS)
    private User user;
    private StudyPlanDAO studyPlanDAO;
    private StudyTaskDAO studyTaskDAO;
    private UserDAO userDAO;
    private JTable plansTable;
    private DefaultTableModel tableModel;

    private final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private final Color DANGER_COLOR = new Color(239, 68, 68);
    private final Color CARD_BG = Color.WHITE;
    private final Color BORDER_COLOR = new Color(229, 231, 235);

    // UPDATED CONSTRUCTOR – takes parent frame
    public StudyPlanListFrame(NormalStudyPlannerFrame parent, User user) {
        this.parentFrame = parent;   // store parent
        this.user = user;
        this.studyPlanDAO = new StudyPlanDAO();
        this.studyTaskDAO = new StudyTaskDAO();
        this.userDAO = new UserDAO();

        initUI();
        loadPlans();
    }

    private void initUI() {
        setTitle("My Study Plans");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(CARD_BG);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BG);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Your Study Plans");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setBackground(PRIMARY_COLOR);
        refreshBtn.setBorderPainted(false);
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> loadPlans());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        // Table with two button columns
        String[] columns = {"Plan ID", "Subjects", "Deadline", "Tasks", "Progress", "Manage", "Activate"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return c == 5 || c == 6; }
        };
        plansTable = new JTable(tableModel);
        plansTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        plansTable.setRowHeight(45);
        plansTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        plansTable.getTableHeader().setBackground(new Color(249, 250, 251));
        plansTable.setShowGrid(false);

        // Set custom renderers and editors for button columns
        plansTable.getColumn("Manage").setCellRenderer(new ManageButtonRenderer());
        plansTable.getColumn("Manage").setCellEditor(new ManageButtonEditor(new JCheckBox()));
        plansTable.getColumn("Activate").setCellRenderer(new ActivateButtonRenderer());
        plansTable.getColumn("Activate").setCellEditor(new ActivateButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(plansTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void loadPlans() {
        tableModel.setRowCount(0);
        List<StudyPlan> plans = studyPlanDAO.findByUserId(user.getId());
        Integer activePlanId = user.getActivePlanId();

        for (StudyPlan plan : plans) {
            List<model.StudyTask> tasks = studyTaskDAO.findByGoalId(plan.getId());
            int total = tasks.size();
            int completed = (int) tasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
            int progress = total > 0 ? (completed * 100 / total) : 0;

            // Debug line to verify each plan's task counts
            System.out.println("Plan " + plan.getId() + ": " + completed + "/" + total + " tasks");

            String subjectDisplay = plan.getSubjects() != null ? plan.getSubjects().replace(",", ", ") : plan.getSubjectName();
            if (subjectDisplay == null) subjectDisplay = "N/A";

            String actionText = (activePlanId != null && activePlanId == plan.getId()) ? "Deactivate" : "Activate";

            tableModel.addRow(new Object[]{
                    plan.getId(),
                    subjectDisplay,
                    plan.getDeadline().toString(),
                    completed + "/" + total,
                    progress + "%",
                    "Manage",
                    actionText
            });
        }
    }

    // Renderer for Manage button
    class ManageButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ManageButtonRenderer() {
            setOpaque(true);
            setBackground(PRIMARY_COLOR);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setBorderPainted(false);
            setFocusPainted(false);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // Editor for Manage button
    class ManageButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int selectedRow;
        private int selectedPlanId;

        public ManageButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(PRIMARY_COLOR);
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setBorderPainted(false);
            button.setFocusPainted(false);

            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            selectedRow = row;
            selectedPlanId = (int) table.getValueAt(row, 0);
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                StudyPlan plan = studyPlanDAO.findById(selectedPlanId);
                if (plan != null) {
                    SwingUtilities.invokeLater(() -> {
                        new PlanManagementFrame(user, plan).setVisible(true);
                    });
                }
            }
            isPushed = false;
            return label;
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

    // Renderer for Activate/Deactivate button
    class ActivateButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ActivateButtonRenderer() {
            setOpaque(true);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setBorderPainted(false);
            setFocusPainted(false);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            String text = (value == null) ? "" : value.toString();
            setText(text);
            if ("Deactivate".equals(text)) {
                setBackground(DANGER_COLOR);
            } else {
                setBackground(SUCCESS_COLOR);
            }
            return this;
        }
    }

    // Editor for Activate/Deactivate button (UPDATED with refresh callback)
    class ActivateButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int selectedRow;
        private int selectedPlanId;
        private String currentAction;

        public ActivateButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            selectedRow = row;
            selectedPlanId = (int) table.getValueAt(row, 0);
            currentAction = (String) value;
            label = currentAction;
            button.setText(label);
            if ("Deactivate".equals(currentAction)) {
                button.setBackground(DANGER_COLOR);
            } else {
                button.setBackground(SUCCESS_COLOR);
            }
            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                if ("Deactivate".equals(currentAction)) {
                    userDAO.updateActivePlan(user.getId(), null);
                    user.setActivePlanId(null);
                    JOptionPane.showMessageDialog(button, "Plan deactivated.");
                    // --- ADD THIS CALLBACK ---
                    if (parentFrame != null) {
                        parentFrame.refreshDashboardFromPlans();
                    }
                } else {
                    userDAO.updateActivePlan(user.getId(), selectedPlanId);
                    user.setActivePlanId(selectedPlanId);
                    JOptionPane.showMessageDialog(button, "Plan " + selectedPlanId + " is now active.");
                    // --- ADD THIS CALLBACK ---
                    if (parentFrame != null) {
                        parentFrame.refreshDashboardFromPlans();
                    }
                }
                // Refresh table to update button texts
                loadPlans();
            }
            isPushed = false;
            return label;
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
}