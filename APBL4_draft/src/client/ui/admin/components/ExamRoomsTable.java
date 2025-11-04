package client.ui.admin.components;

import model.ExamRoom;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * ExamRooms Table Component
 * 
 * @author linhnguyen10c1
 * @since 2025-10-15 08:36:16 UTC
 */
public class ExamRoomsTable extends JPanel {
    
    private JTable table;
    private DefaultTableModel tableModel;
    private ExamRoomSelectionListener selectionListener;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    public interface ExamRoomSelectionListener {
        void onExamRoomSelected(ExamRoom examRoom);
        void onExamRoomDeselected();
        void onExamRoomDoubleClicked(ExamRoom examRoom);
    }
    
    public ExamRoomsTable() {
        initializeUI();
        setupEventHandlers();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Create table model
        String[] columnNames = {
            "ID", "Room Name", "Subject", "Password", "Questions", 
            "Total Score", "Duration (min)", "Start Time", "End Time", 
            "Students", "Status"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0: case 4: case 6: case 9: return Integer.class;
                    case 5: return Double.class;
                    default: return String.class;
                }
            }
        };
        
        // Create table
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.setFillsViewportHeight(true);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(150);  // Room Name
        table.getColumnModel().getColumn(2).setPreferredWidth(120);  // Subject
        table.getColumnModel().getColumn(3).setPreferredWidth(80);   // Password
        table.getColumnModel().getColumn(4).setPreferredWidth(80);   // Questions
        table.getColumnModel().getColumn(5).setPreferredWidth(80);   // Total Score
        table.getColumnModel().getColumn(6).setPreferredWidth(80);   // Duration
        table.getColumnModel().getColumn(7).setPreferredWidth(120);  // Start Time
        table.getColumnModel().getColumn(8).setPreferredWidth(120);  // End Time
        table.getColumnModel().getColumn(9).setPreferredWidth(80);   // Students
        table.getColumnModel().getColumn(10).setPreferredWidth(80);  // Status
        
        // Add to scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Exam Rooms"));
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        // Selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0 && selectionListener != null) {
                    ExamRoom examRoom = getExamRoomFromRow(selectedRow);
                    selectionListener.onExamRoomSelected(examRoom);
                } else if (selectionListener != null) {
                    selectionListener.onExamRoomDeselected();
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
                        ExamRoom examRoom = getExamRoomFromRow(selectedRow);
                        selectionListener.onExamRoomDoubleClicked(examRoom);
                    }
                }
            }
        });
    }
    
    public void setExamRooms(List<ExamRoom> examRooms) {
        // Clear existing data
        tableModel.setRowCount(0);
        
        // Add exam rooms
        for (ExamRoom room : examRooms) {
            Object[] rowData = {
                room.getRoomId(),
                room.getRoomName(),
                room.getSubjectName(),
                room.getRoomPassword(),
                room.getQuestionCount(),
                room.getTotalScore(),
                room.getDurationMinutes(),
                room.getStartTimeAsString() != null ? formatForDisplay(room.getStartTimeAsString()) : "Not set",
                room.getEndTimeAsString() != null ? formatForDisplay(room.getEndTimeAsString()) : "Not set",
                room.getAllowedStudentIds().size(),
                room.getExamStatus()
            };
            tableModel.addRow(rowData);
        }
    }
    
    public ExamRoom getSelectedExamRoom() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            return getExamRoomFromRow(selectedRow);
        }
        return null;
    }
    
    private ExamRoom getExamRoomFromRow(int row) {
        ExamRoom room = new ExamRoom();
        room.setRoomId((Integer) tableModel.getValueAt(row, 0));
        room.setRoomName((String) tableModel.getValueAt(row, 1));
        room.setSubjectName((String) tableModel.getValueAt(row, 2));
        room.setRoomPassword((String) tableModel.getValueAt(row, 3));
        room.setQuestionCount((Integer) tableModel.getValueAt(row, 4));
        room.setTotalScore((Double) tableModel.getValueAt(row, 5));
        room.setDurationMinutes((Integer) tableModel.getValueAt(row, 6));
        // For full object, you'd need to store or retrieve additional data
        return room;
    }
    
    private String formatForDisplay(String dateTimeString) {
        try {
            java.text.SimpleDateFormat input = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
            java.text.SimpleDateFormat output = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
            java.util.Date date = input.parse(dateTimeString);
            return output.format(date);
        } catch (Exception e) {
            return dateTimeString; // Fallback
        }
    }

//    private String getRoomStatus(ExamRoom room) {
//        if (!room.isActive()) {
//            return "Inactive";
//        }
//        
//        if (room.getStartTime() == null || room.getEndTime() == null) {
//            return "Draft";
//        }
//        
//        try {
//            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
//            long now = System.currentTimeMillis();
//            long startTime = sdf.parse(room.getStartTime()).getTime();
//            long endTime = sdf.parse(room.getEndTime()).getTime();
//            
//            if (now < startTime) {
//                return "Scheduled";
//            } else if (now >= startTime && now <= endTime) {
//                return "Active";
//            } else {
//                return "Finished";
//            }
//        } catch (Exception e) {
//            return "Draft"; // Fallback
//        }
//    }
    
    public void setSelectionListener(ExamRoomSelectionListener listener) {
        this.selectionListener = listener;
    }
    
    public void refreshTable() {
        tableModel.fireTableDataChanged();
    }
    
    public void clearSelection() {
        table.clearSelection();
    }
}