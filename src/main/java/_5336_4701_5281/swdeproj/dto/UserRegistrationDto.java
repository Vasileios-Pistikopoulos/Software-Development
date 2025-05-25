package _5336_4701_5281.swdeproj.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRegistrationDto {

    // Κοινά πεδία για όλους τους χρήστες
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotBlank(message = "Role is required")
    private String role; // TRAINEE, COMPANY, PROFESSOR, COMMITTEE

    // Trainee fields
    private String traineeFullName;
    private String studentId;
    private String traineeSkills;        // comma-separated string
    private String traineeInterests;     // comma-separated string
    private String preferredLocation;

    // Company fields
    private String companyName;
    private String companyLocation;
    private String companyPhone;

    // Professor fields
    private String professorFullName;

    // === Getters & Setters ===

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTraineeFullName() { return traineeFullName; }
    public void setTraineeFullName(String traineeFullName) { this.traineeFullName = traineeFullName; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getTraineeSkills() { return traineeSkills; }
    public void setTraineeSkills(String traineeSkills) { this.traineeSkills = traineeSkills; }

    public String getTraineeInterests() { return traineeInterests; }
    public void setTraineeInterests(String traineeInterests) { this.traineeInterests = traineeInterests; }

    public String getPreferredLocation() { return preferredLocation; }
    public void setPreferredLocation(String preferredLocation) { this.preferredLocation = preferredLocation; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyLocation() { return companyLocation; }
    public void setCompanyLocation(String companyLocation) { this.companyLocation = companyLocation; }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }

    public String getProfessorFullName() { return professorFullName; }
    public void setProfessorFullName(String professorFullName) { this.professorFullName = professorFullName; }
}
