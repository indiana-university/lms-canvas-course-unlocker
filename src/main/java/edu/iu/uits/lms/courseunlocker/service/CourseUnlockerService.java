package edu.iu.uits.lms.courseunlocker.service;

import canvas.client.generated.api.CoursesApi;
import canvas.client.generated.model.CanvasTerm;
import canvas.client.generated.model.Course;
import canvas.helpers.CourseHelper;
import canvas.helpers.TermHelper;
import edu.iu.uits.lms.courseunlocker.model.CourseUnlockStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Date;

@Service
@Slf4j
public class CourseUnlockerService  {

   @Autowired
   private CoursesApi coursesApi = null;

   public CourseUnlockStatus getCourseUnlockStatus(String courseId) {
      Course course = coursesApi.getCourse(courseId);
      CanvasTerm term = course.getTerm();
      Date now = new Date();

      boolean rendered = TermHelper.getEndDate(term) != null && now.after(TermHelper.getEndDate(term));
      boolean locked = CourseHelper.isLocked(course,true);

      return new CourseUnlockStatus(rendered, locked, courseId);
   }

   public void toggleCourseLock(String courseId) {
      //Check the current status
      CourseUnlockStatus currentStatus = getCourseUnlockStatus(courseId);

      boolean restrictEnrollmentsToCourseDates = false;

      Course course = coursesApi.getCourse(courseId);
      OffsetDateTime courseStartDate = CourseHelper.getStartOffsetDateTime(course);

      OffsetDateTime newEndDate = null;
      OffsetDateTime newStartDate = courseStartDate;
      OffsetDateTime now = OffsetDateTime.now();

      // Figure out what the new date should be (if anything)
      // We're unlocking...
      if (currentStatus.isCourseLocked()) {

         if (courseStartDate != null && now.isBefore(courseStartDate) || (courseStartDate == null)) {
            newStartDate = now;
         }

         //Add 30 days
         newEndDate = now.plusDays(30);
         restrictEnrollmentsToCourseDates = true;
         // course is unlocked
      }
      // else (if locking) the behavior is to remove the course's end date and set restrictEnrollmentToCourseDates to false.
      // That's happening right now if nothing is done due to initialization default variable values.
      // This is documented here in case in the future more functionality is needed more than the default for locking.

      //Update the date
      coursesApi.updateCourseEndDate(courseId, null, newStartDate, newEndDate, restrictEnrollmentsToCourseDates);
   }

}
