package client.ui.student.components;

import model.ExamResult;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * ExamResultsTable - Table component for displaying exam results
 */
public class ExamResultsTable extends JPanel {
    
    private JTable table;
    private DefaultTableModel tableModel;
    private ExamResultSelectionListener selectionListener;
    private List<ExamResult> examResults;
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    public ExamResultsTable() {
        initializeUI();
        setupEventHandlers();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Create table model
        String[] columnNames = {
            "Exam", "Subject", "Date", "Score", "Percentage", "Grade", 
            "Status", "Time Spent", "Ranking"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 3: case 4: return Double.class;
                    case 8: return Integer.class;
                    default: return String.class;
                }
            }
        };
        
        // Create table
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(35);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Set column widths
        setupColumnWidths();
        
        // Custom renderers
        setupCustomRenderers();
        
        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(1000, 400));
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void setupColumnWidths() {
        table.getColumnModel().getColumn(0).setPreferredWidth(200); // Exam
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Subject
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Date
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Score
        table.getColumnModel().getColumn(4).setPreferredWidth(80);  // Percentage
        table.getColumnModel().getColumn(5).setPreferredWidth(60);  // Grade
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Status
        table.getColumnModel().getColumn(7).setPreferredWidth(100); // Time Spent
        table.getColumnModel().getColumn(8).setPreferredWidth(80);  // Ranking
    }
    
    private void setupCustomRenderers() {
        // Percentage renderer with color coding
        table.getColumnModel().getColumn(4).setCellRenderer(new PercentageRenderer());
        
        // Grade renderer with color coding
        table.getColumnModel().getColumn(5).setCellRenderer(new GradeRenderer());
        
        // Status renderer with color coding
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());
    }
    
    private void setupEventHandlers() {
        // Selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0 && selectionListener != null) {
                    ExamResult selectedResult = examResults.get(selectedRow);
                    selectionListener.onExamResultSelected(selectedResult);
                } else if (selectionListener != null) {
                    selectionListener.onExamResultDeselected();
                }
            }
        });
        
        // Double-click listener
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    if (selectionListener != null && examResults != null) {
                        ExamResult selectedResult = examResults.get(table.getSelectedRow());
                        selectionListener.onExamResultDoubleClicked(selectedResult);
                    }
                }
            }
        });
    }
    
    public void setExamResults(List<ExamResult> examResults) {
        this.examResults = examResults;
        updateTableData();
    }
    
    private void updateTableData() {
        tableModel.setRowCount(0);
        
        if (examResults != null) {
            for (ExamResult result : examResults) {
                Object[] rowData = {
                    result.getRoomName(),
                    result.getSubjectName(),
                    result.getSubmittedAt(),
                    String.format("%.1f / %.1f", result.getTotalScore(), result.getMaxScore()),
                    result.getPercentage(),
                    result.getGrade(),
                    result.getStatus(),
                    result.getFormattedTimeSpent(),
                    result.getRanking() > 0 ? result.getRanking() + "/" + result.getTotalParticipants() : "N/A"
                };
                tableModel.addRow(rowData);
            }
        }
    }
    
    public ExamResult getSelectedExamResult() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0 && examResults != null && selectedRow < examResults.size()) {
            return examResults.get(selectedRow);
        }
        return null;
    }
    
    public void clearSelection() {
        table.clearSelection();
    }
    
    public void setSelectionListener(ExamResultSelectionListener listener) {
        this.selectionListener = listener;
    }
    
    /**
     * Custom renderer for percentage column
     */
    private static class PercentageRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof Double) {
                double percentage = (Double) value;
                setText(String.format("%.1f%%", percentage));
                
                if (!isSelected) {
                    if (percentage >= 80) {
                        setForeground(new Color(0, 150, 0));
                    } else if (percentage >= 60) {
                        setForeground(new Color(255, 140, 0));
                    } else {
                        setForeground(Color.RED);
                    }
                } else {
                    setForeground(Color.WHITE);
                }
            }
            
            setHorizontalAlignment(SwingConstants.CENTER);
            return this;
        }
    }
    
    /**
     * Custom renderer for grade column
     */
    private static class GradeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            String grade = value.toString();
            
            if (!isSelected) {
                switch (grade) {
                    case "A":
                        setForeground(new Color(0, 150, 0));
                        setText("🏆 A");
                        break;
                    case "B":
                        setForeground(new Color(100, 200, 100));
                        setText("🥈 B");
                        break;
                    case "C":
                        setForeground(new Color(255, 140, 0));
                        setText("🥉 C");
                        break;
                    case "D":
                        setForeground(new Color(255, 100, 100));
                        setText("📝 D");
                        break;
                    case "F":
                        setForeground(Color.RED);
                        setText("❌ F");
                        break;
                    default:
                        setForeground(Color.BLACK);
                        setText(grade);
                        break;
                }
            } else {
                setForeground(Color.WHITE);
                setText(grade);
            }
            
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(getFont().deriveFont(Font.BOLD));
            return this;
        }
    }
    
    /**
     * Custom renderer for status column
     */
    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            String status = value.toString();
            
            if (!isSelected) {
                switch (status) {
                    case "SUBMITTED":
                        setForeground(new Color(0, 150, 0));
                        setText("✅ Submitted");
                        break;
                    case "AUTO_SUBMITTED":
                        setForeground(new Color(255, 140, 0));
                        setText("⏰ Auto-submitted");
                        break;
                    case "IN_PROGRESS":
                        setForeground(new Color(0, 100, 200));
                        setText("🔄 In Progress");
                        break;
                    default:
                        setForeground(Color.BLACK);
                        setText(status);
                        break;
                }
            } else {
                setForeground(Color.WHITE);
                setText(status);
            }
            
            return this;
        }
    }
    
    /**
     * Selection listener interface
     */
    public interface ExamResultSelectionListener {
        void onExamResultSelected(ExamResult result);
        void onExamResultDeselected();
        void onExamResultDoubleClicked(ExamResult result);
    }
}