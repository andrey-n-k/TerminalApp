package com.arellomobile.terminal.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.*;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.arellomobile.terminal.R;
import com.arellomobile.terminal.data.data.*;
import com.arellomobile.terminal.helper.adapter.ListScreenAdapter;
import com.arellomobile.terminal.helper.uihelper.BasicActivity;
import com.arellomobile.terminal.service.LocationService;
import com.arellomobile.terminal.tasks.*;
import com.arellomobile.terminal.tasks.callback.LoadListCallBack;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * User: AndreyKo
 * Date: 22.05.12
 */
public class ListScreenActivity extends BasicActivity implements LoadListCallBack, View.OnClickListener,
                                                                 AdapterView.OnItemClickListener
{
    public static final String DATA_TYPE = "data_type";
    public static final String ROUTE_ID = "route_id";

    public static enum typeEnum
    {
        NOT, VEHICLE_TYPE, ROUTE_TYPE, TRIP_TYPE, LANG_TYPE, COMPANY_TYPE
    }

    ;
    public static typeEnum type = typeEnum.NOT;

    private TextView mTitleView;
    private TextView mEmptyText;
    private Button mUpdateBtn;
    private ListView mListView;
    private ListScreenAdapter mAdapter;

    private AsyncTask<Void, Void, Void> mVenicleTask;
    private AsyncTask<Void, Void, Void> mRouteTask;
    private AsyncTask<Void, Void, Void> mTripTask;
    private AsyncTask<Void, Void, Void> mLangTask;
    private AsyncTask<Void, Void, Void> mCompanyTask;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_screen);

        mListView = (ListView) findViewById(R.id.listView);
        mListView.setOnItemClickListener(this);
        mUpdateBtn = (Button) findViewById(R.id.updateBtn);
        mUpdateBtn.setOnClickListener(this);
        mEmptyText = (TextView) findViewById(R.id.emptyList);
        mEmptyText.setVisibility(View.VISIBLE);
        findViewById(R.id.back_btn).setOnClickListener(this);
        mListView.setVisibility(View.GONE);
        mTitleView = (TextView) findViewById(R.id.title);

        type = (typeEnum) getIntent().getSerializableExtra(DATA_TYPE);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        switch (type)
        {
            case VEHICLE_TYPE:
                initLabel("vehicleTitleLabel");
                List<Vehicle> vehicles = mStorageApi.getVehicles(mDataManager.getCompanyId(this));
                if (vehicles != null && vehicles.size() > 0)
                {
                    setData(vehicles);
                    break;
                }
                if (null != mVenicleTask)
                {
                    mVenicleTask.cancel(true);
                }
                mVenicleTask = new LoadVehicle(this, this).execute();
                break;
            case ROUTE_TYPE:
                initLabel("routeTitleLabel");
                List<Route> routes = mStorageApi.getRoutes(mDataManager.getCompanyId(this));
                if (routes != null && routes.size() > 0)
                {
                    setData(routes);
                    break;
                }
                if (null != mRouteTask)
                {
                    mRouteTask.cancel(true);
                }
                mRouteTask = new LoadRoute(this, this).execute();
                break;
            case TRIP_TYPE:
                initLabel("tripTitleLabel");
                long routeId = getIntent().getExtras().getLong(ROUTE_ID);
                List<Trip> trips = mStorageApi.getTrips(routeId);
                if (trips != null && trips.size() > 0)
                {
                    Collections.sort(trips);
                    mAdapter = new ListScreenAdapter<Trip>(this, trips);
                    mListView.setAdapter(mAdapter);
                    mListView.setVisibility(View.VISIBLE);
                    mEmptyText.setVisibility(View.GONE);
                }
                else
                {
                    mEmptyText.setVisibility(View.VISIBLE);
                }
                break;
            case LANG_TYPE:
                initLabel("selectAppLanguageLabel2");
                List<Language> languages = mStorageApi.getLanguages();
                if (languages != null)
                {
                    mAdapter = new ListScreenAdapter(this, languages);
                    mListView.setAdapter(mAdapter);
                    mListView.setVisibility(View.VISIBLE);
                    mEmptyText.setVisibility(View.GONE);
                }
                else
                {
                    mEmptyText.setVisibility(View.VISIBLE);
                }
                break;
            case COMPANY_TYPE:
                initLabel("selectAppCompanyLabel");
                List<Company> companies = mStorageApi.getCompanies();
                if (companies != null)
                {
                    mAdapter = new ListScreenAdapter(this, companies);
                    mListView.setAdapter(mAdapter);
                    mListView.setVisibility(View.VISIBLE);
                    mEmptyText.setVisibility(View.GONE);
                }
                else
                {
                    mEmptyText.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void initLabel(String label)
    {
        mTitleView.setText(mLangFactory.getString(label));
        mUpdateBtn.setText(mLangFactory.getString("updateLabel"));
        mEmptyText.setText(mLangFactory.getString("emptyList"));
    }

    @Override
    public void setData(final List data)
    {
        if (data != null && data.size() > 0)
        {
            if (data.get(0) instanceof Comparable)
            {
                Collections.sort(data);
            }
/*            if (data.get(0) instanceof Route)
            {
                showProgress();
                bindService(new Intent(this, LocationService.class), new ServiceConnection()
                {
                    private ServiceConnection mServiceConnection = this;

                    public Messenger mMessenger = new Messenger(new Handler()
                    {
                        @Override
                        public void handleMessage(android.os.Message msg)
                        {
                            switch (msg.what)
                            {
                                case LocationService.GET_LOCATION:
                                    Location location = (Location) msg.getData().get(String.valueOf(msg.what));

                                    hideProgress();

                                    if(null == location)
                                    {
                                        setData(data);
                                    }
                                    else
                                    {
                                        Location routeLoc = new Location(LocationManager.GPS_PROVIDER);
                                        for(Object routeObj : data)
                                        {
                                            Route route = (Route) routeObj;
                                            routeLoc.setLatitude(route.getLastSelectedTime());
                                        }
                                    }

                                    unbindService(mServiceConnection);
                                    break;
                                default:
                                    super.handleMessage(msg);
                            }
                        }
                    });

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
                            // pass
                        }
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName componentName)
                    {

                    }
                }, BIND_AUTO_CREATE);
            }
            else
            {*/
                setAdapterData(data);
//            }
        }
    }

    private void setAdapterData(List data)
    {
        mAdapter = new ListScreenAdapter(this, data);
        mListView.setAdapter(mAdapter);
        mListView.setVisibility(View.VISIBLE);
        mEmptyText.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.back_btn:
                setResult(RESULT_CODE_CANCEL);
                finish();
                break;
            case R.id.updateBtn:
            {
                switch (type)
                {
                    case VEHICLE_TYPE:
                        if (null != mVenicleTask)
                        {
                            mVenicleTask.cancel(true);
                        }
                        mVenicleTask = new LoadVehicle(this, this).execute();
                        break;
                    case ROUTE_TYPE:
                        if (null != mRouteTask)
                        {
                            mRouteTask.cancel(true);
                        }
                        mRouteTask = new LoadRoute(this, this).execute();
                        break;
                    case TRIP_TYPE:
                        if (null != mTripTask)
                        {
                            mTripTask.cancel(true);
                        }
                        long routeId = getIntent().getExtras().getLong(ROUTE_ID);
                        mTripTask = new LoadTrip(this, this, routeId).execute();
                        break;
                    case LANG_TYPE:
                        if (null != mLangTask)
                        {
                            mLangTask.cancel(true);
                        }
                        mLangTask = new LoadLanguage(this, this).execute();
                        break;
                    case COMPANY_TYPE:
                        if (null != mCompanyTask)
                        {
                            mCompanyTask.cancel(true);
                        }
                        mCompanyTask = new LoadCompany(this, this).execute();
                        break;
                }
                break;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        Intent result = new Intent();
        result.putExtra(DATA_TYPE, (Serializable) mListView.getAdapter().getItem(i));
        setResult(RESULT_CODE_OK, result);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            setResult(RESULT_CODE_CANCEL);
            finish();
        }

        return false;
    }

    @Override
    public void onDestroy()
    {
        if (null != mRouteTask)
        {
            mRouteTask.cancel(true);
        }
        if (null != mVenicleTask)
        {
            mVenicleTask.cancel(true);
        }
        if (null != mTripTask)
        {
            mTripTask.cancel(true);
        }
        if (null != mLangTask)
        {
            mLangTask.cancel(true);
        }
        if (null != mCompanyTask)
        {
            mCompanyTask.cancel(true);
        }
        super.onDestroy();
    }
}
