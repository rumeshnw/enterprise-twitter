package com.shivang.twitter.unittests.controller

import com.netflix.hystrix.exception.HystrixRuntimeException
import com.shivang.twitter.config.WebSecurityConfig
import com.shivang.twitter.controller.UserController
import com.shivang.twitter.controller.advice.GlobalExceptionControllerAdvice
import com.shivang.twitter.model.CustomException
import com.shivang.twitter.model.SecurityUser
import com.shivang.twitter.model.TwitterUser
import com.shivang.twitter.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.security.web.FilterChainProxy
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import rx.Observable
import spock.lang.Specification

import static org.hamcrest.Matchers.instanceOf
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ContextConfiguration(classes = [WebSecurityTestAuthConfig, WebSecurityConfig])
class UserControllerSpec extends Specification {

    UserService userService
    UserController userController
    MockMvc mockMvc
    @Autowired
    FilterChainProxy springSecurityFilterChain;

    def setup() {
        userService = Mock(UserService)
        userController = new UserController(userService)
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionControllerAdvice())
                .apply(SecurityMockMvcConfigurers.springSecurity(springSecurityFilterChain))
                .build()
    }

    def "do user profile request when circuit breaker is open"() {

        given: "a request to get user profile"

        when: "the hystrix circuit breaker is open (system temporarily unavailable)"
            1 * userService.getUserByUsername(_) >>
                    Observable.error(new HystrixRuntimeException(
                            HystrixRuntimeException.FailureType.SHORTCIRCUIT,
                            null, "message", null, null))
            MvcResult result = mockMvc
                    .perform(get("/users/me")
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic("username", "password")))
                    .andExpect(request().asyncStarted())
                    .andExpect(request().asyncResult(instanceOf(HystrixRuntimeException.class)))
                    .andReturn();

        then: "Verify that user profile is not retrieved and instead a service unavailable error is thrown"
            this.mockMvc.perform(asyncDispatch(result))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("status").value(HttpStatus.SERVICE_UNAVAILABLE.value()))
    }

    def "do user profile request when service (wrapped with hystrix) throws an error"() {
        given: "a request to get user profile"

        when: "the hystrix backed service call throws an error"
            1 * userService.getUserByUsername(_) >>
                    Observable.error(new HystrixRuntimeException(
                            HystrixRuntimeException.FailureType.SHORTCIRCUIT,
                            null, "message", new CustomException(HttpStatus.BAD_REQUEST, "developerMessage"), null))
            MvcResult result = mockMvc
                    .perform(get("/users/me")
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic("username", "password")))
                    .andExpect(request().asyncStarted())
                    .andExpect(request().asyncResult(instanceOf(HystrixRuntimeException.class)))
                    .andReturn();

        then: "Verify that user profile is not retrieved and instead a bad request error is thrown"
            this.mockMvc.perform(asyncDispatch(result))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("status").value(HttpStatus.BAD_REQUEST.value()))

    }

    @Configuration
    @EnableAutoConfiguration
    static class WebSecurityTestAuthConfig extends GlobalAuthenticationConfigurerAdapter {

        @Override
        public void init(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(userDetailsService())
        }

        @Bean
        @Primary
        static UserDetailsService userDetailsService() {
            return new UserDetailsService() {
                @Override
                UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                    return new SecurityUser(new TwitterUser("fn", "ln", "username", "password"
                            , new ArrayList<String>(), new ArrayList<String>()))
                }
            }
        }
    }
}