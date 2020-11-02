package edu.iu.uits.lms.courseunlocker.services;

import edu.iu.uits.lms.courseunlocker.config.ToolConfig;
import edu.iu.uits.lms.courseunlocker.model.CourseUnlockStatus;
import edu.iu.uits.lms.courseunlocker.rest.CourseUnlockerRestController;
import edu.iu.uits.lms.courseunlocker.service.CourseUnlockerService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(CourseUnlockerRestController.class)
@Import(ToolConfig.class)
@Slf4j
@ActiveProfiles("none")
public class RestLaunchSecurityTest {

   public static String COURSE_ID_TST = "1234";

   @Autowired
   private MockMvc mvc;

   @MockBean
   private CourseUnlockerService courseUnlockerService;

   @Before
   public void setup() {
      CourseUnlockStatus status = new CourseUnlockStatus(true, true, "1234");
      when(courseUnlockerService.getCourseUnlockStatus(COURSE_ID_TST)).thenReturn(status);
   }

   @Test
   public void restNoAuthnLaunch() throws Exception {
      //This is not a secured endpoint so should be successful
      SecurityContextHolder.getContext().setAuthentication(null);
      mvc.perform(get("/rest/course/unlockstatus/1234")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.buttonDisplayText").value("Unlock Course"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.buttonRendered").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.courseLocked").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.courseId").value(COURSE_ID_TST));
   }

}
