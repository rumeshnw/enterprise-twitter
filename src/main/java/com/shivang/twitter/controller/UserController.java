package com.shivang.twitter.controller;

import com.shivang.twitter.model.Tweet;
import com.shivang.twitter.model.User;
import com.shivang.twitter.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * @author Shivang Shah
 */
@RestController
@RequestMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = {"Twitter User Service"})
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        LOGGER.debug("User controller initialized ..");
        this.userService = userService;
    }

    @ApiOperation(value = "", notes = "Get all users")
    @ApiResponses(
            @ApiResponse(code = 200, message = "", response = User.class))
    @RequestMapping(method = RequestMethod.GET)
    public DeferredResult<ResponseEntity<Page<User>>> getAllUsers(
            @RequestParam("page") Integer page,
            @RequestParam("pageSize") Integer pageSize) {
        LOGGER.debug("get all users");
        // Using DeferredResult to provide non-blocking rest capabilities
        DeferredResult<ResponseEntity<Page<User>>> result = new DeferredResult<>();
        Observable<Page<User>> obsResult = userService.getUsers(page, pageSize);
        // Setting the result and errorResult in case of any Exceptions wrapper by RxJava
        obsResult.subscribeOn(Schedulers.io()).subscribe(searchResults -> {
            ResponseEntity<Page<User>> entity = new ResponseEntity<>(searchResults, HttpStatus.OK);
            result.setResult(entity);
        }, result::setErrorResult);
        return result;
    }

    @ApiOperation(value = "/login", notes = "Upon user login, show user's feed")
    @ApiResponses(
            @ApiResponse(code = 200, message = "", response = Tweet.class))
    @RequestMapping(method = RequestMethod.POST, value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public DeferredResult<ResponseEntity<Page<Tweet>>> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("page") Integer page,
            @RequestParam("pageSize") Integer pageSize) {
        LOGGER.debug("request params: username: {}, password: {}, page: {}, pageSize: {}",
                username, password, page, pageSize);
        // Using DeferredResult to provide non-blocking rest capabilities
        DeferredResult<ResponseEntity<Page<Tweet>>> result = new DeferredResult<>();
        Observable<Page<Tweet>> obsResult = userService.login(username, password, page, pageSize);
        // Setting the result and errorResult in case of any Exceptions wrapper by RxJava
        obsResult.subscribeOn(Schedulers.io()).subscribe(searchResults -> {
            ResponseEntity<Page<Tweet>> entity = new ResponseEntity<>(searchResults, HttpStatus.OK);
            result.setResult(entity);
        }, result::setErrorResult);
        return result;
    }

    @ApiOperation(value = "/{username}", notes = "Get user by username")
    @ApiResponses(
            @ApiResponse(code = 200, message = "", response = User.class))
    @RequestMapping(method = RequestMethod.GET, value = "/{username}")
    public DeferredResult<ResponseEntity<User>> getUser(
            @PathVariable("username") String username) {
        LOGGER.debug("request params: username: {}", username);
        // Using DeferredResult to provide non-blocking rest capabilities
        DeferredResult<ResponseEntity<User>> result = new DeferredResult<>();
        Observable<User> obsResult = userService.getUserByUsername(username);
        // Setting the result and errorResult in case of any Exceptions wrapper by RxJava
        obsResult.subscribeOn(Schedulers.io()).subscribe(searchResults -> {
            ResponseEntity<User> entity = new ResponseEntity<>(searchResults, HttpStatus.OK);
            result.setResult(entity);
        }, result::setErrorResult);
        return result;
    }

    @ApiOperation(value = "/{username}/tweets", notes = "Get user tweets")
    @ApiResponses(
            @ApiResponse(code = 200, message = "", response = Tweet.class))
    @RequestMapping(method = RequestMethod.GET, value = "/{username}/tweets")
    public DeferredResult<ResponseEntity<Page<Tweet>>> getUserTweets(
            @RequestParam("username") String username,
            @RequestParam("page") Integer page,
            @RequestParam("pageSize") Integer pageSize) {
        LOGGER.debug("request params: username: {}, page: {}, pageSize: {}",
                username, page, pageSize);
        // Using DeferredResult to provide non-blocking rest capabilities
        DeferredResult<ResponseEntity<Page<Tweet>>> result = new DeferredResult<>();
        Observable<Page<Tweet>> obsResult = userService.getTweets(username, page, pageSize);
        // Setting the result and errorResult in case of any Exceptions wrapper by RxJava
        obsResult.subscribeOn(Schedulers.io()).subscribe(searchResults -> {
            ResponseEntity<Page<Tweet>> entity = new ResponseEntity<>(searchResults, HttpStatus.OK);
            result.setResult(entity);
        }, result::setErrorResult);
        return result;
    }
}
