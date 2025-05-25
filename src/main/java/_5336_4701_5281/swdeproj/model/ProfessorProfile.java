package _5336_4701_5281.swdeproj.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

@Entity
@Table(name = "professor_profiles")
public class ProfessorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @ElementCollection
    @CollectionTable(name = "professor_interests", joinColumns = @JoinColumn(name = "professor_id"))
    @Column(name = "interest")
    private Set<String> interests = new HashSet<>();

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "supervisor")
    private List<Traineeship> supervisedTraineeships;

    private double loadScore;
    private double interestScore;
    private double suitabilityScore;

    // === Constructors ===

    public ProfessorProfile() {
        this.interests = new HashSet<>();
        this.supervisedTraineeships = new ArrayList<>();
    }

    public ProfessorProfile(String fullName) {
        this();
        this.fullName = fullName;
    }

    // === Getters & Setters ===

    public Long getId() { return id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Set<String> getInterests() { return interests; }
    public void setInterests(Set<String> interests) { this.interests = interests; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<Traineeship> getSupervisedTraineeships() { return supervisedTraineeships; }
    public void setSupervisedTraineeships(List<Traineeship> supervisedTraineeships) { this.supervisedTraineeships = supervisedTraineeships; }

    public double getLoadScore() {
        return loadScore;
    }

    public void setLoadScore(double loadScore) {
        this.loadScore = loadScore;
    }

    public double getInterestScore() {
        return interestScore;
    }

    public void setInterestScore(double interestScore) {
        this.interestScore = interestScore;
    }

    public double getSuitabilityScore() {
        return suitabilityScore;
    }

    public void setSuitabilityScore(double suitabilityScore) {
        this.suitabilityScore = suitabilityScore;
    }

    // === equals & hashCode ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProfessorProfile that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
