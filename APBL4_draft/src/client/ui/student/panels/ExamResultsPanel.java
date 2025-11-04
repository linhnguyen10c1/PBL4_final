package client.ui.student.panels;

import client.controller.StudentExamController;
import client.ui.student.interfaces.StudentDashboardCallbacks;
import client.ui.student.components.ExamResultsTable;
import model.ExamResult;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Exam Results Panel - Shows student's exam history and results
 */
public class ExamResultsPanel extends JPanel {
    
    private StudentDashboardCallbacks callbacks;
    private StudentExamController examController;
    
    // UI Components
    private ExamResultsTable resultsTable;
    private JButton refreshButton;
    private JButton viewDetailButton;
    private JLabel statusLabel;
    private JLabel summaryLabel;
    
    // Data
    private List<ExamResult> currentResults;
    
    public ExamResultsPanel(StudentDashboardCallbacks callbacks, StudentExamController examController) {
        this.callbacks = callbacks;
        this.examController = examController;
        
        initializeUI();
        setupEventHandlers();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content - table
        resultsTable = new ExamResultsTable();
        resultsTable.setSelectionListener(new ExamResultsTable.ExamResultSelectionListener() {
            @Override
            public void onExamResultSelected(ExamResult result) {
                viewDetailButton.setEnabled(true);
                updateStatus("Selected: " + result.getRoomName());
            }
            
            @Override
            public void onExamResultDeselected() {
                viewDetailButton.setEnabled(false);
                updateStatus("Ready");
            }
            
            @Override
            public void onExamResultDoubleClicked(ExamResult result) {
                viewResultDetail();
            }
        });
        
        add(resultsTable, BorderLayout.CENTER);
        
        // Bottom panel
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Title and summary
        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("My Exam Results");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        summaryLabel = new JLabel("No results available");
        summaryLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        summaryLabel.setForeground(Color.GRAY);
        
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(summaryLabel, BorderLayout.SOUTH);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshButton = new JButton("🔄 Refresh");
        refreshButton.addActionListener(e -> loadResults());
        buttonPanel.add(refreshButton);
        
        panel.add(titlePanel, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        viewDetailButton = new JButton("📊 View Details");
        viewDetailButton.setEnabled(false);
        viewDetailButton.addActionListener(e -> viewResultDetail());
        buttonPanel.add(viewDetailButton);
        
        // Status label
        statusLabel = new JLabel("Ready to load results");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // Double-click on table
        resultsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewResultDetail();
                }
            }
        });
    }
    
    public void loadResults() {
        updateStatus("Loading exam results...");
        refreshButton.setEnabled(false);
        
        if (callbacks != null) {
            callbacks.onRefreshResultsRequested();
        }
    }
    
    private void viewResultDetail() {
        ExamResult selectedResult = resultsTable.getSelectedExamResult();
        if (selectedResult == null) {
            JOptionPane.showMessageDialog(this,
                "Please select an exam result to view details.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (callbacks != null) {
            callbacks.onResultDetailRequested(selectedResult);
        }
    }
    
    private void updateStatus(String message) {
        statusLabel.setText(message);
        if (callbacks != null) {
            callbacks.updateStatus(message);
        }
    }
    
    private void updateSummary() {
        if (currentResults == null || currentResults.isEmpty()) {
            summaryLabel.setText("No exam results found");
            return;
        }
        
        int totalExams = currentResults.size();
        int passedExams = (int) currentResults.stream().filter(ExamResult::isPassed).count();
        double averageScore = currentResults.stream()
            .mapToDouble(ExamResult::getPercentage)
            .average()
            .orElse(0.0);
        
        summaryLabel.setText(String.format(
            "Total: %d exams | Passed: %d | Average: %.1f%%",
            totalExams, passedExams, averageScore));
    }
    
    // Public methods for external control
    public void setExamResults(List<ExamResult> results) {
        this.currentResults = results;
        resultsTable.setExamResults(results);
        updateSummary();
        updateStatus("Loaded " + results.size() + " exam results");
        refreshButton.setEnabled(true);
    }
    
    public ExamResult getSelectedExamResult() {
        return resultsTable.getSelectedExamResult();
    }
    
    public void clearSelection() {
        resultsTable.clearSelection();
    }
}