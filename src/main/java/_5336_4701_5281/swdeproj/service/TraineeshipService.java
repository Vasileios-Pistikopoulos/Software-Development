package _5336_4701_5281.swdeproj.service;

import _5336_4701_5281.swdeproj.model.ProfessorProfile;
import _5336_4701_5281.swdeproj.model.Traineeship;
import _5336_4701_5281.swdeproj.repository.ProfessorProfileRepository;
import _5336_4701_5281.swdeproj.repository.TraineeshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.AbstractMap;

@Service
public class TraineeshipService {

    @Autowired
    private ProfessorProfileRepository professorRepo;

    @Autowired
    private TraineeshipRepository traineeshipRepo;

    public List<ProfessorProfile> findSuitableProfessors(Traineeship traineeship) {
        // Get all professors
        List<ProfessorProfile> allProfessors = professorRepo.findAll();
        
        // Calculate professor load (number of active traineeships)
        Map<Long, Long> professorLoad = new HashMap<>();
        for (ProfessorProfile prof : allProfessors) {
            long activeTraineeships = prof.getSupervisedTraineeships().stream()
                .filter(t -> t.getStatus() == Traineeship.Status.FILLED)
                .count();
            professorLoad.put(prof.getId(), activeTraineeships);
        }

        // Score professors based on interests match and load
        return allProfessors.stream()
            .map(prof -> {
                double interestScore = calculateInterestScore(prof, traineeship);
                double loadScore = calculateLoadScore(professorLoad.get(prof.getId()));
                double totalScore = interestScore * 0.7 + loadScore * 0.3; // 70% weight to interests, 30% to load
                return new AbstractMap.SimpleEntry<>(prof, totalScore);
            })
            .sorted(Map.Entry.<ProfessorProfile, Double>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    private double calculateInterestScore(ProfessorProfile professor, Traineeship traineeship) {
        if (professor.getInterests() == null || professor.getInterests().isEmpty()) {
            return 0.0;
        }

        // Convert interests to lowercase for case-insensitive matching
        Set<String> professorInterests = professor.getInterests().stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        // Check for matches in position title and description
        String positionText = (traineeship.getTitle() + " " + 
                             traineeship.getDescription()).toLowerCase();

        // Count matching interests
        long matches = professorInterests.stream()
            .filter(interest -> positionText.contains(interest))
            .count();

        // Calculate score based on number of matches
        return matches > 0 ? 1.0 : 0.0;
    }

    private double calculateLoadScore(long currentLoad) {
        // Score decreases as load increases
        // Maximum load considered is 5 traineeships
        return Math.max(0, 1.0 - (currentLoad / 5.0));
    }

    @Transactional
    public void assignSupervisor(Long traineeshipId, Long professorId) {
        Traineeship traineeship = traineeshipRepo.findById(traineeshipId)
            .orElseThrow(() -> new RuntimeException("Traineeship not found"));
        
        ProfessorProfile professor = professorRepo.findById(professorId)
            .orElseThrow(() -> new RuntimeException("Professor not found"));

        traineeship.setSupervisor(professor);
        traineeshipRepo.save(traineeship);
    }

    public Traineeship getTraineeshipById(Long id) {
        return traineeshipRepo.findById(id)
                .orElse(null);
    }
} 