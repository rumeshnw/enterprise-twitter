package com.shivang.twitter.controller

import com.netflix.hystrix.exception.HystrixRuntimeException
import com.shivang.twitter.controller.advice.GlobalExceptionControllerAdvice
import com.shivang.twitter.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import rx.Observable
import spock.lang.Specification

import static org.hamcrest.Matchers.instanceOf
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class UserControllerSpec extends Specification {

    UserService userService
    UserController userController
    MockMvc mockMvc

    def setup() {
        userService = Mock(UserService)
        userController = new UserController(userService)
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionControllerAdvice())
                .build()
    }

    def "user login with invalid params"() {
        int page
        int pageSize
        String username

        given: "a request to user login without required password parameter"
            page = 0
            pageSize = 10
            username = "test"

        when: "such a request is made"
            ResultActions result = mockMvc.perform(post("/users/login")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("username", username)
                    .param("page", page.toString())
                    .param("pageSize", pageSize.toString()))

        then: "Verify that expected error response is received"
            result.andExpect(status().isBadRequest())
    }

    def "do user login when circuit breaker is open"() {
        int page
        int pageSize
        String username
        String password

        given: "a request to login"
            page = 0
            pageSize = 10
            username = "username"
            password = "password"

        when: "the hystrix circuit breaker is open (system temporarily unavailable)"
            1 * userService.login(username, password, page, pageSize) >>
                    Observable.error(new HystrixRuntimeException(
                            HystrixRuntimeException.FailureType.SHORTCIRCUIT,
                            null, "message", null, null))
            MvcResult result = mockMvc.perform(post("/users/login")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("username", username)
                    .param("password", password)
                    .param("page", page.toString())
                    .param("pageSize", pageSize.toString()))
                    .andExpect(request().asyncStarted())
                    .andExpect(request().asyncResult(instanceOf(HystrixRuntimeException.class)))
                    .andReturn();

        then: "Verify that login is NOT successful and instead a service unavailable error is thrown"
            this.mockMvc.perform(asyncDispatch(result))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("status").value(HttpStatus.SERVICE_UNAVAILABLE.value()))
    }
}
