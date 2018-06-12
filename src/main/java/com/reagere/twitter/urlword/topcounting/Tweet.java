package com.reagere.twitter.urlword.topcounting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tweet {
    private String text;
    public boolean hasUrl() {
        return text.contains("http://");
    }
}
