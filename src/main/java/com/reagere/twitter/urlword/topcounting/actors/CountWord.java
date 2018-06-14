//package com.reagere.twitter.urlword.topcounting.actors;
//
//import akka.actor.AbstractActor;
//import akka.actor.ActorRef;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class CountWord extends AbstractActor {
//
//    private ActorRef target = null;
//    private Map<String, Integer> counts = new HashMap<>();
//
//    @Override
//    public Receive createReceive() {
//        return receiveBuilder()
//                .match(String.class, word -> {
//                    String result = word + "=" + counts.compute(word, (k, v) -> {
//                        if (v == null) v = 0;
//                        return v + 1;
//                    });
//                    getSender().tell(result, getSelf());
//                    if (target != null) target.forward(result, getContext());
//                })
//                .match(ActorRef.class, actorRef -> {
//                    target = actorRef;
//                    getSender().tell("done", getSelf());
//                })
//                .build();
//    }
//}
