package com.arellomobile.terminal.tasks;

import android.content.Context;
import com.arellomobile.terminal.data.data.Trip;
import com.arellomobile.terminal.tasks.callback.LoadListCallBack;

import java.util.Collections;
import java.util.List;

/**
 * User: AndreyKo
 * Date: 25.05.12
 */
public class LoadTrip extends BasicAsyncTask
{
    private List<Trip> trips;
    private LoadListCallBack<Trip> callback;
    private long routeId;

    public LoadTrip(Context context, LoadListCallBack<Trip> callback, long routeId)
    {
        super(context, callback);
        this.callback = callback;
        this.routeId = routeId;
    }

    @Override
    protected void doWork() throws Exception
    {
        trips = mServerApi.getTrip(routeId);
        Collections.sort(trips);
    }

    @Override
    protected void getFromStorage() throws Exception
    {
        trips = mStorageApi.getTrips(routeId);
        Collections.sort(trips);
    }

    @Override
    protected boolean isWorkNull()
    {
        return trips == null;
    }

    @Override
    protected boolean isNoData()
    {
        return trips == null || trips.size() == 0;
    }

    @Override
    protected void storeWork() throws Exception
    {
        mStorageApi.saveTrips(trips);
    }

    @Override
    protected void publishWork()
    {
        if (callback != null)
        {
            callback.setData(trips);
        }
    }
}