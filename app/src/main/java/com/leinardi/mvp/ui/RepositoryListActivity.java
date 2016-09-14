package com.leinardi.mvp.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.leinardi.mvp.R;
import com.leinardi.mvp.model.Repository;
import com.leinardi.mvp.provider.RepositoryContract;
import com.leinardi.mvp.ui.adapter.RepositoryRecyclerViewAdapter;
import com.leinardi.mvp.ui.base.BaseActivity;
import com.leinardi.mvp.ui.dialog.OpenInBrowserAlertDialogFragment;

public class RepositoryListActivity extends BaseActivity implements RepositoryListView {
    private static final String TAG = RepositoryListActivity.class.getSimpleName();
    // Defines the id of the loader for later reference
    public static final int REPOSITORY_LOADER_ID = 82; // From docs: A unique identifier for this loader. Can be whatever you want.

    private RecyclerView mRecyclerView;

    private Toolbar mToolbar;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RepositoryRecyclerViewAdapter mAdapter;

    private RepositoryListPresenter mPresenter;
    private LinearLayoutManager mLinearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repository_list);
        mPresenter = new RepositoryListPresenterImpl(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.item_list);
        mToolbar = ((Toolbar) findViewById(R.id.toolbar));
        mSwipeRefreshLayout = ((SwipeRefreshLayout) findViewById(R.id.swipe_layout));

        setSupportActionBar(mToolbar);
        mToolbar.setTitle(getTitle());

        setupRecyclerView();
    }

    @Override
    protected void onDestroy() {
        mPresenter.cancel();
        super.onDestroy();
    }

    private RepositoryRecyclerViewAdapter.OnItemLongClickListener itemClickListener = new RepositoryRecyclerViewAdapter.OnItemLongClickListener() {
        @Override
        public void onItemLongClick(Repository repository) {
            mPresenter.onRepositoryLongClicked(repository);
        }
    };

    private void setupRecyclerView() {
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent, R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.refreshRepositoryList();
            }
        });

        mAdapter = new RepositoryRecyclerViewAdapter(null, itemClickListener);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.abc_list_divider_mtrl_alpha)));

        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mPresenter.loadStartingPage();

        // Initialize the loader with a special ID and the defined callbacks from above
        getSupportLoaderManager().initLoader(REPOSITORY_LOADER_ID,
                new Bundle(), repositoryLoader);
    }

    // Defines the asynchronous callback for the repository data loader
    private LoaderManager.LoaderCallbacks<Cursor> repositoryLoader =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                // Create and return the actual cursor loader for the repository data
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    // Construct the loader
                    return new CursorLoader(RepositoryListActivity.this,
                            RepositoryContract.RepositoryEntry.CONTENT_URI, // URI
                            null, // projection fields
                            null, // the selection criteria
                            null, // the selection args
                            null // the sort order
                    );
                }

                // When the system finishes retrieving the Cursor through the CursorLoader,
                // a call to the onLoadFinished() method takes place.
                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                    // The swapCursor() method assigns the new Cursor to the mAdapter
                    mAdapter.swapCursor(cursor);
                }

                // This method is triggered when the loader is being reset
                // and the loader data is no longer available. Called if the data
                // in the provider changes and the Cursor becomes stale.
                @Override
                public void onLoaderReset(Loader<Cursor> loader) {
                    // Clear the Cursor we were using with another call to the swapCursor()
                    mAdapter.swapCursor(null);
                }
            };

    @Override
    public void startRefreshing() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void stopRefreshing() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void setStartingPage(int startingPage) {
        mRecyclerView.clearOnScrollListeners();
        mRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(mLinearLayoutManager, startingPage) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                mPresenter.loadMoreRepositories(page);
            }
        });
    }

    @Override
    public void showOpenInBrowserDialog(Repository repository) {
        OpenInBrowserAlertDialogFragment openInBrowserAlertDialogFragment =
                OpenInBrowserAlertDialogFragment.newInstance(
                        repository.getHtmlUrl(),
                        repository.getOwner().getHtmlUrl()
                );
        openInBrowserAlertDialogFragment.show(getSupportFragmentManager(), OpenInBrowserAlertDialogFragment.TAG);
    }
}
