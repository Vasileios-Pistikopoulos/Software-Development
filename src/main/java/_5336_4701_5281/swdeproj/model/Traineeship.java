package _5336_4701_5281.swdeproj.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "traineeships")
public class Traineeship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @ElementCollection
    @CollectionTable(name = "traineeship_skills")
    private Set<String> requiredSkills = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "traineeship_topics")
    private Set<String> topics = new HashSet<>();

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne
    @JoinColumn(name = "assigned_trainee_id")
    private TraineeProfile assignedTrainee;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private ProfessorProfile  supervisor;

    @Enumerated(EnumType.STRING)
    private Status status = Status.OPEN;

    @AssertTrue(message = "End date must be after start date")
    private boolean isEndDateValid() {
        if (startDate == null || endDate == null) {
            return true; // Let @NotNull handle null validation
        }
        return endDate.isAfter(startDate);
    }

    public enum Status {
        OPEN, CLOSED, FILLED
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Set<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(Set<String> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public Set<String> getTopics() {
        return topics;
    }

    public void setTopics(Set<String> topics) {
        this.topics = topics;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public TraineeProfile getAssignedTrainee() {
        return assignedTrainee;
    }

    public void setAssignedTrainee(TraineeProfile assignedTrainee) {
        this.assignedTrainee = assignedTrainee;
    }

    public ProfessorProfile  getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(ProfessorProfile  supervisor) {
        this.supervisor = supervisor;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void complete() {
        this.status = Status.CLOSED;
    }
} 