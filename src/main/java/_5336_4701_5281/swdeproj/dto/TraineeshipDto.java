package _5336_4701_5281.swdeproj.dto;

import java.time.LocalDate;
import java.util.List;

public class TraineeshipDto {
    private Long id;
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
    private Long supervisorId;
    private String supervisorName;
    private boolean isAssigned;
    private boolean hasEvaluation;

    // === Getters & Setters ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Long getSupervisorId() { return supervisorId; }
    public void setSupervisorId(Long supervisorId) { this.supervisorId = supervisorId; }

    public String getSupervisorName() { return supervisorName; }
    public void setSupervisorName(String supervisorName) { this.supervisorName = supervisorName; }

    public boolean isAssigned() { return isAssigned; }
    public void setAssigned(boolean assigned) { isAssigned = assigned; }

    public boolean isHasEvaluation() { return hasEvaluation; }
    public void setHasEvaluation(boolean hasEvaluation) { this.hasEvaluation = hasEvaluation; }
} 