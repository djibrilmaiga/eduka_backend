package com.groupe2_ionic.eduka.repository;

import com.groupe2_ionic.eduka.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
}
