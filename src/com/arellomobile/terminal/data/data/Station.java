package com.arellomobile.terminal.data.data;

import com.arellomobile.android.libs.cache.ormlite.field.DatabaseField;
import com.arellomobile.android.libs.cache.ormlite.table.DatabaseTable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * User: AndreyKo
 * Date: 05.06.12
 */
@DatabaseTable(tableName = "station")
public class Station implements Serializable
{
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private long stationId;
    @DatabaseField
    private String name;
    @DatabaseField
    private String localName;
    @DatabaseField
    private double lat;
    @DatabaseField
    private double lng;
    @DatabaseField
    private long duration;
    @DatabaseField(columnName = "trip_id")
    private long trip_id;
    @DatabaseField
    private String price;

    private Station() {}

    public String getName()
    {
        return name;
    }
    public String getLocalName()
    {
        return localName;
    }
    public double getLat()
    {
        return lat;
    }
    public double getLng()
    {
        return lng;
    }
    public long getDuration()
    {
        return duration;
    }
    public long getStationId()
    {
        return stationId;
    }
    public String getPrice()
    {
        return price == null ? "" : price;
    }
    public void setPrice(String price)
    {
        this.price = price;
    }
    public void setTripId(long tripId)
    {
        trip_id = tripId;
    }

    public static Station build(JSONObject obj, long tripId) throws JSONException
    {
        Station result = new Station();
        result.stationId = Long.valueOf(obj.getString("station_id"));
        result.name = obj.getString("station_name");
        result.localName = obj.optString("name_local");
        result.lat = Double.valueOf(obj.getString("lat"));
        result.lng = Double.valueOf(obj.getString("lng"));
        result.duration = Long.valueOf(obj.getString("duration"));
        result.trip_id = tripId;
        result.price = obj.optString("price");

        return result;
    }

    public static Station build(JSONObject obj) throws JSONException
    {
        Station result = new Station();
        result.stationId = Long.valueOf(obj.getString("station_id"));
        result.name = obj.optString("station_name");
        if (result.name == null || result.name.length() == 0)
        {
            result.name = obj.optString("name");
        }
        result.localName = obj.optString("name_local");
        result.lat = Double.valueOf(obj.getString("lat"));
        result.lng = Double.valueOf(obj.getString("lng"));
        result.duration = Long.valueOf(obj.getString("duration"));
        result.price = obj.optString("price");

        return result;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
