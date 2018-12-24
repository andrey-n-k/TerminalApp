package com.arellomobile.terminal.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.arellomobile.terminal.R;
import com.arellomobile.terminal.data.data.Passenger;
import com.arellomobile.terminal.data.data.Station;
import com.arellomobile.terminal.helper.uihelper.BasicActivity;
import com.arellomobile.terminal.service.CommitService;

import java.util.List;

/**
 * User: AndreyKo
 * Date: 15.06.12
 */
public class PassengerDetails extends BasicActivity
{
    public static final String PASS_KEY = "pass_key";
    public static final String STATION_KEY = "station_key";


    private static final String CHECK_IN_STATUS = "BOARDED";
    private static final String CHECK_OUT_STATUS = "PAID";

    private Passenger passenger;
    private View checkIn;
    private View checkOut;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.passenger_details);

        passenger = (Passenger) getIntent().getExtras().getSerializable(PASS_KEY);
        List<Station> stations = (List<Station>) getIntent().getExtras().getSerializable(STATION_KEY);

        String fromStation = "";
        String toStation = "";

        for (Station s : stations)
        {
            if (passenger.getFromStationId() == s.getStationId())
            {
                fromStation = s.getName();
            }
            if (passenger.getToStationId() == s.getStationId())
            {
                toStation = s.getName();
            }
        }
        ((TextView) findViewById(R.id.trip))
                .setText(String.format(getString(R.string.tripPassenger), fromStation, toStation));

        findViewById(R.id.okBtn).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                switch (view.getId())
                {
                    case R.id.okBtn:
                        finish();
                        break;
                }
            }
        });

        checkIn = findViewById(R.id.checkIn);
        checkOut = findViewById(R.id.checkOut);

        checkIn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showProgress();
                bindService(new Intent(PassengerDetails.this, CommitService.class), new ServiceConnection()
                {
                    @Override
                    public void onServiceConnected(ComponentName componentName, IBinder iBinder)
                    {
                        setPassengerStatus(CHECK_IN_STATUS, iBinder, this);
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName componentName)
                    {
                        hideProgress();
                    }
                }, BIND_AUTO_CREATE);
            }
        });
        checkOut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showProgress();
                bindService(new Intent(PassengerDetails.this, CommitService.class), new ServiceConnection()
                {
                    @Override
                    public void onServiceConnected(ComponentName componentName, IBinder iBinder)
                    {
                        setPassengerStatus(CHECK_OUT_STATUS, iBinder, this);
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName componentName)
                    {
                        hideProgress();
                    }
                }, BIND_AUTO_CREATE);
            }
        });

        checkStatus();
    }

    private void setPassengerStatus(String status, IBinder iBinder, ServiceConnection serviceConnection)
    {
        passenger.setStatus(status);
        mStorageApi.updatePassenger(passenger);

        Messenger messenger = new Messenger(iBinder);

        Message message = new Message();
        message.what = CommitService.SET_PASSENGER_STATUS;

        Bundle bundle = new Bundle();
        bundle.putSerializable(String.valueOf(message.what), passenger);

        message.setData(bundle);

        try
        {
            messenger.send(message);
        } catch (RemoteException e)
        {
            // pass
        }
        unbindService(serviceConnection);
        hideProgress();
        checkStatus();

        initLabel(passenger);
    }

    private void checkStatus()
    {
        checkIn.setVisibility(View.GONE);
        checkOut.setVisibility(View.GONE);

        if (passenger.getStatus().equals("PAID"))
        {
            checkIn.setVisibility(View.VISIBLE);
            checkOut.setVisibility(View.GONE);
        }

        if (passenger.getStatus().equals("BOARDED"))
        {
            checkOut.setVisibility(View.VISIBLE);
            checkIn.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        initLabel(passenger);
    }

    private void initLabel(Passenger passenger)
    {
        ((TextView) findViewById(R.id.name)).setText(passenger.getName());
        ((TextView) findViewById(R.id.gender)).setText(passenger.isMale() == true ?
                                                               mLangFactory.getString("maleLabel") : mLangFactory
                .getString("femaleLabel"));
        ((TextView) findViewById(R.id.age)).setText(passenger.getAge());
        ((TextView) findViewById(R.id.who)).setText(passenger.isLocal() == true ?
                                                            mLangFactory.getString("localLabel") : mLangFactory
                .getString("foreignerLabel"));
        ((TextView) findViewById(R.id.status)).setText(passenger.getStatus());

        ((TextView) findViewById(R.id.genderLabel)).setText(mLangFactory.getString("genderLabel"));
        ((TextView) findViewById(R.id.ageLabel)).setText(mLangFactory.getString("ageLabel"));
        ((TextView) findViewById(R.id.whoLabel)).setText(mLangFactory.getString("whoLabel"));
        ((TextView) findViewById(R.id.statusLabel)).setText(mLangFactory.getString("statusLabel"));
        ((Button) findViewById(R.id.okBtn)).setText(mLangFactory.getString("okLabel"));
    }

}
