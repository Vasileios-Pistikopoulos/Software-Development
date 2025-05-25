package _5336_4701_5281.swdeproj.dto;

import _5336_4701_5281.swdeproj.model.Application;
import java.time.LocalDateTime;
import java.util.List;

public class ApplicationDto {
    private Long id;
    private LocalDateTime applicationDate;
    private String coverLetter;
    private String comments;
    private Application.Status status;
    private String traineeName;
    private String studentId;
    private TraineeshipDto traineeship;
    private List<String> skills;
    private List<String> interests;
    private String preferredLocation;

    // === Getters & Setters ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getApplicationDate() { return applicationDate; }
    public void setApplicationDate(LocalDateTime applicationDate) { this.applicationDate = applicationDate; }

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Application.Status getStatus() { return status; }
    public void setStatus(Application.Status status) { this.status = status; }

    public String getTraineeName() { return traineeName; }
    public void setTraineeName(String traineeName) { this.traineeName = traineeName; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public TraineeshipDto getTraineeship() { return traineeship; }
    public void setTraineeship(TraineeshipDto traineeship) { this.traineeship = traineeship; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }

    public String getPreferredLocation() { return preferredLocation; }
    public void setPreferredLocation(String preferredLocation) { this.preferredLocation = preferredLocation; }
} 