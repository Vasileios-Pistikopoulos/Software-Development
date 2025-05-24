package _5336_4701_5281.swdeproj.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    private Role role;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "roles")
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "trainee")
    private Set<LogbookEntry> logbookEntries = new HashSet<>();

    @OneToMany(mappedBy = "evaluator")
    private Set<Evaluation> evaluations = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Notification> notifications = new HashSet<>();

    // === Συσχετίσεις με τα προφίλ ===

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private TraineeProfile traineeProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private CompanyProfile companyProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Company company;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProfessorProfile professorProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private CommitteeProfile committeeProfile;

    public enum Role {
        ROLE_TRAINEE,
        ROLE_COMPANY,
        ROLE_PROFESSOR,
        ROLE_COMMITTEE
    }

    // === Constructors ===

    public User() {}

    public User(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles = new HashSet<>();
        this.roles.add(Role.valueOf(role));
    }

    // === Getters & Setters ===

    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public void addRole(Role role) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(role);
    }

    public boolean hasRole(Role role) { return roles.contains(role); }

    public Set<Notification> getNotifications() { return notifications; }
    public void setNotifications(Set<Notification> notifications) { this.notifications = notifications; }

    public void addNotification(Notification notification) {
        notifications.add(notification);
        notification.setUser(this);
    }

    public void removeNotification(Notification notification) {
        notifications.remove(notification);
        notification.setUser(null);
    }

    public TraineeProfile getTraineeProfile() { return traineeProfile; }
    public void setTraineeProfile(TraineeProfile traineeProfile) {
        this.traineeProfile = traineeProfile;
        if (traineeProfile != null) traineeProfile.setUser(this);
    }

    public CompanyProfile getCompanyProfile() { return companyProfile; }
    public void setCompanyProfile(CompanyProfile companyProfile) {
        this.companyProfile = companyProfile;
        if (companyProfile != null) {
            companyProfile.setUser(this);
            if (this.company != null) {
                companyProfile.setCompany(this.company);
                this.company.setProfile(companyProfile);
            }
        }
    }

    public Company getCompany() { return company; }
    public void setCompany(Company company) {
        this.company = company;
        if (company != null) {
            company.setUser(this);
            if (this.companyProfile != null) {
                this.companyProfile.setCompany(company);
                company.setProfile(this.companyProfile);
            }
        }
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

    public Set<LogbookEntry> getLogbookEntries() { return logbookEntries; }
    public void setLogbookEntries(Set<LogbookEntry> logbookEntries) { this.logbookEntries = logbookEntries; }

    public Set<Evaluation> getEvaluations() { return evaluations; }
    public void setEvaluations(Set<Evaluation> evaluations) { this.evaluations = evaluations; }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
        if (role != null) {
            addRole(role);
        }
    }

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
