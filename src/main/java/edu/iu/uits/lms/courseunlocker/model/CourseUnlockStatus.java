package edu.iu.uits.lms.courseunlocker.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseUnlockStatus {
    private boolean buttonRendered;
    private boolean courseLocked;
    private String courseId;
}
