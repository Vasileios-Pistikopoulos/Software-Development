package _5336_4701_5281.swdeproj.model;

import jakarta.persistence.*;

@Entity
@Table(name = "professors")
public class Professor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Σύνδεση με τον User (login)
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Σύνδεση με το profile (περιέχει full name, interests)
    @OneToOne
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private ProfessorProfile profile;

    // === Constructors ===

    public Professor() {}

    public Professor(User user, ProfessorProfile profile) {
        this.user = user;
        this.profile = profile;
    }

    // === Getters & Setters ===

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ProfessorProfile getProfile() { return profile; }
    public void setProfile(ProfessorProfile profile) { this.profile = profile; }
}
