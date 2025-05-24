package _5336_4701_5281.swdeproj.repository;

import _5336_4701_5281.swdeproj.model.Notification;
import _5336_4701_5281.swdeproj.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByTimestampDesc(User user);
    List<Notification> findByUserAndIsReadOrderByTimestampDesc(User user, boolean isRead);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.isRead = false")
    long countUnreadNotifications(@Param("user") User user);
} 