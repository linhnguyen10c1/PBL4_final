package client.ui.admin.components;

import model.Question;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class QuestionsTable extends JPanel {
    
    private JTable table;
    private DefaultTableModel tableModel;
    private QuestionSelectionListener selectionListener;
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public interface QuestionSelectionListener {
        void onQuestionSelected(Question question);
        void onQuestionDeselected();
        void onQuestionDoubleClicked(Question question);
    }
    
    public QuestionsTable() {
        initializeUI();
        setupEventHandlers();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Create table model
        String[] columnNames = {
            "ID", "Subject", "Question Text", "Correct Answer", "Difficulty", "Status", "Created At"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0: return Integer.class;
                    default: return String.class;
                }
            }
        };
        
        // Create table
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(120);  // Subject
        table.getColumnModel().getColumn(2).setPreferredWidth(300);  // Question Text
        table.getColumnModel().getColumn(3).setPreferredWidth(100);  // Correct Answer
        table.getColumnModel().getColumn(4).setPreferredWidth(80);   // Difficulty
        table.getColumnModel().getColumn(5).setPreferredWidth(80);   // Status
        table.getColumnModel().getColumn(6).setPreferredWidth(120);  // Created At
        
        // Add to scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Questions"));
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        // Selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0 && selectionListener != null) {
                    Question question = getQuestionFromRow(selectedRow);
                    selectionListener.onQuestionSelected(question);
                } else if (selectionListener != null) {
                    selectionListener.onQuestionDeselected();
                }
            }
        });
        
        // Double-click listener
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0 && selectionListener != null) {
                        Question question = getQuestionFromRow(selectedRow);
                        selectionListener.onQuestionDoubleClicked(question);
                    }
                }
            }
        });
    }
    
    public void setQuestions(List<Question> questions) {
        // Clear existing data
        tableModel.setRowCount(0);
        
        // Add questions
        for (Question question : questions) {
            Object[] rowData = {
                question.getQuestionId(),
                question.getSubjectName(),
                truncateText(question.getQuestionText(), 80),
                question.getCorrectAnswer() + " - " + truncateText(question.getCorrectOptionText(), 30),
                question.getDifficulty(),
                question.isActive() ? "Active" : "Inactive",
                formatDate(question.getCreatedAt())
            };
            tableModel.addRow(rowData);
        }
    }
    
    public Question getSelectedQuestion() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            return getQuestionFromRow(selectedRow);
        }
        return null;
    }
    
    private Question getQuestionFromRow(int row) {
        Question question = new Question();
        question.setQuestionId((Integer) tableModel.getValueAt(row, 0));
        question.setSubjectName((String) tableModel.getValueAt(row, 1));
        return question;
    }
    
    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
    
    private String formatDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return "N/A";
        }

        try {
            dateString = dateString.replace("T", " ");

            // cắt milliseconds nếu có
            if (dateString.contains(".")) {
                dateString = dateString.substring(0, dateString.indexOf("."));
            }

            LocalDateTime dateTime =
                    LocalDateTime.parse(dateString, INPUT_FORMATTER);

            return dateTime.format(OUTPUT_FORMATTER);
        } catch (Exception e) {
            return dateString;
        }
    }

    public void setSelectionListener(QuestionSelectionListener listener) {
        this.selectionListener = listener;
    }
    
    public void refreshTable() {
        tableModel.fireTableDataChanged();
    }
    
    public void clearSelection() {
        table.clearSelection();
    }
    
    public int getQuestionCount() {
        return tableModel.getRowCount();
    }
}