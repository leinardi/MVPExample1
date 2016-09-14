package com.leinardi.mvp.ui;

import com.leinardi.mvp.model.Repository;

import static com.leinardi.mvp.provider.RepositoryContract.ConfigEntry;

/**
 * Created by leinardi on 18/07/16.
 */

public class RepositoryListPresenterImpl implements RepositoryListPresenter, RepositoryListListener {

    private static final int DEFAULT_STARTING_PAGE = Integer.parseInt(ConfigEntry.DEFAULT_PAGE_VALUE);
    final private RepositoryListView mView;
    final private RepositoryListInteractor mInteractor;

    public RepositoryListPresenterImpl(RepositoryListView view) {
        this.mView = view;
        this.mInteractor = new RepositoryListInteractorImpl();
    }

    @Override
    public void loadStartingPage() {
        int startingPage = mInteractor.getStartingPage();
        mView.setStartingPage(startingPage);
        if (startingPage == DEFAULT_STARTING_PAGE && mInteractor.isRepositoryContentProviderEmpty()) {
            mView.startRefreshing();
            mInteractor.loadRepositoryListPage(DEFAULT_STARTING_PAGE, this);
        }
    }

    @Override
    public void refreshRepositoryList() {
        mView.startRefreshing();
        mInteractor.deleteAllData();
        mInteractor.loadRepositoryListPage(DEFAULT_STARTING_PAGE, this);
        mView.setStartingPage(DEFAULT_STARTING_PAGE);
    }

    @Override
    public void loadMoreRepositories(int page) {
        mView.startRefreshing();
        mInteractor.loadRepositoryListPage(page, this);
    }


    @Override
    public void onRepositoryLongClicked(Repository repository) {
        mView.showOpenInBrowserDialog(repository);
    }

    @Override
    public void cancel() {
        mInteractor.cancel();
    }

    @Override
    public void onSuccess() {
        mView.stopRefreshing();
    }

    @Override
    public void onFailure(String message) {
        mView.stopRefreshing();
        mView.showError(message);
    }
}
