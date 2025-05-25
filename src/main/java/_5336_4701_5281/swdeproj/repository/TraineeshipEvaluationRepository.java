package _5336_4701_5281.swdeproj.repository;

import _5336_4701_5281.swdeproj.model.TraineeshipEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TraineeshipEvaluationRepository extends JpaRepository<TraineeshipEvaluation, Long> {
    Optional<TraineeshipEvaluation> findByTraineeshipId(Long traineeshipId);
    boolean existsByTraineeshipId(Long traineeshipId);
} 