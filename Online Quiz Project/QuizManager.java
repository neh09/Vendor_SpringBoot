import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class QuizManager {

    public static void mainMenu(Scanner scanner, int userId) {
        while (true) {
            System.out.println("1. Take Quiz");
            System.out.println("2. Manage Quizzes (Admin only)");
            System.out.println("3. View Past Attempts");
            System.out.println("4. Logout");
            System.out.print("Select an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    takeQuiz(scanner, userId);
                    break;
                case 2:
                    manageQuizzes(scanner);
                    break;
                case 3:
                    viewPastAttempts(scanner, userId);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    public static void manageQuizzes(Scanner scanner) {
        while (true) {
            System.out.println("1. Create Quiz");
            System.out.println("2. Edit Quiz");
            System.out.println("3. Delete Quiz");
            System.out.println("4. Back");
            System.out.print("Select an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    createQuiz(scanner);
                    break;
                case 2:
                    editQuiz(scanner);
                    break;
                case 3:
                    deleteQuiz(scanner);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    public static void createQuiz(Scanner scanner) {
        System.out.print("Enter quiz title: ");
        String title = scanner.nextLine();

        try {
            Connection conn = Database.getConnection();
            String sql = "INSERT INTO quizzes (title) VALUES (?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, title);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();

            if (rs.next()) {
                int quizId = rs.getInt(1);
                System.out.println("Quiz created successfully with ID: " + quizId);
                addQuestionsToQuiz(scanner, quizId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addQuestionsToQuiz(Scanner scanner, int quizId) {
        while (true) {
            System.out.print("Enter question title: ");
            String questionTitle = scanner.nextLine();
            System.out.print("Enter the correct answer: ");
            String correctAnswer = scanner.nextLine();

            try {
                Connection conn = Database.getConnection();
                String sql = "INSERT INTO questions (quiz_id, title, correct_answer) VALUES (?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pstmt.setInt(1, quizId);
                pstmt.setString(2, questionTitle);
                pstmt.setString(3, correctAnswer);
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();

                if (rs.next()) {
                    int questionId = rs.getInt(1);
                    addOptionsToQuestion(scanner, questionId);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            System.out.print("Add another question? (yes/no): ");
            String addMore = scanner.nextLine();
            if (!addMore.equalsIgnoreCase("yes")) {
                break;
            }
        }
    }

    public static void addOptionsToQuestion(Scanner scanner, int questionId) {
        while (true) {
            System.out.print("Enter option text: ");
            String optionText = scanner.nextLine();

            try {
                Connection conn = Database.getConnection();
                String sql = "INSERT INTO options (question_id, option_text) VALUES (?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, questionId);
                pstmt.setString(2, optionText);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            System.out.print("Add another option? (yes/no): ");
            String addMore = scanner.nextLine();
            if (!addMore.equalsIgnoreCase("yes")) {
                break;
            }
        }
    }

    public static void editQuiz(Scanner scanner) {
        // Implement edit functionality
        System.out.println("Edit Quiz functionality is not implemented yet.");
    }

    public static void deleteQuiz(Scanner scanner) {
        // Implement delete functionality
        System.out.println("Delete Quiz functionality is not implemented yet.");
    }

    public static void takeQuiz(Scanner scanner, int userId) {
        try {
            Connection conn = Database.getConnection();
            String sql = "SELECT * FROM quizzes";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            List<Integer> quizIds = new ArrayList<>();
            int i = 1;
            while (rs.next()) {
                System.out.println(i + ". " + rs.getString("title"));
                quizIds.add(rs.getInt("id"));
                i++;
            }

            if (quizIds.size() == 0) {
                System.out.println("No quizzes available.");
                return;
            }

            System.out.print("Select a quiz: ");
            int quizChoice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            if (quizChoice < 1 || quizChoice > quizIds.size()) {
                System.out.println("Invalid choice!");
                return;
            }

            int quizId = quizIds.get(quizChoice - 1);
            executeQuiz(scanner, quizId, userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void executeQuiz(Scanner scanner, int quizId, int userId) {
        try {
            Connection conn = Database.getConnection();
            String sql = "SELECT * FROM questions WHERE quiz_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quizId);
            ResultSet rs = pstmt.executeQuery();

            int score = 0;
            while (rs.next()) {
                int questionId = rs.getInt("id");
                String questionTitle = rs.getString("title");
                String correctAnswer = rs.getString("correct_answer");

                System.out.println(questionTitle);
                List<Integer> optionIds = displayOptions(questionId);

                System.out.print("Enter your answer (option number): ");
                int userAnswer = scanner.nextInt();
                scanner.nextLine(); // consume newline

                if (optionIds.get(userAnswer - 1) == questionId) {
                    System.out.println("Correct!");
                    score++;
                } else {
                    System.out.println("Incorrect! The correct answer is: " + correctAnswer);
                }
            }

            saveQuizAttempt(userId, quizId, score);
            System.out.println("Quiz completed. Your score: " + score);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Integer> displayOptions(int questionId) throws SQLException {
        Connection conn = Database.getConnection();
        String sql = "SELECT * FROM options WHERE question_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, questionId);
        ResultSet rs = pstmt.executeQuery();

        List<Integer> optionIds = new ArrayList<>();
        int i = 1;
        while (rs.next()) {
            System.out.println(i + ". " + rs.getString("option_text"));
            optionIds.add(rs.getInt("id"));
            i++;
        }
        return optionIds;
    }

    public static void saveQuizAttempt(int userId, int quizId, int score) {
        try {
            Connection conn = Database.getConnection();
            String sql = "INSERT INTO quiz_attempts (user_id, quiz_id, score, attempt_date) VALUES (?, ?, ?, datetime('now'))";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, quizId);
            pstmt.setInt(3, score);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void viewPastAttempts(Scanner scanner, int userId) {
        try {
            Connection conn = Database.getConnection();
            String sql = "SELECT quizzes.title, quiz_attempts.score, quiz_attempts.attempt_date " +
                    "FROM quiz_attempts " +
                    "JOIN quizzes ON quiz_attempts.quiz_id = quizzes.id " +
                    "WHERE quiz_attempts.user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                System.out.println("Quiz: " + rs.getString("title"));
                System.out.println("Score: " + rs.getInt("score"));
                System.out.println("Attempt Date: " + rs.getString("attempt_date"));
                System.out.println("-----");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
