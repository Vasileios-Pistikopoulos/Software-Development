package _5336_4701_5281.swdeproj.service;

import _5336_4701_5281.swdeproj.model.*;
import _5336_4701_5281.swdeproj.repository.NotificationRepository;
import _5336_4701_5281.swdeproj.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public void notifyApplicationStatusChange(Application application) {
        User trainee = application.getTrainee().getUser();
        String message = String.format("Your application has been %s",
            application.getStatus().toString().toLowerCase());
        
        if (application.getStatus() == Application.Status.APPROVED && application.getAssignedTraineeship() != null) {
            message += String.format(" for the position: %s at %s",
                application.getAssignedTraineeship().getTitle(),
                application.getAssignedTraineeship().getCompany().getName());
        }

        Notification notification = new Notification(
            trainee,
            message,
            Notification.NotificationType.APPLICATION_STATUS
        );
        notificationRepository.save(notification);
    }

    public void notifyNewApplication(Application application) {
        // Get all committee members
        List<User> committeeMembers = userRepository.findByRolesContaining(User.Role.ROLE_COMMITTEE);
        
        // Create notification for each committee member
        String message = String.format("New application received from %s",
            application.getTrainee().getFullName());
        
        committeeMembers.forEach(committeeMember -> {
            Notification notification = new Notification(
                committeeMember,
                message,
                Notification.NotificationType.APPLICATION_STATUS
            );
            notificationRepository.save(notification);
        });
    }

    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByTimestampDesc(user);
    }

    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadOrderByTimestampDesc(user, false);
    }

    public long getUnreadNotificationCount(User user) {
        return notificationRepository.countUnreadNotifications(user);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = getUnreadNotifications(user);
        unreadNotifications.forEach(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }
} 