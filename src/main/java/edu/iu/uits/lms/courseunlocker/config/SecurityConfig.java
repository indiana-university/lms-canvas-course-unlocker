package edu.iu.uits.lms.courseunlocker.config;

import edu.iu.uits.lms.common.oauth.CustomJwtAuthenticationConverter;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.security.LtiAuthenticationProvider;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
public class SecurityConfig {

    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 4)
    public static class CourseUnlockerWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authenticationProvider(new LtiAuthenticationProvider());
            http
                  .requestMatchers().antMatchers("/lti", "/app/**")
                  .and()
                  .authorizeRequests()
                  .antMatchers("/lti").permitAll() // use this syntax for the lock status possibly change courseid to end
                  .antMatchers("/app/**").hasRole(LTIConstants.INSTRUCTOR_ROLE);

            //Need to disable csrf so that we can use POST via REST
            http.csrf().disable();

            //Need to disable the frame options so we can embed this in another tool
            http.headers().frameOptions().disable();

            http.exceptionHandling().accessDeniedPage("/accessDenied");
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            // ignore everything except paths specified
            web.ignoring().antMatchers("/app/jsrivet/**", "/app/webjars/**", "/actuator/**", "/app/css/**", "/app/js/**");
        }

    }

// Open up the rest endpoint for checking lock status
    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 3)
    public static class CourseUnlockerRestSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.requestMatchers().antMatchers("/rest/**")
                  .and()
                  .authorizeRequests()
                  .antMatchers("/course/unlockstatus/**").permitAll()
                  //.access("hasAuthority('SCOPE_lms:rest') and hasAuthority('ROLE_LMS_REST_ADMINS')")
                  .and()
                  .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                  .and()
                  .oauth2ResourceServer()
                  .jwt().jwtAuthenticationConverter(new CustomJwtAuthenticationConverter());
        }
    }

    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 2)
    public static class CourseUnlockerCatchAllSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.requestMatchers().antMatchers("/**")
                  .and()
                  .authorizeRequests()
                  .anyRequest().authenticated();
        }
    }
}
