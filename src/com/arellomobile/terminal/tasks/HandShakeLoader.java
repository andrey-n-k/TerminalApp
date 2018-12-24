package com.arellomobile.terminal.tasks;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import com.arellomobile.terminal.data.data.*;
import com.arellomobile.terminal.service.CommitService;
import com.arellomobile.terminal.service.LocationService;
import com.arellomobile.terminal.tasks.callback.LoadObjectCallBack;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * User: AndreyKo
 * Date: 29.05.12
 */
public class HandShakeLoader extends BasicAsyncTask
{
    private LoadObjectCallBack<TripInfo> callback;
    private volatile TripInfo mTripInfo;
    private String md5;
    private long carId;
    private long routeId;
    private long tripId;

    private Vehicle mVehicle;
    private Route mRoute;
    private Trip mTrip;

    public HandShakeLoader(Context context, LoadObjectCallBack<TripInfo> mCallBack, String md5, long carId,
                           long routeId,
                           long tripId)
    {
        super(context, mCallBack);
        this.callback = mCallBack;
        this.md5 = md5;
        this.carId = carId;
        this.routeId = routeId;
        this.tripId = tripId;
    }

    @Override
    protected void doWork() throws Exception
    {
        mVehicle = mStorageApi.getVehicleById(carId);
        mRoute = mStorageApi.getRouteById(routeId);
        mTrip = mStorageApi.getTripById(tripId);

//        String pin = mDataManager.getPin(mContext);
        mTripInfo = mServerApi.getHandShakeResponse(mDataManager, carId, tripId, md5);
        mTrip.setStations(mTripInfo.getStations());
        setStations();
    }

    @Override
    protected void getFromStorage() throws Exception
    {
        boolean correctPin = mStorageApi.checkPin(md5);

        if (correctPin)
        {
            final String commitString = mDataManager.getCommit();
            mTripInfo = new TripInfo();
            mStorageApi.saveTripInfo(mTripInfo, tripId);

            mContext.bindService(new Intent(mContext, CommitService.class), new ServiceConnection()
            {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder service)
                {
                    Messenger messenger = new Messenger(service);
                    Message msg = Message.obtain(null, CommitService.ADD_HANDSHAKE_COMMIT);
                    Bundle bundle = new Bundle();
                    bundle.putString(String.valueOf(CommitService.ADD_HANDSHAKE_COMMIT), commitString);
                    msg.setData(bundle);
                    try
                    {
                        messenger.send(msg);
                    } catch (RemoteException e)
                    {
                        Logger.getLogger(getClass().getName()).severe("error while sending message");
                    }
                    mContext.unbindService(this);
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName)
                {
                }
            }, Context.BIND_AUTO_CREATE);

            setStations();
        }
    }

    private void setStations()
    {
        mContext.bindService(new Intent(mContext, LocationService.class), new ServiceConnection()
        {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service)
            {
                Messenger messenger = new Messenger(service);
                Message msg = Message.obtain(null, LocationService.SET_STATIONS);
                Bundle bundle = new Bundle();
                bundle.putSerializable(String.valueOf(LocationService.SET_STATIONS),
                                       new ArrayList<Station>(mTrip.getStations()));
                msg.setData(bundle);
                try
                {
                    messenger.send(msg);
                } catch (RemoteException e)
                {
                    Logger.getLogger(getClass().getName()).severe("error while sending message");
                }
                mContext.unbindService(this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName)
            {
            }
        }, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected boolean isWorkNull()
    {
        return mTripInfo == null;
    }

    @Override
    protected boolean isNoData()
    {
        return mTripInfo == null;
    }

    @Override
    protected void storeWork() throws Exception
    {
        if (!mStorageApi.checkPin(md5))
        {
            mStorageApi.addPin(md5);
        }
        mStorageApi.saveTripInfo(mTripInfo, tripId);
    }

    @Override
    protected void publishWork()
    {
        if (callback != null)
        {
            callback.setData(mTripInfo, mVehicle, mRoute, mTrip);
        }
    }
}
