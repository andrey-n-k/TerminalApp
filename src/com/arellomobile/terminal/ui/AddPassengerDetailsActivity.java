package com.arellomobile.terminal.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.arellomobile.terminal.R;
import com.arellomobile.terminal.data.data.*;
import com.arellomobile.terminal.helper.adapter.ListScreenAdapter;
import com.arellomobile.terminal.helper.uihelper.AlertAdapter;
import com.arellomobile.terminal.helper.uihelper.BasicActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * User: AndreyKo
 * Date: 06.06.12
 */
public class AddPassengerDetailsActivity extends BasicActivity implements View.OnClickListener,
                                                                          AdapterView.OnItemClickListener
{
    public static final String STATION_KEY = "station_key";
    public static final String ROUTE_KEY = "route_key";
    public static final String TRIP_KEY = "trip_key";
    public static final String CURRENT_STATION_KEY = "current_station_key";

    private RadioButton mMale;
    private RadioButton mFemale;
    private RadioButton mLocal;
    private RadioButton mForeigner;

    private ListView mListView;
    private ListScreenAdapter mAdapter;

    private TripInfo mTripInfo;
    private Route mRoute;
    private Trip mTrip;
    private Station mCurrentStation;

    private static Map<Integer, String> mAgeRadioButton;
    private RadioGroup mAgeRadioGroup;
    private Station mSelectStation;

    private String mGenderAlert;
    private String mWhoAlert;
    private String mAgeAlert;
    private String mStationAlert;

    static
    {
        mAgeRadioButton = new HashMap<Integer, String>();
        mAgeRadioButton.put(Integer.valueOf(R.id.btn_1), "1-15");
        mAgeRadioButton.put(Integer.valueOf(R.id.btn_2), "15-30");
        mAgeRadioButton.put(Integer.valueOf(R.id.btn_3), "30-45");
        mAgeRadioButton.put(Integer.valueOf(R.id.btn_4), "45-60");
        mAgeRadioButton.put(Integer.valueOf(R.id.btn_5), ">60");
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_passenger);

        mTripInfo = (TripInfo) getIntent().getExtras().getSerializable(STATION_KEY);
        mRoute = (Route) getIntent().getExtras().getSerializable(ROUTE_KEY);
        mTrip = (Trip) getIntent().getExtras().getSerializable(TRIP_KEY);
        mCurrentStation = (Station) getIntent().getExtras().getSerializable(CURRENT_STATION_KEY);

        mListView = (ListView) findViewById(R.id.listView);

        if (mCurrentStation != null)
        {
            int index = 0;
            for (int i = 0; i < mTrip.getStations().size(); ++i)
            {
                if (mTrip.getStations().get(i).getStationId() == mCurrentStation.getStationId())
                {
                    index = i;
                    break;
                }
            }
            if (index < mTrip.getStations().size())
            {
                ++index;
            }
            mAdapter = new ListScreenAdapter<Station>(this, mTrip.getStations().subList(index, mTrip.getStations().size()));
        }
        else
        {
            mAdapter = new ListScreenAdapter<Station>(this, mTrip.getStations());
        }

        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mAdapter);

        findViewById(R.id.back_btn).setOnClickListener(this);
        findViewById(R.id.cancelBtn).setOnClickListener(this);
        findViewById(R.id.setBtn).setOnClickListener(this);

        mMale = (RadioButton) findViewById(R.id.maleBtn);
        mFemale = (RadioButton) findViewById(R.id.femaleBtn);
        mLocal = (RadioButton) findViewById(R.id.localBtn);
        mForeigner = (RadioButton) findViewById(R.id.foreignerBtn);

        mAgeRadioGroup = (RadioGroup) findViewById(R.id.ageRadioGroup);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        initLabel();
    }

    private void initLabel()
    {
        ((TextView) findViewById(R.id.passengerDetailsLabel)).setText(mLangFactory.getString("passengerDetailsLabel"));
        ((TextView) findViewById(R.id.genderLabel)).setText(mLangFactory.getString("genderLabel"));
        ((TextView) findViewById(R.id.whoLabel)).setText(mLangFactory.getString("whoLabel"));
        ((TextView) findViewById(R.id.ageLabel)).setText(mLangFactory.getString("ageLabel"));
        ((TextView) findViewById(R.id.stationLabel)).setText(mLangFactory.getString("stationLabel"));
        ((Button) findViewById(R.id.setBtn)).setText(mLangFactory.getString("setLabel"));
        ((Button) findViewById(R.id.cancelBtn)).setText(mLangFactory.getString("cancelLabel"));

        mMale.setText(mLangFactory.getString("maleLabel"));
        mFemale.setText(mLangFactory.getString("femaleLabel"));
        mForeigner.setText(mLangFactory.getString("foreignerLabel"));
        mLocal.setText(mLangFactory.getString("localLabel"));

        mGenderAlert = mLangFactory.getString("notSelectGenderLabel");
        mWhoAlert = mLangFactory.getString("notSelectWhoLabel");
        mAgeAlert = mLangFactory.getString("notSelectAgeLabel");
        mStationAlert = mLangFactory.getString("notSelectStationLabel");
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.back_btn:
            case R.id.cancelBtn:
                finish();
                break;
            case R.id.setBtn:
                if (!mMale.isChecked() && !mFemale.isChecked())
                {
                    AlertAdapter.showAlert(this, mGenderAlert);
                    break;
                }
                if (!mLocal.isChecked() && !mForeigner.isChecked())
                {
                    AlertAdapter.showAlert(this, mWhoAlert);
                    break;
                }
                if (mAgeRadioGroup.getCheckedRadioButtonId() == -1)
                {
                    AlertAdapter.showAlert(this, mAgeAlert);
                    break;
                }
                if (null == mSelectStation)
                {
                    AlertAdapter.showAlert(this, mStationAlert);
                    break;
                }
                addPassenger();
                finish();
                break;
        }
    }

    private void addPassenger()
    {
        long fromStationId = mCurrentStation != null ? mCurrentStation.getStationId() : 0;
        OfflinePassenger p = new OfflinePassenger(mMale.isChecked(), mLocal.isChecked(),
                                                  mAgeRadioButton.get(mAgeRadioGroup.getCheckedRadioButtonId()),
                                                  fromStationId, mSelectStation.getStationId());
        mStorageApi.addOfflinePassenger(p, mTripInfo.getId());
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        mSelectStation = (Station) mAdapter.getItem(i);
        mAdapter.setSelectedPosition(i);
    }
}
