package _5336_4701_5281.swdeproj.repository;

import _5336_4701_5281.swdeproj.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByTraineeId(Long traineeId);
    List<Application> findByTraineeshipId(Long traineeshipId);
    List<Application> findByTraineeIdAndStatus(Long traineeId, Application.Status status);
    List<Application> findByTraineeshipIdAndStatus(Long traineeshipId, Application.Status status);
    List<Application> findByStatus(Application.Status status);
    
    @Query("SELECT a FROM Application a WHERE a.trainee.id = :traineeId AND " +
           "a.applicationDate BETWEEN :startDate AND :endDate")
    List<Application> findByTraineeIdAndDateRange(@Param("traineeId") Long traineeId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    long countByTraineeIdAndStatus(Long traineeId, Application.Status status);
    long countByTraineeshipCompanyIdAndStatus(Long companyId, Application.Status status);
    long countByStatus(Application.Status status);
    
    Optional<Application> findByTraineeIdAndTraineeshipId(Long traineeId, Long traineeshipId);

    List<Application> findTop5ByOrderByApplicationDateDesc();
} 