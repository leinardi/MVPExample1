package com.leinardi.mvp.ui;

import com.leinardi.mvp.model.Repository;
import com.leinardi.mvp.ui.base.BasePresenter;

/**
 * Created by leinardi on 18/07/16.
 */

public interface RepositoryListPresenter extends BasePresenter {

    void loadStartingPage();

    void refreshRepositoryList();

    void loadMoreRepositories(int page);

    void onRepositoryLongClicked(Repository repository);
}
