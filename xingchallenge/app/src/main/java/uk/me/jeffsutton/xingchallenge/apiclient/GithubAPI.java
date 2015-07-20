package uk.me.jeffsutton.xingchallenge.apiclient;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by jeffsutton on 20/07/15.
 */
public class GithubAPI {

    public static final String API_BASE = "https://api.github.com";
    public static final String URL_GET_REPO_LIST = API_BASE + "/users/%1$s/repos";
private static final String LOG_TAG = GithubAPI.class.getSimpleName();

    public static Response getRepositoryList(String username) throws IOException {
        URL requestURL = new URL(String.format(URL_GET_REPO_LIST, username));
        HttpURLConnection connection = (HttpURLConnection) requestURL.openConnection();
       connection = configureConnection(connection);
        connection.connect();

        int responseCode = connection.getResponseCode();
        Log.d(LOG_TAG, "getRepositoryList() response code: " + responseCode);

        String response = getResponseString(connection.getInputStream());

        if (connection.getInputStream() != null) {
            connection.getInputStream().close();
        }

        connection.disconnect();

        Response responseData = new Response();
        responseData.responseCode = responseCode;
        responseData.data = response;

        return responseData;
    }


    public static HttpURLConnection configureConnection(HttpURLConnection connection) throws ProtocolException {
        connection.setRequestMethod("GET");
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(15000);
        connection.setInstanceFollowRedirects(true);
        connection.setDoInput(true);
        return connection;
    }

    /**
     * Take an input stream and return a String representing its contents
     *
     * @param stream - input stream from which to read
     * @return String - representation of the read contents
     * @throws IOException
     */
    public static String getResponseString(InputStream stream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line = null;
        while ((line = reader.readLine()) != null)
        {
            stringBuilder.append(line + "\n");
        }
        return stringBuilder.toString();
    }

    public static class Response {
        public int responseCode;
        public String data;
    }

}
