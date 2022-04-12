package edu.iu.uits.lms.courseunlocker.controller;

import edu.iu.uits.lms.courseunlocker.config.ToolConfig;
import edu.iu.uits.lms.courseunlocker.model.CourseUnlockStatus;
import edu.iu.uits.lms.courseunlocker.service.CourseUnlockerService;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.controller.OidcTokenAwareController;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/app")
@Slf4j
public class CourseUnlockerController extends OidcTokenAwareController {

    @Autowired
    private ToolConfig toolConfig = null;

    @Autowired
    CourseUnlockerService courseUnlockerService = null;

    @RequestMapping("/launch")
    public ModelAndView launch(Model model, HttpServletRequest request) {
        OidcAuthenticationToken token = getTokenWithoutContext();
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);
        String courseId = oidcTokenUtils.getCourseId();

        return index(courseId, model, request);
    }

    @RequestMapping("/index/{courseId}")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public ModelAndView index(@PathVariable("courseId") String courseId, Model model, HttpServletRequest request) {
        log.debug("in /index");
        OidcAuthenticationToken token = getValidatedToken(courseId);
        model.addAttribute("courseId", courseId);

        CourseUnlockStatus status = courseUnlockerService.getCourseUnlockStatus(courseId);
        if (status.isButtonRendered()) {
            try {
                courseUnlockerService.toggleCourseLock(courseId);
            } catch (Exception e) {
                log.error("Exception trying to unlock course with id " + courseId, e);
                throw new CourseUnlockerException();
            }
        } else {
            log.error("Attempt to change lock status of course " + courseId + " but course not eligible. Course not updated.");
            throw new CourseUnlockerException();
        }

        return new ModelAndView("index");
    }

    @ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="Error saving course dates in Canvas to lock/unlock course")
    public class CourseUnlockerException extends RuntimeException {
    }

    @RequestMapping(value = "/accessDenied")
    public String accessDenied() {
        return "accessDenied";
    }
}
