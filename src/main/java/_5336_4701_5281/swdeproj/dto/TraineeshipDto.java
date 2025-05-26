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

    // Company evaluation fields
    private boolean hasCompanyEvaluation;
    private Integer companyStudentMotivation;
    private Integer companyStudentEffectiveness;
    private Integer companyStudentEfficiency;
    private String companyComments;
    private LocalDateTime companyEvaluationDate;

    // Professor evaluation fields
    private boolean hasProfessorEvaluation;
    private Integer professorStudentMotivation;
    private Integer professorStudentEffectiveness;
    private Integer professorStudentEfficiency;
    private Integer professorCompanyFacilities;
    private Integer professorCompanyGuidance;
    private String professorComments;
    private LocalDateTime professorEvaluationDate;

    // Completion fields
    private String completionOutcome;
    private String completionComments;
    private LocalDate completionDate;
    private boolean hasBothEvaluations;

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

    public boolean isHasCompanyEvaluation() {
        return hasCompanyEvaluation;
    }

    public void setHasCompanyEvaluation(boolean hasCompanyEvaluation) {
        this.hasCompanyEvaluation = hasCompanyEvaluation;
    }

    public boolean isHasProfessorEvaluation() {
        return hasProfessorEvaluation;
    }

    public void setHasProfessorEvaluation(boolean hasProfessorEvaluation) {
        this.hasProfessorEvaluation = hasProfessorEvaluation;
    }

    // Company evaluation getters and setters
    public Integer getCompanyStudentMotivation() {
        return companyStudentMotivation;
    }

    public void setCompanyStudentMotivation(Integer companyStudentMotivation) {
        this.companyStudentMotivation = companyStudentMotivation;
    }

    public Integer getCompanyStudentEffectiveness() {
        return companyStudentEffectiveness;
    }

    public void setCompanyStudentEffectiveness(Integer companyStudentEffectiveness) {
        this.companyStudentEffectiveness = companyStudentEffectiveness;
    }

    public Integer getCompanyStudentEfficiency() {
        return companyStudentEfficiency;
    }

    public void setCompanyStudentEfficiency(Integer companyStudentEfficiency) {
        this.companyStudentEfficiency = companyStudentEfficiency;
    }

    public String getCompanyComments() {
        return companyComments;
    }

    public void setCompanyComments(String companyComments) {
        this.companyComments = companyComments;
    }

    public LocalDateTime getCompanyEvaluationDate() {
        return companyEvaluationDate;
    }

    public void setCompanyEvaluationDate(LocalDateTime companyEvaluationDate) {
        this.companyEvaluationDate = companyEvaluationDate;
    }

    // Professor evaluation getters and setters
    public Integer getProfessorStudentMotivation() {
        return professorStudentMotivation;
    }

    public void setProfessorStudentMotivation(Integer professorStudentMotivation) {
        this.professorStudentMotivation = professorStudentMotivation;
    }

    public Integer getProfessorStudentEffectiveness() {
        return professorStudentEffectiveness;
    }

    public void setProfessorStudentEffectiveness(Integer professorStudentEffectiveness) {
        this.professorStudentEffectiveness = professorStudentEffectiveness;
    }

    public Integer getProfessorStudentEfficiency() {
        return professorStudentEfficiency;
    }

    public void setProfessorStudentEfficiency(Integer professorStudentEfficiency) {
        this.professorStudentEfficiency = professorStudentEfficiency;
    }

    public Integer getProfessorCompanyFacilities() {
        return professorCompanyFacilities;
    }

    public void setProfessorCompanyFacilities(Integer professorCompanyFacilities) {
        this.professorCompanyFacilities = professorCompanyFacilities;
    }

    public Integer getProfessorCompanyGuidance() {
        return professorCompanyGuidance;
    }

    public void setProfessorCompanyGuidance(Integer professorCompanyGuidance) {
        this.professorCompanyGuidance = professorCompanyGuidance;
    }

    public String getProfessorComments() {
        return professorComments;
    }

    public void setProfessorComments(String professorComments) {
        this.professorComments = professorComments;
    }

    public LocalDateTime getProfessorEvaluationDate() {
        return professorEvaluationDate;
    }

    public void setProfessorEvaluationDate(LocalDateTime professorEvaluationDate) {
        this.professorEvaluationDate = professorEvaluationDate;
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

    public boolean isHasBothEvaluations() {
        return hasProfessorEvaluation && hasCompanyEvaluation;
    }

    public void setHasBothEvaluations(boolean hasBothEvaluations) {
        this.hasBothEvaluations = hasBothEvaluations;
    }
} 