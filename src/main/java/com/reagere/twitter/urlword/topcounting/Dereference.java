package com.reagere.twitter.urlword.topcounting;

import io.reactivex.functions.Function;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.HashSet;
import java.util.Set;

public class Dereference implements Function<Tweet, Set<String>> {

    OkHttpClient client = new OkHttpClient();

    @Override
    public Set<String> apply(Tweet tweet) throws Exception {
        Set<String> dereferencedUrls = new HashSet<>();
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
        return dereferencedUrls;
    }
}
