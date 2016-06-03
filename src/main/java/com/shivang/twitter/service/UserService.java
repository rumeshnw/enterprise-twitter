package com.shivang.twitter.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.shivang.twitter.model.CustomException;
import com.shivang.twitter.model.Tweet;
import com.shivang.twitter.model.User;
import com.shivang.twitter.repository.TweetRepository;
import com.shivang.twitter.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.Subscriber;

@Component
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private UserRepository userRepository;
    private TweetRepository tweetRepository;

    @Autowired
    public UserService(UserRepository userRepository, TweetRepository tweetRepository) {
        LOGGER.debug("User service initialized ..");
        this.userRepository = userRepository;
        this.tweetRepository = tweetRepository;
    }

    @HystrixCommand
    public Observable<User> getUserByUsername(String username) {
        return Observable.create(new Observable.OnSubscribe<User>() {
            @Override
            public void call(Subscriber<? super User> subscriber) {
                try {
                    User user = userRepository.getUserByUsername(username);
                    if (user == null) {
                        throw new CustomException(HttpStatus.NOT_FOUND, "user not found with username: " + username);
                    }
                    subscriber.onNext(user);
                } catch (Exception ex) {
                    subscriber.onError(ex);
                }
            }
        });
    }

    @HystrixCommand
    public Observable<Page<Tweet>> login(String username, String password, Integer page, Integer pageSize) {
        return Observable.create(new Observable.OnSubscribe<Page<Tweet>>() {
            @Override
            public void call(Subscriber<? super Page<Tweet>> subscriber) {
                try {
                    User user = userRepository.getUserByUsername(username);
                    if (user == null || !user.getPassword().equals(password)) {
                        throw new CustomException(HttpStatus.UNAUTHORIZED, "invalid username or password provided");
                    }
                    subscriber.onNext(tweetRepository.findByUserIdIn(user.getFollowingIds(),
                            new PageRequest(page, pageSize, Sort.Direction.DESC, "timeCreatedInMillis")));
                } catch (Exception ex) {
                    subscriber.onError(ex);
                }
            }
        });
    }

    @HystrixCommand
    public Observable<Page<Tweet>> getTweets(String username, Integer page, Integer pageSize) {
        return Observable.create(new Observable.OnSubscribe<Page<Tweet>>() {
            @Override
            public void call(Subscriber<? super Page<Tweet>> subscriber) {
                try {
                    User user = userRepository.getUserByUsername(username);
                    if (user == null) {
                        throw new CustomException(HttpStatus.NOT_FOUND, "user not found with username: " + username);
                    }
                    subscriber.onNext(tweetRepository.findByUserId(user.getId(),
                            new PageRequest(page, pageSize, Sort.Direction.DESC, "timeCreatedInMillis")));
                } catch (Exception ex) {
                    subscriber.onError(ex);
                }
            }
        });
    }

    @HystrixCommand
    public Observable<Page<User>> getUsers(Integer page, Integer pageSize) {
        return Observable.create(new Observable.OnSubscribe<Page<User>>() {
            @Override
            public void call(Subscriber<? super Page<User>> subscriber) {
                try {
                    Page<User> users = userRepository.findAll(new PageRequest(page, pageSize, Sort.Direction.DESC, "timeCreatedInMillis"));
                    subscriber.onNext(users);
                } catch (Exception ex) {
                    subscriber.onError(ex);
                }
            }
        });
    }
}
