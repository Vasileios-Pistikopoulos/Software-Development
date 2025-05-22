package _5336_4701_5281.swdeproj.repository;

import _5336_4701_5281.swdeproj.model.Traineeship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TraineeshipRepository extends JpaRepository<Traineeship, Long> {
    List<Traineeship> findByLocation(String location);
    
    List<Traineeship> findByStatus(Traineeship.Status status);
    
    @Query("SELECT t FROM Traineeship t WHERE :skill MEMBER OF t.requiredSkills")
    List<Traineeship> findByRequiredSkill(@Param("skill") String skill);
    
    @Query("SELECT t FROM Traineeship t WHERE :topic MEMBER OF t.topics")
    List<Traineeship> findByTopic(@Param("topic") String topic);
    
    List<Traineeship> findByCompanyId(Long companyId);
    
    @Query("SELECT t FROM Traineeship t WHERE t.status = 'OPEN' AND " +
           "(:location IS NULL OR t.location = :location) AND " +
           "(:skill IS NULL OR :skill MEMBER OF t.requiredSkills) AND " +
           "(:topic IS NULL OR :topic MEMBER OF t.topics)")
    List<Traineeship> findByFilters(
        @Param("location") String location,
        @Param("skill") String skill,
        @Param("topic") String topic
    );

    long countByStatus(Traineeship.Status status);

    @Query("SELECT COUNT(t) FROM Traineeship t WHERE t.company.id = :companyId AND t.assignedTrainee IS NULL")
    long countByCompanyIdAndAssignedTraineeIsNull(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(t) FROM Traineeship t WHERE t.company.id = :companyId AND t.assignedTrainee IS NOT NULL")
    long countByCompanyIdAndAssignedTraineeIsNotNull(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(t) FROM Traineeship t WHERE t.supervisor.id = :supervisorId")
    long countBySupervisorId(@Param("supervisorId") Long supervisorId);

    @Query("SELECT COUNT(t) FROM Traineeship t WHERE t.supervisor.id = :supervisorId " +
           "AND NOT EXISTS (SELECT e FROM Evaluation e WHERE e.traineeship = t AND e.evaluatorType = 'SUPERVISOR')")
    long countBySupervisorIdAndEvaluationNotSubmitted(@Param("supervisorId") Long supervisorId);

    @Query("SELECT COUNT(DISTINCT t.assignedTrainee) FROM Traineeship t WHERE t.supervisor.id = :supervisorId")
    long countDistinctTraineesBySupervisorId(@Param("supervisorId") Long supervisorId);

    @Query("SELECT COUNT(t) FROM Traineeship t WHERE t.assignedTrainee IS NOT NULL")
    long countByAssignedTraineeIsNotNull();

    Traineeship findFirstByAssignedTraineeIdOrderByStartDateDesc(Long traineeId);

    List<Traineeship> findByCompanyIdAndAssignedTraineeIsNotNull(Long companyId);
    
    List<Traineeship> findBySupervisorId(Long supervisorId);
} 