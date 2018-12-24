package com.arellomobile.terminal.tasks;

import android.content.Context;
import com.arellomobile.terminal.data.data.Company;
import com.arellomobile.terminal.data.data.Language;
import com.arellomobile.terminal.tasks.callback.LoadListCallBack;

import java.util.Collections;
import java.util.List;

/**
 * User: AndreyKo
 * Date: 25.05.12
 */
public class LoadCompany extends BasicAsyncTask
{
    private List<Company> mCompany;
    private LoadListCallBack<Company> mCallback;

    public LoadCompany(Context context, LoadListCallBack<Company> callback)
    {
        super(context, callback);
        this.mCallback = callback;
    }

    @Override
    protected void doWork() throws Exception
    {
        mCompany = mServerApi.getCompany();
        Collections.sort(mCompany);
    }

    @Override
    protected void getFromStorage() throws Exception
    {
        mCompany = mStorageApi.getCompanies();
        Collections.sort(mCompany);
    }

    @Override
    protected boolean isWorkNull()
    {
        return mCompany == null;
    }

    @Override
    protected boolean isNoData()
    {
        return mCompany == null || mCompany.size() == 0;
    }

    @Override
    protected void storeWork() throws Exception
    {
        mStorageApi.saveCompanies(mCompany);
    }

    @Override
    protected void publishWork()
    {
        if (mCallback != null)
        {
            mCallback.setData(mCompany);
        }
    }
}