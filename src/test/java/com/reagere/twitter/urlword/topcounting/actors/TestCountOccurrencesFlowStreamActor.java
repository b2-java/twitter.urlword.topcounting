package com.reagere.twitter.urlword.topcounting.actors;

import com.google.common.collect.Lists;
import com.reagere.twitter.urlword.topcounting.model.PairKeyCount;
import org.junit.Assert;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestCountOccurrencesFlowStreamActor {
    @Test
    public void test() {
        CountOccurrencesFlowStreamActor c = new CountOccurrencesFlowStreamActor();
        final List<PairKeyCount> history = new ArrayList<>();
        AtomicBoolean hasError = new AtomicBoolean();
        c.subscribe(new Subscriber<PairKeyCount>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                //
            }

            @Override
            public void onNext(PairKeyCount pairKeyCount) {
                history.add(pairKeyCount);
            }

            @Override
            public void onError(Throwable throwable) {
                hasError.set(true);
            }

            @Override
            public void onComplete() {
                //
            }
        });
        Assert.assertEquals(0, c.getCounts().size());

        c.onNext(Lists.newArrayList("a", "b", "a"));
        Assert.assertEquals(2, c.getCounts().size());
        Assert.assertEquals(2, c.getCounts().get("a").longValue());
        Assert.assertEquals(1, c.getCounts().get("b").longValue());
        Assert.assertEquals(Lists.newArrayList(new PairKeyCount(1L, "a"), new PairKeyCount(1L, "b"), new PairKeyCount(2L, "a")), history);

        c.onNext(Lists.newArrayList("b", "a", "c"));
        Assert.assertEquals(3, c.getCounts().size());
        Assert.assertEquals(3, c.getCounts().get("a").longValue());
        Assert.assertEquals(2, c.getCounts().get("b").longValue());
        Assert.assertEquals(1, c.getCounts().get("c").longValue());
        Assert.assertEquals(Lists.newArrayList(new PairKeyCount(1L, "a"), new PairKeyCount(1L, "b"), new PairKeyCount(2L, "a"), new PairKeyCount(2L, "b"), new PairKeyCount(3L, "a"), new PairKeyCount(1L, "c")), history);

        Assert.assertFalse(hasError.get());
    }
}
