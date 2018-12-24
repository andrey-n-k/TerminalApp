package com.arellomobile.terminal.tasks;

import android.content.Context;
import com.arellomobile.terminal.data.data.LangPack;
import com.arellomobile.terminal.tasks.callback.AsyncTaskCallBack;

import java.util.Collections;

/**
 * User: AndreyKo
 * Date: 12.05.12
 */
public class LoadLangPack extends BasicAsyncTask
{
    public interface LoadLangPackCallback extends AsyncTaskCallBack
    {
        public void setData(LangPack langPack);
    }

    private LoadLangPackCallback mCallback;
    private LangPack mLangPack;

    public LoadLangPack(Context context, LoadLangPackCallback callback)
    {
        super(context, callback);
        this.mCallback = callback;
    }

    @Override
    protected void doWork() throws Exception
    {
        mLangPack = mServerApi.getLangPack();
    }

    @Override
    protected void getFromStorage() throws Exception
    {
        mLangPack = mStorageApi.getLangPack();
    }

    @Override
    protected boolean isWorkNull()
    {
        return mLangPack == null;
    }

    @Override
    protected boolean isNoData()
    {
        return mLangPack == null;
    }

    @Override
    protected void storeWork() throws Exception
    {
        mStorageApi.saveLangPack(mLangPack);
    }

    @Override
    protected void publishWork()
    {
        if (mCallback != null)
        {
            mCallback.setData(mLangPack);
        }
    }
}