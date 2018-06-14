package com.reagere.twitter.urlword.topcounting;

import io.reactivex.Flowable;
import io.reactivex.functions.Predicate;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RxMain {
    public static void main(String[] args) {
        TwitterStreamActor source = new TwitterStreamActor();
        Predicate<Tweet> hasUrl = t -> t.getText().replace("\n", " ").matches(".*http(s)?://.*");

        CountOccurences countUrls = new CountOccurences();
        Flowable.fromPublisher(source).repeat()
                .filter(hasUrl)
                .map(new Dereference())
                .subscribe(countUrls);

        CountOccurences countWords = new CountOccurences();
        Flowable.fromPublisher(source).repeat()
                .filter(t -> !hasUrl.test(t))
                .map(t -> Arrays.asList(t.getText().replace("\n", " ").split(" ")).stream().filter(s -> !s.isEmpty()).collect(Collectors.toSet()))
                .subscribe(countWords);
        source.run(i -> {
            System.out.println("[DONE] " + i + " tweets processed");
            countUrls.onComplete();
            countWords.onComplete();
            return null;
        });
    }
}
