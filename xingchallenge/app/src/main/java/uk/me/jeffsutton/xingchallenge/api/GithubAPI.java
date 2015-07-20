package uk.me.jeffsutton.xingchallenge.api;

import android.content.Context;

import java.net.URL;

/**
 * Created by jeffsutton on 20/07/15.
 */
public class GithubAPI {

    public static final String API_BASE = "https://api.github.com";
    public static final String URL_GET_REPO_LIST = API_BASE + "/users/%1$s/repos";

    public static String getRepositoryList(Context context, String username) {
        URL requestURL = new URL(String.format(URL_GET_REPO_LIST, username));
    }

}
