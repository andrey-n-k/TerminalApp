package com.arellomobile.terminal.data.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * User: MiG35
 * Date: 21.06.12
 * Time: 13:54
 */
public class NearestStation implements Serializable
{
    private long mStationId;
    private String mType;
    private String mName;
    private String mLocalName;
    private double lat;
    private double lng;
    private String mCountry;

    public static NearestStation build(JSONObject jsonObject) throws JSONException
    {
        NearestStation station = new NearestStation();

        station.mStationId = jsonObject.getLong("id");
        station.mType = jsonObject.getString("type");
        station.mName = jsonObject.getString("name");
        station.mLocalName = jsonObject.getString("province_local");
        station.lat = jsonObject.getDouble("lat");
        station.lng = jsonObject.getDouble("lng");
        station.mCountry = jsonObject.getString("id");

        return station;
    }

    public String getName()
    {
        return mName;
    }

    public long getStationId()
    {
        return mStationId;
    }
}
