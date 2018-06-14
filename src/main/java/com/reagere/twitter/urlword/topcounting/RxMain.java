package com.reagere.twitter.urlword.topcounting;

import io.reactivex.Flowable;
import io.reactivex.functions.Predicate;

public class RxMain {
    public static void main(String[] args) {
        TwitterStreamActor source = new TwitterStreamActor();
        Predicate<Tweet> p = t -> t.getText().matches(".*http(s)?://.*");
        Flowable.fromPublisher(source).repeat().filter(p).subscribe(System.out::println);
        Flowable.fromPublisher(source).repeat().filter(t -> !p.test(t)).subscribe(System.err::println);
        source.run(i -> {
            System.out.println("[DONE] " + i + " tweets processed");
            return null;
        });
    }
}
