package com.arellomobile.terminal.helper.application;

import android.app.Application;
import android.content.res.Configuration;
import com.arellomobile.terminal.data.manager.Managers;
import com.arellomobile.terminal.data.manager.cache.StorageApi;
import com.arellomobile.terminal.data.manager.cache.TerminalStorageApi;
import com.arellomobile.terminal.data.manager.datamanager.DataManager;
import com.arellomobile.terminal.data.manager.datamanager.TerminalDataManager;
import com.arellomobile.terminal.data.manager.langservice.JsonFactory;
import com.arellomobile.terminal.data.manager.langservice.LangFactory;
import com.arellomobile.terminal.data.manager.service.ServerApi;
import com.arellomobile.terminal.data.manager.service.TerminalServerApiImpl;
import org.json.JSONException;

/**
 * Date: 07.03.12
 * Time: 11:40
 *
 * @author AndreyKo
 */
public class TerminalApplicationImpl extends Application implements TerminalApplication
{
    private Managers mManager;

    @Override
    public void onCreate()
    {
        super.onCreate();

        mManager = new Managers();

        TerminalStorageApi cache = new TerminalStorageApi(this);
        mManager.setCache(cache);
        mManager.setService(new TerminalServerApiImpl(cache));
        mManager.setDataManager(new TerminalDataManager(this));
        try
        {
            mManager.setLangFactory(new JsonFactory("{}", "en", this));
        }
        catch (JSONException e) {/*pass*/}
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public ServerApi getServerApi()
    {
        return mManager.getService();
    }

    @Override
    public StorageApi getStorageApi()
    {
        return mManager.getCache();
    }

    @Override
    public DataManager getDataManager()
    {
        return mManager.getDataManger();
    }

    @Override
    public LangFactory getLangFactory()
    {
        return mManager.getLangFactory();
    }
}

