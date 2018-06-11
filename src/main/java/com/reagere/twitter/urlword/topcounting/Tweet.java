package com.reagere.twitter.urlword.topcounting;

public class Tweet {
    private final String msg;

    public Tweet(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public boolean hasUrl() {
        return msg.contains("http://");
    }
}
