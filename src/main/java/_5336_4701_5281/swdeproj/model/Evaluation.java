package _5336_4701_5281.swdeproj.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "evaluations")
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private int motivationRating;

    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private int effectivenessRating;

    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private int efficiencyRating;

    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private int facilitiesRating;

    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private int guidanceRating;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @ManyToOne
    @JoinColumn(name = "traineeship_id")
    private Traineeship traineeship;

    @ManyToOne
    @JoinColumn(name = "evaluator_id")
    private User evaluator;

    @Enumerated(EnumType.STRING)
    private EvaluatorType evaluatorType;

    public enum EvaluatorType {
        COMPANY, PROFESSOR
    }

    // === Constructors ===

    public Evaluation() {}

    public Evaluation(Traineeship traineeship, EvaluatorType evaluatorType,
                     Integer motivation, Integer effectiveness, Integer efficiency) {
        this.traineeship = traineeship;
        this.evaluatorType = evaluatorType;
        this.motivationRating = motivation;
        this.effectivenessRating = effectiveness;
        this.efficiencyRating = efficiency;
    }

    // === Getters & Setters ===

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }

    public void setDate(LocalDate date) { this.date = date; }

    public int getMotivationRating() { return motivationRating; }

    public void setMotivationRating(int motivationRating) { this.motivationRating = motivationRating; }

    public int getEffectivenessRating() { return effectivenessRating; }

    public void setEffectivenessRating(int effectivenessRating) { this.effectivenessRating = effectivenessRating; }

    public int getEfficiencyRating() { return efficiencyRating; }

    public void setEfficiencyRating(int efficiencyRating) { this.efficiencyRating = efficiencyRating; }

    public int getFacilitiesRating() { return facilitiesRating; }

    public void setFacilitiesRating(int facilitiesRating) { this.facilitiesRating = facilitiesRating; }

    public int getGuidanceRating() { return guidanceRating; }

    public void setGuidanceRating(int guidanceRating) { this.guidanceRating = guidanceRating; }

    public String getComments() { return comments; }

    public void setComments(String comments) { this.comments = comments; }

    public Traineeship getTraineeship() { return traineeship; }

    public void setTraineeship(Traineeship traineeship) { this.traineeship = traineeship; }

    public User getEvaluator() { return evaluator; }

    public void setEvaluator(User evaluator) { this.evaluator = evaluator; }

    public EvaluatorType getEvaluatorType() { return evaluatorType; }

    public void setEvaluatorType(EvaluatorType evaluatorType) { this.evaluatorType = evaluatorType; }

    // === equals & hashCode ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Evaluation that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
} 