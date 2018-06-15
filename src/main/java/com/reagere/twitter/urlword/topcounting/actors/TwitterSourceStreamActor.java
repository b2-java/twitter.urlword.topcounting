package com.reagere.twitter.urlword.topcounting.actors;

import com.google.gson.Gson;
import com.reagere.twitter.urlword.topcounting.model.TweetText;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class TwitterSourceStreamActor implements Publisher<TweetText> {

    private final Client hosebirdClient;
    private final BlockingQueue<String> msgQueue;
    private final Set<Subscriber<? super TweetText>> subscribers = new HashSet<>();
    private final Gson gson = new Gson();
    private final long limit;

    /**
     * You need to provide these JVM System properties: consumerKey, consumerSecret, token, secret from your tweeter dev/app account.
     * @param limitNbTweets
     */
    public TwitterSourceStreamActor(long limitNbTweets, List<String> terms) {
        this.limit = limitNbTweets;

        // code from official tweeter client

        /** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
        msgQueue = new LinkedBlockingQueue<>(100000);
        BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>(1000);

        /** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
        hosebirdEndpoint.trackTerms(terms);
        // Optional: set up some followings and track terms
        //List<Long> followings = Lists.newArrayList(1234L, 566788L);
        //hosebirdEndpoint.followings(followings);

        // These secrets should be read from a config file
        String consumerKey = System.getProperty("consumerKey");
        String consumerSecret = System.getProperty("consumerSecret");
        String token = System.getProperty("token");
        String secret = System.getProperty("secret");
//        System.out.println("twitter OAuth1 > consumerKey: " + consumerKey + ", consumerSecret: " + consumerSecret + ", token: " + token + ", secret: " + secret);
        validateProperties(consumerKey, consumerSecret, token, secret);
        Authentication hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, secret);

        ClientBuilder builder = new ClientBuilder()
                .name("twitter.urlword.topcounting")
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue))
                .eventMessageQueue(eventQueue);

        hosebirdClient = builder.build();
        // Attempts to establish a connection.
        hosebirdClient.connect();
        new Thread(() -> eventQueue.forEach(System.out::println)).start(); // display events if any, none found in real
    }

    private void validateProperties(String consumerKey, String consumerSecret, String token, String secret) {
        if (consumerKey == null || consumerSecret == null || token == null || secret == null) {
            throw new IllegalStateException("You need to provide these JVM System properties: consumerKey, consumerSecret, token, secret from your tweeter dev/app account.");
        }
    }

    public void run(Function<Long, Void> calledWhenDone) {
        long count = 0;
        long start = System.currentTimeMillis();
        while (!hosebirdClient.isDone() && (limit <= 0 || count < limit)) {
            try {
                String msg = msgQueue.poll(10, TimeUnit.SECONDS);
                if (msg != null) {
                    TweetText t = getTweet(msg);
                    if (t != null) {
                        subscribers.stream().filter(Objects::nonNull).forEach(s -> s.onNext(t));
                        count++;
                    }
                }
            } catch (InterruptedException e) {
                System.err.println("Twitter client interrupted : " + e.getLocalizedMessage());
                Thread.currentThread().interrupt();
            }
//            try {
//                Stream<String> tweetsFlow = msgQueue.stream().filter(t -> t != null);
//                int diff = limit - count.get();
//                if (diff > 0) {
//                    tweetsFlow.limit(diff);
//                }
//                tweetsFlow.forEach(msg -> {
//                    subscribers.stream().filter(s -> s != null).forEach(s -> s.onNext(getTweet(msg)));
//                    count.incrementAndGet();
//                });
//            } catch (IllegalStateException e) {
//                // not ready
//            }
            if (count % 100 == 0) {
                System.out.println(count + " tweets fetched so far in " + (System.currentTimeMillis() - start) / 1000 + "s");
            }
        }
        hosebirdClient.stop();
        calledWhenDone.apply(count);
    }

    private TweetText getTweet(String msg) {
        return gson.fromJson(msg, TweetText.class);
    }

    @Override
    public void subscribe(Subscriber<? super TweetText> subscriber) {
        subscribers.add(subscriber);
    }

    public Gson getGson() {
        return gson;
    }
}
