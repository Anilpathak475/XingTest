package uk.me.jeffsutton.xingchallenge.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import uk.me.jeffsutton.xingchallenge.R;
import uk.me.jeffsutton.xingchallenge.model.GithubRepo;
import uk.me.jeffsutton.xingchallenge.model.GithubRepos;

/**
 * Created by jeffsutton on 20/07/15.
 */
public class RepositoryListAdapter extends BaseAdapter {

    private static final String LOG_TAG = RepositoryListAdapter.class.getSimpleName();

    GithubRepos data;
    Context mContext;
    LayoutInflater mInflater;

    public RepositoryListAdapter(Context context, GithubRepos repositoryList) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
        this.data = repositoryList;
    }

    /**
     * Append additional data to the end of the list backing this adapter
     * @param repositoryList - new data to append
     */
    public void appendItems(GithubRepos repositoryList) {
        if (data != null && data.repositories != null) {
            this.data.repositories.addAll(repositoryList.repositories);
            this.notifyDataSetChanged();
        }
    }


    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        if (data == null || data.repositories == null) {
            return 0;
        } else {
            return data.repositories.size();
        }
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        if (data == null || data.repositories == null || getCount() < position) {
            return null;
        } else {
            return data.repositories.get(position);
        }
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        if (data == null || data.repositories == null || getCount() < position) {
            return 0;
        } else {
            return ((GithubRepo) getItem(position)).id;
        }
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        RepoListViewHolder viewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_repository, parent, false);
            viewHolder = new RepoListViewHolder();
            viewHolder.repoName = (TextView) convertView.findViewById(R.id.repo_name);
            viewHolder.repoOwner = (TextView) convertView.findViewById(R.id.repo_owner);
            viewHolder.repoDescription = (TextView) convertView.findViewById(R.id.repo_description);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (RepoListViewHolder) convertView.getTag();
        }

        GithubRepo repo = (GithubRepo) getItem(position);

        if (repo != null) {
            viewHolder.repoName.setText(repo.name);
            viewHolder.repoOwner.setText(repo.owner.login);
            viewHolder.repoDescription.setText(repo.description);

            if (repo.fork) {
                convertView.setBackgroundColor(mContext.getResources().getColor(R.color.background_fork));
            } else {
                convertView.setBackgroundColor(mContext.getResources().getColor(R.color.background_normal));
            }
        }

        return convertView;
    }
}
