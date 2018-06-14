package com.reagere.twitter.urlword.topcounting;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class CountOccurences implements Subscriber<Set<String>> {

    private ConcurrentHashMap<String, AtomicLong> counts = new ConcurrentHashMap<>();
    private Set<Subscription> subscriptionSet = new HashSet<>();

    @Override
    public void onSubscribe(Subscription subscription) {
        subscriptionSet.add(subscription);
    }

    @Override
    public void onNext(Set<String> words) {
        words.stream().forEach(word -> counts.compute(word, (w, c) -> {
            if (c == null) {
                c = new AtomicLong(1);
            } else {
                c.incrementAndGet();
            }
            return c;
        }));
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onComplete() {
        subscriptionSet.stream().forEach(s -> s.cancel());
        System.out.println(counts);
    }
}
