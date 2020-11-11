package edu.iu.uits.lms.courseunlocker.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by chmaurer on 1/17/17.
 */
@Data
@AllArgsConstructor
public class CourseUnlockResponseObject {
    private String buttonDisplayText;
    private boolean buttonRendered;
    private boolean courseLocked;
    private String courseId;
}
