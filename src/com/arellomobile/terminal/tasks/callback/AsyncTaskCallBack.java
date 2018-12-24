package com.arellomobile.terminal.tasks.callback;

import com.arellomobile.android.libs.network.NetworkException;
import com.arellomobile.android.libs.network.utils.ServerApiException;
import com.arellomobile.android.libs.network.utils.ServerErrorException;

/**
 * User: AndreyKo
 * Date: 12.05.12
 */
public interface AsyncTaskCallBack
{
    void showProgress();

    void hideProgress();

    void onServerSideError(String errorMessage);

    void onServerError(String errorMessage);

    void onNetworkError();

    void onUnknownError();
}
