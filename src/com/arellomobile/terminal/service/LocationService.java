package com.arellomobile.terminal.service;

import android.app.AlarmManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.*;
import android.widget.Toast;
import com.arellomobile.terminal.data.data.Station;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: AndreyKo
 * Date: 30.05.12
 */
public class LocationService extends Service implements LocationListener
{
    public static final int SET_STATIONS = 1;

    public static final int GET_CURRENT_STATION = 2;
    public static final int GET_LOCATION = 3;
    public static final int GET_CURRENT_AND_NEXT_STATION = 6;

    public static final int ADD_LOCATION_LISTENER = 4;
    public static final int REMOVE_LOCATION_LISTENER = 5;


    public static final int ADD_CURRENT_AND_NEXT_STATION_LISTENER = 7;
    public static final int REMOVE_CURRENT_AND_NEXT_STATION_LISTENER = 8;

    public static final String GET_CURRENT_AND_NEXT_STATION_INDEX_CURRENT = "current";
    public static final String GET_CURRENT_AND_NEXT_STATION_INDEX_NEXT = "next";

	public static final String MIN_SPEED = "min_speed";
	public static final String MAX_SPEED = "max_speed";

    // The maximum time that should pass before the user gets a location update.
    public static long MAX_TIME = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

    private static final double sStationRadius = 1000;

    private LocationManager locationManager;
    protected Logger log = Logger.getLogger(getClass().getName());

    private final Object mStationMutex = new Object();

    private volatile Location mCurrentLocation;   
	private volatile Location mLastLocation;

	private float mMinSpeed;
	private float mMaxSpeed;

    private volatile List<Station> mStations;
    private List<Messenger> mLocationCallBacks;
    private volatile int mCurrentStation;
    private volatile int mNextStation;
    private final List<Messenger> mCurrentAndNextStationListeners = new ArrayList<Messenger>();

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case SET_STATIONS:
                    synchronized (mStationMutex)
                    {
                        mStations = (List<Station>) msg.getData().getSerializable(String.valueOf(msg.what));
                        if (mStations == null || mStations.size() == 0)
                        {
                            mStations = null;
                            return;
                        }
                        mCurrentStation = 0;
                        mNextStation = -1;
                        updateStations(mCurrentLocation);
                    }
                    break;
//                case GET_CURRENT_STATION:
//                    synchronized (mStationMutex)
//                    {
//                        if (mStations != null)
//                        {
//                            Message message1 = new Message();
//                            message1.what = msg.what;
//                            Bundle bundle = new Bundle();
//
//                            int stationPosition = -1;
//                            Location location = mCurrentLocation;
//
//                            for (int i = mCurrentStation; i < mStations.size(); ++i)
//                            {
//                                Station station = mStations.get(i);
//                                double minValue = getDistanceBetweenLocationAndStation(location, station);
//                                if (minValue < sStationRadius)
//                                {
//                                    stationPosition = i;
//                                }
//                            }
//                            Station station = null;
//                            if (-1 != stationPosition)
//                            {
//                                station = mStations.get(stationPosition);
//                            }
//
//                            bundle.putSerializable(String.valueOf(msg.what), station);
//                            message1.setData(bundle);
//                            try
//                            {
//                                msg.replyTo.send(message1);
//                            } catch (RemoteException e)
//                            {
//                                // pass
//                            }
//                        }
//                    }
//                    break;
                case GET_CURRENT_AND_NEXT_STATION:
                    synchronized (mStationMutex)
                    {
                        if (mStations != null)
                        {
                            Message message1 = getCurrentAndNextMassage();
                            try
                            {
                                msg.replyTo.send(message1);
                            } catch (RemoteException e)
                            {
                                // pass
                            }
                        }
                    }
                    break;
                case ADD_CURRENT_AND_NEXT_STATION_LISTENER:
                    synchronized (mStationMutex)
                    {
                        mCurrentAndNextStationListeners.add(msg.replyTo);
                        if (mStations != null)
                        {
                            updateStations(mCurrentLocation);
                        }
                    }
                    break;
                case REMOVE_CURRENT_AND_NEXT_STATION_LISTENER:
                    synchronized (mStationMutex)
                    {
                        mCurrentAndNextStationListeners.remove(msg.replyTo);
                    }
                    break;
                case GET_LOCATION:
                    Message message = getLocationMessage();
                    try
                    {
                        msg.replyTo.send(message);
                    } catch (Exception e)
                    {
                        // pass
                    }
                    break;
                case ADD_LOCATION_LISTENER:
                    mLocationCallBacks.add(msg.replyTo);
                    break;
                case REMOVE_LOCATION_LISTENER:
                    mLocationCallBacks.remove(msg.replyTo);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private Message getCurrentAndNextMassage()
    {
        Message message = new Message();
        message.what = GET_CURRENT_AND_NEXT_STATION;
        Bundle bundle = new Bundle();
        bundle.putSerializable(GET_CURRENT_AND_NEXT_STATION_INDEX_CURRENT, mStations.get(mCurrentStation));
        if (mNextStation != -1)
        {
            bundle.putSerializable(GET_CURRENT_AND_NEXT_STATION_INDEX_NEXT, mStations.get(mNextStation));
        }
        message.setData(bundle);
        return message;
    }

    private Message getLocationMessage()
    {
        Message message = new Message();
        message.what = GET_LOCATION;
        Bundle bundle = new Bundle();
        bundle.putParcelable(String.valueOf(GET_LOCATION), mCurrentLocation);
	    bundle.putFloat(MIN_SPEED, mMinSpeed);
	    bundle.putFloat(MAX_SPEED, mMaxSpeed);
        message.setData(bundle);
        return message;
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public IBinder onBind(Intent intent)
    {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate()
    {
        log.info("Create service");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationCallBacks = Collections.synchronizedList(new ArrayList<Messenger>());

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

        mCurrentLocation = getLastBestLocation(System.currentTimeMillis() - MAX_TIME);

	    mMaxSpeed = 0;
	    mMinSpeed = 0;
    }

    @Override
    public void onDestroy()
    {
        log.info("Destroy service");
        super.onDestroy();
        locationManager.removeUpdates(this);
    }

    public Location getLastBestLocation(long minTime)
    {
        Location bestResult = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MAX_VALUE;

        // Iterate through all the providers on the system, keeping
        // note of the most accurate result within the acceptable time limit.
        // If no result is found within maxTime, return the newest Location.
        List<String> matchingProviders = locationManager.getAllProviders();
        for (String provider : matchingProviders)
        {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null)
            {
                float accuracy = location.getAccuracy();
                long time = location.getTime();

                if ((time < minTime && accuracy < bestAccuracy))
                {
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                }
                else if (time > minTime && bestAccuracy == Float.MAX_VALUE && time < bestTime)
                {
                    bestResult = location;
                    bestTime = time;
                }
            }
        }

        return bestResult;
    }

    private void updateStations(Location location)
    {
        synchronized (mStationMutex)
        {
            if (null != mStations && location != null)
            {
                double currentRadius = sStationRadius;
                int stationPosition = mCurrentStation;
                for (int i = mCurrentStation; i < mStations.size(); ++i)
                {
                    Station station = mStations.get(i);
                    double minValue = getDistanceBetweenLocationAndStation(location, station);

                    if (minValue < currentRadius)
                    {
                        stationPosition = i;
                        currentRadius = minValue;
                    }
                }
                if (mCurrentStation != stationPosition)
                {
                    // location station change
                    sendCommitServiceNotify(mStations.get(stationPosition));
                }

                mCurrentStation = stationPosition;
                if (mCurrentStation < mStations.size() - 1)
                {
                    mNextStation = mCurrentStation + 1;
                }
                else
                {
                    mNextStation = -1;
                }

                Iterator<Messenger> iterator = mCurrentAndNextStationListeners.iterator();
                while (iterator.hasNext())
                {
                    Message message = getCurrentAndNextMassage();

                    Messenger messenger = iterator.next();
                    try
                    {
                        messenger.send(message);
                    } catch (RemoteException e)
                    {
                        iterator.remove();
                    }
                }
            }
        }
    }

    private void sendCommitServiceNotify(final Station station)
    {
        bindService(new Intent(this, CommitService.class), new ServiceConnection()
        {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder)
            {
                Message message = new Message();
                message.what = CommitService.ADD_ROUTE_ID_TO_COMMIT;
                message.arg1 = (int) station.getStationId();

                Messenger service = new Messenger(iBinder);
                try
                {
                    service.send(message);
                } catch (RemoteException e)
                {
                    // pass
                }
                unbindService(this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName)
            {

            }
        }, BIND_AUTO_CREATE);
    }

    private double getDistanceBetweenLocationAndStation(Location location, Station station)
    {
        return Math.round((Math.sqrt(
                Math.pow(location.getLongitude() - station.getLng(), 2) +
                        Math.pow(location.getLatitude() - station.getLat(), 2)) * 111111));
    }

    @Override
    public void onLocationChanged(Location location)
    {
        log.info(location.toString());
        Iterator<Messenger> iterator = mLocationCallBacks.iterator();
        Message message = getLocationMessage();

        while (iterator.hasNext())
        {
            Messenger messenger = iterator.next();

            try
            {
                messenger.send(message);
            } catch (RemoteException e)
            {
                iterator.remove();
            }
        }
	    mLastLocation = mCurrentLocation;
        mCurrentLocation = location;

	    if (mMaxSpeed < location.getSpeed())
	    {
		    mMaxSpeed = location.getSpeed();
	    }
	    if (mMinSpeed == 0)
	    {
		    mMinSpeed = location.getSpeed();
	    }
	    if (mMinSpeed > location.getSpeed() && location.getSpeed() > 0)
	    {
		    mMinSpeed = location.getSpeed();
	    }

        updateStations(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle)
    {
    }

    @Override
    public void onProviderEnabled(String s)
    {
    }

    @Override
    public void onProviderDisabled(String s)
    {
    }
}
