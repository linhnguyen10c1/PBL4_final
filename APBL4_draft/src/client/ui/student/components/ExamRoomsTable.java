package client.ui.student.components;

import model.ExamRoom;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * ExamRoomsTable - Table component for displaying available exam rooms
 * 
 * @author linhnguyen10c1
 * @since 2025-10-30 04:01:54 UTC
 */
public class ExamRoomsTable extends JPanel {
    
    private JTable table;
    private DefaultTableModel tableModel;
    private ExamRoomSelectionListener selectionListener;
    private List<ExamRoom> examRooms;
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    /**
     * Selection listener interface
     */
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
            "Room Name", "Subject", "Questions", "Duration", "Score", 
            "Start Time", "End Time", "Status", "Password"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 2: case 3: case 4: return Integer.class;
                    case 8: return String.class; // Changed from Boolean to String
                    default: return String.class;
                }
            }
        };
        
        // Create table
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(30);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setFillsViewportHeight(true);
        
        // Set column widths
        setupColumnWidths();
        
        // Custom renderer for status column
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusCellRenderer());
        
        // Custom renderer for password column
        table.getColumnModel().getColumn(8).setCellRenderer(new PasswordCellRenderer());
        
        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Available Exam Rooms"));
        scrollPane.setPreferredSize(new Dimension(1000, 400));
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void setupColumnWidths() {
        table.getColumnModel().getColumn(0).setPreferredWidth(200); // Room Name
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Subject
        table.getColumnModel().getColumn(2).setPreferredWidth(80);  // Questions
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Duration
        table.getColumnModel().getColumn(4).setPreferredWidth(60);  // Score
        table.getColumnModel().getColumn(5).setPreferredWidth(120); // Start Time
        table.getColumnModel().getColumn(6).setPreferredWidth(120); // End Time
        table.getColumnModel().getColumn(7).setPreferredWidth(100); // Status
        table.getColumnModel().getColumn(8).setPreferredWidth(80);  // Password
    }
    
    private void setupEventHandlers() {
        // Selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0 && selectionListener != null && examRooms != null) {
                    ExamRoom selectedRoom = examRooms.get(selectedRow);
                    selectionListener.onExamRoomSelected(selectedRoom);
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
                    if (selectedRow >= 0 && selectionListener != null && examRooms != null) {
                        ExamRoom selectedRoom = examRooms.get(selectedRow);
                        selectionListener.onExamRoomDoubleClicked(selectedRoom);
                    }
                }
            }
        });
    }
    
    public void setExamRooms(List<ExamRoom> examRooms) {
        this.examRooms = examRooms;
        updateTableData();
    }
    
    private void updateTableData() {
        // Clear existing data
        tableModel.setRowCount(0);
        
        if (examRooms != null) {
            for (ExamRoom room : examRooms) {
                Object[] rowData = {
                    room.getRoomName(),
                    room.getSubjectName(),
                    room.getQuestionCount(),
                    room.getDurationMinutes(),
                    (int) room.getTotalScore(),
                    formatTimestamp(room.getStartTime()),
                    formatTimestamp(room.getEndTime()),
                    getExamStatus(room),
                    getPasswordStatus(room) // Changed to String method
                };
                tableModel.addRow(rowData);
            }
        }
    }
    
    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "Not set";
        }
        try {
            return DATE_FORMAT.format(timestamp);
        } catch (Exception e) {
            System.err.println("Error formatting timestamp: " + e.getMessage());
            return timestamp.toString();
        }
    }
    
    private String getExamStatus(ExamRoom room) {
        if (!room.isActive()) {
            return "Inactive";
        }
        
        if (room.getStartTime() == null || room.getEndTime() == null) {
            return "Available";
        }
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        
        if (now.before(room.getStartTime())) {
            return "Scheduled";
        } else if (now.after(room.getStartTime()) && now.before(room.getEndTime())) {
            return "Available";
        } else {
            return "Expired";
        }
    }
    
    private String getPasswordStatus(ExamRoom room) {
        if (room.getRoomPassword() != null && !room.getRoomPassword().trim().isEmpty()) {
            return "Required";
        } else {
            return "No";
        }
    }
    
    public ExamRoom getSelectedExamRoom() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0 && examRooms != null && selectedRow < examRooms.size()) {
            return examRooms.get(selectedRow);
        }
        return null;
    }
    
    public void clearSelection() {
        table.clearSelection();
    }
    
    public void setSelectionListener(ExamRoomSelectionListener listener) {
        this.selectionListener = listener;
    }
    
    public void refreshTable() {
        tableModel.fireTableDataChanged();
    }
    
    /**
     * Custom renderer for status column
     */
    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null) {
                String status = value.toString();
                setHorizontalAlignment(SwingConstants.CENTER);
                
                if (!isSelected) {
                    switch (status) {
                        case "Available":
                            setForeground(new Color(0, 150, 0));
                            setText("✅ Available");
                            break;
                        case "Scheduled":
                            setForeground(new Color(255, 140, 0));
                            setText("📅 Scheduled");
                            break;
                        case "Expired":
                            setForeground(Color.RED);
                            setText("❌ Expired");
                            break;
                        case "Inactive":
                            setForeground(Color.GRAY);
                            setText("⏸️ Inactive");
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
            }
            
            return this;
        }
    }
    
    /**
     * Custom renderer for password column
     */
    private static class PasswordCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null) {
                String passwordStatus = value.toString();
                setHorizontalAlignment(SwingConstants.CENTER);
                
                if (!isSelected) {
                    switch (passwordStatus) {
                        case "Required":
                            setForeground(new Color(200, 100, 0));
                            setText("🔒 Required");
                            break;
                        case "No":
                            setForeground(new Color(0, 150, 0));
                            setText("🔓 No");
                            break;
                        default:
                            setForeground(Color.BLACK);
                            setText(passwordStatus);
                            break;
                    }
                } else {
                    setForeground(Color.WHITE);
                    setText(passwordStatus);
                }
            }
            
            return this;
        }
    }
}