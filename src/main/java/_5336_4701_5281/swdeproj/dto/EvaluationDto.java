package _5336_4701_5281.swdeproj.dto;

import _5336_4701_5281.swdeproj.model.Evaluation.EvaluatorType;

public class EvaluationDto {
    private Long id;
    private Long traineeshipId;
    private String traineeName;
    private String companyName;
    private EvaluatorType evaluatorType;
    private Integer motivation;
    private Integer effectiveness;
    private Integer efficiency;
    private Integer facilities;
    private Integer guidance;

    // === Getters & Setters ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTraineeshipId() { return traineeshipId; }
    public void setTraineeshipId(Long traineeshipId) { this.traineeshipId = traineeshipId; }

    public String getTraineeName() { return traineeName; }
    public void setTraineeName(String traineeName) { this.traineeName = traineeName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public EvaluatorType getEvaluatorType() { return evaluatorType; }
    public void setEvaluatorType(EvaluatorType evaluatorType) { this.evaluatorType = evaluatorType; }

    public Integer getMotivation() { return motivation; }
    public void setMotivation(Integer motivation) { this.motivation = motivation; }

    public Integer getEffectiveness() { return effectiveness; }
    public void setEffectiveness(Integer effectiveness) { this.effectiveness = effectiveness; }

    public Integer getEfficiency() { return efficiency; }
    public void setEfficiency(Integer efficiency) { this.efficiency = efficiency; }

    public Integer getFacilities() { return facilities; }
    public void setFacilities(Integer facilities) { this.facilities = facilities; }

    public Integer getGuidance() { return guidance; }
    public void setGuidance(Integer guidance) { this.guidance = guidance; }
} 