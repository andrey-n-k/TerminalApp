package com.arellomobile.terminal.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.*;
import com.arellomobile.terminal.data.data.Passenger;
import com.arellomobile.terminal.data.data.commit.CommitHelperObject;
import com.arellomobile.terminal.data.manager.cache.StorageApi;
import com.arellomobile.terminal.data.manager.datamanager.DataManager;
import com.arellomobile.terminal.helper.application.TerminalApplication;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * User: AndreyKo
 * Date: 12.06.12
 * Time: 12:52
 */
public class CommitGeneratorHelper implements ServiceConnection
{
    private static final long sDefaultTime = 1111111111l;

    private Context mContext;

    private Messenger mMessenger = new Messenger(new IncomingHandler());
    private Messenger mService;

    private volatile Location mCurrentLocation;
	private volatile float mMinSpeed;
	private volatile float mMaxSpeed;

    public CommitGeneratorHelper(Context context)
    {
        mContext = context;

        bindToService();
    }

    private void bindToService()
    {
        mContext.bindService(new Intent(mContext, LocationService.class), this, Context.BIND_AUTO_CREATE);
    }

    public CommitHelperObject getCommit()
    {
        return getCommit(null, null);
    }

    public CommitHelperObject getCommit(String station)
    {
        return getCommit(station, null);
    }

    public CommitHelperObject getCommit(int routeId)
    {
        return getCommit(null, routeId);
    }

    public CommitHelperObject getCommit(Passenger passenger)
    {
        CommitHelperObject commitHelperObject = getCommit(null, null);

        JSONObject passengerJson = passenger.getJSON();

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(passengerJson);

        commitHelperObject.setCheckin(jsonArray.toString());

        return commitHelperObject;
    }

    private CommitHelperObject getCommit(String station, Integer routeID)
    {
        Location location = mCurrentLocation;
        if (null != location)
        {
            DataManager dataManager = ((TerminalApplication) mContext).getDataManager();
            StorageApi storageApi = ((TerminalApplication) mContext).getStorageApi();

            long vehicleId = dataManager.getVehicleId(mContext);
            long tripId = dataManager.getTripId(mContext);
            String pin = dataManager.getPin(mContext);
            String bookpack = storageApi.getBookPackTime();
            String langpack = storageApi.getLangPackTime();

            if (-1 == vehicleId || -1 == tripId)
            {
                return null;
            }
            if (bookpack == null)
            {
                bookpack = getDataStringValue(sDefaultTime);
            }
            if(langpack == null)
            {
                langpack = getDataStringValue(sDefaultTime);
            }

            return generateCommitObjectJSON(location, vehicleId, tripId, pin, bookpack, langpack, station, routeID);
        }
        return null;
    }

    private CommitHelperObject generateCommitObjectJSON(Location location, long vehicleId, long tripId, String pin,
                                                        String bookpack, String langpack, String station, Integer routeID)
    {
        CommitHelperObject commitObject = new CommitHelperObject();

        commitObject.setFleetId(vehicleId);
        commitObject.setTripId(tripId);
        commitObject.setPin(pin);

        commitObject.setTimeStamp(getDataStringValue(new Date().getTime())); // to seconds
        commitObject.setBookPackTimeStamp(bookpack);
        commitObject.setLangPackTimeStamp(langpack);

        if (null != routeID)
        {
            commitObject.setRoute(routeID);
        }

        try
        {
            commitObject.setTrackArray(new JSONArray().put(getTrackData(location, station)).toString());
        } catch (JSONException e)
        {
            // pass
        }

        return commitObject;
    }

    private JSONObject getTrackData(Location location, String station) throws JSONException
    {
        JSONObject trackObj = new JSONObject();

        trackObj.put("lat", String.valueOf(location.getLatitude()));
        trackObj.put("lng", String.valueOf(location.getLongitude()));
        trackObj.put("acc", String.valueOf((int) location.getAccuracy()));
        trackObj.put("spd_com", String.valueOf((int) location.getSpeed()));
        trackObj.put("stamp", getDataStringValue(location.getTime()));
	    trackObj.put("spd_min", String.valueOf(mMinSpeed));
	    trackObj.put("spd_max", String.valueOf(mMaxSpeed));
        if (null != station)
        {
            trackObj.put("station", station);
        }

        return trackObj;
    }

    public void onDestroy()
    {
        Message loginMessage = new Message();
        loginMessage.what = LocationService.REMOVE_LOCATION_LISTENER;
        loginMessage.replyTo = mMessenger;
        try
        {
            mService.send(loginMessage);
        } catch (RemoteException e)
        {
            // pass
        }
        mContext.unbindService(this);

        mContext = null;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service)
    {
        mService = new Messenger(service);
        Message loginMessage = new Message();
        loginMessage.what = LocationService.ADD_LOCATION_LISTENER;
        loginMessage.replyTo = mMessenger;
        try
        {
            mService.send(loginMessage);
        } catch (RemoteException e)
        {
            // pass
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName)
    {
        mService = null;
    }

    private class IncomingHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case LocationService.GET_LOCATION:
                    mCurrentLocation = (Location) msg.getData().getParcelable(String.valueOf(msg.what));
	                mMinSpeed = msg.getData().getFloat(LocationService.MIN_SPEED);
	                mMaxSpeed = msg.getData().getFloat(LocationService.MAX_SPEED);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private String getDataStringValue(Long time)
    {
        return String.valueOf(new Date(time).getTime() / 1000);
    }
}
