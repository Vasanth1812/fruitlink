package com.fruitlink.notifications.service;

import com.fruitlink.auth.entity.User;
import com.fruitlink.auth.repository.UserRepository;
import com.fruitlink.common.BusinessException;
import com.fruitlink.notifications.dto.NotificationDto.*;
import com.fruitlink.notifications.entity.Notification;
import com.fruitlink.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // Called internally by other services to send notifications
    @Transactional
    public void send(String userId, String type, String title, String body, String referenceId) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new BusinessException("User not found"));
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        if (referenceId != null) n.setReferenceId(UUID.fromString(referenceId));
        notificationRepository.save(n);
    }

    // Admin-send via API
    @Transactional
    public NotificationResponse sendNotification(SendNotificationRequest req) {
        send(req.getUserId(), req.getType(), req.getTitle(), req.getBody(), req.getReferenceId());
        return new NotificationResponse(); // simplified return
    }

    public List<NotificationResponse> getMyNotifications(String phone, boolean unreadOnly) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("User not found"));
        List<Notification> list = unreadOnly
                ? notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId())
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return list.stream().map(this::toResponse).toList();
    }

    public UnreadCountResponse getUnreadCount(String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("User not found"));
        UnreadCountResponse r = new UnreadCountResponse();
        r.setUnreadCount(notificationRepository.countByUserIdAndIsReadFalse(user.getId()));
        return r;
    }

    @Transactional
    public void markRead(String id) {
        notificationRepository.findById(UUID.fromString(id))
                .ifPresent(n -> { n.setRead(true); notificationRepository.save(n); });
    }

    @Transactional
    public void markAllRead(String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("User not found"));
        notificationRepository.markAllReadByUserId(user.getId());
    }

    private NotificationResponse toResponse(Notification n) {
        NotificationResponse r = new NotificationResponse();
        r.setId(n.getId().toString());
        r.setType(n.getType());
        r.setTitle(n.getTitle());
        r.setBody(n.getBody());
        r.setRead(n.isRead());
        if (n.getReferenceId() != null) r.setReferenceId(n.getReferenceId().toString());
        r.setCreatedAt(n.getCreatedAt());
        return r;
    }
}
