package uk.me.jeffsutton.xingchallenge.fragment;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uk.me.jeffsutton.xingchallenge.R;
import uk.me.jeffsutton.xingchallenge.adapter.RepositoryListAdapter;
import uk.me.jeffsutton.xingchallenge.apiclient.GithubAPI;
import uk.me.jeffsutton.xingchallenge.model.GithubRepo;
import uk.me.jeffsutton.xingchallenge.model.GithubRepos;


/**
 * Simple ListFragment to contain list of public repositories
 */
public class MainActivityFragment extends ListFragment implements AdapterView.OnItemLongClickListener {

    protected static final ExecutorService workerThread = Executors.newSingleThreadExecutor();
    protected static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HHmmss.SSS'Z'").setPrettyPrinting().create();
    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private static final int FETCH_ITEM_COUNT = 10;

    private GithubRepos repositories = new GithubRepos();
    private boolean continueToFetch = true;
    private int pagePosition = 0;

    /**
     *  Get the list of repositories for the specified user (in this instance XING).
     *  <p/>
     *  We want to do this in the background to prevent locking-up the UI Thread.
     */
    Runnable getRepositoryList = new Runnable() {
        @Override
        public void run() {
            try {
                GithubAPI.Response repoList = GithubAPI.getRepositoryList("xing", GithubAPI.REPO_TYPE_PUBLIC, pagePosition, FETCH_ITEM_COUNT);
                if (repoList.responseCode == 200) {
                    // Response code: 200 - everything went OK
                    Log.i(LOG_TAG, repoList.data);

                    Type targetClassType = new TypeToken<ArrayList<GithubRepo>>() { }.getType();
                    repositories.repositories = gson.fromJson(repoList.data,targetClassType);

                    final RepositoryListAdapter adapter = new RepositoryListAdapter(getActivity(), repositories);

                    // We can't change the Ui from here, so run this on the UI thread.
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(LOG_TAG, "Setting repository list adapter");
                            setListAdapter(adapter);
                            setListShown(true);
                        }
                    });

                } else {
                    // We need to handle an invalid response here
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public MainActivityFragment() {
    }

    /**
     * Basic utility method to open the web browser to a specific URL
     * @param context
     * @param url
     */
    public static void openBrowser(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
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
        getListView().setOnItemLongClickListener(this);
    }

    /**
     * Callback method to be invoked when an item in this view has been
     * clicked and held.
     * <p/>
     * Implementers can call getItemAtPosition(position) if they need to access
     * the data associated with the selected item.
     *
     * @param parent   The AbsListView where the click happened
     * @param view     The view within the AbsListView that was clicked
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     * @return true if the callback consumed the long click, false otherwise
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final GithubRepo repository = (GithubRepo) parent.getAdapter().getItem(position);
        if (repository == null) {
            return false;
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(repository.full_name);
            builder.setMessage(getActivity().getString(R.string.dialog_message, repository.name, repository.owner.login));
            builder.setPositiveButton(getActivity().getString(R.string.repository), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    openBrowser(getActivity(), repository.html_url);
                }
            });
            builder.setNeutralButton(getActivity().getString(R.string.owner), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    openBrowser(getActivity(), repository.owner.html_url);
                }
            });
            builder.setNegativeButton(getActivity().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
            return true;
        }
    }
}
