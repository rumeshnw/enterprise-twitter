package com.shivang.twitter;

import com.shivang.twitter.model.Tweet;
import com.shivang.twitter.model.TwitterUser;
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
        LOGGER.debug("Adding twitterUsers .. ");
        List<TwitterUser> twitterUsers = new ArrayList<>();
        for (int i = 0; i < numberOfUsers; i++) {
            TwitterUser twitterUser = new TwitterUser("first" + i, "last" + i, "username" + i, "password" + i, new ArrayList<>(), new ArrayList<>());
            twitterUser = this.userRepository.save(twitterUser);
            twitterUsers.add(twitterUser);
        }

        Observable.from(twitterUsers)
                .flatMap((Func1<TwitterUser, Observable<TwitterUser>>) user ->
                        Observable.zip(updateUserWithFollowingAndFollowers(user, twitterUsers),
                                createAndSaveTweetsForUser(user), (user1, user2) -> user1))
                .toList().toBlocking().last();
        LOGGER.debug("All Twitter users and their corresponding tweets are populated !");
    }

    private Observable<TwitterUser> updateUserWithFollowingAndFollowers(TwitterUser twitterUser, List<TwitterUser> allTwitterUsers) {
        return Observable.create(new Observable.OnSubscribe<TwitterUser>() {
            @Override
            public void call(Subscriber<? super TwitterUser> subscriber) {
                LOGGER.debug("Updating twitterUser {} with followers & followings ..", twitterUser.getUsername());
                for (int i = 0; i < allTwitterUsers.size(); i++) {
                    if (!allTwitterUsers.get(i).getId().equals(twitterUser.getId())) {
                        twitterUser.getFollowerIds().add(allTwitterUsers.get(i).getId());
                        twitterUser.getFollowingIds().add(allTwitterUsers.get(i).getId());
                    }
                }
                subscriber.onNext(userRepository.save(twitterUser));
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io());
    }

    private Observable<TwitterUser> createAndSaveTweetsForUser(TwitterUser twitterUser) {
        return Observable.create(new Observable.OnSubscribe<TwitterUser>() {
            @Override
            public void call(Subscriber<? super TwitterUser> subscriber) {
                LOGGER.debug("Adding tweets by twitterUser {}", twitterUser.getUsername());
                for (int j = 0; j < numberOfTweetsPerUser; j++) {
                    tweetRepository.save(new Tweet(twitterUser.getId(),
                            "tweet number is " + j + " by twitterUser " + twitterUser.getUsername()));
                }
                subscriber.onNext(twitterUser);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }
}
