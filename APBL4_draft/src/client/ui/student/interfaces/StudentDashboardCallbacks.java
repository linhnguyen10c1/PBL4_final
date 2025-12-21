package client.ui.student.interfaces;

import model.*;

/**
 * Interface cho các callbacks từ Student Dashboard panels
 * 
 * ✅ FIX:  Đã xóa method onNavigateToQuestion(int questionIndex)
 * Lý do:  Method này gây ra vòng lặp vô hạn khi: 
 * - ExamInterfacePanel gọi callbacks.onNavigateToQuestion()
 * - StudentDashboard nhận callback và gọi lại examInterfacePanel. navigateToQuestion()
 * - Vòng lặp tiếp tục... 
 */
public interface StudentDashboardCallbacks {
    // Navigation callbacks
    void onLogoutRequested();
    void onTabChanged(int tabIndex);
    
    // Exam management callbacks
    void onRefreshExamsRequested();
    void onJoinExamRequested(ExamRoom examRoom);
    void onStartExamRequested(ExamSession session);
    void onSubmitExamRequested(ExamSession session, boolean isAutoSubmit);
    void onExamTimeExpired(ExamSession session);
    
    // Answer management callbacks
    void onAnswerChanged(int questionId, String answer);
    void onAnswerSaved(int questionId, String answer);
    
    // Status updates
    void updateStatus(String message);
    void updateStatus(String message, boolean isError);
}