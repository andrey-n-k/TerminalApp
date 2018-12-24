package com.arellomobile.terminal.data.manager;

import com.arellomobile.terminal.data.manager.cache.StorageApi;
import com.arellomobile.terminal.data.manager.datamanager.DataManager;
import com.arellomobile.terminal.data.manager.langservice.LangFactory;
import com.arellomobile.terminal.data.manager.service.ServerApi;

public class Managers
{
    private StorageApi mStorageApiInstance;
    private ServerApi mServerApiInstance;
    private DataManager mDataManager;
    private LangFactory mLangFactory;

    public void setCache(StorageApi storageApi)
    {
        mStorageApiInstance = storageApi;
    }

    public void setService(ServerApi serverApi)
    {
        mServerApiInstance = serverApi;
    }

    public void setDataManager(DataManager dataManager)
    {
        mDataManager = dataManager;
    }

    public void setLangFactory(LangFactory langFactory)
    {
        mLangFactory = langFactory;
    }

    public StorageApi getCache()
    {
        return mStorageApiInstance;
    }

    public ServerApi getService()
    {
        return mServerApiInstance;
    }

    public DataManager getDataManger()
    {
        return mDataManager;
    }

    public LangFactory getLangFactory()
    {
        return mLangFactory;
    }
}
