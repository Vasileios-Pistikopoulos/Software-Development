package _5336_4701_5281.swdeproj.controller;

import _5336_4701_5281.swdeproj.model.Notification;
import _5336_4701_5281.swdeproj.model.User;
import _5336_4701_5281.swdeproj.service.NotificationService;
import _5336_4701_5281.swdeproj.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @Autowired
    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getUserNotifications(Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        return ResponseEntity.ok(notificationService.getUserNotifications(user));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        return ResponseEntity.ok(notificationService.getUnreadNotifications(user));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadNotificationCount(Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        long count = notificationService.getUnreadNotificationCount(user);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }
} 