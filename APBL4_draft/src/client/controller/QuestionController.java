package client.controller;

import client.network.NetworkManager;
import client.network.NetworkManager.ResponseData;
import model.Question;
import model.Subject;
import utils.JsonUtil;
import utils.Protocol;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionController extends BaseController {
    
//    private QuestionListener listener;
    
    public QuestionController(NetworkManager networkManager) {
        super(networkManager);
    }
    
    /**
     * Create new question
     */
    public boolean createQuestion(Question question) {
        try {
            logAction("createQuestion", "Creating question for subject: " + question.getSubjectId());
            
            if (!validateSession()) {
                return false;
            }
            
            ResponseData response = sendJsonRequest(Protocol.CREATE_QUESTION, question);
            
            if (response.isSuccess()) {
                logAction("createQuestion", "Question created successfully");
                showSuccessMessage("Success", "Question created successfully!");
                return true;
            } else {
                handleServerError(response.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            handleNetworkError(e);
            return false;
        }
    }
    
    /**
     * Update existing question
     */
    public boolean updateQuestion(Question question) {
        try {
            logAction("updateQuestion", "Updating question ID: " + question.getQuestionId());
            
            if (!validateSession()) {
                return false;
            }
            
            ResponseData response = sendJsonRequest(Protocol.UPDATE_QUESTION, question);
            
            if (response.isSuccess()) {
                logAction("updateQuestion", "Question updated successfully");
                showSuccessMessage("Success", "Question updated successfully!");
                return true;
            } else {
                handleServerError(response.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            handleNetworkError(e);
            return false;
        }
    }
    
    /**
     * Delete question
     */
    public boolean deleteQuestion(int questionId, String questionText) {
        try {
            logAction("deleteQuestion", "Deleting question ID: " + questionId);
            
            if (!validateSession()) {
                return false;
            }
            
            // Confirm deletion
            String truncatedText = questionText.length() > 50 ? 
                questionText.substring(0, 50) + "..." : questionText;
            boolean confirmed = showConfirmDialog("Delete Question", 
                "Are you sure you want to delete this question?\n\n" + truncatedText);
            if (!confirmed) {
                return false;
            }
            
            Map<String, Object> deleteData = new HashMap<>();
            deleteData.put("questionId", questionId);
            
            ResponseData response = sendJsonRequest(Protocol.DELETE_QUESTION, deleteData);
            
            if (response.isSuccess()) {
                logAction("deleteQuestion", "Question deleted successfully");
                showSuccessMessage("Success", "Question deleted successfully!");
                
                return true;
            } else {
                handleServerError(response.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            handleNetworkError(e);
            return false;
        }
    }
    
    /**
     * Get all questions
     */
    public List<Question> getAllQuestions() {
        try {
            logAction("getAllQuestions", "Fetching all questions");
            
            if (!validateSession()) {
                return null;
            }
            
            ResponseData response = sendRequest(Protocol.GET_QUESTIONS);
            
            if (response.isSuccess()) {
                List<Question> questions = JsonUtil.fromJsontoList(response.getData(), Question.class);
                logAction("getAllQuestions", "Retrieved " + (questions != null ? questions.size() : 0) + " questions");

                return questions;
            } else {
                handleServerError(response.getMessage());
                return null;
            }
            
        } catch (Exception e) {
            handleNetworkError(e);
            return null;
        }
    }
    
    /**
     * Search questions
     */
    public List<Question> searchQuestions(String keyword) {
        try {
            logAction("searchQuestions", "Searching with keyword: " + keyword);
            
            if (!validateSession()) {
                return null;
            }
            
            Map<String, Object> searchData = new HashMap<>();
            searchData.put("keyword", keyword);
            
            ResponseData response = sendJsonRequest(Protocol.SEARCH_QUESTIONS, searchData);
            
            if (response.isSuccess()) {
                List<Question> questions = JsonUtil.fromJsontoList(response.getData(), Question.class);
                logAction("searchQuestions", "Found " + (questions != null ? questions.size() : 0) + " questions");
                return questions;
            } else {
                handleServerError(response.getMessage());
                return null;
            }
            
        } catch (Exception e) {
            handleNetworkError(e);
            return null;
        }
    }
    
    /**
     * Get questions by subject
     */
    public List<Question> getQuestionsBySubject(int subjectId) {
        try {
            logAction("getQuestionsBySubject", "Fetching questions for subject: " + subjectId);
            
            if (!validateSession()) {
                return null;
            }
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("subjectId", subjectId);
            
            ResponseData response = sendJsonRequest(Protocol.GET_QUESTIONS_BY_SUBJECT, requestData);
            
            if (response.isSuccess()) {
                List<Question> questions = JsonUtil.fromJsontoList(response.getData(), Question.class);
                logAction("getQuestionsBySubject", "Retrieved " + (questions != null ? questions.size() : 0) + " questions for subject");

                return questions;
            } else {
                handleServerError(response.getMessage());
                return null;
            }
            
        } catch (Exception e) {
            handleNetworkError(e);
            return null;
        }
    }
    
    /**
     * Get questions by difficulty
     */
    public List<Question> getQuestionsByDifficulty(String difficulty) {
        try {
            logAction("getQuestionsByDifficulty", "Fetching questions by difficulty: " + difficulty);
            
            if (!validateSession()) {
                return null;
            }
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("difficulty", difficulty);
            
            ResponseData response = sendJsonRequest(Protocol.GET_QUESTIONS_BY_DIFFICULTY, requestData);
            
            if (response.isSuccess()) {
                List<Question> questions = JsonUtil.fromJsontoList(response.getData(), Question.class);
                logAction("getQuestionsByDifficulty", "Retrieved " + (questions != null ? questions.size() : 0) + " " + difficulty + " questions");
                
                return questions;
            } else {
                handleServerError(response.getMessage());
                return null;
            }
            
        } catch (Exception e) {
            handleNetworkError(e);
            return null;
        }
    }
    
    /**
     * Get all subjects for dropdown
     */
    public List<Subject> getAllSubjects() {
        try {
            logAction("getAllSubjects", "Fetching all subjects for question management");
            
            if (!validateSession()) {
                return null;
            }
            
            ResponseData response = sendRequest(Protocol.GET_SUBJECTS);
            
            if (response.isSuccess()) {
                List<Subject> subjects = JsonUtil.fromJsontoList(response.getData(), Subject.class);
                logAction("getAllSubjects", "Retrieved " + (subjects != null ? subjects.size() : 0) + " subjects");
                return subjects;
            } else {
                handleServerError(response.getMessage());
                return null;
            }
            
        } catch (Exception e) {
            handleNetworkError(e);
            return null;
        }
    }

}