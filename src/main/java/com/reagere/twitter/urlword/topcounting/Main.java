package com.reagere.twitter.urlword.topcounting;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.japi.function.Predicate;
import akka.stream.*;
import akka.stream.javadsl.*;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
        TwitterStreamActor twitterStream = new TwitterStreamActor();
        Source<Tweet, NotUsed> twitterSource = Source.fromPublisher(twitterStream);

        Flow<Tweet, Tweet, NotUsed> tweetsWithUrls = Flow.of(Tweet.class).map(t -> t.hasUrl() ? t : null);
        Flow<Tweet, Tweet, NotUsed> tweetsWithoutUrls = Flow.of(Tweet.class).map(t -> !t.hasUrl() ? t : null);

        Counter countWith = new Counter("with URL");
        Counter countWithout = new Counter("without URL");
        Flow<Tweet, Tweet, NotUsed> countWithFlow = Flow.of(Tweet.class).map(countWith);
        Flow<Tweet, Tweet, NotUsed> countWithOutFlow = Flow.of(Tweet.class).map(countWithout);
        Sink<Tweet, CompletionStage<Integer>> sink = Sink.fold(0, (aggr, next) -> {
            if (next != null) {
                int v = aggr + 1;
                System.out.println("tweet #" + v + " arrived");
                return v;
            }
            return aggr;
        });

        final RunnableGraph<CompletionStage<Integer>> runnableGraph = RunnableGraph.fromGraph(GraphDSL     // create() function binds sink, out which is sink's out port and builder DSL
                                .create(   // we need to reference out's shape in the builder DSL below (in to() function)
                                        sink,                // previously created sink (Sink)
                                        (builder, out) -> {  // variables: builder (GraphDSL.Builder) and out (SinkShape)
                                            final UniformFanOutShape<Tweet, Tweet> bcast = builder.add(Broadcast.create(2));
                                            final UniformFanInShape<Tweet, Tweet> merge = builder.add(Merge.create(2));
//                                            final UniformFanInShape<Tweet, Tweet> concat = builder.add(Concat.create(2));
                                            final Outlet<Tweet> source = builder.add(twitterSource).out();
                                            builder.from(source)
                                                    .viaFanOut(bcast)
                                                    .via(builder.add(tweetsWithUrls))
                                                    .via(builder.add(countWithFlow))
                                                    .viaFanIn(merge)
                                                    .to(out);  // to() expects a SinkShape
                                            builder.from(bcast)
                                                    .via(builder.add(tweetsWithoutUrls))
                                                    .via(builder.add(countWithOutFlow))
                                                    .toFanIn(merge);
                                            return ClosedShape.getInstance();
                                        }));

        ActorSystem system = ActorSystem.create("TweetStream");
        ActorMaterializer mat = ActorMaterializer.create(system);
        final CompletionStage<Integer> totalSumStage = runnableGraph.run(mat);
        twitterStream.run(i -> {
            System.out.println("# tweets fetched : " + i);
            try {
                System.out.println("# tweets processed : " + totalSumStage.toCompletableFuture().get(10, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            System.out.println("with    URL : " + countWith.getCount());
            System.out.println("without URL : " + countWithout.getCount());
            mat.shutdown();
            system.terminate();
            return null;
        });
    }

}
