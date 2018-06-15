package com.reagere.twitter.urlword.topcounting.actors;

import com.reagere.twitter.urlword.topcounting.model.PairKeyCount;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.*;

public class TopNSinkStreamActor implements Subscriber<PairKeyCount> {

    private final int limit = 10;
    private final PriorityQueue<PairKeyCount> pQueue = new PriorityQueue<>(limit);
    private final Set<Subscription> subscriptionSet = new HashSet<>();

    @Override
    public void onSubscribe(Subscription subscription) {
        subscriptionSet.add(subscription);
    }

    @Override
    public void onNext(PairKeyCount pair) {
        pQueue.remove(new PairKeyCount(pair.getCount() - 1, pair.getKey()));
        pQueue.add(pair);
        while (pQueue.size() > limit) {
            pQueue.poll();
        }
    }

    public List<PairKeyCount> getTopN() {
        List<PairKeyCount> topn = Arrays.asList(pQueue.toArray(new PairKeyCount[Math.min(limit, pQueue.size())]));
        topn.sort(Comparator.reverseOrder());
        return topn;
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onComplete() {
        subscriptionSet.forEach(Subscription::cancel);
        subscriptionSet.clear();
    }
}
