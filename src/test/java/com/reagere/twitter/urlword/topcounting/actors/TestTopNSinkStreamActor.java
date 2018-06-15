package com.reagere.twitter.urlword.topcounting.actors;

import com.google.common.collect.Lists;
import com.reagere.twitter.urlword.topcounting.model.PairKeyCount;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestTopNSinkStreamActor {
    @Test
    public void testTopN() {
        TopNSinkStreamActor s = new TopNSinkStreamActor();
        Assert.assertEquals(new ArrayList<>(), s.getTopN());
        s.onNext(new PairKeyCount(1L, "a"));
        Assert.assertEquals(Lists.newArrayList(new PairKeyCount(1L, "a")), s.getTopN());
        s.onNext(new PairKeyCount(2L, "a"));
        Assert.assertEquals(Lists.newArrayList(new PairKeyCount(2L, "a")), s.getTopN());
        IntStream.range(0, 11).forEachOrdered(i -> s.onNext(new PairKeyCount(3L, "a" + i))); // we push more than the TopN values, check only the last one are in topN
        Assert.assertEquals(new HashSet<>(IntStream.range(1, 11).mapToObj(i -> new PairKeyCount(3L, "a" + i)).collect(Collectors.toList())), new HashSet<>(s.getTopN()));
    }
}
