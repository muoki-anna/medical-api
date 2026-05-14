package com.medicore.api.util;

import com.medicore.api.model.Activity;
import com.medicore.api.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ActivityLogger {

    @Autowired
    private ActivityRepository activityRepository;

    public void log(String icon, String description, String patientName) {
        // Prevent exact duplicates within a short timeframe (e.g. page refresh)
        Activity lastActivity = activityRepository.findTopByOrderByIdDesc();
        if (lastActivity != null && 
            lastActivity.getDescription().equals(description) && 
            lastActivity.getPatientName().equals(patientName)) {
            return; // Skip duplicate
        }

        Activity activity = new Activity();
        activity.setIcon(icon);
        activity.setDescription(description);
        activity.setPatientName(patientName);
        activity.setActionDate(LocalDate.now());
        activityRepository.save(activity);
    }
}
