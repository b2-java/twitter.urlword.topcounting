//package com.reagere.twitter.urlword.topcounting;
//
//import akka.NotUsed;
//import akka.actor.ActorSystem;
//import akka.japi.Pair;
//import akka.japi.function.Predicate;
//import akka.routing.ConsistentHashingPool;
//import akka.routing.ConsistentHashingRouter;
//import akka.stream.*;
//import akka.stream.javadsl.*;
//
//import java.util.Arrays;
//import java.util.concurrent.CompletionStage;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
//
//public class Main {
//
//    public static void main(String[] args) {
//        TwitterStreamActor twitterStream = new TwitterStreamActor();
//
//        Source<Tweet, NotUsed> twitterSource = Source.fromPublisher(twitterStream);
//
//        Flow<Tweet, Tweet, NotUsed> tweetsWithUrls = Flow.of(Tweet.class).filter(t -> t.hasUrl());
//        Flow<Tweet, Tweet, NotUsed> tweetsWithoutUrls = Flow.of(Tweet.class).filter(t -> !t.hasUrl());
//
//        Counter countWith = new Counter("with URL");
//        Counter countWithout = new Counter("without URL");
//        Flow<Tweet, Tweet, NotUsed> countWithFlow = Flow.of(Tweet.class).map(countWith);
//        Flow<Tweet, Tweet, NotUsed> countWithOutFlow = Flow.of(Tweet.class).map(countWithout);
//        Sink<Tweet, CompletionStage<Integer>> sink = Sink.fold(0, (aggr, next) -> {
//            if (next != null) {
//                int v = aggr + 1;
//                System.out.println("tweet #" + v + " arrived");
//                return v;
//            }
//            return aggr;
//        });
//
//        final RunnableGraph<CompletionStage<Integer>> runnableGraph = RunnableGraph.fromGraph(GraphDSL     // create() function binds sink, out which is sink's out port and builder DSL
//                                .create(   // we need to reference out's shape in the builder DSL below (in to() function)
//                                        sink,                // previously created sink (Sink)
//                                        (builder, out) -> {  // variables: builder (GraphDSL.Builder) and out (SinkShape)
//                                            final UniformFanOutShape<Tweet, Tweet> bcast = builder.add(Broadcast.create(2));
////                                            final UniformFanInShape<Tweet, Tweet> merge = builder.add(Merge.create(2));
////                                            final UniformFanInShape<Tweet, Tweet> merge = builder.add(Concat.create(2));
//                                            final Outlet<Tweet> source = builder.add(twitterSource).out();
//                                            builder.from(source)
//                                                    .viaFanOut(bcast)
//                                                    .via(builder.add(tweetsWithUrls))
//                                                    .via(builder.add(countWithFlow))
//                                                    .to(builder.add(Sink.ignore()));
////                                                    .viaFanIn(merge)
////                                                    .to(out);  // to() expects a SinkShape
//                                            builder.from(bcast)
//                                                    .via(builder.add(tweetsWithoutUrls))
//                                                    .via(builder.add(countWithOutFlow))
//                                                    .to(builder.add(Sink.ignore()));
////                                                    .toFanIn(merge);
//                                            return ClosedShape.getInstance();
//                                        }));
//
//        ActorSystem system = ActorSystem.create("TweetStream");
//        ActorMaterializer mat = ActorMaterializer.create(system);
//        final CompletionStage<Integer> totalSumStage = runnableGraph.run(mat);
//        twitterStream.run(i -> {
//            System.out.println("# tweets fetched : " + i);
//            try {
//                System.out.println("# tweets processed : " + totalSumStage.toCompletableFuture().get(10, TimeUnit.SECONDS));
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            } catch (TimeoutException e) {
//                e.printStackTrace();
//            }
//            System.out.println("with    URL : " + countWith.getCount());
//            System.out.println("without URL : " + countWithout.getCount());
//            mat.shutdown();
//            system.terminate();
//            return null;
//        });
//    }
//
//}
