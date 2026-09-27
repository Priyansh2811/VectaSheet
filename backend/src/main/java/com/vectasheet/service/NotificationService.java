package com.vectasheet.service;

import com.vectasheet.dto.NotificationDto;
import com.vectasheet.entity.Notification;
import com.vectasheet.entity.NotificationType;
import com.vectasheet.exception.ApiException;
import com.vectasheet.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void notify(UUID recipientId, UUID actorId, NotificationType type, String message, UUID workspaceId, UUID entityId) {
        if (recipientId.equals(actorId)) return; // don't notify people about their own actions
        Notification n = new Notification();
        n.setRecipientId(recipientId);
        n.setActorId(actorId);
        n.setType(type);
        n.setMessage(message);
        n.setWorkspaceId(workspaceId);
        n.setEntityId(entityId);
        repository.save(n);
    }

    public List<NotificationDto> list(UUID userId) {
        return repository.findByRecipientIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationDto::from)
                .collect(Collectors.toList());
    }

    public long unreadCount(UUID userId) {
        return repository.countByRecipientIdAndReadFalse(userId);
    }

    @Transactional
    public NotificationDto setRead(UUID userId, UUID notificationId, boolean read) {
        Notification n = repository.findById(notificationId)
                .orElseThrow(() -> ApiException.notFound("Notification not found"));
        if (!n.getRecipientId().equals(userId)) {
            throw ApiException.forbidden("This notification doesn't belong to you");
        }
        n.setRead(read);
        n = repository.save(n);
        return NotificationDto.from(n);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        for (Notification n : repository.findByRecipientIdOrderByCreatedAtDesc(userId)) {
            if (!n.isRead()) {
                n.setRead(true);
                repository.save(n);
            }
        }
    }

    @Transactional
    public void delete(UUID userId, UUID notificationId) {
        Notification n = repository.findById(notificationId)
                .orElseThrow(() -> ApiException.notFound("Notification not found"));
        if (!n.getRecipientId().equals(userId)) {
            throw ApiException.forbidden("This notification doesn't belong to you");
        }
        repository.delete(n);
    }
}
