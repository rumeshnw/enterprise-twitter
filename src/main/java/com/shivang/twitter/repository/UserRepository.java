package com.shivang.twitter.repository;

import com.shivang.twitter.model.TwitterUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;


public interface UserRepository extends ElasticsearchRepository<TwitterUser, String> {

    TwitterUser getUserByUsername(String username);

    Page<TwitterUser> findByIdIn(List<String> userIds, Pageable pageable);
}
