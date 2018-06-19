# Top Counting Word and URLs of twitter stream

## Usage

```
java -DconsumerKey=... -DconsumerSecret=... -Dtoken=... -Dsecret=... -DtweetsLimit=-1 -DhttpPort=8080 -DtopN=10 -jar twitter-url-word-top-counting-1.0-SNAPSHOT.jar twitter-topic-1 ...
```

You need to defined the tweeter application keys as Java System properties. (-DconsumerKey=... -DconsumerSecret=... -Dtoken=... -Dsecret=...)

By default, the process is infinite, but you can limit the number of tweets analyzed with the system property : -DtweetsLimit=INT ]0..N[

The top N is 10, but you can override it with -DtopN=INT

The web server starts at the port 8080, and you can override it with -DhttpPort=INT

## Implementation

Java 8 has been used with ReactiveX framework.

## Console

Every 10 seconds, each branch prints the latency found and # of tweets.

## Runtime

I made a thread for each step so it is like actor model in the behavior.
Yet I have not added parallelism yet.