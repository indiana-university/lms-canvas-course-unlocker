package edu.iu.uits.lms.courseunlocker.services.swagger;

import edu.iu.uits.lms.courseunlocker.WebApplication;
import edu.iu.uits.lms.courseunlocker.config.SecurityConfig;
import edu.iu.uits.lms.lti.swagger.AbstractSwaggerUiCustomTest;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {WebApplication.class, SecurityConfig.class})
public class SwaggerUiCustomTest extends AbstractSwaggerUiCustomTest {

}
