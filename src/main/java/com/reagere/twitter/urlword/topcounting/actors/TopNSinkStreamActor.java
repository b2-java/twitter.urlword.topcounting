package com.reagere.twitter.urlword.topcounting.actors;

import com.reagere.twitter.urlword.topcounting.model.PairKeyCount;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.*;

public class TopNSinkStreamActor implements Subscriber<PairKeyCount> {

    // arguments
    private final int limit;
    private final String name;

    // state
    private final PriorityQueue<PairKeyCount> pQueue;
    private final Set<Subscription> subscriptionSet = new HashSet<>();

    // latency
    private long maxLatency = 0L;
    private long minLatency = Long.MAX_VALUE;
    private long sumLatency = 0;
    private long nbLatencies = 0;
    private long lastDisplay = 0;

    public TopNSinkStreamActor(int limit, String name) {
        this.limit = limit;
        this.name = name;
        pQueue = new PriorityQueue<>(limit);
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        subscriptionSet.add(subscription);
    }

    @Override
    public void onNext(PairKeyCount pair) {
        pQueue.remove(new PairKeyCount(pair.getCount() - 1, pair.getKey(), pair.getTime()));
        pQueue.add(pair);
        while (pQueue.size() > limit) {
            pQueue.poll();
        }
        doLatency(pair.getTime());
    }

    private void doLatency(long requestTime) {
        long time = System.currentTimeMillis();
        long delta = time - requestTime;
        maxLatency = Math.max(maxLatency, delta);
        minLatency = Math.min(minLatency, delta);
        sumLatency += delta;
        ++nbLatencies;
        if (time - lastDisplay > 10_000) { // 10s
            System.out.println(name + " > latency : max=" + maxLatency + " min=" + minLatency + " mean=" + (sumLatency / nbLatencies) + " for " + nbLatencies + " tweets");
            lastDisplay = time;
            maxLatency = 0;
            minLatency = Long.MAX_VALUE;
            sumLatency = 0;
            nbLatencies = 0;
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
