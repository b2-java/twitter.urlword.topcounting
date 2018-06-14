package com.reagere.twitter.urlword.topcounting;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class TopN implements Subscriber<Pair> {

    private final int limit = 10;
    private final PriorityQueue<Pair> pQueue = new PriorityQueue<>(limit);

    @Override
    public void onSubscribe(Subscription subscription) {

    }

    @Override
    public void onNext(Pair pair) {
        pQueue.remove(new Pair(pair.getCount() - 1, pair.getKey()));
        pQueue.add(pair);
        while (pQueue.size() > limit) {
            pQueue.poll();
        }
    }

    public List<Pair> getTopN() {
        List<Pair> topn = Arrays.asList(pQueue.toArray(new Pair[limit]));
        topn.sort(Comparator.reverseOrder());
        return topn;
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }
}
