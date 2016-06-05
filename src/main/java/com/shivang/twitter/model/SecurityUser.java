package com.shivang.twitter.model;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

public class SecurityUser extends User {

    private TwitterUser twitterUser;

    public SecurityUser(TwitterUser twitterUser) {
        super(twitterUser.getUsername(), twitterUser.getPassword(),
                true, true, true, true, AuthorityUtils.createAuthorityList("ADMIN"));
        this.twitterUser = twitterUser;
    }

    public TwitterUser getTwitterUser() {
        return twitterUser;
    }
}
