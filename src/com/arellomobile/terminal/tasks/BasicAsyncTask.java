package com.arellomobile.terminal.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.arellomobile.android.libs.network.NetworkException;
import com.arellomobile.android.libs.network.utils.ServerApiException;
import com.arellomobile.android.libs.network.utils.ServerErrorException;
import com.arellomobile.terminal.data.manager.cache.StorageApi;
import com.arellomobile.terminal.data.manager.datamanager.DataManager;
import com.arellomobile.terminal.data.manager.service.ServerApi;
import com.arellomobile.terminal.helper.application.TerminalApplication;
import com.arellomobile.terminal.tasks.callback.AsyncTaskCallBack;

import java.io.Serializable;

/**
 * User: AndreyKo
 * Date: 12.05.12
 */
public abstract class BasicAsyncTask extends AsyncTask<Void, Void, Void>
{
    private final AsyncTaskCallBack mCallBack;

    private Exception mError;

    protected final Context mContext;
    protected final ServerApi mServerApi;
    protected final StorageApi mStorageApi;
    protected final DataManager mDataManager;

    public BasicAsyncTask(Context context, AsyncTaskCallBack mCallBack)
    {
        this.mCallBack = mCallBack;
        mContext = context;
        TerminalApplication terminalApplication = (TerminalApplication) context.getApplicationContext();
        mServerApi = terminalApplication.getServerApi();
        mStorageApi = terminalApplication.getStorageApi();
        mDataManager = terminalApplication.getDataManager();
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();

        if (null != mCallBack)
        {
            mCallBack.showProgress();
        }
    }

    @Override
    protected Void doInBackground(Void... voids)
    {
        try
        {
            doWork();
            if (!isWorkNull())
            {
                // if work is not null, store it
                try
                {
                    storeWork();
                }
                catch (Exception e)
                {
                    Log.d(getClass().getName(), "storeWork", e);
                }
            }
        }
        catch (Exception e)
        {
            Log.d(getClass().getName(), "doWork", e);
            try
            {
                // if error, try to use local data
                getFromStorage();
            }
            catch (Exception e2)
            {
                Log.d(getClass().getName(), "getFromStorage", e2);
            }
            if (isNoData())
            {
                // remember about the error, if fail to load
                mError = e;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        super.onPostExecute(aVoid);

        if (null != mCallBack)
        {
            mCallBack.hideProgress();
        }

        if (mError != null)
        {
            if (mCallBack != null)
            {
                // if error
                if (mError instanceof ServerErrorException)
                {
                    mCallBack.onServerSideError(mError.getMessage());
                }
                else if (mError instanceof ServerApiException)
                {
                    mCallBack.onServerError(mError.getMessage());
                }
                else if (mError instanceof NetworkException)
                {
                    mCallBack.onNetworkError();
                }
                else
                {
                    mCallBack.onUnknownError();
                }
            }
        }
        else
        {
            if (isWorkNull())
            {
                if (mCallBack != null)
                {
                    // if no data laded
                    mCallBack.onUnknownError();
                }
            }
            else
            {
                // on success
                publishWork();
            }
        }
    }

    /**
     * Do some work on this method.  It calls NOT in UI thread
     *
     * @throws Exception on any error
     */
    protected abstract void doWork() throws Exception;

    /**
     * Load data from storage
     *
     * @throws Exception on any error
     */
    protected abstract void getFromStorage() throws Exception;

    /**
     * Check network work
     *
     * @return - true is work data is null, false else
     */
    protected abstract boolean isWorkNull();

    /**
     * Check storage work
     *
     * @return - true is work data is null, false else
     */
    protected abstract boolean isNoData();

    /**
     * Storage work. It calls NOT in UI thread
     *
     * @throws Exception on any error
     */
    protected abstract void storeWork() throws Exception;

    /**
     * Calls on success
     */
    protected abstract void publishWork();
}