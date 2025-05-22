package _5336_4701_5281.swdeproj.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "logbook_entries")
public class LogbookEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotBlank(message = "Task description is required")
    @Column(columnDefinition = "TEXT")
    private String taskDescription;

    @Min(value = 0, message = "Hours must be greater than or equal to 0")
    private double hoursSpent;

    @Column(columnDefinition = "TEXT")
    private String learningOutcomes;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @ManyToOne
    @JoinColumn(name = "trainee_id")
    private User trainee;

    @ManyToOne
    @JoinColumn(name = "traineeship_id")
    private Traineeship traineeship;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    // === Constructors ===

    public LogbookEntry() {}

    public LogbookEntry(LocalDate date, String taskDescription, double hoursSpent, String learningOutcomes, User trainee, Traineeship traineeship) {
        this.date = date;
        this.taskDescription = taskDescription;
        this.hoursSpent = hoursSpent;
        this.learningOutcomes = learningOutcomes;
        this.trainee = trainee;
        this.traineeship = traineeship;
    }

    // === Getters & Setters ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getTaskDescription() { return taskDescription; }
    public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }

    public double getHoursSpent() { return hoursSpent; }
    public void setHoursSpent(double hoursSpent) { this.hoursSpent = hoursSpent; }

    public String getLearningOutcomes() { return learningOutcomes; }
    public void setLearningOutcomes(String learningOutcomes) { this.learningOutcomes = learningOutcomes; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public User getTrainee() { return trainee; }
    public void setTrainee(User trainee) { this.trainee = trainee; }

    public Traineeship getTraineeship() { return traineeship; }
    public void setTraineeship(Traineeship traineeship) { this.traineeship = traineeship; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    // === equals & hashCode ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogbookEntry that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
} 