package com.arellomobile.terminal.tasks;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.*;
import com.arellomobile.android.libs.network.utils.ServerApiException;
import com.arellomobile.terminal.data.data.NearestStation;
import com.arellomobile.terminal.data.data.Route;
import com.arellomobile.terminal.data.data.TripInfo;
import com.arellomobile.terminal.service.LocationService;
import com.arellomobile.terminal.tasks.callback.CheckCurrentStationCallback;

import java.util.List;

/**
 * User: AndreyKo
 * Date: 20.06.12
 * Time: 20:13
 */
public class CheckCurrentStationsTask extends BasicAsyncTask implements ServiceConnection
{
    private CheckCurrentStationCallback mCallback;

    private final Object mSyncObj = new Object();

    private volatile Location mLocation;
    private volatile Exception mIsError;

    private Messenger mMessenger = new Messenger(new Handler()
    {
        @Override
        public void handleMessage(android.os.Message msg)
        {
            switch (msg.what)
            {
                case LocationService.GET_LOCATION:
                    synchronized (mSyncObj)
                    {
                        mLocation = (Location) msg.getData().getParcelable(String.valueOf(msg.what));
                        mSyncObj.notifyAll();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    });
    private List<NearestStation> mStations;

    public CheckCurrentStationsTask(Context context, CheckCurrentStationCallback callBack)
    {
        super(context, callBack);

        mCallback = callBack;

        mContext.bindService(new Intent(mContext, LocationService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void doWork() throws Exception
    {
        synchronized (mSyncObj)
        {
            long tripId = mDataManager.getTripId(mContext);
            long routeId = mDataManager.getRouteId(mContext);
            if (-1 == tripId || -1 == routeId)
            {
                mContext.unbindService(this);
                throw new ServerApiException();
            }
            TripInfo tripInfo = mStorageApi.getTripInfo(tripId);
            if (null == tripInfo || tripInfo.getStations() == null || tripInfo.getStations().size() == 0)
            {
                mContext.unbindService(this);
                throw new ServerApiException();
            }
            Route route = mStorageApi.getRouteById(routeId);
            if (mIsError != null || null == route)
            {
                mContext.unbindService(this);
                throw new ServerApiException();
            }
            if (null == mLocation)
            {
                mSyncObj.wait();
            }
            if (null == mLocation)
            {
                mContext.unbindService(this);
                throw new ServerApiException();
            }
            mContext.unbindService(this);
            mStations = mServerApi
                    .getCurrentStations(route.getType(), mLocation.getLatitude(), mLocation.getLongitude());
        }
    }

    @Override
    protected void getFromStorage() throws Exception
    {
    }

    @Override
    protected boolean isWorkNull()
    {
        return mStations == null;
    }

    @Override
    protected boolean isNoData()
    {
        return isWorkNull();
    }

    @Override
    protected void storeWork() throws Exception
    {
    }

    @Override
    protected void publishWork()
    {
        mCallback.setCurrentStations(mStations);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder)
    {
        Messenger serviceMessenger = new Messenger(iBinder);

        Message message = new Message();
        message.what = LocationService.GET_LOCATION;
        message.replyTo = mMessenger;

        try
        {
            serviceMessenger.send(message);
        } catch (RemoteException e)
        {
            synchronized (mSyncObj)
            {
                mIsError = e;
                mSyncObj.notifyAll();
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName)
    {
        synchronized (mSyncObj)
        {
            mIsError = new Exception();
            mSyncObj.notifyAll();
        }
        // pass
    }
}
