package edu.iu.uits.lms.courseunlocker.services;

import edu.iu.uits.lms.courseunlocker.config.ToolConfig;
import edu.iu.uits.lms.courseunlocker.model.CourseUnlockStatus;
import edu.iu.uits.lms.courseunlocker.service.CourseUnlockerService;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.security.LtiAuthenticationProvider;
import edu.iu.uits.lms.lti.security.LtiAuthenticationToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(properties = {"oauth.tokenprovider.url=http://foo"})
@Import(ToolConfig.class)
@ActiveProfiles("none")
public class AppLaunchSecurityTest {

   public static String COURSE_ID_TST = "1234";

   @Autowired
   private MockMvc mvc;

   @MockBean
   private CourseUnlockerService courseUnlockerService;

   @BeforeEach
   public void setup() {
      CourseUnlockStatus status = new CourseUnlockStatus(true, true, "1234");
      when(courseUnlockerService.getCourseUnlockStatus(COURSE_ID_TST)).thenReturn(status);
   }

   @Test
   public void appNoAuthnLaunch() throws Exception {
      //This is a secured endpoint and should not not allow access without authn
      mvc.perform(get("/app/index/"  + COURSE_ID_TST)
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
   }

   @Test
   public void appAuthnWrongContextLaunch() throws Exception {
      LtiAuthenticationToken token = new LtiAuthenticationToken("userId",
            "asdf", "systemId",
            AuthorityUtils.createAuthorityList(LTIConstants.INSTRUCTOR_AUTHORITY, "authority"),
            "unit_test");

      SecurityContextHolder.getContext().setAuthentication(token);

      NestedServletException t = Assertions.assertThrows(NestedServletException.class, () ->
              mvc.perform(get("/app/index/" + COURSE_ID_TST)
                              .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                              .contentType(MediaType.APPLICATION_JSON))
                      .andExpect(status().isOk())
              );
   }

   @Test
   public void appAuthnLaunch() throws Exception {
      LtiAuthenticationToken token = new LtiAuthenticationToken("userId",
              COURSE_ID_TST, "systemId",
              AuthorityUtils.createAuthorityList(LTIConstants.INSTRUCTOR_AUTHORITY, "authority"),
              "unit_test");

      SecurityContextHolder.getContext().setAuthentication(token);

      //This is a secured endpoint and should not not allow access without authn
      mvc.perform(get("/app/index/" + COURSE_ID_TST)
              .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
              .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk());
   }

   @Test
   public void appAuthnLaunchNonInstructor() throws Exception {
      LtiAuthenticationToken token = new LtiAuthenticationToken("userId",
              COURSE_ID_TST, "systemId",
              AuthorityUtils.createAuthorityList(LTIConstants.TA_AUTHORITY, "authority"),
              "unit_test");

      SecurityContextHolder.getContext().setAuthentication(token);

      //This is a secured endpoint and should not not allow access without instructor authn
      mvc.perform(get("/app/index/" + COURSE_ID_TST)
              .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
              .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isForbidden());
   }

   @Test
   public void randomUrlNoAuth() throws Exception {
      //This is a secured endpoint and should not not allow access without authn
      mvc.perform(get("/asdf/foobar")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
   }

   @Test
   public void randomUrlWithAuth() throws Exception {
      LtiAuthenticationToken token = new LtiAuthenticationToken("userId",
            COURSE_ID_TST, "systemId",
            AuthorityUtils.createAuthorityList(LtiAuthenticationProvider.LTI_USER_ROLE, "authority"),
            "unit_test");
      SecurityContextHolder.getContext().setAuthentication(token);

      //This is a secured endpoint and should not not allow access without authn
      mvc.perform(get("/asdf/foobar")
            .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
   }

}
