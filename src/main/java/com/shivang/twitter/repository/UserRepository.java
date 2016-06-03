package com.shivang.twitter.repository;

import com.shivang.twitter.model.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


public interface UserRepository extends ElasticsearchRepository<User, String> {

    User getUserByUsername(String username);
}
