package _5336_4701_5281.swdeproj.repository;

import _5336_4701_5281.swdeproj.model.Evaluation;
import _5336_4701_5281.swdeproj.model.Evaluation.EvaluatorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findByTraineeshipId(Long traineeshipId);
    Optional<Evaluation> findByTraineeshipIdAndEvaluatorType(Long traineeshipId, EvaluatorType evaluatorType);
    
    @Query("SELECT AVG(e.motivationRating) FROM Evaluation e")
    Optional<Double> findAverageMotivationRating();
    
    @Query("SELECT AVG(e.effectivenessRating) FROM Evaluation e")
    Optional<Double> findAverageEffectivenessRating();
    
    @Query("SELECT AVG(e.efficiencyRating) FROM Evaluation e")
    Optional<Double> findAverageEfficiencyRating();
    
    @Query("SELECT AVG(e.motivationRating) FROM Evaluation e WHERE e.traineeship.company.id = :companyId")
    Optional<Double> getAverageMotivationForCompany(@Param("companyId") Long companyId);
    
    @Query("SELECT AVG(e.effectivenessRating) FROM Evaluation e WHERE e.traineeship.company.id = :companyId")
    Optional<Double> getAverageEffectivenessForCompany(@Param("companyId") Long companyId);
    
    @Query("SELECT AVG(e.efficiencyRating) FROM Evaluation e WHERE e.traineeship.company.id = :companyId")
    Optional<Double> getAverageEfficiencyForCompany(@Param("companyId") Long companyId);
    
    @Query("SELECT AVG(e.facilitiesRating) FROM Evaluation e WHERE " +
           "e.traineeship.company.id = :companyId AND e.evaluatorType = 'PROFESSOR'")
    Optional<Double> getAverageFacilitiesForCompany(@Param("companyId") Long companyId);
    
    @Query("SELECT AVG(e.guidanceRating) FROM Evaluation e WHERE " +
           "e.traineeship.company.id = :companyId AND e.evaluatorType = 'PROFESSOR'")
    Optional<Double> getAverageGuidanceForCompany(@Param("companyId") Long companyId);

    long countByEvaluatorType(EvaluatorType evaluatorType);

    List<Evaluation> findTop5ByOrderByDateDesc();
} 