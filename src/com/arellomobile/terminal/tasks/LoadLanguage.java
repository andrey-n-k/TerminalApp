package com.arellomobile.terminal.tasks;

import android.content.Context;
import com.arellomobile.terminal.data.data.Language;
import com.arellomobile.terminal.data.data.Route;
import com.arellomobile.terminal.tasks.callback.LoadListCallBack;

import java.util.Collections;
import java.util.List;

/**
 * User: AndreyKo
 * Date: 25.05.12
 */
public class LoadLanguage extends BasicAsyncTask
{
    private List<Language> mLanguage;
    private LoadListCallBack<Language> mCallback;

    public LoadLanguage(Context context, LoadListCallBack<Language> callback)
    {
        super(context, callback);
        this.mCallback = callback;
    }

    @Override
    protected void doWork() throws Exception
    {
        mLanguage = mServerApi.getLanguage();
        Collections.sort(mLanguage);
    }

    @Override
    protected void getFromStorage() throws Exception
    {
        mLanguage = mStorageApi.getLanguages();
        Collections.sort(mLanguage);
    }

    @Override
    protected boolean isWorkNull()
    {
        return mLanguage == null;
    }

    @Override
    protected boolean isNoData()
    {
        return mLanguage == null || mLanguage.size() == 0;
    }

    @Override
    protected void storeWork() throws Exception
    {
        mStorageApi.saveLanguages(mLanguage);
    }

    @Override
    protected void publishWork()
    {
        if (mCallback != null)
        {
            mCallback.setData(mLanguage);
        }
    }
}