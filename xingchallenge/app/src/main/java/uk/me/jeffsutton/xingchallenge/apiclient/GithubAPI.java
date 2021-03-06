package uk.me.jeffsutton.xingchallenge.apiclient;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by jeffsutton on 20/07/15.
 */
public class GithubAPI {

    /**
     * URL Base for all api requests
     */
    private static final String API_BASE = "https://api.github.com";
    /**
     * URL Path for list of user repositories
     */
    private static final String URL_GET_REPO_LIST = API_BASE + "/users/%1$s/repos?type=%2$s&page=%3$d&per_page=%4$d&access_token=%5$s";

    /**
     * Access token for requests.  In a real app we would want to hide this - perhaps as a byte[]
     * or using string encryption from DexGuard
     */
    private static final String ACCESS_TOKEN = "";

    /**
     * Modifier for getRepositoryList().  Request all user repositories.
     */
    public static final String REPO_TYPE_ALL = "all";
    /**
     * Modifier for getRepositoryList().  Request public user repositories.
     */
    public static final String REPO_TYPE_PUBLIC = "public";
    /**
     * Modifier for getRepositoryList().  Request private user repositories.
     */
    public static final String REPO_TYPE_PRIVATE = "private";
    private static final String LOG_TAG = GithubAPI.class.getSimpleName();

    /**
     * Get the list of available repositories for a given user
     *
     * @param username - name of the user we want to get repositories for
     * @param type - modifier to filter returned repositories REPO_TYPE_ALL, REPO_TYPE_PUBLIC, REPO_TYPE_PRIVATE
     * @param page - page or results to request
     * @param itemCount - number of items to return in response
     * @return Response - the API response
     * @throws IOException
     */
    public static Response getRepositoryList(String username, String type, int page, int itemCount) throws IOException {
        URL requestURL = new URL(String.format(URL_GET_REPO_LIST, username, type, page, itemCount, ACCESS_TOKEN));
        HttpURLConnection connection = (HttpURLConnection) requestURL.openConnection();
        connection = configureConnection(connection);
        connection.connect();

        int responseCode = connection.getResponseCode();
        Map<String, List<String>> headers = connection.getHeaderFields();
        String response = getResponseString(connection.getInputStream());

        if (connection.getInputStream() != null) {
            connection.getInputStream().close();
        }
        connection.disconnect();

        Response responseData = new Response();
        responseData.responseCode = responseCode;
        responseData.headers = headers;
        responseData.data = response;

//        logResponse(headers);

        return responseData;
    }


    /**
     * generic connection configuration.  Handy if we deal with more than one API endpoint
     * @param connection
     * @return
     * @throws ProtocolException
     */
    private static HttpURLConnection configureConnection(HttpURLConnection connection) throws ProtocolException {
        connection.setRequestMethod("GET");
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(15000);
        connection.setInstanceFollowRedirects(true);
        connection.setDoInput(true);
        return connection;
    }

    /**
     * Log HTTP response headers, handy for debugging
     * @param headers
     */
    public static void logResponse(Map<String, List<String>> headers) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String h = entry.getKey() + " :\t " + entry.getValue();
            Log.d("Response Header", h);
        }
    }

    /**
     * Take an input stream and return a String representing its contents
     *
     * @param stream - input stream from which to read
     * @return String - representation of the read contents
     * @throws IOException
     */
    private static String getResponseString(InputStream stream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = reader.readLine()) != null)
        {
            stringBuilder.append(line).append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     * Class to hold API responses, containing response code, response headers, and data as a String
     */
    public static class Response {
        public int responseCode;
        public Map<String, List<String>> headers;
        public String data;
    }

}
