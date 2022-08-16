package edu.iu.uits.lms.courseunlocker.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("swagger")
@Configuration
//@OpenAPIDefinition(info = @Info(title = "Course Unlocker REST Endpoints", version = "${course-unlocker.version}"))
@SecurityScheme(name = "security_auth_courseunlocker", type = SecuritySchemeType.OAUTH2,
      flows = @OAuthFlows(authorizationCode = @OAuthFlow(
            authorizationUrl = "${springdoc.oAuthFlow.authorizationUrl}",
            scopes = {@OAuthScope(name = "lms:rest")},
            tokenUrl = "${springdoc.oAuthFlow.tokenUrl}")))
public class SwaggerConfig {

   @Bean
   public GroupedOpenApi groupedOpenApi() {
      return GroupedOpenApi.builder()
            .group("courseunlocker")
            .packagesToScan("edu.iu.uits.lms.courseunlocker")
            .build();
   }

}
