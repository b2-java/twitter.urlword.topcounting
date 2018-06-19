package com.reagere.twitter.urlword.topcounting.functions;

import com.reagere.twitter.urlword.topcounting.model.TupleListTime;
import com.reagere.twitter.urlword.topcounting.model.TweetText;
import io.reactivex.functions.Function;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.List;

public class DereferenceUrlsOfTweetText implements Function<TweetText, TupleListTime> {

    private final OkHttpClient client = new OkHttpClient();

    @Override
    public TupleListTime apply(TweetText tweet) {
        List<String> dereferencedUrls = new ArrayList<>();
        String text = tweet.getText();
        int debutIdx = text.indexOf("http");
        while (debutIdx >= 0) {
            int idx = text.indexOf(" ", debutIdx);
            String candidat = idx > debutIdx ? text.substring(debutIdx, idx) : text.substring(debutIdx);
            if (candidat.matches("http(s)?://.*")) {
                Request request = new Request.Builder()
                        .url(candidat)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        dereferencedUrls.add(response.request().url().toString());
                    }
                } catch (Exception e) {
                    // ignore url
                }
            }
            debutIdx = tweet.getText().indexOf("http", debutIdx + 4);
        }
        return new TupleListTime(dereferencedUrls, tweet.getTime());
    }
}
