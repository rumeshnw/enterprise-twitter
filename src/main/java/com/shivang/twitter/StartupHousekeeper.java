package com.shivang.twitter;

import com.shivang.twitter.model.Tweet;
import com.shivang.twitter.model.User;
import com.shivang.twitter.repository.TweetRepository;
import com.shivang.twitter.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;

@Component
public class StartupHousekeeper implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TweetRepository tweetRepository;

    private static final int numberOfUsers = 10;
    private static final int numberOfTweetsPerUser = 100;

    private static final Logger LOGGER = LoggerFactory.getLogger(StartupHousekeeper.class);

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        this.userRepository.deleteAll();
        this.tweetRepository.deleteAll();
        saveUsersAndTweets();
    }

    private void saveUsersAndTweets() {
        LOGGER.debug("Adding users .. ");
        List<User> users = new ArrayList<>();
        for (int i = 0; i < numberOfUsers; i++) {
            User user = new User("first" + i, "last" + i, "username" + i, "password" + i, new ArrayList<>(), new ArrayList<>());
            user = this.userRepository.save(user);
            users.add(user);
        }

        Observable.from(users)
                .flatMap((Func1<User, Observable<User>>) user ->
                        Observable.zip(updateUserWithFollowingAndFollowers(user, users),
                                createAndSaveTweetsForUser(user), (user1, user2) -> user1))
                .subscribe();
        LOGGER.debug("Users and their corresponding Tweets populated !");
    }

    private Observable<User> updateUserWithFollowingAndFollowers(User user, List<User> allUsers) {
        return Observable.create(new Observable.OnSubscribe<User>() {
            @Override
            public void call(Subscriber<? super User> subscriber) {
                LOGGER.debug("Updating user {} with followers & followings ..", user.getUsername());
                for (int i = 0; i < allUsers.size(); i++) {
                    if (!allUsers.get(i).getId().equals(user.getId())) {
                        user.getFollowerIds().add(allUsers.get(i).getId());
                        user.getFollowingIds().add(allUsers.get(i).getId());
                    }
                }
                subscriber.onNext(userRepository.save(user));
            }
        })
                .subscribeOn(Schedulers.io());
    }

    private Observable<User> createAndSaveTweetsForUser(User user) {
        return Observable.create(new Observable.OnSubscribe<User>() {
            @Override
            public void call(Subscriber<? super User> subscriber) {
                LOGGER.debug("Adding tweets by user {}", user.getUsername());
                for (int j = 0; j < numberOfTweetsPerUser; j++) {
                    tweetRepository.save(new Tweet(user.getId(),
                            "tweet number is " + j + " by user " + user.getUsername()));
                }
                subscriber.onNext(user);
            }
        }).subscribeOn(Schedulers.io());
    }
}
