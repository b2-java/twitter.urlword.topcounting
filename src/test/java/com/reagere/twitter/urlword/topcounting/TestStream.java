//package com.reagere.twitter.urlword.topcounting;
//
//import akka.NotUsed;
//import akka.actor.ActorRef;
//import akka.actor.ActorSystem;
//import akka.japi.Pair;
//import akka.stream.ActorMaterializer;
//import akka.stream.ClosedShape;
//import akka.stream.OverflowStrategy;
//import akka.stream.javadsl.*;
//import akka.stream.testkit.TestPublisher;
//import akka.stream.testkit.TestSubscriber;
//import akka.stream.testkit.javadsl.TestSink;
//import akka.stream.testkit.javadsl.TestSource;
//import akka.testkit.TestProbe;
//import akka.testkit.javadsl.TestKit;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import scala.concurrent.duration.FiniteDuration;
//
//import java.time.Duration;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.*;
//
//import static org.junit.Assert.assertEquals;
//
//public class TestStream {
//    private static ActorSystem system;
//    private static ActorMaterializer mat;
//
//    @BeforeClass
//    public static void setup() {
//        system = ActorSystem.create();
//        mat = ActorMaterializer.create(system);
//    }
//
//    @AfterClass
//    public static void teardown() {
//        mat.shutdown();
//        TestKit.shutdownActorSystem(system);
//        system = null;
//    }
//    @Test
//    public void testSink() throws InterruptedException, ExecutionException, TimeoutException {
//        final Sink<Integer, CompletionStage<Integer>> sinkUnderTest = Flow.of(Integer.class)
//                .map(i -> i * 2)
//                .toMat(Sink.fold(0, (agg, next) -> agg + next), Keep.right());
//
//        final CompletionStage<Integer> future = Source.from(Arrays.asList(1, 2, 3, 4))
//                .runWith(sinkUnderTest, mat);
//        final Integer result = future.toCompletableFuture().get(3, TimeUnit.SECONDS);
//        assert (result == 20);
//    }
//
//    @Test
//    public void testSource() throws InterruptedException, ExecutionException, TimeoutException {
//        final Source<Integer, NotUsed> sourceUnderTest = Source.repeat(1)
//                .map(i -> i * 2);
//
//        final CompletionStage<List<Integer>> future = sourceUnderTest
//                .take(10)
//                .runWith(Sink.seq(), mat);
//        final List<Integer> result = future.toCompletableFuture().get(3, TimeUnit.SECONDS);
//        assertEquals(result, Collections.nCopies(10, 2));
//    }
//
//    @Test
//    public void testFlow() throws InterruptedException, ExecutionException, TimeoutException {
//        final Flow<Integer, Integer, NotUsed> flowUnderTest = Flow.of(Integer.class)
//                .takeWhile(i -> i < 5);
//
//        final CompletionStage<Integer> future = Source.from(Arrays.asList(1, 2, 3, 4, 5, 6))
//                .via(flowUnderTest).runWith(Sink.fold(0, (agg, next) -> agg + next), mat);
//        final Integer result = future.toCompletableFuture().get(3, TimeUnit.SECONDS);
//        assert(result == 10);
//    }
//
//    @Test
//    public void testKit() {
//        final Source<List<Integer>, NotUsed> sourceUnderTest = Source
//                .from(Arrays.asList(1, 2, 3, 4))
//                .grouped(2);
//
//        final TestProbe probe = new TestProbe(system);
//        final CompletionStage<List<List<Integer>>> future = sourceUnderTest
//                .grouped(2)
//                .runWith(Sink.head(), mat);
//        akka.pattern.PatternsCS.pipe(future, system.dispatcher()).to(probe.ref());
//        probe.expectMsg(FiniteDuration.create(3, TimeUnit.SECONDS),
//                Arrays.asList(Arrays.asList(1, 2), Arrays.asList(3, 4))
//        );
//    }
//
//    @Test
//    public void testActorRef() throws InterruptedException, ExecutionException, TimeoutException {
//        final Sink<Integer, CompletionStage<String>> sinkUnderTest = Flow.of(Integer.class)
//                .map(i -> i.toString())
//                .toMat(Sink.fold("", (agg, next) -> agg + next), Keep.right());
//
//        final Pair<ActorRef, CompletionStage<String>> refAndCompletionStage =
//                Source.<Integer>actorRef(8, OverflowStrategy.fail())
//                        .toMat(sinkUnderTest, Keep.both())
//                        .run(mat);
//        final ActorRef ref = refAndCompletionStage.first();
//        final CompletionStage<String> future = refAndCompletionStage.second();
//
//        ref.tell(1, ActorRef.noSender());
//        ref.tell(2, ActorRef.noSender());
//        ref.tell(3, ActorRef.noSender());
//        ref.tell(new akka.actor.Status.Success("done"), ActorRef.noSender());
//
//        final String result = future.toCompletableFuture().get(1, TimeUnit.SECONDS);
//        assertEquals(result, "123");
//    }
//
//    @Test
//    public void testStreamsWithTestKit() {
//        final Source<Integer, NotUsed> sourceUnderTest = Source.from(Arrays.asList(1, 2, 3, 4))
//                .filter(elem -> elem % 2 == 0)
//                .map(elem -> elem * 2);
//
//        sourceUnderTest
//                .runWith(TestSink.probe(system), mat)
//                .request(2)
//                .expectNext(4, 8);
//    }
//
//    @Test
//    public void testCancel() {
//        final Sink<Integer, NotUsed> sinkUnderTest = Sink.cancelled();
//
//        TestSource.<Integer>probe(system)
//                .toMat(sinkUnderTest, Keep.left())
//                .run(mat)
//                .expectCancellation();
//    }
//
//    @Test
//    public void testInjection() throws TimeoutException, InterruptedException {
//        final Sink<Integer, CompletionStage<Integer>> sinkUnderTest = Sink.head();
//
//        final Pair<TestPublisher.Probe<Integer>, CompletionStage<Integer>> probeAndCompletionStage =
//                TestSource.<Integer>probe(system)
//                        .toMat(sinkUnderTest, Keep.both())
//                        .run(mat);
//        final TestPublisher.Probe<Integer> probe = probeAndCompletionStage.first();
//        final CompletionStage<Integer> future = probeAndCompletionStage.second();
//        probe.sendError(new Exception("boom"));
//
//        try {
//            future.toCompletableFuture().get(3, TimeUnit.SECONDS);
//            assert false;
//        } catch (ExecutionException ee) {
//            final Throwable exception = ee.getCause();
//            assertEquals(exception.getMessage(), "boom");
//        }
//    }
//
//    @Test
//    public void testCompleteStream() {
//        final Flow<Integer, Integer, NotUsed> flowUnderTest = Flow.of(Integer.class)
//                .mapAsyncUnordered(2, sleep -> akka.pattern.PatternsCS.after(
//                        Duration.ofMillis(10),
//                        system.scheduler(),
//                        system.dispatcher(),
//                        CompletableFuture.completedFuture(sleep)
//                ));
//
//        final Pair<TestPublisher.Probe<Integer>, TestSubscriber.Probe<Integer>> pubAndSub =
//                TestSource.<Integer>probe(system)
//                        .via(flowUnderTest)
//                        .toMat(TestSink.probe(system), Keep.both())
//                        .run(mat);
//        final TestPublisher.Probe<Integer> pub = pubAndSub.first();
//        final TestSubscriber.Probe<Integer> sub = pubAndSub.second();
//
//        sub.request(3);
//        pub.sendNext(3);
//        pub.sendNext(2);
//        pub.sendNext(1);
//        sub.expectNextUnordered(1, 2, 3);
//
//        pub.sendError(new Exception("Power surge in the linear subroutine C-47!"));
//        final Throwable ex = sub.expectError();
//        assert(ex.getMessage().contains("C-47"));
//    }
//
////    @Test
////    public void testCompleteStreamMultipleSink() {
////        final Sink<Integer, CompletionStage<Integer>> sinkUnderTest = Sink.head();
////        final RunnableGraph<CompletionStage<Integer>> result = RunnableGraph.fromGraph(GraphDSL.create(sinkUnderTest, (builder, out) -> {
////            ClosedShape.getInstance();
////        }));
////        final Flow<Integer, Integer, NotUsed> flowUnderTest = Flow.of(Integer.class)
////                .mapAsyncUnordered(2, sleep -> akka.pattern.PatternsCS.after(
////                        Duration.ofMillis(10),
////                        system.scheduler(),
////                        system.dispatcher(),
////                        CompletableFuture.completedFuture(sleep)
////                ));
////
////        final Pair<TestPublisher.Probe<Integer>, TestSubscriber.Probe<Integer>> pubAndSub =
////                TestSource.<Integer>probe(system)
////                        .via(flowUnderTest)
////                        .toMat(TestSink.probe(system), Keep.both())
////                        .run(mat);
////        final TestPublisher.Probe<Integer> pub = pubAndSub.first();
////        final TestSubscriber.Probe<Integer> sub = pubAndSub.second();
////
////        sub.request(3);
////        pub.sendNext(3);
////        pub.sendNext(2);
////        pub.sendNext(1);
////        sub.expectNextUnordered(1, 2, 3);
////
////        pub.sendError(new Exception("Power surge in the linear subroutine C-47!"));
////        final Throwable ex = sub.expectError();
////        assert(ex.getMessage().contains("C-47"));
////    }
//}
