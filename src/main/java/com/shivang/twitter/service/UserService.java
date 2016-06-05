package com.shivang.twitter.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.shivang.twitter.model.CustomException;
import com.shivang.twitter.model.Tweet;
import com.shivang.twitter.model.TwitterUser;
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
        LOGGER.debug("TwitterUser service initialized ..");
        this.userRepository = userRepository;
        this.tweetRepository = tweetRepository;
    }

    @HystrixCommand
    public Observable<TwitterUser> getUserByUsername(String username) {
        return Observable.create(new Observable.OnSubscribe<TwitterUser>() {
            @Override
            public void call(Subscriber<? super TwitterUser> subscriber) {
                try {
                    TwitterUser twitterUser = userRepository.getUserByUsername(username);
                    if (twitterUser == null) {
                        throw new CustomException(HttpStatus.NOT_FOUND, "twitterUser not found with username: " + username);
                    }
                    subscriber.onNext(twitterUser);
                    subscriber.onCompleted();
                } catch (Exception ex) {
                    subscriber.onError(ex);
                }
            }
        });
    }

    @HystrixCommand
    public Observable<Page<Tweet>> getUserFeed(String username, Integer page, Integer pageSize) {
        return Observable.create(new Observable.OnSubscribe<Page<Tweet>>() {
            @Override
            public void call(Subscriber<? super Page<Tweet>> subscriber) {
                try {
                    TwitterUser twitterUser = userRepository.getUserByUsername(username);
                    subscriber.onNext(tweetRepository.findByUserIdIn(twitterUser.getFollowingIds(),
                            new PageRequest(page, pageSize, Sort.Direction.DESC, "timeCreatedInMillis")));
                    subscriber.onCompleted();
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
                    TwitterUser twitterUser = userRepository.getUserByUsername(username);
                    subscriber.onNext(tweetRepository.findByUserId(twitterUser.getId(),
                            new PageRequest(page, pageSize, Sort.Direction.DESC, "timeCreatedInMillis")));
                    subscriber.onCompleted();
                } catch (Exception ex) {
                    subscriber.onError(ex);
                }
            }
        });
    }

    @HystrixCommand
    public Observable<Page<TwitterUser>> getUsers(Integer page, Integer pageSize) {
        return Observable.create(new Observable.OnSubscribe<Page<TwitterUser>>() {
            @Override
            public void call(Subscriber<? super Page<TwitterUser>> subscriber) {
                try {
                    Page<TwitterUser> users = userRepository.findAll(new PageRequest(page, pageSize, Sort.Direction.DESC, "timeCreatedInMillis"));
                    subscriber.onNext(users);
                    subscriber.onCompleted();
                } catch (Exception ex) {
                    subscriber.onError(ex);
                }
            }
        });
    }

    @HystrixCommand
    public Observable<Page<TwitterUser>> getFollowers(String username, Integer page, Integer pageSize) {
        return Observable.create(new Observable.OnSubscribe<Page<TwitterUser>>() {
            @Override
            public void call(Subscriber<? super Page<TwitterUser>> subscriber) {
                try {
                    TwitterUser user = userRepository.getUserByUsername(username);
                    Page<TwitterUser> users = userRepository.findByIdIn(user.getFollowerIds(),
                            new PageRequest(page, pageSize, Sort.Direction.DESC, "timeCreatedInMillis"));
                    subscriber.onNext(users);
                    subscriber.onCompleted();
                } catch (Exception ex) {
                    subscriber.onError(ex);
                }
            }
        });
    }

    @HystrixCommand
    public Observable<Page<TwitterUser>> getFollowings(String username, Integer page, Integer pageSize) {
        return Observable.create(new Observable.OnSubscribe<Page<TwitterUser>>() {
            @Override
            public void call(Subscriber<? super Page<TwitterUser>> subscriber) {
                try {
                    TwitterUser user = userRepository.getUserByUsername(username);
                    Page<TwitterUser> users = userRepository.findByIdIn(user.getFollowingIds(),
                            new PageRequest(page, pageSize, Sort.Direction.DESC, "timeCreatedInMillis"));
                    subscriber.onNext(users);
                    subscriber.onCompleted();
                } catch (Exception ex) {
                    subscriber.onError(ex);
                }
            }
        });
    }
}
