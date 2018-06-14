package com.reagere.twitter.urlword.topcounting;

import com.reagere.twitter.urlword.topcounting.web.TopNController;
import com.reagere.twitter.urlword.topcounting.web.WebServer;
import io.reactivex.Flowable;
import io.reactivex.functions.Predicate;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class RxMain {
    public static void main(String[] args) throws IOException {
        TwitterStreamActor source = new TwitterStreamActor(500);
        Predicate<Tweet> hasUrl = t -> t.getText().replace("\n", " ").matches(".*http(s)?://.*");

        CountOccurences countUrls = new CountOccurences();
        final TopN topNurl = new TopN();
        Flowable.fromPublisher(source).repeat()
                .filter(hasUrl)
                .map(new Dereference())
                .subscribe(countUrls);
        countUrls.subscribe(topNurl);

        CountOccurences countWords = new CountOccurences();
        final TopN topNwords = new TopN();
        Flowable.fromPublisher(source).repeat()
                .filter(t -> !hasUrl.test(t))
                .map(t -> Arrays.asList(t.getText().replace("\n", " ").split(" ")).stream().filter(s -> !s.isEmpty()).collect(Collectors.toSet()))
                .subscribe(countWords);
        countWords.subscribe(topNwords);

        // webserver
        final WebServer webServer = new WebServer(8080, new TopNController(topNwords, source.getGson()), new TopNController(topNurl, source.getGson()));

        source.run(i -> {
            System.out.println("[DONE] " + i + " tweets processed");
            countUrls.onComplete();
            countWords.onComplete();
            System.out.println("topN URL : " + topNurl.getTopN());
            System.out.println("topN Words : " + topNwords.getTopN());
            webServer.stop();
            return null;
        });
    }
}
