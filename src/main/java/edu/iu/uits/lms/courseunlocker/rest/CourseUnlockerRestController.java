package edu.iu.uits.lms.courseunlocker.rest;

import edu.iu.uits.lms.courseunlocker.controller.CourseUnlockerController;
import edu.iu.uits.lms.courseunlocker.model.CourseUnlockResponseObject;
import edu.iu.uits.lms.courseunlocker.model.CourseUnlockStatus;
import edu.iu.uits.lms.courseunlocker.service.CourseUnlockerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/course")
@Slf4j
public class CourseUnlockerRestController extends CourseUnlockerController {

   @Autowired
   CourseUnlockerService courseUnlockerService = null;

   @GetMapping("/unlockstatus/{courseId}")
   public @ResponseBody CourseUnlockResponseObject courseUnlockStatus(@PathVariable String courseId) {
      CourseUnlockStatus courseUnlockStatus = courseUnlockerService.getCourseUnlockStatus(courseId);
      String displayText = courseUnlockStatus.isCourseLocked()? "Unlock Course" : "Lock Course";

      return new CourseUnlockResponseObject(displayText, courseUnlockStatus.isButtonRendered(), courseUnlockStatus.isCourseLocked(), courseId);
   }

}
