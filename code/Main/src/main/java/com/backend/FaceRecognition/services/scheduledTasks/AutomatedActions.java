package com.backend.FaceRecognition.services.scheduledTasks;

import com.backend.FaceRecognition.entities.Notification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
@Slf4j
public class AutomatedActions {

    @Autowired
    private EntityManager entityManager;
    @Scheduled(cron = "0 0 0 * * *") // Runs every day at midnight
    public void cleanupExpiredNotifications() {
        log.info("Cleaning up Expired Notifications................");
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<Notification> deleteQuery = cb.createCriteriaDelete(Notification.class);
        Root<Notification> notificationRoot = deleteQuery.from(Notification.class);
        deleteQuery.where(cb.lessThan(notificationRoot.get("validUntil"), LocalDate.now()));
        entityManager.createQuery(deleteQuery).executeUpdate();
        log.info("Done");
    }


}
