package _5336_4701_5281.swdeproj.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;

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
    private List<String> interests;

    @ElementCollection
    @CollectionTable(name = "trainee_skills", joinColumns = @JoinColumn(name = "trainee_id"))
    @Column(name = "skill")
    private List<String> skills;

    private String preferredLocation;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // === Constructors ===

    public TraineeProfile() {}

    public TraineeProfile(String fullName, String studentId, List<String> interests, List<String> skills, String preferredLocation) {
        this.fullName = fullName;
        this.studentId = studentId;
        this.interests = interests;
        this.skills = skills;
        this.preferredLocation = preferredLocation;
    }

    // === Getters & Setters ===

    public Long getId() { return id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public String getPreferredLocation() { return preferredLocation; }
    public void setPreferredLocation(String preferredLocation) { this.preferredLocation = preferredLocation; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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
