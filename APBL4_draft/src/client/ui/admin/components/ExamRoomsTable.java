package client.ui.admin.components;

import model.ExamRoom;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ExamRoomsTable extends JPanel {
    
    private JTable table;
    private DefaultTableModel tableModel;
    private ExamRoomSelectionListener selectionListener;
    
    private List<ExamRoom> currentExamRooms = new ArrayList<>();
    
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
        
        String[] columnNames = {
            "ID", "Room Name", "Subject", "Password", "Questions", 
            "Total Score", "Duration (min)", "Start Time", "End Time", 
            "Students", "Status"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            
            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0: case 4: case 6: case 9: return Integer.class;
                    case 5: return Double.class;
                    default: return String.class;
                }
            }
        };
        
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.setFillsViewportHeight(true);
        
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(7).setPreferredWidth(120);
        table.getColumnModel().getColumn(8).setPreferredWidth(120); 
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Exam Rooms Management"));
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0 && selectionListener != null) {
                    selectionListener.onExamRoomSelected(getExamRoomFromRow(selectedRow));
                } else if (selectionListener != null) {
                    selectionListener.onExamRoomDeselected();
                }
            }
        });
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0 && selectionListener != null) {
                        selectionListener.onExamRoomDoubleClicked(getExamRoomFromRow(selectedRow));
                    }
                }
            }
        });
    }
    
    public void setExamRooms(List<ExamRoom> examRooms) {
        this.currentExamRooms = (examRooms != null) ? examRooms : new ArrayList<>();
        tableModel.setRowCount(0);
        
        for (ExamRoom room : currentExamRooms) {
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
                (room.getAllowedStudentIds() != null) ? room.getAllowedStudentIds().size() : 0,
                room.getExamStatus()
            };
            tableModel.addRow(rowData);
        }
    }
    
    public ExamRoom getSelectedExamRoom() {
        int selectedRow = table.getSelectedRow();
        return (selectedRow >= 0) ? getExamRoomFromRow(selectedRow) : null;
    }
    
    private ExamRoom getExamRoomFromRow(int row) {
        if (row >= 0 && row < currentExamRooms.size()) {
            return currentExamRooms.get(row);
        }
        return null;
    }
    
    private String formatForDisplay(String dateTimeString) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return output.format(input.parse(dateTimeString));
        } catch (Exception e) {
            return dateTimeString;
        }
    }

    public void setSelectionListener(ExamRoomSelectionListener listener) {
        this.selectionListener = listener;
    }
    
    public void refreshTable() { tableModel.fireTableDataChanged(); }
    public void clearSelection() { table.clearSelection(); }
}