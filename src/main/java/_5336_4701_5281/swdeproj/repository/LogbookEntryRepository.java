package _5336_4701_5281.swdeproj.repository;

import _5336_4701_5281.swdeproj.model.LogbookEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LogbookEntryRepository extends JpaRepository<LogbookEntry, Long> {
    List<LogbookEntry> findByTraineeId(Long traineeId);
    
    List<LogbookEntry> findByTraineeshipId(Long traineeshipId);
    
    List<LogbookEntry> findByTraineeIdAndDateBetween(
        Long traineeId,
        LocalDate startDate,
        LocalDate endDate
    );
    
    @Query("SELECT l FROM LogbookEntry l WHERE l.trainee.id = :traineeId " +
           "AND l.traineeship.id = :traineeshipId ORDER BY l.date DESC")
    List<LogbookEntry> findByTraineeAndTraineeship(
        @Param("traineeId") Long traineeId,
        @Param("traineeshipId") Long traineeshipId
    );
    
    @Query("SELECT SUM(l.hoursSpent) FROM LogbookEntry l " +
           "WHERE l.trainee.id = :traineeId AND l.traineeship.id = :traineeshipId")
    Double sumHoursByTraineeAndTraineeship(
        @Param("traineeId") Long traineeId,
        @Param("traineeshipId") Long traineeshipId
    );
    
    @Query("SELECT COUNT(l) FROM LogbookEntry l " +
           "WHERE l.trainee.id = :traineeId AND l.traineeship.id = :traineeshipId " +
           "AND l.status = 'APPROVED'")
    Long countApprovedEntriesByTraineeAndTraineeship(
        @Param("traineeId") Long traineeId,
        @Param("traineeshipId") Long traineeshipId
    );

    long countByTraineeId(Long traineeId);
} 