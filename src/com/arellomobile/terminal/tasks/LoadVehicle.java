package com.arellomobile.terminal.tasks;

import android.content.Context;
import com.arellomobile.terminal.data.data.Vehicle;
import com.arellomobile.terminal.tasks.callback.LoadListCallBack;

import java.util.Collections;
import java.util.List;

/**
 * User: AndreyKo
 * Date: 22.05.12
 */
public class LoadVehicle extends BasicAsyncTask
{
    private List<Vehicle> vehicles;
    private LoadListCallBack<Vehicle> callback;

    public LoadVehicle(Context context, LoadListCallBack<Vehicle> callback)
    {
        super(context, callback);
        this.callback = callback;
    }

    @Override
    protected void doWork() throws Exception
    {
        vehicles = mServerApi.getVehicle(mDataManager.getCompanyId(mContext));
        Collections.sort(vehicles);
    }

    @Override
    protected void getFromStorage() throws Exception
    {
        vehicles = mStorageApi.getVehicles(mDataManager.getCompanyId(mContext));
        Collections.sort(vehicles);
    }

    @Override
    protected boolean isWorkNull()
    {
        return vehicles == null;
    }

    @Override
    protected boolean isNoData()
    {
        return vehicles == null || vehicles.size() == 0;
    }

    @Override
    protected void storeWork() throws Exception
    {
        mStorageApi.saveVehicles(vehicles);
    }

    @Override
    protected void publishWork()
    {
        if (callback != null)
        {
            callback.setData(vehicles);
        }
    }
}
