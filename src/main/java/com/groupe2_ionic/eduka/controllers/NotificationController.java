package com.groupe2_ionic.eduka.controllers;

import com.groupe2_ionic.eduka.dto.NotificationResponseDto;
import com.groupe2_ionic.eduka.repository.UtilisateurRepository;
import com.groupe2_ionic.eduka.services.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Gestion des notifications (Email + SMS + Base de données)")
public class NotificationController {

    private final NotificationService notificationService;
    private final UtilisateurRepository utilisateurRepository;


    @GetMapping("/user/{userId}")
    @Operation(summary = "Récupérer les notifications d'un utilisateur",
            description = "CACHE RECOMMANDÉ - Données stables, actualiser toutes les 5 minutes")
    public ResponseEntity<List<NotificationResponseDto>> getNotificationsByUser(
            @Parameter(description = "ID de l'utilisateur") @PathVariable int userId) {
        try {
            List<NotificationResponseDto> notifications = notificationService.getNotificationsByUtilisateur(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/user/{userId}/paginated")
    @Operation(summary = "Récupérer les notifications avec pagination",
            description = "CACHE RECOMMANDÉ - Pagination pour listes importantes")
    public ResponseEntity<Page<NotificationResponseDto>> getNotificationsPaginated(
            @Parameter(description = "ID de l'utilisateur") @PathVariable int userId,
            @Parameter(description = "Numéro de page (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de page") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<NotificationResponseDto> notifications = notificationService
                    .getNotificationsByUtilisateurWithPagination(userId, page, size);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/user/{userId}/unread")
    @Operation(summary = "Récupérer les notifications non lues",
            description = "TEMPS RÉEL - Données sensibles, actualiser à chaque navigation")
    public ResponseEntity<List<NotificationResponseDto>> getUnreadNotifications(
            @Parameter(description = "ID de l'utilisateur") @PathVariable int userId) {
        try {
            List<NotificationResponseDto> notifications = notificationService.getUnreadNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/user/{userId}/unread/count")
    @Operation(summary = "Compter les notifications non lues",
            description = "TEMPS RÉEL - Badge de notification, actualiser fréquemment")
    public ResponseEntity<Long> countUnreadNotifications(
            @Parameter(description = "ID de l'utilisateur") @PathVariable int userId) {
        try {
            long count = notificationService.countUnreadNotifications(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Marquer une notification comme lue")
    public ResponseEntity<NotificationResponseDto> markAsRead(
            @Parameter(description = "ID de la notification") @PathVariable int notificationId) {
        try {
            NotificationResponseDto response = notificationService.markAsRead(notificationId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/user/{userId}/read-all")
    @Operation(summary = "Marquer toutes les notifications comme lues")
    public ResponseEntity<Void> markAllAsRead(
            @Parameter(description = "ID de l'utilisateur") @PathVariable int userId) {
        try {
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Supprimer une notification")
    public ResponseEntity<Void> deleteNotification(
            @Parameter(description = "ID de la notification") @PathVariable int notificationId) {
        try {
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/user/{userId}/all")
    @Operation(summary = "Supprimer toutes les notifications d'un utilisateur")
    public ResponseEntity<Void> deleteAllNotifications(
            @Parameter(description = "ID de l'utilisateur") @PathVariable int userId) {
        try {
            notificationService.deleteAllNotifications(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
