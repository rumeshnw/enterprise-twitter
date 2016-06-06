package com.shivang.twitter.integrationtests.controller

import com.shivang.twitter.EnterpriseTwitterApplication
import com.shivang.twitter.integrationtests.support.ResponsePageImpl
import com.shivang.twitter.model.Tweet
import com.shivang.twitter.model.TwitterUser
import com.shivang.twitter.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.web.client.AsyncRestTemplate
import org.springframework.web.client.HttpClientErrorException
import spock.lang.Specification

import java.nio.charset.Charset
import java.util.concurrent.ExecutionException

@SpringApplicationConfiguration(EnterpriseTwitterApplication)
@WebIntegrationTest(["server.port=9000"])

class UserControllerSpec extends Specification {

    @Autowired
    private UserRepository userRepository
    private AsyncRestTemplate restTemplate = new AsyncRestTemplate()
    private static final String USER_URL = "http://localhost:9000/users"
    private HttpHeaders headers
    private static final String LOGGED_IN_USERNAME = "username0"
    private static final String LOGGED_IN_PASSWORD = "password0"
    private static final int PAGE = 0
    private static final int PAGE_SIZE = 10
    private static TwitterUser twitterUser

    def setup() {
        headers = createHeaders(LOGGED_IN_USERNAME, LOGGED_IN_PASSWORD)
        twitterUser = userRepository.getUserByUsername(LOGGED_IN_USERNAME)
    }

    def cleanup() {
    }

    HttpHeaders createHeaders(String username, String password) {
        return new HttpHeaders() {
            {
                String auth = username + ":" + password
                byte[] encodedAuth = Base64.getEncoder().encode(
                        auth.getBytes(Charset.forName("US-ASCII")))
                String authHeader = "Basic " + new String(encodedAuth)
                set("Authorization", authHeader)
            }
        };
    }

    def "get all twitter users"() {
        ResponseEntity<ResponsePageImpl<TwitterUser>> usersResponse
        ResponsePageImpl<TwitterUser> users

        given: "a request to all twitter users with a given page number and pageSize"

        when: "such a request is made"
            ParameterizedTypeReference<ResponsePageImpl<TwitterUser>> responseType =
                    new ParameterizedTypeReference<ResponsePageImpl<TwitterUser>>() {};
            usersResponse = restTemplate.exchange(USER_URL + "?page={page}&pageSize={pageSize}", HttpMethod.GET,
                    new HttpEntity<>(headers),
                    responseType, PAGE, PAGE_SIZE)
                    .get()
            users = usersResponse.getBody()

        then: "Verify that expected response is received"
            assert usersResponse.statusCode == HttpStatus.OK
            assert users.getContent().size() == PAGE_SIZE
    }

    def "get logged in user's profile"() {
        ResponseEntity<TwitterUser> usersResponse
        TwitterUser user

        given: "a logged in user"

        when: "a request to get logged in user's profile '/me' is made"
            ParameterizedTypeReference<TwitterUser> responseType =
                    new ParameterizedTypeReference<TwitterUser>() {};
            usersResponse = restTemplate.exchange(USER_URL + "/me", HttpMethod.GET,
                    new HttpEntity<>(headers),
                    responseType)
                    .get()
            user = usersResponse.getBody()

        then: "Verify that expected response is received with user's profile"
            assert usersResponse.statusCode == HttpStatus.OK
            assert user.getUsername() == LOGGED_IN_USERNAME
    }

    def "get a user's profile"() {
        ResponseEntity<TwitterUser> usersResponse
        TwitterUser user
        String username = "username1"

        given: "a user with username"

        when: "a request to get user profile is made"
            ParameterizedTypeReference<TwitterUser> responseType =
                    new ParameterizedTypeReference<TwitterUser>() {};
            usersResponse = restTemplate.exchange(USER_URL + "/{username}", HttpMethod.GET,
                    new HttpEntity<>(headers),
                    responseType, username)
                    .get()
            user = usersResponse.getBody()

        then: "Verify that expected response is received with user's profile"
            assert usersResponse.statusCode == HttpStatus.OK
            assert user.getUsername() == username
    }

    def "get a user's profile with username that does not exist"() {
        ResponseEntity<TwitterUser> usersResponse
        String username = "doesNotExist"

        given: "a username that does not exist"

        when: "a request to get user profile is made"
            ParameterizedTypeReference<TwitterUser> responseType =
                    new ParameterizedTypeReference<TwitterUser>() {};
            restTemplate.exchange(USER_URL + "/{username}", HttpMethod.GET,
                    new HttpEntity<>(headers),
                    responseType, username)
                    .get()

        then: "Verify that expected response is received with user's profile"
            ExecutionException ex = thrown()
            HttpClientErrorException e = (HttpClientErrorException) ex.getCause()
            assert e.getStatusCode() == HttpStatus.NOT_FOUND
    }

    def "get user's profile with wrong credentials"() {

        given: "a logged in user with wrong credential"

        when: "a request to get logged in user's profile '/me' is made"
            ParameterizedTypeReference<TwitterUser> responseType =
                    new ParameterizedTypeReference<TwitterUser>() {};
            restTemplate.exchange(USER_URL + "/me", HttpMethod.GET,
                    new HttpEntity<>(createHeaders("wrong", "wrong")),
                    responseType)
                    .get()

        then: "Verify that expected response is user unauthorized"
            ExecutionException ex = thrown()
            HttpClientErrorException e = (HttpClientErrorException) ex.getCause()
            assert e.getStatusCode() == HttpStatus.UNAUTHORIZED
    }

    def "get logged in user's tweets"() {
        ResponseEntity<ResponsePageImpl<Tweet>> tweetsResponse
        ResponsePageImpl<Tweet> tweets

        given: "a logged in user"

        when: "a request to get logged in user's tweets '/me/tweets' is made"
            ParameterizedTypeReference<ResponsePageImpl<Tweet>> responseType =
                    new ParameterizedTypeReference<ResponsePageImpl<Tweet>>() {};
            tweetsResponse = restTemplate.exchange(USER_URL + "/me/tweets?page={page}&pageSize={pageSize}",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    responseType, PAGE, PAGE_SIZE)
                    .get()
            tweets = tweetsResponse.getBody()

        then: "Verify that expected response is received with user's tweets"
            assert tweetsResponse.statusCode == HttpStatus.OK
            assert tweets.getContent().size() == PAGE_SIZE
            for (Tweet tweet : tweets.getContent()) {
                assert tweet.getUserId() == twitterUser.getId()
            }
    }

    def "get logged in user's feed"() {
        ResponseEntity<ResponsePageImpl<Tweet>> tweetsResponse
        ResponsePageImpl<Tweet> tweets

        given: "a logged in user"

        when: "a request to get logged in user's tweets '/me/feed' is made"
            ParameterizedTypeReference<ResponsePageImpl<Tweet>> responseType =
                    new ParameterizedTypeReference<ResponsePageImpl<Tweet>>() {};
            tweetsResponse = restTemplate.exchange(USER_URL + "/me/feed?page={page}&pageSize={pageSize}",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    responseType, PAGE, PAGE_SIZE)
                    .get()
            tweets = tweetsResponse.getBody()

        then: "Verify that expected response is received with user's feed"
            assert tweetsResponse.statusCode == HttpStatus.OK
            assert tweets.getContent().size() == PAGE_SIZE
            for (Tweet tweet : tweets.getContent()) {
                assert twitterUser.getFollowingIds().contains(tweet.getUserId())
            }
    }

    def "get logged-in user's followers"() {
        ResponseEntity<ResponsePageImpl<TwitterUser>> usersResponse
        ResponsePageImpl<TwitterUser> users

        given: "a logged in user"

        when: "a request to get all followers with a given page number and pageSize is made"
            ParameterizedTypeReference<ResponsePageImpl<TwitterUser>> responseType =
                    new ParameterizedTypeReference<ResponsePageImpl<TwitterUser>>() {};
            usersResponse = restTemplate.exchange(USER_URL + "/me/followers?page={page}&pageSize={pageSize}", HttpMethod.GET,
                    new HttpEntity<>(headers),
                    responseType, PAGE, PAGE_SIZE)
                    .get()
            users = usersResponse.getBody()

        then: "Verify that expected response is received with all user's followers"
            assert usersResponse.statusCode == HttpStatus.OK
            assert users.getContent().size() == twitterUser.getFollowerIds().size()
            for (TwitterUser user : users.getContent()) {
                assert twitterUser.getFollowerIds().contains(user.getId())
            }
    }

    def "get TwitterUsers that logged-in user is following"() {
        ResponseEntity<ResponsePageImpl<TwitterUser>> usersResponse
        ResponsePageImpl<TwitterUser> users

        given: "a logged in user"

        when: "a request to get all twitter users he is following with a given page number and pageSize is made"
            ParameterizedTypeReference<ResponsePageImpl<TwitterUser>> responseType =
                    new ParameterizedTypeReference<ResponsePageImpl<TwitterUser>>() {};
            usersResponse = restTemplate.exchange(USER_URL + "/me/followings?page={page}&pageSize={pageSize}", HttpMethod.GET,
                    new HttpEntity<>(headers),
                    responseType, PAGE, PAGE_SIZE)
                    .get()
            users = usersResponse.getBody()

        then: "Verify that expected response is received with all users that user is following"
            assert usersResponse.statusCode == HttpStatus.OK
            assert users.getContent().size() == twitterUser.getFollowerIds().size()
            for (TwitterUser user : users.getContent()) {
                assert twitterUser.getFollowingIds().contains(user.getId())
            }
    }
}
