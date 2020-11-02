package edu.iu.uits.lms.courseunlocker.controller;

import edu.iu.uits.lms.courseunlocker.config.ToolConfig;
import edu.iu.uits.lms.courseunlocker.model.CourseUnlockStatus;
import edu.iu.uits.lms.courseunlocker.service.CourseUnlockerService;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.controller.LtiAuthenticationTokenAwareController;
import edu.iu.uits.lms.lti.security.LtiAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/app")
@Slf4j
public class CourseUnlockerController extends LtiAuthenticationTokenAwareController {

    @Autowired
    private ToolConfig toolConfig = null;

    @Autowired
    CourseUnlockerService courseUnlockerService = null;

    // move toggle here
    @RequestMapping("/index/{courseId}")
    @Secured(LTIConstants.INSTRUCTOR_AUTHORITY)
    public ModelAndView index(@PathVariable("courseId") String courseId, Model model, HttpServletRequest request) {
        log.debug("in /index");
        LtiAuthenticationToken token = getValidatedToken(courseId);
        String displayText = courseUnlockerService.getCourseUnlockStatus(courseId).isCourseLocked() ? "Unlock course" : "Lock course";
        model.addAttribute("displayText", displayText);
        model.addAttribute("courseId", courseId);

        CourseUnlockStatus status = courseUnlockerService.getCourseUnlockStatus(courseId);
        if (status.isButtonRendered()) {
            try {
                courseUnlockerService.toggleCourseLock(courseId);
            } catch (Exception e) {
                log.error("Exception trying to unlock course with id " + courseId);
                // TODO what do I do if there is an exception?
            }
        }

        return new ModelAndView("index");
    }

    @RequestMapping(value = "/accessDenied")
    public String accessDenied() {
        return "accessDenied";
    }
}
