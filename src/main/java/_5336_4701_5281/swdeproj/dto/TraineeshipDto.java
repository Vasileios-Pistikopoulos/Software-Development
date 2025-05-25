package _5336_4701_5281.swdeproj.dto;

import _5336_4701_5281.swdeproj.model.Traineeship;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TraineeshipDto {
    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> skillsRequired;
    private List<String> topics;
    private String location;
    private Long companyId;
    private String companyName;
    private Long assignedTraineeId;
    private String assignedTraineeName;
    private String assignedTraineeStudentId;
    private Long supervisorId;
    private String supervisorName;
    private boolean isAssigned;
    private boolean hasEvaluation;
    private Traineeship.Status status;
    private List<ApplicationDto> applications;

    // Evaluation fields
    private Integer studentMotivation;
    private Integer studentEffectiveness;
    private Integer studentEfficiency;
    private Integer companyFacilities;
    private Integer companyGuidance;
    private String evaluationComments;
    private LocalDateTime evaluationDate;

    // Add completion fields
    private String completionOutcome;
    private String completionComments;
    private LocalDate completionDate;

    // === Getters & Setters ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public List<String> getSkillsRequired() { return skillsRequired; }
    public void setSkillsRequired(List<String> skillsRequired) { this.skillsRequired = skillsRequired; }

    public List<String> getTopics() { return topics; }
    public void setTopics(List<String> topics) { this.topics = topics; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public Long getAssignedTraineeId() { return assignedTraineeId; }
    public void setAssignedTraineeId(Long assignedTraineeId) { this.assignedTraineeId = assignedTraineeId; }

    public String getAssignedTraineeName() { return assignedTraineeName; }
    public void setAssignedTraineeName(String assignedTraineeName) { this.assignedTraineeName = assignedTraineeName; }

    public String getAssignedTraineeStudentId() { return assignedTraineeStudentId; }
    public void setAssignedTraineeStudentId(String assignedTraineeStudentId) { this.assignedTraineeStudentId = assignedTraineeStudentId; }

    public Long getSupervisorId() { return supervisorId; }
    public void setSupervisorId(Long supervisorId) { this.supervisorId = supervisorId; }

    public String getSupervisorName() { return supervisorName; }
    public void setSupervisorName(String supervisorName) { this.supervisorName = supervisorName; }

    public boolean isAssigned() { return isAssigned; }
    public void setAssigned(boolean assigned) { isAssigned = assigned; }

    public boolean isHasEvaluation() { return hasEvaluation; }
    public void setHasEvaluation(boolean hasEvaluation) { this.hasEvaluation = hasEvaluation; }

    public Traineeship.Status getStatus() { return status; }
    public void setStatus(Traineeship.Status status) { this.status = status; }

    public List<ApplicationDto> getApplications() { return applications; }
    public void setApplications(List<ApplicationDto> applications) { this.applications = applications; }

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

    public Integer getCompanyFacilities() {
        return companyFacilities;
    }

    public void setCompanyFacilities(Integer companyFacilities) {
        this.companyFacilities = companyFacilities;
    }

    public Integer getCompanyGuidance() {
        return companyGuidance;
    }

    public void setCompanyGuidance(Integer companyGuidance) {
        this.companyGuidance = companyGuidance;
    }

    public String getEvaluationComments() {
        return evaluationComments;
    }

    public void setEvaluationComments(String evaluationComments) {
        this.evaluationComments = evaluationComments;
    }

    public LocalDateTime getEvaluationDate() {
        return evaluationDate;
    }

    public void setEvaluationDate(LocalDateTime evaluationDate) {
        this.evaluationDate = evaluationDate;
    }

    public String getCompletionOutcome() {
        return completionOutcome;
    }

    public void setCompletionOutcome(String completionOutcome) {
        this.completionOutcome = completionOutcome;
    }

    public String getCompletionComments() {
        return completionComments;
    }

    public void setCompletionComments(String completionComments) {
        this.completionComments = completionComments;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }
} 