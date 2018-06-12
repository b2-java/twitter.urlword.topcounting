package com.reagere.twitter.urlword.topcounting;

import akka.japi.function.Function;
import lombok.Getter;

public class Counter implements Function<Tweet, Tweet> {

    private String name;

    @Getter
    private int count = 0;

    public Counter(String name) {
        this.name = name;
    }

    @Override
    public Tweet apply(Tweet param) {
        System.out.println("count " + name + " : " + ++count);
        return param;
    }
}
