package com.reagere.twitter.urlword.topcounting;

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
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TwitterStreamActor implements Publisher<Tweet>, Runnable {

    private final Client hosebirdClient;
    private final BlockingQueue<String> msgQueue;
    private final Set<Subscriber<? super Tweet>> subscribers = new HashSet<>();
    private volatile boolean running = false;

    public TwitterStreamActor() {
        /** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
        msgQueue = new LinkedBlockingQueue<>(100000);
        BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>(1000);

        /** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
        // Optional: set up some followings and track terms
        //List<Long> followings = Lists.newArrayList(1234L, 566788L);
        //List<String> terms = Lists.newArrayList("apple");
        //hosebirdEndpoint.followings(followings);
        //hosebirdEndpoint.trackTerms(terms);

        // These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1("consumerKey", "consumerSecret", "token", "secret");

        ClientBuilder builder = new ClientBuilder()
                .name("twitter.urlword.topcounting")                     // optional: mainly for the logs
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue))
                .eventMessageQueue(eventQueue);                          // optional: use this if you want to process client events

        hosebirdClient = builder.build();
        // Attempts to establish a connection.
        hosebirdClient.connect();
        new Thread(() -> eventQueue.stream().forEach(System.out::println)).start();
    }

    public void run() {
        System.out.println("twitter thread started");
        int i = 0;
        while (!hosebirdClient.isDone() && i < 6) {
            try {
                String msg = msgQueue.poll(10, TimeUnit.SECONDS);
                if (msg != null) {
                    System.out.println(msg);
                    Tweet tweet = new Tweet(msg);
                    subscribers.forEach(s -> s.onNext(tweet));
                } else {
                    System.err.println("no tweet yet");
                }
                i++;
            } catch (InterruptedException e) {
                System.err.println("Twitter client interrupted : " + e.getLocalizedMessage());
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("twitter thread done");
        hosebirdClient.stop();
    }

    @Override
    public void subscribe(Subscriber<? super Tweet> subscriber) {
        System.out.println("get one subscriber");
        subscribers.add(subscriber);
        if (!running) {
            new Thread(this).start();
        }
    }
}
