package com.shivang.twitter.repository;

import com.shivang.twitter.model.Tweet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;


public interface TweetRepository extends ElasticsearchRepository<Tweet, String> {

    Page<Tweet> findByUserIdIn(List<String> userIds, Pageable pageable);

    Page<Tweet> findByUserId(String userId, Pageable pageable);
}
