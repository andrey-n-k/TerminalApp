package com.arellomobile.terminal.data.data;

import com.arellomobile.android.libs.cache.ormlite.field.DataType;
import com.arellomobile.android.libs.cache.ormlite.field.DatabaseField;
import com.arellomobile.android.libs.cache.ormlite.table.DatabaseTable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: AndreyKo
 * Date: 22.05.12
 */
@DatabaseTable
public class Trip implements Serializable, Comparable<Trip>
{
    @DatabaseField(id = true)
    private long id;
    @DatabaseField
    private long routeId;
    @DatabaseField
    private String departure;
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private Date mLastSelectedTime;
    @DatabaseField(columnName = "route_id")
    private long route_id;

    private List<Station> stations;

    private Trip()
    {
        mLastSelectedTime = new Date(0);
    }

    public long getRouteId()
    {
        return routeId;
    }

    public long getId()
    {
        return id;
    }

    public String getDeparture()
    {
        return departure;
    }

    public List<Station> getStations()
    {
        return stations;
    }

    public void setStations(List<Station> stations)
    {
        this.stations = stations;
    }

    public static Trip build(JSONObject json) throws JSONException
    {
        Trip result = new Trip();
        result.routeId = Long.valueOf(json.getString("route_id"));
        result.id = Long.valueOf(json.getString("id"));
        result.departure = json.optString("departure");
        result.route_id = result.routeId;

        result.stations = new ArrayList<Station>();
        JSONArray stations = json.getJSONArray("route");
        for (int i = 0; i < stations.length(); ++i)
        {
            result.stations.add(Station.build(stations.getJSONObject(i), result.id));
        }

        return result;
    }

    public long getTime()
    {
        String[] tmp = departure.split(":");
        long time = Long.valueOf(tmp[0]) * 60 + Long.valueOf(tmp[1]);
        return time;
    }

    @Override
    public int compareTo(Trip trip)
    {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm");
        long time1;
        long time2;
        try
        {
            time1 = format.parse(departure).getTime();
            time2 = format.parse(trip.departure).getTime();
            if (time1 < time2)
            {
                return -1;
            }
            if (time2 > time1)
            {
                return 1;
            }
        } catch (ParseException e)
        {
            return 0;
        }
        return 0;
    }

    @Override
    public String toString()
    {
        return departure;
    }

    public void setLastSelectedTime()
    {
        mLastSelectedTime = new Date();
    }

    public Date getLastSelectedTime()
    {
        return mLastSelectedTime;
    }

    public void setLastSelectedTime(Date lastSelectedTime)
    {
        mLastSelectedTime = lastSelectedTime;
    }

    @Override
    public boolean equals(Object o)
    {
        if(null != o && o instanceof Trip)
        {
            return id == ((Trip) o).getId();
        }
        return false;
    }
}
