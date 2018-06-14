package com.reagere.twitter.urlword.topcounting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pair implements Comparable {
    private Long count;
    private String key;

    @Override
    public int compareTo(Object o) {
        if (o != null && o instanceof Pair) {
            return count.compareTo(((Pair) o).count);
        }
        return -1;
    }
}
