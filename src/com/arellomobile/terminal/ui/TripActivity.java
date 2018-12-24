package com.arellomobile.terminal.ui;

import android.content.*;
import android.net.ConnectivityManager;
import android.os.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.arellomobile.terminal.R;
import com.arellomobile.terminal.data.data.*;
import com.arellomobile.terminal.helper.uihelper.AlertAdapter;
import com.arellomobile.terminal.helper.uihelper.BasicActivity;
import com.arellomobile.terminal.helper.uihelper.CurrentStationsAlertAdapter;
import com.arellomobile.terminal.service.CommitService;
import com.arellomobile.terminal.service.LocationService;
import com.arellomobile.terminal.tasks.CheckCurrentStationsTask;
import com.arellomobile.terminal.tasks.callback.CheckCurrentStationCallback;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: AndreyKo
 * Date: 05.06.12
 */
public class TripActivity extends BasicActivity implements View.OnClickListener, ServiceConnection,
                                                           CheckCurrentStationCallback
{
    private static final int COMMIT_UPDATE_WHAT = 1;
    private static final long COMMIT_UPDATE_DELAY = 1000 * 10;

    private static final int FINISH_CODE = 123;
    private static final int ENTER_STATION_NAME_CODE = 234;

    private View off;
    private View on;

    private Vehicle vehicle;
    private Route route;
    private Trip trip;
    private TripInfo mTripInfo;
    private Station mCurrentStation;

    private LinearLayout passengerListView;

    private BroadcastReceiver broadcastReceiver;

    public static final String TRIP_INFO = "trip_info";
    public static final String VEHICLE_KEY = "vehicle_key";
    public static final String TRIP_KEY = "trip_key";
    public static final String ROUTE_KEY = "route_key";

    private TextView mNowStation;
    private TextView mTimeDeparture;
    private TextView mNextStation;
    private TextView mDuration;
    private TextView mStewardName;

    private Messenger mLocationServiceMessenger;
    private Handler mHandler;

    private Handler timeHandler;

    private View mStationButton;

    private CheckCurrentStationsTask mCheckCurrentStationTask;

    private String mOfflinePassLabel;
    private String mBoardPassLabel;
    private String mFinishTripAlert;
    private String mStationDialogLabel;
   

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.item_trip_screen);

        mTripInfo = (TripInfo) getIntent().getSerializableExtra(TRIP_INFO);

        vehicle = (Vehicle) getIntent().getExtras().getSerializable(VEHICLE_KEY);
        trip = (Trip) getIntent().getExtras().getSerializable(TRIP_KEY);
        route = (Route) getIntent().getExtras().getSerializable(ROUTE_KEY);

        findViewById(R.id.finishBtn).setOnClickListener(this);
        findViewById(R.id.boardOfflineBtn).setOnClickListener(this);
        mStationButton = findViewById(R.id.stationButton);
        mStationButton.setOnClickListener(this);
        mStationButton.setEnabled(false);

        off = findViewById(R.id.statusOff);
        on = findViewById(R.id.statusOn);
        String[] tmp = route.getRouteName().split("-");
        if (tmp[0].substring(0, 1).equals(" "))
        {
            tmp[0] = tmp[0].substring(1, tmp[0].length());
        }
        ((TextView) findViewById(R.id.fromStation)).setText(tmp[0]);
        ((TextView) findViewById(R.id.toStation)).setText(tmp[1]);

        ((TextView) findViewById(R.id.seating)).setText(vehicle.getCapacity());
        ((TextView) findViewById(R.id.numberCar)).setText(vehicle.getRegNo());
        final TextView commitCount = (TextView) findViewById(R.id.commitCount);

        mHandler = new Handler()
        {
            @Override
            public void handleMessage(android.os.Message msg)
            {
                commitCount.setText(String.valueOf(mDataManager.getCommitCount()));
                mHandler.sendMessageDelayed(Message.obtain(null, COMMIT_UPDATE_WHAT), COMMIT_UPDATE_DELAY);
            }
        };

        mStewardName = (TextView) findViewById(R.id.stewardName);
        if (mTripInfo.getStewardName() != null)
        {
            initStewardName();
        }
        else
        {
            mStewardName.setText(String.format(getString(R.string.stewardShow), "-"));
        }

        mNowStation = (TextView) findViewById(R.id.nowStation);
        mTimeDeparture = (TextView) findViewById(R.id.timeDeparture);
        mNextStation = (TextView) findViewById(R.id.nextStation);
        mDuration = (TextView) findViewById(R.id.duration);

        initNowNextDurationStationsWithDefaultValues();

        passengerListView = (LinearLayout) findViewById(R.id.passengerList);

        bindService(new Intent(this, LocationService.class), this, BIND_AUTO_CREATE);
        bindService(new Intent(this, CommitService.class), mCommitServiceConnection, Context.BIND_AUTO_CREATE);

        timeHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                switch (msg.what)
                {
                    case UPDATE:
                        setCurrentDataTime((TextView) findViewById(R.id.date), (TextView) findViewById(R.id.time));
                        timeHandler.removeMessages(UPDATE);
                        Message message = new Message();
                        message.what = UPDATE;
                        timeHandler.sendMessageDelayed(message, TIME);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    private void initLabel()
    {
        ((TextView) findViewById(R.id.seatingLabel)).setText(mLangFactory.getString("seatingLabel"));
        ((TextView) findViewById(R.id.nowLabel)).setText(mLangFactory.getString("nowLabel"));
        ((TextView) findViewById(R.id.nextLabel)).setText(mLangFactory.getString("nextLabel"));
        ((TextView) findViewById(R.id.boardOfflineBtn)).setText(mLangFactory.getString("boardOfflineLabel"));
        ((TextView) findViewById(R.id.sellTicketBtn)).setText(mLangFactory.getString("sellTicketLabel"));
        ((TextView) findViewById(R.id.stationButton)).setText(mLangFactory.getString("stationLabel"));
        ((TextView) findViewById(R.id.finishBtn)).setText(mLangFactory.getString("finishTripLabel"));

        mOfflinePassLabel = mLangFactory.getString("offlinePassengerLabel");
        mFinishTripAlert = mLangFactory.getString("finishTripQuestionLabel");
        mStationDialogLabel = mLangFactory.getString("stationDialogLabel");
    }

    private void initStewardName()
    {
        mStewardName.setText(String.format(getString(R.string.stewardShow), mTripInfo.getStewardName()));
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.finishBtn:
                AlertAdapter.showAlert(this, mFinishTripAlert, R.string.cancelLabel, FINISH_CODE);
                break;
            case R.id.boardOfflineBtn:
                startActivity(new Intent(this, AddPassengerDetailsActivity.class)
                                      .putExtra(AddPassengerDetailsActivity.STATION_KEY, mTripInfo)
                                      .putExtra(AddPassengerDetailsActivity.ROUTE_KEY, route)
                                      .putExtra(AddPassengerDetailsActivity.TRIP_KEY, trip)
                                      .putExtra(AddPassengerDetailsActivity.CURRENT_STATION_KEY, mCurrentStation));
                break;
            case R.id.listItem:
                startActivity(new Intent(this, PassengerDetails.class).putExtra(PassengerDetails.PASS_KEY,
                                                                                (Passenger) view.getTag())
                                      .putExtra(PassengerDetails.STATION_KEY, (Serializable) trip.getStations()));
                break;
            case R.id.stationButton:
                if (!view.isEnabled())
                {
                    return;
                }
                checkCurrentStation();
                break;
        }
    }

    private void checkCurrentStation()
    {
        showProgress();
        mCheckCurrentStationTask = new CheckCurrentStationsTask(this, this);
        mCheckCurrentStationTask.execute((Void) null);
    }

    private void finishTrip()
    {
        stopService(new Intent(this, LocationService.class));
        mDataManager.setTripInfo(-1, -1, -1, null, this);

        Message message = Message.obtain(null, CommitService.REMOVE_TRIP_INFO);
        try
        {
            mCommitServiceMessenger.send(message);
        } catch (RemoteException e)
        {
            // pass
        }

        mStorageApi.removeTripInfo();
        finish();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        timeHandler.removeMessages(UPDATE);
        Message message = new Message();
        message.what = UPDATE;
        timeHandler.sendMessageDelayed(message, 0);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                if (noConnectivity)
                {
                    on.setVisibility(View.GONE);
                    off.setVisibility(View.VISIBLE);
                }
                else
                {
                    on.setVisibility(View.VISIBLE);
                    off.setVisibility(View.GONE);
                }
            }
        };
        registerReceiver(broadcastReceiver, filter);

        setPassengerList();

        mHandler.sendMessageDelayed(Message.obtain(null, COMMIT_UPDATE_WHAT), COMMIT_UPDATE_DELAY);

        initLabel();
    }

    private void setPassengerList()
    {
        passengerListView.removeAllViews();
        passengerListView.invalidate();

        List<OfflinePassenger> offlinePassengers = mStorageApi.getOfflinePassengers(mTripInfo.getId());
//        List<OnlinePassenger> onlinePassengers = mTripInfo.getOnlinePassengers();
        List<OnlinePassenger> onlinePassengers = mStorageApi.getOnlinePassengers(mTripInfo.getId());

        if ((offlinePassengers != null && offlinePassengers.size() > 0) || (onlinePassengers != null && onlinePassengers
                .size() > 0))
        {
            passengerListView.setVisibility(View.VISIBLE);
            if (offlinePassengers != null && offlinePassengers.size() > 0)
            {
                for (Station s : trip.getStations())
                {
                    int count = 0;
                    for (OfflinePassenger p : offlinePassengers)
                    {
                        if (p.getToStationId() == s.getStationId())
                        {
                            ++count;
                        }
                    }
                    if (count > 0)
                    {
                        View view = View.inflate(this, R.layout.passenger_list_item, null);
                        passengerListView.addView(view);
                        ((TextView) view.findViewById(R.id.name)).setText(mOfflinePassLabel);
                        ((TextView) view.findViewById(R.id.status)).setText(getString(R.string.boardedPassengerLabel));
                        view.findViewById(R.id.count).setVisibility(View.VISIBLE);
                        ((TextView) view.findViewById(R.id.count)).setText(String.valueOf(count));
                        ((TextView) view.findViewById(R.id.trip)).setText(s.getName());

                        View divide = new View(this);
                        divide.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 2));
                        divide.setBackgroundColor(0xFFababab);
                        passengerListView.addView(divide);
                    }
                }
            }
            if (onlinePassengers != null && onlinePassengers.size() > 0)
            {
                for (OnlinePassenger p : onlinePassengers)
                {
                    View view = View.inflate(this, R.layout.passenger_list_item, null);
                    ((TextView) view.findViewById(R.id.name)).setText(p.getName());
                    ((TextView) view.findViewById(R.id.status)).setText(p.getStatus());
                    view.findViewById(R.id.count).setVisibility(View.GONE);

                    String fromStation = "";
                    String toStation = "";
                    for (Station s : trip.getStations())
                    {
                        if (p.getFromStationId() == s.getStationId())
                        {
                            fromStation = s.getName();
                        }
                        if (p.getToStationId() == s.getStationId())
                        {
                            toStation = s.getName();
                        }
                    }

                    ((TextView) view.findViewById(R.id.trip)).setText(String.format(getString(R.string.tripPassenger),
                                                                                    String.valueOf(fromStation),
                                                                                    String.valueOf(toStation)));
                    passengerListView.addView(view);

                    View divide = new View(this);
                    divide.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 2));
                    divide.setBackgroundColor(0xFFababab);
                    passengerListView.addView(divide);

                    View item = view.findViewById(R.id.listItem);
                    item.setOnClickListener(this);
                    item.setTag(p);
                }
            }
        }
        else
        {
            passengerListView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        timeHandler.removeMessages(UPDATE);

        unregisterReceiver(broadcastReceiver);

        mHandler.removeMessages(COMMIT_UPDATE_WHAT);
    }

    private Messenger mCommitServiceMessenger;

    public final Messenger mCommitMessenger = new Messenger(new Handler()
    {
        @Override
        public void handleMessage(android.os.Message msg)
        {
            switch (msg.what)
            {
                case CommitService.ADD_TRIP_INFO_LISTENER:
                    mTripInfo = (TripInfo) msg.getData().getSerializable(String.valueOf(msg.what));
                    initStewardName();
                    setPassengerList();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    });

    private ServiceConnection mCommitServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            mCommitServiceMessenger = new Messenger(iBinder);

            Message message = new Message();
            message.what = CommitService.ADD_TRIP_INFO_LISTENER;
            message.replyTo = mCommitMessenger;
            try
            {
                mCommitServiceMessenger.send(message);
            } catch (RemoteException e)
            {
                // pass
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
        }
    };

    private final Messenger mLocationMessenger = new Messenger(new Handler()
    {
        @Override
        public void handleMessage(android.os.Message msg)
        {
            switch (msg.what)
            {
                case LocationService.GET_CURRENT_AND_NEXT_STATION:
                    Station currentStation = (Station) msg.getData()
                            .getSerializable(LocationService.GET_CURRENT_AND_NEXT_STATION_INDEX_CURRENT);
                    Station nextStation = (Station) msg.getData()
                            .getSerializable(LocationService.GET_CURRENT_AND_NEXT_STATION_INDEX_NEXT);
                    initNowNextDurationStationsWithDefaultValues();

                    if (null != currentStation)
                    {
                        mNowStation.setText(currentStation.getName());
                        
                        if (mCurrentStation == null || mCurrentStation.getStationId() != currentStation.getStationId())
                        {
                            Date d = new Date();
                            long min = (d.getHours() * 60 + d.getMinutes());
                            mTimeDeparture.setText(String.format(getString(R.string.durationString),
                                                                 String.valueOf(min / 60),
                                                                 ((min % 60) < 10 ? "0" + min % 60 : String
                                                                         .valueOf(min % 60))));
                        }
                        
                        TripActivity.this.mCurrentStation = currentStation;
                    }

                    if (null != nextStation)
                    {
                        mNextStation.setText(nextStation.getName());
                        mDuration.setText(String.format(getString(R.string.durationString),
                                                        String.valueOf(nextStation.getDuration() / 60),
                                                        String.valueOf(nextStation.getDuration() % 60)));
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    });

    private void sentStationCommit(String s)
    {
        Message message = new Message();
        message.what = CommitService.ADD_STATION_TO_COMMIT;

        Bundle bundle = new Bundle();
        bundle.putString(String.valueOf(message.what), s);
        message.setData(bundle);

        try
        {
            mCommitServiceMessenger.send(message);
        } catch (RemoteException e)
        {
            // pass
        }
    }

    @Override
    public void setCurrentStations(List<NearestStation> stations)
    {
        CurrentStationsAlertAdapter
                .showInputAlertDialog(this, mStationDialogLabel, R.string.cancelLabel, ENTER_STATION_NAME_CODE,
                                      new ArrayList<NearestStation>(stations));
    }


    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data)
    {
        if (requestCode == FINISH_CODE && resultCode == RESULT_OK)
        {
            finishTrip();
        }
        else if (requestCode == ENTER_STATION_NAME_CODE && resultCode == RESULT_OK && data != null)
        {
            String stationName = data.getStringExtra(String.valueOf(RESULT_OK));
            sentStationCommit(stationName);
        }
    }

    private void initNowNextDurationStationsWithDefaultValues()
    {
        mNowStation.setText("-");
        mNextStation.setText("-");
        mDuration.setText("-");
    }

    @Override
    public void onDestroy()
    {
        if (null != mCheckCurrentStationTask)
        {
            mCheckCurrentStationTask.cancel(true);
        }

        Message message1 = new Message();
        message1.what = LocationService.REMOVE_CURRENT_AND_NEXT_STATION_LISTENER;
        message1.replyTo = mLocationMessenger;

        try
        {
            mLocationServiceMessenger.send(message1);
        } catch (RemoteException e)
        {
            // pass
        }

        Message message = Message.obtain(null, CommitService.REMOVE_TRIP_INFO_LISTENER);
        message.replyTo = mCommitMessenger;
        try
        {
            mCommitServiceMessenger.send(message);
        } catch (RemoteException e)
        {
            // pass
        }

        unbindService(this);
        unbindService(mCommitServiceConnection);

        super.onDestroy();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder)
    {
        mStationButton.setEnabled(true);

        mLocationServiceMessenger = new Messenger(iBinder);

        Message message = new Message();
        message.what = LocationService.ADD_CURRENT_AND_NEXT_STATION_LISTENER;
        message.replyTo = mLocationMessenger;
        try
        {
            mLocationServiceMessenger.send(message);
        } catch (RemoteException e)
        {
            // pass
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName)
    {
        mStationButton.setEnabled(false);
    }
}
