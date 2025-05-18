package _5336_4701_5281.swdeproj.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;

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
    private List<String> interests;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // === Constructors ===

    public ProfessorProfile() {}

    public ProfessorProfile(String fullName, List<String> interests) {
        this.fullName = fullName;
        this.interests = interests;
    }

    // === Getters & Setters ===

    public Long getId() { return id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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
