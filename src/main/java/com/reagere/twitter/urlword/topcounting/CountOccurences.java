package com.reagere.twitter.urlword.topcounting;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CountOccurences implements Subscriber<Set<String>>, Publisher<Pair> {

    private Map<String, Long> counts = new HashMap<>();
    private Set<Subscription> subscriptionSet = new HashSet<>();
    private Set<Subscriber<? super Pair>> subscriberSet = new HashSet<>();

    @Override
    public void onSubscribe(Subscription subscription) {
        subscriptionSet.add(subscription);
    }

    @Override
    public void onNext(Set<String> words) {
        words.stream().forEach(word -> push(new Pair(counts.compute(word, (w, c) -> c == null ? 1 : c + 1), word)));
    }

    private void push(Pair p) {
        subscriberSet.stream().forEach(s -> s.onNext(p));
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onComplete() {
        subscriptionSet.stream().forEach(s -> s.cancel());
    }

    @Override
    public void subscribe(Subscriber<? super Pair> subscriber) {
        subscriberSet.add(subscriber);
    }
}
