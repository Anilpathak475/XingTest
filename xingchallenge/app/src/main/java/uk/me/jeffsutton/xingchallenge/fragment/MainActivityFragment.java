package uk.me.jeffsutton.xingchallenge.fragment;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uk.me.jeffsutton.xingchallenge.apiclient.GithubAPI;
import uk.me.jeffsutton.xingchallenge.model.GithubRepos;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends ListFragment {

    protected static final ExecutorService workerThread = Executors.newSingleThreadExecutor();
    protected static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HHmmss.SSS'Z'").setPrettyPrinting().create();
private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    /**
     *  Get the list of repositories for the specified user (in this instance XING)
     */
    Runnable getRepositoryList = new Runnable() {
        @Override
        public void run() {
            try {
                GithubAPI.Response repoList = GithubAPI.getRepositoryList("xing");
                if (repoList.responseCode == 200) {
                    Log.i(LOG_TAG, repoList.data);
                    GithubRepos repositories = gson.fromJson(repoList.data, GithubRepos.class);
                } else {
                    // We need to handle an invalid response here
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public MainActivityFragment() {
    }

    /**
     * Attach to list view once the view hierarchy has been created.
     *
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        workerThread.submit(getRepositoryList);
    }
}
