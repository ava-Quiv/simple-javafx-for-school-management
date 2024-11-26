package University;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

public class University extends Application {
    private Connection conn;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        setupDatabaseConnection();

        // Start with login page
        //primaryStage.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, null, null)));
        primaryStage.setScene(new Scene(createLoginPage(primaryStage), 400, 300));
        primaryStage.setTitle("school management");
        primaryStage.show();
    }

    private void setupDatabaseConnection() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/university", "root", "");
        } catch (SQLException e) {
        }
    }

    private HBox createLoginPage(Stage stage) {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-background-color: blue");
        VBox vb2 = new VBox();
        vb2.setStyle("-fx-background-color: pink");
        HBox hb1 = new HBox();
        Label roleLabel = new Label("Duty select");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("Student", "Lecturer", "Finance");
        roleCombo.setValue("Student");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> {
            String role = roleCombo.getValue();
            String username = usernameField.getText();
            String password = passwordField.getText();

            if ("Student".equals(role)) {
                if (authenticateUser("students", username, password)) {
                    stage.setScene(new Scene(createStudentDashboard(stage, username), 600, 400));
                } else {
                    showAlert("Invalid credentials.");
                }
            } else if ("Lecturer".equals(role)) {
                if (authenticateLecturer(username, password)) {
                    int lecturerId = getLecturerId(username);
                    stage.setScene(new Scene(createLecturerDashboard(stage, username, lecturerId), 600, 400));
                } else {
                    showAlert("Invalid credentials.");
                }
            } else if ("Finance".equals(role)) {
                if (authenticateFinance(username, password)) {
                    stage.setScene(new Scene(createFinanceDashboard(stage, username), 600, 400));
                } else {
                    showAlert("Invalid credentials.");
                }
            } else {
                showAlert("Please select a valid role.");
            }
        });

        Button signupButton = new Button("Sign Up (Student Only)");
        signupButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (signupUser(username, password)) {
                showAlert("Signup successful. You can now log in.");
            }
        });
        vb2.getChildren().addAll(roleLabel,roleCombo);
        vbox.getChildren().addAll(usernameField, passwordField, loginButton, signupButton);
        hb1.getChildren().addAll(vb2,vbox);
        hb1.setSpacing(10);
        return hb1; 
    }

    private boolean authenticateUser(String table, String username, String password) {
        String query = "SELECT * FROM " + table + " WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean authenticateLecturer(String username, String password) {
        String query = "SELECT * FROM lecturer WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int getLecturerId(String username) {
    String query = "SELECT lecturer_id FROM lecturer WHERE username = ?";
    try (PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("lecturer_id");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return -1;
}


    private boolean authenticateFinance(String username, String password) {
        String query = "SELECT * FROM finance WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean signupUser(String username, String password) {
        String query = "INSERT INTO students (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            showAlert("Signup failed. Username might already exist.");
            e.printStackTrace();
            return false;
        }
    }

private VBox createStudentDashboard(Stage stage, String username) {
    VBox vbox = new VBox(10);
    vbox.setStyle("-fx-background-color: #0000FF;");
    Label welcomeLabel = new Label("Welcome, " + username);

    Button registerButton = new Button("Register Courses");
    registerButton.setOnAction(e -> stage.setScene(new Scene(createCourseRegistrationPage(stage, username), 600, 400)));

    Button dropButton = new Button("Drop Courses");
    dropButton.setOnAction(e -> stage.setScene(new Scene(createCourseDropPage(stage, username), 600, 400)));

    Button viewGradesButton = new Button("View Grades");
    viewGradesButton.setOnAction(e -> stage.setScene(new Scene(createViewGradesPage(stage, username), 600, 400)));

    Button feesButton = new Button("View Fees");
    feesButton.setOnAction(e -> stage.setScene(new Scene(createFeesPage(stage, username), 600, 400)));

    Button signoutButton = new Button("Sign Out");
    signoutButton.setOnAction(e -> stage.setScene(new Scene(createLoginPage(stage), 400, 300)));

    vbox.getChildren().addAll(welcomeLabel, registerButton, dropButton, viewGradesButton, feesButton, signoutButton);
    return vbox;
}
// Method to create the View Grades page
// Method to create the View Grades page


// Method to fetch grades for a specific student
private List<Grade> fetchGradesForStudent(String username) {
    List<Grade> grades = new ArrayList<>();
    String query = "SELECT course_code, grade FROM grades WHERE student_id = (SELECT id FROM students WHERE username = ?)";

    try (PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            String courseCode = rs.getString("course_code");
            String grade = rs.getString("grade");
            grades.add(new Grade(courseCode, grade));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return grades;
}
// Method to create the View Fees page


// Method to fetch fees for a specific student
private List<Fee> fetchFeesForStudent(String username) {
    List<Fee> fees = new ArrayList<>();
    String query = "SELECT amount_paid, credit, debit FROM fees WHERE student_id = (SELECT id FROM students WHERE username = ?)";

    try (PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            double amountPaid = rs.getDouble("amount_paid");
            double credit = rs.getDouble("credit");
            double debit = rs.getDouble("debit");
            fees.add(new Fee(amountPaid, credit, debit));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return fees;
}

// Grade class to represent a grade entry
public class Grade {
    private String courseCode;
    private String grade;

    public Grade(String courseCode, String grade) {
        this.courseCode = courseCode;
        this.grade = grade;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getGrade() {
        return grade;
    }
}

// Fee class to represent a fee entry
public class Fee {
    private double amountPaid;
    private double credit;
    private double debit;

    public Fee(double amountPaid, double credit, double debit) {
        this.amountPaid = amountPaid;
        this.credit = credit;
        this.debit = debit;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public double getCredit() {
        return credit;
    }

    public double getDebit() {
        return debit;
    }
}


    private VBox createCourseRegistrationPage(Stage stage, String username) {
        VBox vbox = new VBox(10);
        //vbox.setStyle("-fx-background-color: #0000FF;");
        Label label = new Label("Register Courses for " + username);
        
        // Fetch available courses from the database
        List<CheckBox> courseCheckBoxes = new ArrayList<>();
        loadAvailableCourses(courseCheckBoxes);

        Button registerButton = new Button("Register Selected Courses");
        registerButton.setOnAction(e -> {
            int selectedCount = 0;
            for (CheckBox checkBox : courseCheckBoxes) {
                if (checkBox.isSelected()) {
                    selectedCount++;
                }
            }
            if (selectedCount > 6) {
                showAlert("You can only select up to 6 courses.");
            } else {
                for (CheckBox checkBox : courseCheckBoxes) {
                    if (checkBox.isSelected()) {
                        registerCourse(username, checkBox.getText());
                    }
                }
                showAlert("Courses registered successfully!");
            }
        });
        
        Button backButton = new Button("Back to Dashboard");
        backButton.setOnAction(e -> stage.setScene(new Scene(createStudentDashboard(stage, username), 600, 400)));

        vbox.getChildren().addAll(label);
        vbox.getChildren().addAll(courseCheckBoxes);
        vbox.getChildren().addAll(registerButton, backButton);
        return vbox;
    }

    private void loadAvailableCourses(List<CheckBox> courseCheckBoxes) {
        String query = "SELECT course_code, course_name FROM courses";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String courseCode = rs.getString("course_code");
                String courseName = rs.getString("course_name");
                CheckBox checkBox = new CheckBox(courseCode + " - " + courseName);
                courseCheckBoxes.add(checkBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void registerCourse(String username, String course) {
        String courseCode = course.split(" - ")[0]; // Get the course code
        String query = "INSERT INTO registrations (student_id, course_code) VALUES ((SELECT id FROM students WHERE username = ?), ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, courseCode);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createCourseDropPage(Stage stage, String username) {
        VBox vbox = new VBox(10);
        Label label = new Label("Drop Courses for " + username);

        List<CheckBox> registeredCourseCheckBoxes = new ArrayList<>();
        loadRegisteredCourses(username, registeredCourseCheckBoxes);

        Button dropButton = new Button("Drop Selected Courses");
        dropButton.setOnAction(e -> {
            for (CheckBox checkBox : registeredCourseCheckBoxes) {
                if (checkBox.isSelected()) {
                    dropCourse(username, checkBox.getText());
                }
            }
            showAlert("Selected courses dropped successfully!");
        });

        Button dropAllButton = new Button("Drop All Courses");
        dropAllButton.setOnAction(e -> {
            for (CheckBox checkBox : registeredCourseCheckBoxes) {
                dropCourse(username, checkBox.getText());
            }
            showAlert("All courses dropped successfully!");
        });

        Button backButton = new Button("Back to Dashboard");
        backButton.setOnAction(e -> stage.setScene(new Scene(createStudentDashboard(stage, username), 600, 400)));

        vbox.getChildren().addAll(label);
        vbox.getChildren().addAll(registeredCourseCheckBoxes);
        vbox.getChildren().addAll(dropButton, dropAllButton, backButton);
        return vbox;
    }

    private void loadRegisteredCourses(String username, List<CheckBox> courseCheckBoxes) {
        String query = "SELECT c.course_code, c.course_name FROM courses c JOIN registrations r ON c.course_code = r.course_code WHERE r.student_id = (SELECT id FROM students WHERE username = ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String courseCode = rs.getString("course_code");
                String courseName = rs.getString("course_name");
                CheckBox checkBox = new CheckBox(courseCode + " - " + courseName);
                courseCheckBoxes.add(checkBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void dropCourse(String username, String course) {
        String courseCode = course.split(" - ")[0]; // Get the course code
        String query = "DELETE FROM registrations WHERE student_id = (SELECT id FROM students WHERE username = ?) AND course_code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, courseCode);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

// Method to create the View Grades page
private VBox createViewGradesPage(Stage stage, String username) {
    VBox vbox = new VBox(10);
    Label label = new Label("View Grades for " + username);

    // Create TableView for displaying grades
    TableView<Grade> gradesTable = new TableView<>();
    TableColumn<Grade, String> courseColumn = new TableColumn<>("Course Code");
    courseColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
    
    TableColumn<Grade, String> gradeColumn = new TableColumn<>("Grade");
    gradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));

    gradesTable.getColumns().addAll(courseColumn, gradeColumn);

    // Fetch grades for the student and add to the table
    List<Grade> grades = fetchGradesForStudent(username);
    gradesTable.getItems().addAll(grades);

    // Back button to return to the student dashboard
    Button backButton = new Button("Back to Dashboard");
    backButton.setOnAction(e -> stage.setScene(new Scene(createStudentDashboard(stage, username), 600, 400)));

    vbox.getChildren().addAll(label, gradesTable, backButton);
    return vbox;
}

// Method to create the View Fees page
private VBox createFeesPage(Stage stage, String username) {
    VBox vbox = new VBox(10);
    Label label = new Label("View Fees for " + username);

    // Create TableView for displaying fees
    TableView<Fee> feesTable = new TableView<>();
    TableColumn<Fee, Double> amountPaidColumn = new TableColumn<>("Amount Paid");
    amountPaidColumn.setCellValueFactory(new PropertyValueFactory<>("amountPaid"));

    TableColumn<Fee, Double> creditColumn = new TableColumn<>("Credit");
    creditColumn.setCellValueFactory(new PropertyValueFactory<>("credit"));

    TableColumn<Fee, Double> debitColumn = new TableColumn<>("Debit");
    debitColumn.setCellValueFactory(new PropertyValueFactory<>("debit"));

    feesTable.getColumns().addAll(amountPaidColumn, creditColumn, debitColumn);

    // Fetch fees for the student and add to the table
    List<Fee> fees = fetchFeesForStudent(username);
    feesTable.getItems().addAll(fees);

    // Back button to return to the student dashboard
    Button backButton = new Button("Back to Dashboard");
    backButton.setOnAction(e -> stage.setScene(new Scene(createStudentDashboard(stage, username), 600, 400)));

    vbox.getChildren().addAll(label, feesTable, backButton);
    return vbox;
}

private VBox createLecturerDashboard(Stage stage, String username, int lecturerId) {
    VBox vbox = new VBox(10);
    Label label = new Label("Lecturer Dashboard for " + username);

    // Fetch courses for the logged-in lecturer
    List<String> courses = getCoursesByLecturerId(lecturerId);
    ListView<String> coursesListView = new ListView<>();
    coursesListView.getItems().addAll(courses);

    VBox studentsVBox = new VBox(5);  // Holds student entries with grade text fields
    Label studentsLabel = new Label("Enrolled Students:");

    // Button to grade students
    Button gradeStudentsButton = new Button("Grade Students");

    coursesListView.setOnMouseClicked(event -> {
        String selectedCourse = coursesListView.getSelectionModel().getSelectedItem();
        if (selectedCourse != null) {
            // Fetch students registered in the selected course
            studentsVBox.getChildren().clear();
            List<StudentGradeEntry> students = getStudentGradeEntriesByCourseCode(selectedCourse);
            for (StudentGradeEntry entry : students) {
                studentsVBox.getChildren().add(entry.getHBox()); // Add student entries to the VBox
            }
        }
    });

    gradeStudentsButton.setOnAction(e -> {
        String selectedCourse = coursesListView.getSelectionModel().getSelectedItem();
        if (selectedCourse != null) {
            for (Node node : studentsVBox.getChildren()) {
                if (node instanceof HBox) {
                    HBox hbox = (HBox) node;
                    CheckBox checkBox = (CheckBox) hbox.getChildren().get(0);
                    TextField gradeField = (TextField) hbox.getChildren().get(2);

                    if (checkBox.isSelected()) {
                        int studentId = (int) checkBox.getUserData();
                        String grade = gradeField.getText().trim();
                        if (!grade.isEmpty()) {
                            saveGrade(studentId, selectedCourse, grade);
                        }
                    }
                }
            }
        }
    });

    Button backButton = new Button("Back to Login");
    backButton.setOnAction(e -> stage.setScene(new Scene(createLoginPage(stage), 400, 300)));

    // Arrange components in VBox
    vbox.getChildren().addAll(label, new Label("Courses:"), coursesListView, studentsLabel, studentsVBox, gradeStudentsButton, backButton);
    return vbox;
}

// Class to represent each student with a checkbox and a grade input
private class StudentGradeEntry {
    private int studentId;
    private String studentName;
    private CheckBox checkBox;
    private TextField gradeField;

    public StudentGradeEntry(int studentId, String studentName) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.checkBox = new CheckBox(studentName);
        this.gradeField = new TextField();
        gradeField.setPromptText("Grade");
        checkBox.setUserData(studentId);
    }

    public HBox getHBox() {
        HBox hbox = new HBox(10);
        hbox.getChildren().addAll(checkBox, new Label("Grade:"), gradeField);
        return hbox;
    }
}

// Method to fetch student entries with grade text fields
private List<StudentGradeEntry> getStudentGradeEntriesByCourseCode(String courseCode) {
    List<StudentGradeEntry> entries = new ArrayList<>();
    String registrationQuery = "SELECT student_id FROM registrations WHERE course_code = ?";
    String studentQuery = "SELECT username FROM students WHERE id = ?";

    try (PreparedStatement registrationStmt = conn.prepareStatement(registrationQuery)) {
        registrationStmt.setString(1, courseCode);
        ResultSet regResultSet = registrationStmt.executeQuery();

        while (regResultSet.next()) {
            int studentId = regResultSet.getInt("student_id");
            try (PreparedStatement studentStmt = conn.prepareStatement(studentQuery)) {
                studentStmt.setInt(1, studentId);
                ResultSet studentResultSet = studentStmt.executeQuery();
                if (studentResultSet.next()) {
                    String studentUsername = studentResultSet.getString("username");
                    entries.add(new StudentGradeEntry(studentId, studentUsername));
                }
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return entries;
}

// Method to save the grade to the database
private void saveGrade(int studentId, String courseCode, String grade) {
    String insertGradeQuery = "INSERT INTO grades (student_id, course_code, grade) VALUES (?, ?, ?)";

    try (PreparedStatement stmt = conn.prepareStatement(insertGradeQuery)) {
        stmt.setInt(1, studentId);
        stmt.setString(2, courseCode);
        stmt.setString(3, grade);
        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

private List<String> getCoursesByLecturerId(int lecturerId) {
    List<String> courses = new ArrayList<>();
    String query = "SELECT course_code FROM lecturer_courses WHERE lecturer_id = ?";
    try (PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setInt(1, lecturerId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            courses.add(rs.getString("course_code"));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return courses;
}


private VBox createFinanceDashboard(Stage stage, String username) {
    VBox vbox = new VBox(10);
    //vbox.setStyle("-fx-background-color: #0000FF;");
    Label label = new Label("Finance Dashboard for " + username);

    ListView<HBox> studentsListView = new ListView<>(); // List of students with fees input fields

    // Fetch all students from the students table
    List<StudentFeesEntry> students = getAllStudentsWithFeesEntry();
    for (StudentFeesEntry entry : students) {
        studentsListView.getItems().add(entry.getHBox()); // Add each student entry to the ListView
    }

    // Button to save fees details
    Button saveFeesButton = new Button("Save Fees Details");
    saveFeesButton.setOnAction(e -> {
        for (HBox hbox : studentsListView.getItems()) {
            CheckBox checkBox = (CheckBox) hbox.getChildren().get(0);
            TextField amountField = (TextField) hbox.getChildren().get(2);
            ComboBox<String> paymentModeBox = (ComboBox<String>) hbox.getChildren().get(4);
            TextField debitField = (TextField) hbox.getChildren().get(6);
            TextField creditField = (TextField) hbox.getChildren().get(8);

            if (checkBox.isSelected()) {
                int studentId = (int) checkBox.getUserData();
                String paymentMode = paymentModeBox.getValue();
                double amountPaid = Double.parseDouble(amountField.getText().trim());
                double debit = Double.parseDouble(debitField.getText().trim());
                double credit = Double.parseDouble(creditField.getText().trim());

                saveFeesDetails(studentId, amountPaid, paymentMode, debit, credit);
            }
        }
    });

    Button backButton = new Button("Back to Login");
    backButton.setOnAction(e -> stage.setScene(new Scene(createLoginPage(stage), 400, 300)));

    vbox.getChildren().addAll(label, new Label("Students:"), studentsListView, saveFeesButton, backButton);
    return vbox;
}

// Class to represent each student with fees input fields
private class StudentFeesEntry {
    private int studentId;
    private String studentName;
    private CheckBox checkBox;
    private TextField amountField;
    private ComboBox<String> paymentModeBox;
    private TextField debitField;
    private TextField creditField;

    public StudentFeesEntry(int studentId, String studentName) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.checkBox = new CheckBox(studentName);
        this.amountField = new TextField();
        amountField.setPromptText("Amount Paid");

        // ComboBox for mode of payment
        this.paymentModeBox = new ComboBox<>();
        paymentModeBox.getItems().addAll("Mpesa", "Bank");
        paymentModeBox.setPromptText("Payment Mode");

        this.debitField = new TextField();
        debitField.setPromptText("Debit");

        this.creditField = new TextField();
        creditField.setPromptText("Credit");

        checkBox.setUserData(studentId);
    }

    public HBox getHBox() {
        HBox hbox = new HBox(10);
        hbox.getChildren().addAll(checkBox, new Label("Amount Paid:"), amountField, new Label("Mode:"), paymentModeBox, new Label("Debit:"), debitField, new Label("Credit:"), creditField);
        return hbox;
    }
}

// Method to fetch all students with fees input fields
private List<StudentFeesEntry> getAllStudentsWithFeesEntry() {
    List<StudentFeesEntry> entries = new ArrayList<>();
    String query = "SELECT id, username FROM students WHERE username IS NOT NULL AND username != ''";

    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {
        while (rs.next()) {
            int studentId = rs.getInt("id");
            String studentName = rs.getString("username");

            // Check if student data is valid
            if (studentId > 0 && studentName != null && !studentName.isEmpty()) {
                entries.add(new StudentFeesEntry(studentId, studentName));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return entries;
}

// Method to save fees details to the database
private void saveFeesDetails(int studentId, double amountPaid, String paymentMode, double debit, double credit) {
    String checkExistingQuery = "SELECT amount_paid, credit, debit FROM fees WHERE student_id = ?";
    String updateFeesQuery = "UPDATE fees SET amount_paid = amount_paid + ?, credit = credit + ?, debit = debit + ? WHERE student_id = ?";
    String insertFeesQuery = "INSERT INTO fees (student_id, amount_paid, credit, debit) VALUES (?, ?, ?, ?)";

    try (PreparedStatement checkStmt = conn.prepareStatement(checkExistingQuery)) {
        checkStmt.setInt(1, studentId);
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            // If student ID exists, update the existing record
            try (PreparedStatement updateStmt = conn.prepareStatement(updateFeesQuery)) {
                updateStmt.setDouble(1, amountPaid);
                updateStmt.setDouble(2, credit);
                updateStmt.setDouble(3, debit);
                updateStmt.setInt(4, studentId);
                updateStmt.executeUpdate();
            }
        } else {
            // If student ID does not exist, insert a new record
            try (PreparedStatement insertStmt = conn.prepareStatement(insertFeesQuery)) {
                insertStmt.setInt(1, studentId);
                insertStmt.setDouble(2, amountPaid);
                insertStmt.setDouble(3, credit);
                insertStmt.setDouble(4, debit);
                insertStmt.executeUpdate();
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}



    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}