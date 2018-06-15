package com.reagere.twitter.urlword.topcounting;

import com.reagere.twitter.urlword.topcounting.actors.CountOccurrencesFlowStreamActor;
import com.reagere.twitter.urlword.topcounting.functions.DereferenceUrlsOfTweetText;
import com.reagere.twitter.urlword.topcounting.actors.TopNSinkStreamActor;
import com.reagere.twitter.urlword.topcounting.actors.TwitterSourceStreamActor;
import com.reagere.twitter.urlword.topcounting.model.TweetText;
import com.reagere.twitter.urlword.topcounting.web.TopNController;
import com.reagere.twitter.urlword.topcounting.web.WebServer;
import io.reactivex.Flowable;
import io.reactivex.functions.Predicate;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class RxMain {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("You should add terms you are insterested in as arguments");
        }

        long limit = -1;
        try {
            limit = Long.parseLong(System.getProperty("tweetsLimit"));
        } catch (NullPointerException | NumberFormatException e) {
            // ignore
        }

        TwitterSourceStreamActor source = new TwitterSourceStreamActor(limit, Arrays.asList(args));
        Predicate<TweetText> hasUrl = TweetText::hasUrl;

        CountOccurrencesFlowStreamActor countUrls = new CountOccurrencesFlowStreamActor();
        final TopNSinkStreamActor topNurl = new TopNSinkStreamActor();
        Flowable.fromPublisher(source).repeat()
                .filter(hasUrl)
                .map(new DereferenceUrlsOfTweetText())
                .subscribe(countUrls);
        countUrls.subscribe(topNurl);

        CountOccurrencesFlowStreamActor countWords = new CountOccurrencesFlowStreamActor();
        final TopNSinkStreamActor topNwords = new TopNSinkStreamActor();
        Flowable.fromPublisher(source).repeat()
                .filter(t -> !hasUrl.test(t))
                .map(t -> Arrays.stream(t.getText().replace("\n", " ").split(" ")).filter(s -> !s.isEmpty()).collect(Collectors.toList()))
                .subscribe(countWords);
        countWords.subscribe(topNwords);

        // webserver
        final WebServer webServer = new WebServer(8080, new TopNController(topNwords, source.getGson()), new TopNController(topNurl, source.getGson()));
        System.out.println("Please use http://localhost:8080/top-words and http://localhost:8080/top-urls to get the Top 10 of Words and URLs.");

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
