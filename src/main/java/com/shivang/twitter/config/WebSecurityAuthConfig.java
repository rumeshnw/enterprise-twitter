package com.shivang.twitter.config;


import com.shivang.twitter.model.SecurityUser;
import com.shivang.twitter.model.TwitterUser;
import com.shivang.twitter.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class WebSecurityAuthConfig extends GlobalAuthenticationConfigurerAdapter {

    @Autowired
    private UserService userService;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService());
    }

    @Bean
    UserDetailsService userDetailsService() {
        return (UserDetailsService) username -> {
            TwitterUser twitterUser = userService.getUserByUsername(username).toBlocking().single();
            return new SecurityUser(twitterUser);
        };
    }
}
