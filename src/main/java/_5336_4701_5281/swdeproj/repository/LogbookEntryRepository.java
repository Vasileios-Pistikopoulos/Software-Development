package _5336_4701_5281.swdeproj.repository;

import _5336_4701_5281.swdeproj.model.LogbookEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LogbookEntryRepository extends JpaRepository<LogbookEntry, Long> {
    List<LogbookEntry> findByTraineeId(Long traineeId);
    
    List<LogbookEntry> findByTraineeshipId(Long traineeshipId);
    
    List<LogbookEntry> findByTraineeIdAndDateBetween(
        Long traineeId,
        LocalDate startDate,
        LocalDate endDate
    );
    
    List<LogbookEntry> findByTraineeIdAndDateBetweenAndStatus(Long traineeId, LocalDate startDate, LocalDate endDate, LogbookEntry.Status status);
    
    @Query("SELECT l FROM LogbookEntry l WHERE l.trainee.id = :traineeId " +
           "AND l.traineeship.id = :traineeshipId ORDER BY l.date DESC")
    List<LogbookEntry> findByTraineeAndTraineeship(
        @Param("traineeId") Long traineeId,
        @Param("traineeshipId") Long traineeshipId
    );
    
    @Query("SELECT SUM(e.hoursSpent) FROM LogbookEntry e WHERE e.trainee.id = :traineeId AND e.traineeship.id = :traineeshipId")
    Double sumHoursByTraineeAndTraineeship(@Param("traineeId") Long traineeId, @Param("traineeshipId") Long traineeshipId);
    
    @Query("SELECT SUM(e.hoursSpent) FROM LogbookEntry e WHERE e.trainee.id = :traineeId AND e.traineeship.id = :traineeshipId AND e.status = :status")
    Double sumHoursByTraineeAndTraineeshipAndStatus(@Param("traineeId") Long traineeId, @Param("traineeshipId") Long traineeshipId, @Param("status") LogbookEntry.Status status);
    
    @Query("SELECT COUNT(e) FROM LogbookEntry e WHERE e.trainee.id = :traineeId AND e.traineeship.id = :traineeshipId AND e.status = :status")
    Long countApprovedEntriesByTraineeAndTraineeship(@Param("traineeId") Long traineeId, @Param("traineeshipId") Long traineeshipId, @Param("status") LogbookEntry.Status status);

    long countByTraineeId(Long traineeId);
} 