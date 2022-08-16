package edu.iu.uits.lms.courseunlocker.services.lti;

import edu.iu.uits.lms.canvas.config.CanvasClientTestConfig;
import edu.iu.uits.lms.courseunlocker.config.ToolConfig;
import edu.iu.uits.lms.lti.AbstractLTIRestLaunchSecurityTest;
import edu.iu.uits.lms.lti.config.LtiClientTestConfig;
import org.springframework.context.annotation.Import;

@Import({ToolConfig.class, CanvasClientTestConfig.class, LtiClientTestConfig.class})
public class LTIRestLaunchSecurityTest extends AbstractLTIRestLaunchSecurityTest {

}
