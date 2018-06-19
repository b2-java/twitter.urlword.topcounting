package com.reagere.twitter.urlword.topcounting;

import com.reagere.twitter.urlword.topcounting.actors.CountOccurrencesFlowStreamActor;
import com.reagere.twitter.urlword.topcounting.functions.DereferenceUrlsOfTweetText;
import com.reagere.twitter.urlword.topcounting.actors.TopNSinkStreamActor;
import com.reagere.twitter.urlword.topcounting.actors.TwitterSourceStreamActor;
import com.reagere.twitter.urlword.topcounting.model.TupleListTime;
import com.reagere.twitter.urlword.topcounting.model.TweetText;
import com.reagere.twitter.urlword.topcounting.web.TopNController;
import com.reagere.twitter.urlword.topcounting.web.WebServer;
import io.reactivex.Flowable;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class RxMain {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("You should add terms you are interested in as arguments");
        }

        long limit = -1;
        try {
            limit = Long.parseLong(System.getProperty("tweetsLimit"));
        } catch (NullPointerException | NumberFormatException e) {
            // ignore
        }

        int topN = 10;
        try {
            topN = Integer.parseInt(System.getProperty("topN"));
        } catch (NullPointerException | NumberFormatException e) {
            // ignore
        }

        int httpPort = 8080;
        try {
            httpPort = Integer.parseInt(System.getProperty("httpPort"));
        } catch (NullPointerException | NumberFormatException e) {
            // ignore
        }

        TwitterSourceStreamActor source = new TwitterSourceStreamActor(limit, Arrays.asList(args));
        Predicate<TweetText> hasUrl = TweetText::hasUrl;

        // top URLs flow
        CountOccurrencesFlowStreamActor countUrls = new CountOccurrencesFlowStreamActor();
        final TopNSinkStreamActor topNurl = new TopNSinkStreamActor(topN, "TopN for URLs");
        Flowable.fromPublisher(source).repeat()
                .subscribeOn(Schedulers.newThread()) // each step has a thread, to be like actor model, but no //
                .filter(hasUrl)
                .subscribeOn(Schedulers.newThread())
                .map(new DereferenceUrlsOfTweetText())
                .subscribeOn(Schedulers.newThread())
                .subscribe(countUrls);
        countUrls.subscribe(topNurl);

        // top words flow
        CountOccurrencesFlowStreamActor countWords = new CountOccurrencesFlowStreamActor();
        final TopNSinkStreamActor topNwords = new TopNSinkStreamActor(topN, "TopN for words");
        Flowable.fromPublisher(source).repeat()
                .subscribeOn(Schedulers.newThread())
                .filter(t -> !hasUrl.test(t))
                .subscribeOn(Schedulers.newThread())
                .map(t -> new TupleListTime(Arrays.stream(t.getText().replace("\n", " ").split(" ")).filter(s -> !s.isEmpty()).collect(Collectors.toList()), t.getTime()))
                .subscribeOn(Schedulers.newThread())
                .subscribe(countWords);
        countWords.subscribe(topNwords);

        // webserver
        final WebServer webServer = new WebServer(httpPort, new TopNController(topNwords, source.getGson()), new TopNController(topNurl, source.getGson()));
        System.out.println("Please use http://localhost:" + httpPort + "/top-words and http://localhost:" + httpPort + "/top-urls to get the Top " + topN + " of Words and URLs.");

        // launch the data processus
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
