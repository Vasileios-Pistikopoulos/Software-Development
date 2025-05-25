package _5336_4701_5281.swdeproj.repository;

import _5336_4701_5281.swdeproj.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByTraineeId(Long traineeId);
    List<Application> findByAssignedTraineeshipId(Long traineeshipId);
    List<Application> findByTraineeIdAndStatus(Long traineeId, Application.Status status);
    List<Application> findByAssignedTraineeshipIdAndStatus(Long traineeshipId, Application.Status status);
    List<Application> findByStatus(Application.Status status);
    List<Application> findByTraineeIdOrderByApplicationDateDesc(Long traineeId);
    
    @Query("SELECT a FROM Application a WHERE a.trainee.id = :traineeId AND " +
           "a.applicationDate BETWEEN :startDate AND :endDate")
    List<Application> findByTraineeIdAndDateRange(@Param("traineeId") Long traineeId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DISTINCT a FROM Application a " +
           "LEFT JOIN FETCH a.trainee t " +
           "LEFT JOIN FETCH t.skills " +
           "LEFT JOIN FETCH t.interests " +
           "WHERE a.id = :id")
    Optional<Application> findByIdWithTrainee(@Param("id") Long id);

    @Query("SELECT a FROM Application a " +
           "LEFT JOIN FETCH a.trainee t " +
           "LEFT JOIN FETCH t.skills " +
           "LEFT JOIN FETCH t.interests " +
           "WHERE a.id = :id")
    Optional<Application> findByIdWithTraineeAndCollections(@Param("id") Long id);

    long countByTraineeIdAndStatus(Long traineeId, Application.Status status);
    long countByAssignedTraineeshipCompanyIdAndStatus(Long companyId, Application.Status status);
    long countByStatus(Application.Status status);
    
    Optional<Application> findByTraineeIdAndAssignedTraineeshipId(Long traineeId, Long traineeshipId);

    List<Application> findTop5ByOrderByApplicationDateDesc();
} 