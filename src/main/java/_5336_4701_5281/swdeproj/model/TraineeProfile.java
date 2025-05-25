package _5336_4701_5281.swdeproj.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "trainee_profiles")
public class TraineeProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String studentId;

    @ElementCollection
    @CollectionTable(name = "trainee_interests", joinColumns = @JoinColumn(name = "trainee_id"))
    @Column(name = "interest")
    private Set<String> interests = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "trainee_skills", joinColumns = @JoinColumn(name = "trainee_id"))
    @Column(name = "skill")
    private Set<String> skills = new HashSet<>();

    private String preferredLocation;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "trainee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Application> applications = new ArrayList<>();

    @OneToMany(mappedBy = "assignedTrainee")
    private List<Traineeship> assignedTraineeships = new ArrayList<>();

    // === Constructors ===

    public TraineeProfile() {
        this.interests = new HashSet<>();
        this.skills = new HashSet<>();
        this.applications = new ArrayList<>();
        this.assignedTraineeships = new ArrayList<>();
    }

    public TraineeProfile(String fullName, String studentId, Set<String> interests, Set<String> skills, String preferredLocation) {
        this();
        this.fullName = fullName;
        this.studentId = studentId;
        this.interests = interests != null ? interests : new HashSet<>();
        this.skills = skills != null ? skills : new HashSet<>();
        this.preferredLocation = preferredLocation;
    }

    // === Getters & Setters ===

    public Long getId() { return id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public Set<String> getInterests() { return interests; }
    public void setInterests(Set<String> interests) { this.interests = interests; }

    public Set<String> getSkills() { return skills; }
    public void setSkills(Set<String> skills) { this.skills = skills; }

    public String getPreferredLocation() { return preferredLocation; }
    public void setPreferredLocation(String preferredLocation) { this.preferredLocation = preferredLocation; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<Application> getApplications() { return applications; }
    public void setApplications(List<Application> applications) { this.applications = applications; }

    public List<Traineeship> getAssignedTraineeships() { return assignedTraineeships; }
    public void setAssignedTraineeships(List<Traineeship> assignedTraineeships) { this.assignedTraineeships = assignedTraineeships; }

    // === equals & hashCode ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TraineeProfile that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
