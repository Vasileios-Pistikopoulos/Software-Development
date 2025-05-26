package _5336_4701_5281.swdeproj.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_evaluations")
public class CompanyEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "traineeship_id", nullable = false)
    private Traineeship traineeship;

    // Student evaluation ratings (1-5)
    @Column(nullable = false)
    private Integer studentMotivation;

    @Column(nullable = false)
    private Integer studentEffectiveness;

    @Column(nullable = false)
    private Integer studentEfficiency;

    // Additional comments
    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(nullable = false)
    private LocalDateTime evaluationDate;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Traineeship getTraineeship() {
        return traineeship;
    }

    public void setTraineeship(Traineeship traineeship) {
        this.traineeship = traineeship;
    }

    public Integer getStudentMotivation() {
        return studentMotivation;
    }

    public void setStudentMotivation(Integer studentMotivation) {
        this.studentMotivation = studentMotivation;
    }

    public Integer getStudentEffectiveness() {
        return studentEffectiveness;
    }

    public void setStudentEffectiveness(Integer studentEffectiveness) {
        this.studentEffectiveness = studentEffectiveness;
    }

    public Integer getStudentEfficiency() {
        return studentEfficiency;
    }

    public void setStudentEfficiency(Integer studentEfficiency) {
        this.studentEfficiency = studentEfficiency;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public LocalDateTime getEvaluationDate() {
        return evaluationDate;
    }

    public void setEvaluationDate(LocalDateTime evaluationDate) {
        this.evaluationDate = evaluationDate;
    }

    // Helper method to calculate average student rating
    public double getAverageStudentRating() {
        return (studentMotivation + studentEffectiveness + studentEfficiency) / 3.0;
    }
} 