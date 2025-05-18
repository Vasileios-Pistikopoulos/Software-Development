package _5336_4701_5281.swdeproj.model;

import jakarta.persistence.*;

@Entity
@Table(name = "committee_profiles")
public class CommitteeProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // === Constructors ===
    public CommitteeProfile() {}

    public CommitteeProfile(User user) {
        this.user = user;
    }

    // === Getters & Setters ===
    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
