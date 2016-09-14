package com.leinardi.mvp.ui;

import com.leinardi.mvp.ui.base.BaseInteractor;

/**
 * Created by leinardi on 18/07/16.
 */

public interface RepositoryListInteractor extends BaseInteractor {

    int getStartingPage();

    void setStartingPage(int startingPage);

    void deleteAllData();

    void loadRepositoryListPage(int page, final RepositoryListListener listener);

    boolean isRepositoryContentProviderEmpty();
}
