package com.reagere.twitter.urlword.topcounting.actors;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.reagere.twitter.urlword.topcounting.model.PairKeyCount;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestTopNSinkStreamActor {
    @Test
    public void testTopN() {
        TopNSinkStreamActor s = new TopNSinkStreamActor(10, "test junit");
        Assert.assertEquals(Collections.emptyList(), s.getTopN());

        s.onNext(new PairKeyCount(1L, "a", 0));
        Assert.assertEquals(Lists.newArrayList(new PairKeyCount(1L, "a", 0)), s.getTopN());

        s.onNext(new PairKeyCount(2L, "a", 1));
        Assert.assertEquals(Lists.newArrayList(new PairKeyCount(2L, "a", 1)), s.getTopN());

        s.onNext(new PairKeyCount(1L, "b", 2));
        Assert.assertEquals(Sets.newHashSet(new PairKeyCount(2L, "a", 1), new PairKeyCount(1L, "b", 2)), new HashSet<>(s.getTopN())); // set because the order is not determinist

        s.onNext(new PairKeyCount(3L, "a", 3));
        Assert.assertEquals(Sets.newHashSet(new PairKeyCount(3L, "a", 3), new PairKeyCount(1L, "b", 2)), new HashSet<>(s.getTopN()));

        IntStream.range(0, 10).forEachOrdered(i -> s.onNext(new PairKeyCount(4L, "a" + i, 4))); // we push more than the TopN values, check only the last one are in topN
        Assert.assertEquals(new HashSet<>(IntStream.range(0, 10).mapToObj(i -> new PairKeyCount(4L, "a" + i, 4)).collect(Collectors.toList())), new HashSet<>(s.getTopN()));
    }
}
