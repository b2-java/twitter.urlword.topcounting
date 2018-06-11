package com.reagere.twitter.urlword.topcounting;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.function.Predicate;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.*;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class Main {
    private void firstExample() {

        final Source<Integer, NotUsed> source = Source.from(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
        // note that the Future is scala.concurrent.Future
        final Sink<Integer, CompletionStage<Integer>> sink = Sink.fold(0, (aggr, next) -> aggr + next);

        // connect the Source to the Sink, obtaining a RunnableFlow
        final RunnableGraph<CompletionStage<Integer>> runnable = source.toMat(sink, Keep.right());

        // materialize the flow
        ActorSystem system = ActorSystem.create("ExampleSystem");

        // created from `system`:
        ActorMaterializer mat = ActorMaterializer.create(system);
        final CompletionStage<Integer> sum = runnable.run(mat);
        try {
            System.out.println(sum.toCompletableFuture().get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Source<Tweet, NotUsed> twitterSource = Source.fromPublisher(new TwitterStreamActor());
        Predicate<Tweet> hasNotUrlPredicate = t -> t.hasUrl();
        Source<Tweet, NotUsed> tweetWithUrls = twitterSource.filter(hasNotUrlPredicate).limit(100);
        Source<Tweet, NotUsed> tweetWithoutUrls = twitterSource.filter(hasNotUrlPredicate).limit(100);
        Sink<Tweet, CompletionStage<Integer>> count = Sink.fold(0, (aggr, next) -> aggr + 1);
        final RunnableGraph<CompletionStage<Integer>> runnableWithoutUrls = tweetWithoutUrls.toMat(count, Keep.right());
        final RunnableGraph<CompletionStage<Integer>> runnableWithUrls = tweetWithUrls.toMat(count, Keep.right());

        ActorSystem system = ActorSystem.create("TweetStream");
        ActorMaterializer mat = ActorMaterializer.create(system);
        run(mat, runnableWithUrls, "with");
        run(mat, runnableWithoutUrls, "without");
    }

    private static void run(ActorMaterializer mat, RunnableGraph<CompletionStage<Integer>> runnable, String prefix) {
        final CompletionStage<Integer> sum = runnable.run(mat);
        try {
            System.out.println(prefix + " " + sum.toCompletableFuture().get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
