package com.reagere.twitter.urlword.topcounting.actors;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestCountWord {

    private TestActorRef<Actor> sut;
    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testIt() {
        /*
         * Wrap the whole test procedure within a testkit constructor
         * if you want to receive actor replies or use Within(), etc.
         */
        new TestKit(system) {
            {
                final Props props = Props.create(CountWord.class);
                final ActorRef subject = system.actorOf(props);

                // can also use JavaTestKit “from the outside”
                final TestKit probe = new TestKit(system);
                // “inject” the probe by passing it to the test subject
                // like a real resource would be passed in production
                subject.tell(probe.getRef(), getRef());
                // await the correct response
                expectMsg(java.time.Duration.ofSeconds(1), "done");

                for (int i = 1; i < 10; i++) {
                    for (String value : new String[] {"hello", "world"}) {
                        // the run() method needs to finish within 50 milliseconds
                        final int expectedCount = i;
                        within(java.time.Duration.ofMillis(50), () -> {
                            testCall(value, expectedCount, subject, probe);
                            return null;
                        });
                    }
                }
            }

            private void testCall(String value, int expected, ActorRef subject, TestKit probe) {

                subject.tell(value, getRef());

                // This is a demo: would normally use expectMsgEquals().
                // Wait time is bounded by 50-millisecond deadline above.
                awaitCond(probe::msgAvailable);

                // response must have been enqueued to us before probe
                expectMsg(java.time.Duration.ZERO, value + "=" + expected);

                // check that the probe we injected earlier got the msg
                probe.expectMsg(java.time.Duration.ZERO, value + "=" + expected);
                Assert.assertEquals(getRef(), probe.getLastSender());

                // Will wait for the rest of the 50 milli-seconds
                expectNoMsg();
            }
        };
    }
}
