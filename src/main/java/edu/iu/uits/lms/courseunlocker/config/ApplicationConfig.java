package edu.iu.uits.lms.courseunlocker.config;

import edu.iu.uits.lms.common.cors.LmsCorsInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebMvc
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@Slf4j
public class ApplicationConfig implements WebMvcConfigurer {

   public ApplicationConfig() {
      log.debug("ApplicationConfig()");
   }

   @Override
   public void addInterceptors(InterceptorRegistry registry) {
      List<HttpMethod> allowedMethodList = new ArrayList<>();
      allowedMethodList.add(HttpMethod.GET);

      try {
         registry.addInterceptor(new LmsCorsInterceptor("/rest/unlockstatus",
                 "*",
                 allowedMethodList,
                 null));
      } catch (Exception e) {
         log.error(e.toString());
      }
   }
}
