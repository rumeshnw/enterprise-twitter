package com.shivang.twitter.controller;

import com.shivang.twitter.model.SecurityUser;
import com.shivang.twitter.model.Tweet;
import com.shivang.twitter.model.TwitterUser;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * @author Shivang Shah
 */
@RestController
@RequestMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = {"Twitter TwitterUser Service"})
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        LOGGER.debug("TwitterUser controller initialized ..");
        this.userService = userService;
    }

    @ApiOperation(value = "", notes = "Get all users")
    @ApiResponses(
            @ApiResponse(code = 200, message = "", response = TwitterUser.class))
    @RequestMapping(method = RequestMethod.GET)
    public DeferredResult<ResponseEntity<Page<TwitterUser>>> getAllUsers(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        LOGGER.debug("get all users");
        // Using DeferredResult to provide non-blocking rest capabilities
        DeferredResult<ResponseEntity<Page<TwitterUser>>> result = new DeferredResult<>();
        Observable<Page<TwitterUser>> obsResult = userService.getUsers(page, pageSize);
        // Setting the result and errorResult in case of any Exceptions wrapper by RxJava
        obsResult.subscribeOn(Schedulers.io()).subscribe(searchResults -> {
            ResponseEntity<Page<TwitterUser>> entity = new ResponseEntity<>(searchResults, HttpStatus.OK);
            result.setResult(entity);
        }, result::setErrorResult);
        return result;
    }

    @ApiOperation(value = "/me", notes = "Logged in user's profile")
    @ApiResponses(
            @ApiResponse(code = 200, message = "", response = TwitterUser.class))
    @RequestMapping(method = RequestMethod.GET, value = "/me")
    public DeferredResult<ResponseEntity<TwitterUser>> getLoggedInUserProfile(Authentication auth) {
        LOGGER.debug("get logged in user's profile");
        DeferredResult<ResponseEntity<TwitterUser>> result = new DeferredResult<>();
        SecurityUser currentUser = (SecurityUser) auth.getPrincipal();
        LOGGER.debug("current logged in user is {}", currentUser.getUsername());
        Observable<TwitterUser> obsResult = userService.getUserByUsername(currentUser.getUsername());
        obsResult.subscribeOn(Schedulers.io()).subscribe(searchResults -> {
            ResponseEntity<TwitterUser> entity = new ResponseEntity<>(searchResults, HttpStatus.OK);
            result.setResult(entity);
        }, result::setErrorResult);
        return result;
    }

    @ApiOperation(value = "/{username}", notes = "Get a user's profile with username")
    @ApiResponses(
            @ApiResponse(code = 200, message = "", response = TwitterUser.class))
    @RequestMapping(method = RequestMethod.GET, value = "/{username}")
    public DeferredResult<ResponseEntity<TwitterUser>> getUserByUsername(Authentication auth,
                                                                         @PathVariable("username") String username) {
        LOGGER.debug("get user profile by username {}", username);
        DeferredResult<ResponseEntity<TwitterUser>> result = new DeferredResult<>();
        SecurityUser currentUser = (SecurityUser) auth.getPrincipal();
        LOGGER.debug("current logged in user is {}", currentUser.getUsername());
        Observable<TwitterUser> obsResult = userService.getUserByUsername(username);
        obsResult.subscribeOn(Schedulers.io()).subscribe(searchResults -> {
            ResponseEntity<TwitterUser> entity = new ResponseEntity<>(searchResults, HttpStatus.OK);
            result.setResult(entity);
        }, result::setErrorResult);
        return result;
    }

    @ApiOperation(value = "/me/feed", notes = "Logged in user's feed")
    @ApiResponses(
            @ApiResponse(code = 200, message = "", response = Tweet.class))
    @RequestMapping(method = RequestMethod.GET, value = "/me/feed")
    public DeferredResult<ResponseEntity<Page<Tweet>>> getLoggedInUserFeed(
            Authentication auth,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        LOGGER.debug("request params for getting feed: page: {}, pageSize: {}", page, pageSize);
        DeferredResult<ResponseEntity<Page<Tweet>>> result = new DeferredResult<>();
        SecurityUser currentUser = (SecurityUser) auth.getPrincipal();
        LOGGER.debug("current logged in user is {}", currentUser.getUsername());
        Observable<Page<Tweet>> obsResult = userService.getUserFeed(currentUser.getUsername(), page, pageSize);
        obsResult.subscribeOn(Schedulers.io()).subscribe(searchResults -> {
            ResponseEntity<Page<Tweet>> entity = new ResponseEntity<>(searchResults, HttpStatus.OK);
            result.setResult(entity);
        }, result::setErrorResult);
        return result;
    }

    @ApiOperation(value = "/me/tweets", notes = "Get logged in user's tweets")
    @ApiResponses(
            @ApiResponse(code = 200, message = "", response = Tweet.class))
    @RequestMapping(method = RequestMethod.GET, value = "/me/tweets")
    public DeferredResult<ResponseEntity<Page<Tweet>>> getUserTweets(
            Authentication auth,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        LOGGER.debug("request params: username: {}, page: {}, pageSize: {}", page, pageSize);
        SecurityUser currentUser = (SecurityUser) auth.getPrincipal();
        LOGGER.debug("current logged in user is {}", currentUser.getUsername());
        DeferredResult<ResponseEntity<Page<Tweet>>> result = new DeferredResult<>();
        Observable<Page<Tweet>> obsResult = userService.getTweets(currentUser.getUsername(), page, pageSize);
        obsResult.subscribeOn(Schedulers.io()).subscribe(searchResults -> {
            ResponseEntity<Page<Tweet>> entity = new ResponseEntity<>(searchResults, HttpStatus.OK);
            result.setResult(entity);
        }, result::setErrorResult);
        return result;
    }

    @ApiOperation(value = "/me/followers", notes = "Get followers")
    @ApiResponses(
            @ApiResponse(code = 200, message = "", response = TwitterUser.class))
    @RequestMapping(method = RequestMethod.GET, value = "/me/followers")
    public DeferredResult<ResponseEntity<Page<TwitterUser>>> getFollowers(
            Authentication auth,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        LOGGER.debug("get all followers");
        SecurityUser currentUser = (SecurityUser) auth.getPrincipal();
        LOGGER.debug("current logged in user is {}", currentUser.getUsername());
        DeferredResult<ResponseEntity<Page<TwitterUser>>> result = new DeferredResult<>();
        Observable<Page<TwitterUser>> obsResult = userService.getFollowers(currentUser.getUsername(), page, pageSize);
        obsResult.subscribeOn(Schedulers.io()).subscribe(searchResults -> {
            ResponseEntity<Page<TwitterUser>> entity = new ResponseEntity<>(searchResults, HttpStatus.OK);
            result.setResult(entity);
        }, result::setErrorResult);
        return result;
    }

    @ApiOperation(value = "/me/followings", notes = "Get followings")
    @ApiResponses(
            @ApiResponse(code = 200, message = "", response = TwitterUser.class))
    @RequestMapping(method = RequestMethod.GET, value = "/me/followings")
    public DeferredResult<ResponseEntity<Page<TwitterUser>>> getFollowings(
            Authentication auth,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        LOGGER.debug("get all followers");
        SecurityUser currentUser = (SecurityUser) auth.getPrincipal();
        LOGGER.debug("current logged in user is {}", currentUser.getUsername());
        DeferredResult<ResponseEntity<Page<TwitterUser>>> result = new DeferredResult<>();
        Observable<Page<TwitterUser>> obsResult = userService.getFollowings(currentUser.getUsername(), page, pageSize);
        obsResult.subscribeOn(Schedulers.io()).subscribe(searchResults -> {
            ResponseEntity<Page<TwitterUser>> entity = new ResponseEntity<>(searchResults, HttpStatus.OK);
            result.setResult(entity);
        }, result::setErrorResult);
        return result;
    }
}
