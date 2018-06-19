package com.reagere.twitter.urlword.topcounting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PairKeyCount implements Comparable {

    private Long count;
    private String key;

    @EqualsAndHashCode.Exclude // time is not relevant in equality and hashcode
    private long time;

    @Override
    public int compareTo(Object o) {
        if (o instanceof PairKeyCount) {
            return count.compareTo(((PairKeyCount) o).count);
        }
        return -1;
    }
}
