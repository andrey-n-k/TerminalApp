package com.arellomobile.terminal.tasks;

import android.content.Context;
import com.arellomobile.terminal.data.data.Route;
import com.arellomobile.terminal.tasks.callback.LoadListCallBack;

import java.util.Collections;
import java.util.List;

/**
 * User: AndreyKo
 * Date: 25.05.12
 */
public class LoadRoute extends BasicAsyncTask
{
    private List<Route> routes;
    private LoadListCallBack<Route> callback;

    public LoadRoute(Context context, LoadListCallBack<Route> callback)
    {
        super(context, callback);
        this.callback = callback;
    }

    @Override
    protected void doWork() throws Exception
    {
        routes = mServerApi.getRoute(mDataManager.getCompanyId(mContext));
        Collections.sort(routes);
    }

    @Override
    protected void getFromStorage() throws Exception
    {
        routes = mStorageApi.getRoutes(mDataManager.getCompanyId(mContext));
        Collections.sort(routes);
    }

    @Override
    protected boolean isWorkNull()
    {
        return routes == null;
    }

    @Override
    protected boolean isNoData()
    {
        return routes == null || routes.size() == 0;
    }

    @Override
    protected void storeWork() throws Exception
    {
        mStorageApi.saveRoutes(routes);
    }

    @Override
    protected void publishWork()
    {
        if (callback != null)
        {
            callback.setData(routes);
        }
    }
}