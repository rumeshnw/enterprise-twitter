package com.shivang.twitter.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

@Document(indexName = "user", type = "user", shards = 1, replicas = 0, refreshInterval = "-1")
public class User {

    @Id
    private String id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private List<String> followingIds;
    private List<String> followerIds;
    private long timeCreatedInMillis;

    public User() {
    }

    public User(String firstName, String lastName, String username, String password, List<String> followingIds, List<String> followerIds) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.timeCreatedInMillis = System.currentTimeMillis();
        this.followingIds = followingIds;
        this.followerIds = followerIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getTimeCreatedInMillis() {
        return timeCreatedInMillis;
    }

    public void setTimeCreatedInMillis(long timeCreatedInMillis) {
        this.timeCreatedInMillis = timeCreatedInMillis;
    }

    public List<String> getFollowingIds() {
        return followingIds;
    }

    public void setFollowingIds(List<String> followingIds) {
        this.followingIds = followingIds;
    }

    public List<String> getFollowerIds() {
        return followerIds;
    }

    public void setFollowerIds(List<String> followerIds) {
        this.followerIds = followerIds;
    }
}
