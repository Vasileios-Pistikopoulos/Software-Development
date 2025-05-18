package _5336_4701_5281.swdeproj.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;  // TRAINEE, COMPANY, PROFESSOR, COMMITTEE

    // === Συσχετίσεις με τα προφίλ ===

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private TraineeProfile traineeProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private CompanyProfile companyProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProfessorProfile professorProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private CommitteeProfile committeeProfile;

    // === Constructors ===

    public User() {}

    public User(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // === Getters & Setters ===

    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public TraineeProfile getTraineeProfile() { return traineeProfile; }
    public void setTraineeProfile(TraineeProfile traineeProfile) {
        this.traineeProfile = traineeProfile;
        if (traineeProfile != null) traineeProfile.setUser(this);
    }

    public CompanyProfile getCompanyProfile() { return companyProfile; }
    public void setCompanyProfile(CompanyProfile companyProfile) {
        this.companyProfile = companyProfile;
        if (companyProfile != null) companyProfile.setUser(this);
    }

    public ProfessorProfile getProfessorProfile() { return professorProfile; }
    public void setProfessorProfile(ProfessorProfile professorProfile) {
        this.professorProfile = professorProfile;
        if (professorProfile != null) professorProfile.setUser(this);
    }

    public CommitteeProfile getCommitteeProfile() { return committeeProfile; }
    public void setCommitteeProfile(CommitteeProfile committeeProfile) {
        this.committeeProfile = committeeProfile;
        if (committeeProfile != null) committeeProfile.setUser(this);
    }

    // === equals & hashCode ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
