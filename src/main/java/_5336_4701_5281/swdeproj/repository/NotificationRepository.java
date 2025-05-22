package _5336_4701_5281.swdeproj.repository;

import _5336_4701_5281.swdeproj.model.Notification;
import _5336_4701_5281.swdeproj.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserAndReadFalseOrderByTimestampDesc(User user);
} 