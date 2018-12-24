package com.arellomobile.terminal.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.arellomobile.terminal.R;
import com.arellomobile.terminal.data.data.Route;
import com.arellomobile.terminal.data.data.Trip;
import com.arellomobile.terminal.data.data.TripInfo;
import com.arellomobile.terminal.data.data.Vehicle;
import com.arellomobile.terminal.helper.uihelper.AlertAdapter;
import com.arellomobile.terminal.helper.uihelper.BasicActivity;
import com.arellomobile.terminal.service.LocationService;
import com.arellomobile.terminal.tasks.HandShakeLoader;
import com.arellomobile.terminal.tasks.LoadTrip;
import com.arellomobile.terminal.tasks.callback.LoadListCallBack;
import com.arellomobile.terminal.tasks.callback.LoadObjectCallBack;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * User: AndreyKo
 * Date: 21.05.12
 */
public class SetTripActivity extends BasicActivity implements View.OnClickListener, LoadObjectCallBack, LoadListCallBack
{
    private Handler timeHandler;

    private TextView off;
    private TextView on;

    private EditText pinView;

    private BroadcastReceiver broadcastReceiver;
    private LocationManager locationManager;

    private AsyncTask<Void, Void, Void> mHandShake;
    private AsyncTask<Void, Void, Void> mLoadTrip;

    private Long mTripId;
    private Long mVehicleId;
    private Long mRouteId;
    private String mMd5Pin;

    private String mRouteAlert;
    private String mVehicleAlert;
    private String mTripAlert;
    private String mPinAlert;
    private String mInvalidPinAlert;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_trip_screen);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        findViewById(R.id.vehicleBtn).setOnClickListener(this);
        findViewById(R.id.routeBtn).setOnClickListener(this);
        findViewById(R.id.tripBtn).setOnClickListener(this);
        findViewById(R.id.startTrip).setOnClickListener(this);
        findViewById(R.id.back_btn).setOnClickListener(this);

        off = (TextView) findViewById(R.id.statusOff);
        on = (TextView) findViewById(R.id.statusOn);

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
        long companyId = getIntent().getLongExtra(COMPANY_ID_KEY, 0);
        String companyName = mStorageApi.getCompanyById(companyId).getName();
        ((TextView) findViewById(R.id.company_header)).setText(companyName);

        pinView = (EditText) findViewById(R.id.pin_view);
        pinView.setInputType(InputType.TYPE_CLASS_PHONE);
        pinView.setTransformationMethod(PasswordTransformationMethod.getInstance());

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mTripId = mDataManager.getTripId(this);
        mVehicleId = mDataManager.getVehicleId(this);
        mRouteId = mDataManager.getRouteId(this);
        mMd5Pin = mDataManager.getMD5PinId(this);

        if (mTripId == -1 || mVehicleId == -1 || mRouteId == -1 || null == mMd5Pin)
        {
            mTripId = mVehicleId = mRouteId = null;
            mMd5Pin = null;
            mDataManager.setTripInfo(-1, -1, -1, null, this);
        }
        else
        {
            mHandShake = new HandShakeLoader(this, this, mMd5Pin, mVehicleId, mRouteId, mTripId).execute();
        }

        List<Vehicle> vehicles = mStorageApi.getVehicles(mDataManager.getCompanyId(this));
        if (null != vehicles && vehicles.size() > 0)
        {
            Collections.sort(vehicles);
            setVehicle(vehicles.get(0));
        }
        List<Route> routes = mStorageApi.getRoutes(mDataManager.getCompanyId(this));
        if (null != routes && routes.size() > 0)
        {
            Collections.sort(routes);
            Route route = routes.get(0);
            setRoute(route);

            List<Trip> trips = mStorageApi.getTrips(route.getRouteId());
            if(null != trips && trips.size() > 0)
            {
                Collections.sort(trips);
                setTrip(trips.get(0));
            }
        }
    }

    private void initLabel()
    {
        ((TextView) findViewById(R.id.statusOn)).setText(mLangFactory.getString("statusOnLabel"));
        ((TextView) findViewById(R.id.statusOff)).setText(mLangFactory.getString("statusOffLabel"));
        ((TextView) findViewById(R.id.vehicleLabel)).setText(mLangFactory.getString("vehicleLabel"));
        ((TextView) findViewById(R.id.vehicleHiddenLabel)).setText(mLangFactory.getString("vehicleHiddenLabel"));
        ((TextView) findViewById(R.id.routeLabel)).setText(mLangFactory.getString("routeLabel"));
        ((TextView) findViewById(R.id.routeHiddenLabel)).setText(mLangFactory.getString("routeHiddenLabel"));
        ((TextView) findViewById(R.id.tripLabel)).setText(mLangFactory.getString("tripLabel"));
        ((TextView) findViewById(R.id.tripHiddenLabel)).setText(mLangFactory.getString("tripHiddenLabel"));
        ((TextView) findViewById(R.id.pinLabel)).setText(mLangFactory.getString("pinLabel"));
        ((Button) findViewById(R.id.startTrip)).setText(mLangFactory.getString("startTripLabel"));

        mVehicleAlert = mLangFactory.getString("notSelectVehicleLabel");
        mRouteAlert = mLangFactory.getString("notSelectRouteLabel");
        mTripAlert = mLangFactory.getString("notSelectTripLabel");
        mPinAlert = mLangFactory.getString("notEnterPinLabel");
        mInvalidPinAlert = mLangFactory.getString("invalidPinLabel");
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

        turnGPSOn();
        startService(new Intent(this, LocationService.class));

        initLabel();
        pinView.setText("");
    }

    private void turnGPSOn()
    {
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (!provider.contains("gps")) // if gps is disabled
        {
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        timeHandler.removeMessages(UPDATE);

        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        stopService(new Intent(this, LocationService.class));
        if (null != mHandShake)
        {
            mHandShake.cancel(true);
        }
        if (null != mLoadTrip)
        {
            mLoadTrip.cancel(true);
        }
    }

    @Override
    public void onClick(View view)
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(pinView.getWindowToken(), 0);

        switch (view.getId())
        {
            case R.id.vehicleBtn:
                startActivityForResult(new Intent(this, ListScreenActivity.class).
                        putExtra(ListScreenActivity.DATA_TYPE, ListScreenActivity.typeEnum.VEHICLE_TYPE),
                                       RESULT_CODE_OK);
                break;
            case R.id.routeBtn:
                startActivityForResult(new Intent(this, ListScreenActivity.class).
                        putExtra(ListScreenActivity.DATA_TYPE, ListScreenActivity.typeEnum.ROUTE_TYPE), RESULT_CODE_OK);
                break;
            case R.id.tripBtn:
                if (mRouteId != null)
                {
                    startActivityForResult(new Intent(this, ListScreenActivity.class).
                            putExtra(ListScreenActivity.DATA_TYPE, ListScreenActivity.typeEnum.TRIP_TYPE)
                                                   .putExtra(ListScreenActivity.ROUTE_ID, mRouteId),
                                           RESULT_CODE_OK);
                }
                else
                {
                    AlertAdapter.showAlert(this, mRouteAlert);
                }
                break;
            case R.id.startTrip:
                if (mVehicleId == null)
                {
                    AlertAdapter.showAlert(this, mVehicleAlert);
                    break;
                }
                if (mRouteId == null)
                {
                    AlertAdapter.showAlert(this, mRouteAlert);
                    break;
                }
                if (mTripId == null)
                {
                    AlertAdapter.showAlert(this, mTripAlert);
                    break;
                }
                if (pinView.getText().toString() == null || pinView.getText().toString().trim().length() == 0)
                {
                    AlertAdapter.showAlert(this, mPinAlert);
                    break;
                }
                if (null != mHandShake)
                {
                    mHandShake.cancel(true);
                }
                mMd5Pin = mDataManager.getMD5Pin(pinView.getText().toString());
                mHandShake = new HandShakeLoader(this, this, mMd5Pin, mVehicleId, mRouteId, mTripId).execute();
                break;
            case R.id.back_btn:
                mDataManager.logOutApp(this);
                finish();
                startActivity(new Intent(this, AppLoader.class));
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_CODE_OK)
        {
            Object obj = data.getSerializableExtra(ListScreenActivity.DATA_TYPE);
            if (obj instanceof Vehicle)
            {
                setVehicle((Vehicle) obj);
            }
            else if (obj instanceof Route)
            {
                setRoute((Route) obj);

                List<Trip> trips = mStorageApi.getTrips(mRouteId);
                mTripId = null;
                if (trips == null || trips.size() == 0)
                {
                    if (null != mLoadTrip)
                    {
                        mLoadTrip.cancel(true);
                    }
                    mLoadTrip = new LoadTrip(this, this, mRouteId).execute();
                }
                else
                {
                    setData(trips);
                }
            }
            else if (obj instanceof Trip)
            {
                setTrip((Trip) obj);
            }
        }
    }

    private void setTrip(Trip obj)
    {
        findViewById(R.id.tripHiddenLabel).setVisibility(View.GONE);
        findViewById(R.id.tripMainText).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.tripMainText)).setText(obj.toString());

        Trip trip = (Trip) obj;
        mTripId = trip.getId();
        trip.setLastSelectedTime();
        mStorageApi.updateTrip(trip);
    }

    private void setRoute(Route route)
    {
        findViewById(R.id.routeHiddenLabel).setVisibility(View.GONE);
        findViewById(R.id.routeMainText).setVisibility(View.VISIBLE);
        findViewById(R.id.tripHiddenLabel).setVisibility(View.VISIBLE);
        findViewById(R.id.tripMainText).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.routeMainText)).setText(route.toString());
        ((TextView) findViewById(R.id.current_route))
                .setText(String.format(getString(R.string.selectItem), route.toString()));

        mRouteId = route.getRouteId();
        route.setLastSelectedTime();
        mStorageApi.updateRoute(route);
    }

    private void setVehicle(Vehicle obj)
    {
        findViewById(R.id.vehicleHiddenLabel).setVisibility(View.GONE);
        findViewById(R.id.vehicleMainText).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.vehicleMainText)).setText(obj.toString());
        Vehicle vehicle = (Vehicle) obj;
        mVehicleId = vehicle.getCarId();
        vehicle.setLastSelectedTime();
        mStorageApi.updateVehicle(vehicle);
    }

    @Override
    public void setData(Object data, Vehicle vehicle, Route route, Trip trip)
    {
        if (data instanceof TripInfo)
        {
	        mDataManager
			        .setTripInfo(mVehicleId, mRouteId, mTripId, mDataManager.getMD5Pin(pinView.getText().toString()),
					        this);
            startActivity(new Intent(this, TripActivity.class).putExtra(TripActivity.TRIP_INFO, (TripInfo) data)
                                  .putExtra(TripActivity.VEHICLE_KEY, vehicle)
                                  .putExtra(TripActivity.TRIP_KEY, trip)
                                  .putExtra(TripActivity.ROUTE_KEY, route));
        }
        else
        {
            if (((Boolean) data))
            {
                startActivity(new Intent(this, TripActivity.class).putExtra(TripActivity.VEHICLE_KEY, vehicle)
                                      .putExtra(TripActivity.TRIP_KEY, trip)
                                      .putExtra(TripActivity.ROUTE_KEY, route));
            }
            else
            {
                AlertAdapter.showAlert(this, mInvalidPinAlert);
            }
        }
    }

    @Override
    public void setData(List data) // get list of trip
    {
        if (data != null && data.size() == 1)
        {
            findViewById(R.id.tripHiddenLabel).setVisibility(View.GONE);
            findViewById(R.id.tripMainText).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tripMainText)).setText(data.get(0).toString());
            mTripId = ((Trip) data.get(0)).getId();
        }
        else if (data != null && data.size() > 1)
        {
            Date date = new Date();
            long currentTime = date.getHours() * 60 + date.getMinutes();
            int next = 0;
            for (int i = 0; i < data.size(); ++i)
            {
                Trip t = (Trip) data.get(i);
                if (currentTime < t.getTime())
                {
                    next = i;
                    break;
                }
            }
            findViewById(R.id.tripHiddenLabel).setVisibility(View.GONE);
            findViewById(R.id.tripMainText).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tripMainText)).setText(data.get(next).toString());
            mTripId = ((Trip) data.get(next)).getId();
//            startActivityForResult(new Intent(this, ListScreenActivity.class).
//                    putExtra(ListScreenActivity.DATA_TYPE, ListScreenActivity.typeEnum.TRIP_TYPE)
//                                           .putExtra(ListScreenActivity.ROUTE_ID, mRouteId), RESULT_CODE_OK);
        }
    }
}
