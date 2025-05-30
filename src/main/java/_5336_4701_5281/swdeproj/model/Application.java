package _5336_4701_5281.swdeproj.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Application date is required")
    @Column(name = "application_date", nullable = false)
    private LocalDateTime applicationDate;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @ManyToOne
    @JoinColumn(name = "trainee_id", nullable = false)
    private TraineeProfile trainee;

    @ManyToOne
    @JoinColumn(name = "assigned_traineeship_id", nullable = true)
    private Traineeship assignedTraineeship;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private User supervisor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }

    // === Constructors ===

    public Application() {}

    public Application(TraineeProfile trainee) {
        this.trainee = trainee;
        this.status = Status.PENDING;
        this.applicationDate = LocalDateTime.now();
    }

    // === Getters & Setters ===

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public LocalDateTime getApplicationDate() { return applicationDate; }
    public void setApplicationDate(LocalDateTime applicationDate) { this.applicationDate = applicationDate; }

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public TraineeProfile getTrainee() { return trainee; }
    public void setTrainee(TraineeProfile trainee) { this.trainee = trainee; }

    public Traineeship getAssignedTraineeship() { return assignedTraineeship; }
    public void setAssignedTraineeship(Traineeship assignedTraineeship) { this.assignedTraineeship = assignedTraineeship; }

    public User getSupervisor() { return supervisor; }
    public void setSupervisor(User supervisor) { this.supervisor = supervisor; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    // === equals & hashCode ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Application that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}