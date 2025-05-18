package _5336_4701_5281.swdeproj.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "company_profiles")
public class CompanyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String location;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // === Constructors ===

    public CompanyProfile() {}

    public CompanyProfile(String companyName, String location) {
        this.companyName = companyName;
        this.location = location;
    }

    // === Getters & Setters ===

    public Long getId() { return id; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    // === equals & hashCode ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompanyProfile that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
