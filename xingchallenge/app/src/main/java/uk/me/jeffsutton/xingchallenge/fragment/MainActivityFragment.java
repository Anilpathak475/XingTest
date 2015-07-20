package uk.me.jeffsutton.xingchallenge.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uk.me.jeffsutton.xingchallenge.R;
import uk.me.jeffsutton.xingchallenge.adapter.RepositoryListAdapter;
import uk.me.jeffsutton.xingchallenge.apiclient.GithubAPI;
import uk.me.jeffsutton.xingchallenge.model.GithubRepo;
import uk.me.jeffsutton.xingchallenge.model.GithubRepos;
import uk.me.jeffsutton.xingchallenge.util.Utils;


/**
 * Simple ListFragment to contain list of public repositories
 * <p/>
 * API requests are performed on an ExecutorService to prevent UI thread blocking.
 */
public class MainActivityFragment extends ListFragment implements AdapterView.OnItemLongClickListener,
        AbsListView.OnScrollListener {

    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    /**
     * ExecutorService to handle background API requests
     */
    private static final ExecutorService workerThread = Executors.newSingleThreadExecutor();

    /**
     * Gson instance which also handles Dates as strings in ATOM_TIME format.
     */
    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HHmmss.SSS'Z'")
            .setPrettyPrinting().create();

    /**
     * Number of items to fetch per API request
     */
    private static final int FETCH_ITEM_COUNT = 10;

    /**
     * Runnable to show retry dialog in the event that there is no network connection
     */
    private final Runnable noConnectionRunnable = new Runnable() {
        @Override
        public void run() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getActivity().getString(R.string.network_error));
            builder.setMessage(getActivity().getString(R.string.network_error_message));
            builder.setPositiveButton(getActivity().getString(R.string.retry), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    workerThread.submit(getRepositoryList);
                }
            });

            builder.setNegativeButton(getActivity().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    };

    /**
     * ListAdapter containing repositories
     */
    private RepositoryListAdapter adapter;
    /**
     * Footer view to indicate data is being loaded via the API
     */
    private View loadingFooter;
    /**
     * Flag to indicate if there may still be items to fetch from the API
     */
    private boolean continueToFetch = true;
    /**
     * Current paging position in the GitHub API
     */
    private int pagePosition = 1;
    /**
     * Flag to indicate if we are currently loading data
     */
    private boolean isLoading = false;
    /**
     * Get the list of repositories for the specified user (in this instance XING).
     * <p/>
     * We want to do this in the background to prevent locking-up the UI Thread.
     * <p/>
     * When data is loaded we append it to the list adapter.
     */
    private final Runnable getRepositoryList = new Runnable() {
        @Override
        public void run() {
            if (!Utils.isConnected(getActivity())) {
                Log.i(LOG_TAG, "No network found");
                getActivity().runOnUiThread(noConnectionRunnable);
            } else {
                try {
                    isLoading = true;
                    GithubAPI.Response repoList = GithubAPI.getRepositoryList("xing",
                            GithubAPI.REPO_TYPE_PUBLIC, pagePosition, FETCH_ITEM_COUNT);
                    if (repoList.responseCode == 200) {
                        processRepositoriesAPIResponse(repoList.data);
                    } else if (repoList.responseCode == 403) {
                        processRateLimitedAPIResponse(repoList.headers);
                    } else {
                        showToastMessage(R.string.unknown_error, Toast.LENGTH_SHORT);
                    }
                } catch (UnknownHostException e) {
                    showToastMessage(R.string.unknown_host, Toast.LENGTH_SHORT);
                } catch (Exception e) {
                    showToastMessage(R.string.unknown_error, Toast.LENGTH_SHORT);
                    e.printStackTrace();
                }
            }
            isLoading = false;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getListView().setOnScrollListener(MainActivityFragment.this);
                    setEmptyText(getActivity().getString(R.string.no_data));
                    setListShown(true);
                }
            });
        }
    };
    /**
     * Position of the last visible item in the list view. Used to calculate if we need to start loading more data.
     */
    private int lastVisiblePosition;


    /**
     * Show a Toast popup.  Ensure this is running on the UI Thread
     * @param message - String resource to show
     * @param duration - Toast duration
     */
    private void showToastMessage(final int message, final int duration) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), getString(message), duration).show();
            }
        });
    }

    /**
     * Process an API rate Limited failure response.
     * <p/>
     * We could do something useful here - for simplicity just show a Toast message.
     * @param headers
     */
    private void processRateLimitedAPIResponse(Map<String, List<String>> headers) {
        if (headers.containsKey("X-RateLimit-Remaining") &&
                headers.get("X-RateLimit-Remaining").get(0).equalsIgnoreCase("0")) {
            showToastMessage(R.string.rate_limited, Toast.LENGTH_SHORT);
        }
    }

    /**
     * Take a successful API response and process data to add to the list
     * @param response
     */
    private void processRepositoriesAPIResponse(String response) {
        final GithubRepos apiData = new GithubRepos();

        Type targetClassType = new TypeToken<ArrayList<GithubRepo>>() {}.getType();
        apiData.repositories = gson.fromJson(response, targetClassType);

        if (apiData.repositories.size() == FETCH_ITEM_COUNT) {
            pagePosition++;
        } else {
            continueToFetch = false;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (apiData.repositories.size() > 0) {
                    adapter.appendItems(apiData);
                }
                if (getListAdapter() == null || getListView().getVisibility() != View.VISIBLE) {
                    setListAdapter(adapter);
                    if (adapter.getCount() > 0)
                        setListShown(true);
                }
                getListView().removeFooterView(loadingFooter);
            }
        });
    }

    public MainActivityFragment() {
    }

    /**
     * Basic utility method to open the web browser to a specific URL
     *
     * @param context
     * @param url
     */
    private static void openBrowser(Context context, String url) {
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
        adapter = new RepositoryListAdapter(getActivity(), new GithubRepos());
        setEmptyText(getActivity().getString(R.string.first_load));
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

    /**
     * Callback method to be invoked while the list view or grid view is being scrolled. If the
     * view is being scrolled, this method will be called before the next frame of the scroll is
     * rendered. In particular, it will be called before any calls to
     * {Adapter#getView(int, View, ViewGroup)}.
     *
     * @param view        The view whose scroll state is being reported
     * @param scrollState The current scroll state. One of
     *                    {@link #SCROLL_STATE_TOUCH_SCROLL} or {@link #SCROLL_STATE_IDLE}.
     */
    @SuppressLint("InflateParams")
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE && lastVisiblePosition >= (view.getCount() - 5)) {
            if (!isLoading && continueToFetch) {
                isLoading = true;
                if (loadingFooter == null) {
                    loadingFooter = LayoutInflater.from(getActivity()).inflate(R.layout.list_loading_footer, null, false);
                }
                getListView().addFooterView(loadingFooter);
                if (lastVisiblePosition == view.getCount() - 1) {
                    getListView().scrollBy(0, 60);
                }
                getListView().setOnScrollListener(null);
                workerThread.submit(getRepositoryList);
            }
        }
    }

    /**
     * Callback method to be invoked when the list or grid has been scrolled. This will be
     * called after the scroll has completed
     *
     * @param view             The view whose scroll state is being reported
     * @param firstVisibleItem the index of the first visible cell (ignore if
     *                         visibleItemCount == 0)
     * @param visibleItemCount the number of visible cells
     * @param totalItemCount   the number of items in the list adaptor
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.lastVisiblePosition = firstVisibleItem + visibleItemCount;
    }
}
