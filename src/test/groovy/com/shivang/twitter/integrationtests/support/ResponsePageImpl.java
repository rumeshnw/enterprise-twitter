package com.shivang.twitter.integrationtests.support;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class ResponsePageImpl<T> extends PageImpl<T> {


    public ResponsePageImpl(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    @JsonCreator
    public ResponsePageImpl(@JsonProperty("content") List<T> content) {
        super(content);
    }
}
