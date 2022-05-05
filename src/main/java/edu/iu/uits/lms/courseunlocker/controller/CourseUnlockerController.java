package edu.iu.uits.lms.courseunlocker.controller;

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
    public String launch(Model model, HttpServletRequest request) {
        OidcAuthenticationToken token = getTokenWithoutContext();
        OidcTokenUtils oidcTokenUtils = new OidcTokenUtils(token);
        String courseId = oidcTokenUtils.getCourseId();

        return index(courseId, model, request);
    }

    @RequestMapping("/index/{courseId}")
    @Secured({LTIConstants.INSTRUCTOR_AUTHORITY})
    public String index(@PathVariable("courseId") String courseId, Model model, HttpServletRequest request) {
        log.debug("in /index");
        OidcAuthenticationToken token = getValidatedToken(courseId);

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

        return "redirect:" + courseUnlockerService.getCourseSettingsToolUrl(courseId);
    }

    @ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="Error saving course dates in Canvas to lock/unlock course")
    public class CourseUnlockerException extends RuntimeException {
    }

}
