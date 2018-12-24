package com.arellomobile.terminal.tasks;

import android.content.Context;
import com.arellomobile.terminal.data.data.Company;
import com.arellomobile.terminal.data.data.LangPack;
import com.arellomobile.terminal.data.data.Language;
import com.arellomobile.terminal.tasks.callback.AsyncTaskCallBack;

import java.util.Collections;
import java.util.List;

/**
 * User: AndreyKo
 * Date: 12.05.12
 */
public class LoadFirstInfo extends BasicAsyncTask
{
    public interface LoadFirstInfoCallback extends AsyncTaskCallBack
    {
        public void setData(List<Company> companies, List<Language> languages, LangPack langPack);
    }

    private LoadFirstInfoCallback mCallback;
    private List<Company> mCompanies;
    private List<Language> mLanguages;
    private LangPack mLangPack;

    public  LoadFirstInfo(Context context, LoadFirstInfoCallback callback)
    {
        super(context, callback);
        this.mCallback = callback;
    }

    @Override
    protected void doWork() throws Exception
    {
        mCompanies = mStorageApi.getCompanies();
        if (mCompanies == null || mCompanies.size() == 0)
        {
            mCompanies = mServerApi.getCompany();
            Collections.sort(mCompanies);
        }
        mLanguages = mStorageApi.getLanguages();
        if (mLanguages == null || mLanguages.size() == 0)
        {
            mLanguages = mServerApi.getLanguage();
            Collections.sort(mLanguages);
        }
        mLangPack = mServerApi.getLangPack();
    }

    @Override
    protected void getFromStorage() throws Exception
    {
        mCompanies = mStorageApi.getCompanies();
        Collections.sort(mCompanies);
        mLanguages = mStorageApi.getLanguages();
        Collections.sort(mLanguages);
        mLangPack = mStorageApi.getLangPack();
    }

    @Override
    protected boolean isWorkNull()
    {
        return mCompanies == null || mLanguages == null || mLangPack == null;
    }

    @Override
    protected boolean isNoData()
    {
        return mCompanies == null || mLanguages == null || mLangPack == null || mCompanies.size() == 0 || mLanguages.size() == 0;
    }

    @Override
    protected void storeWork() throws Exception
    {
        mStorageApi.saveCompanies(mCompanies);
        mStorageApi.saveLanguages(mLanguages);
        mStorageApi.saveLangPack(mLangPack);
    }

    @Override
    protected void publishWork()
    {
        if (mCallback != null)
        {
            mCallback.setData(mCompanies, mLanguages, mLangPack);
        }
    }
}