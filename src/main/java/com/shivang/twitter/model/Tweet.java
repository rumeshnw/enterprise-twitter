package com.shivang.twitter.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "tweet", type = "tweet", shards = 1, replicas = 0, refreshInterval = "-1")
public class Tweet {

    @Id
    private String id;
    private String userId;
    private String tweet;
    private long timeCreatedInMillis;

    public Tweet() {
    }

    public Tweet(String userId, String tweet) {
        this.userId = userId;
        this.tweet = tweet;
        this.timeCreatedInMillis = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTweet() {
        return tweet;
    }

    public void setTweet(String tweet) {
        this.tweet = tweet;
    }

    public long getTimeCreatedInMillis() {
        return timeCreatedInMillis;
    }

    public void setTimeCreatedInMillis(long timeCreatedInMillis) {
        this.timeCreatedInMillis = timeCreatedInMillis;
    }
}
