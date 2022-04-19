package edu.iu.uits.lms.courseunlocker.service;

/*-
 * #%L
 * course-unlocker
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import edu.iu.uits.lms.canvas.helpers.CourseHelper;
import edu.iu.uits.lms.canvas.helpers.TermHelper;
import edu.iu.uits.lms.canvas.model.CanvasTerm;
import edu.iu.uits.lms.canvas.model.Course;
import edu.iu.uits.lms.canvas.services.CourseService;
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
   private CourseService courseService = null;

   public CourseUnlockStatus getCourseUnlockStatus(String courseId) {
      Course course = courseService.getCourse(courseId);
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

      Course course = courseService.getCourse(courseId);

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
      courseService.updateCourseEndDate(courseId, null, newStartDate, newEndDate, restrictEnrollmentsToCourseDates);
   }

}
