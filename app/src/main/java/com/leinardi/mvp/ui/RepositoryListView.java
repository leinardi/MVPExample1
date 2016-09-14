package com.leinardi.mvp.ui;

import com.leinardi.mvp.model.Repository;
import com.leinardi.mvp.ui.base.BaseView;

/**
 * Created by leinardi on 18/07/16.
 */

public interface RepositoryListView extends BaseView {
    void startRefreshing();

    void stopRefreshing();

    void setStartingPage(int startingPage);

    void showOpenInBrowserDialog(Repository repository);
}
