package _5336_4701_5281.swdeproj.service;

import _5336_4701_5281.swdeproj.model.*;
import _5336_4701_5281.swdeproj.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void notifyApplicationStatus(Application application) {
        String status = application.getStatus().toString();
        String message = String.format(
            "Your application for %s at %s has been %s",
            application.getTraineeship().getTitle(),
            application.getTraineeship().getCompany().getName(),
            status.toLowerCase()
        );

        if (application.getStatus() == Application.Status.APPROVED) {
            message += String.format(" with %s as your supervisor",
                application.getSupervisor().getFullName());
        }

        Notification notification = new Notification(
            application.getTrainee().getUser(),
            message,
            Notification.NotificationType.APPLICATION_STATUS
        );
        notificationRepository.save(notification);
    }

    public void notifyLogbookStatus(LogbookEntry entry) {
        String status = entry.getStatus().toString();
        String message = String.format(
            "Your logbook entry for %s has been %s",
            entry.getDate().toString(),
            status.toLowerCase()
        );

        if (entry.getStatus() == LogbookEntry.Status.REJECTED && entry.getComments() != null) {
            message += String.format(" with comment: %s", entry.getComments());
        }

        Notification notification = new Notification(
            entry.getTrainee(),
            message,
            Notification.NotificationType.LOGBOOK_STATUS
        );
        notificationRepository.save(notification);
    }

    public void notifyEvaluationSubmitted(Evaluation evaluation) {
        String evaluatorType = evaluation.getEvaluatorType().toString();
        String message = String.format(
            "A new %s evaluation has been submitted for your traineeship",
            evaluatorType.toLowerCase()
        );

        Notification notification = new Notification(
            evaluation.getTraineeship().getAssignedTrainee().getUser(),
            message,
            Notification.NotificationType.EVALUATION_SUBMITTED
        );
        notificationRepository.save(notification);
    }

    @Transactional
    public Notification createNotification(User user, String message, Notification.NotificationType type) {
        Notification notification = new Notification(user, message, type);
        return notificationRepository.save(notification);
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