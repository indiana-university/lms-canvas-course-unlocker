package edu.iu.uits.lms.courseunlocker.rest;

import edu.iu.uits.lms.courseunlocker.controller.CourseUnlockerController;
import edu.iu.uits.lms.courseunlocker.model.CourseUnlockResponseObject;
import edu.iu.uits.lms.courseunlocker.model.CourseUnlockStatus;
import edu.iu.uits.lms.courseunlocker.service.CourseUnlockerService;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.security.LtiAuthenticationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

   // we don't necessarily need to call this directly.  The lti launch will do the toggle. This
   // could be index.html
//   @RequestMapping(value = "/{courseId}/unlockstatus/toggle", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
//   @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
//   public ResponseEntity courseUnlockToggle(@PathVariable String courseId) {
//      try {
//         courseUnlockerService.toggleCourseLock(courseId);
//      } catch (Exception e) {
//         return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
//      }
//
//      //Post a message back so that the caller knows that we are done
//      StringBuilder body = new StringBuilder("<html><head>");
//      body.append("<title>Courseunlock</title>");
//      body.append("<script type='text/javascript'>");
//      body.append("parent.postMessage({subject: 'iu.unlockstatus', courseid: [[" + courseId + "]]}, '*');");
//      body.append("</script>");
//      body.append("</head>");
//      body.append("<body>");
//      body.append("<div>" + courseId + "</div>");
//      body.append("</body></html>");
//
//      return ResponseEntity.ok(body.toString());
//   }

}
