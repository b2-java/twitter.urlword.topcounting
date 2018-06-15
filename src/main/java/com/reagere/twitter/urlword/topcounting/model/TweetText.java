package com.reagere.twitter.urlword.topcounting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TweetText {
    private String text;
    public boolean hasUrl() {
        return text.replace("\n", " ").matches(".*http(s)?://.*");
    }
}
