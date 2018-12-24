package com.arellomobile.terminal.data.data;

import com.arellomobile.android.libs.cache.ormlite.field.DatabaseField;
import com.arellomobile.android.libs.cache.ormlite.table.DatabaseTable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: AndreyKo
 * Date: 05.06.12
 */
@DatabaseTable
public class TripInfo implements Serializable
{
    @DatabaseField(id = true)
    private int id = 1;
    @DatabaseField
    private String stewardName;

    private List<OnlinePassenger> onlinePassengers;
    private List<OfflinePassenger> offlinePassengers;

    private List<Station> stations;
    private LangPack mLangpack;

    public TripInfo()
    {
        offlinePassengers = new ArrayList<OfflinePassenger>();
        onlinePassengers = new ArrayList<OnlinePassenger>();
        stations = new ArrayList<Station>();
    }

    public String getStewardName()
    {
        return stewardName;
    }
    public List<Station> getStations()
    {
        return stations;
    }
    public List<OnlinePassenger> getOnlinePassengers()
    {
        return onlinePassengers;
    }
    public void setOnlinePassengers(List<OnlinePassenger> onlinePassengers)
    {
        this.onlinePassengers = onlinePassengers;
    }
    public List<OfflinePassenger> getOfflinePassengers()
    {
        return offlinePassengers;
    }
    public void setOfflinePassengers(List<OfflinePassenger> offlinePassengers)
    {
        this.offlinePassengers = offlinePassengers;
    }
    public int getId()
    {
        return id;
    }

    public static TripInfo build (JSONObject json) throws JSONException
    {
        TripInfo result = new TripInfo();

        result.stewardName = json.optString("steward");
        result.onlinePassengers = parseBookPack(json);
        result.stations = parseStation(json);
        result.mLangpack = parseLangPack(json);

        result.offlinePassengers = new ArrayList<OfflinePassenger>();

        return result;
    }

    private static List<OnlinePassenger> parseBookPack(JSONObject json) throws JSONException
    {
        List<OnlinePassenger> passengers = new ArrayList<OnlinePassenger>();
        if (json.opt("bookpack") != null)
        {
            if (json.get("bookpack") instanceof JSONObject)
            {
                passengers.add(OnlinePassenger.getPassenger(json.getJSONObject("bookpack")));
            }
            else
            {
                JSONArray array = json.getJSONArray("bookpack");
                for (int i = 0; i < array.length(); ++i)
                {
                    passengers.add(OnlinePassenger.getPassenger(array.getJSONObject(i)));
                }
            }
        }
        return passengers;
    }

    private static List<Station> parseStation(JSONObject json) throws JSONException
    {
        List<Station> stations = new ArrayList<Station>();
        if (json.opt("route") != null)
        {
            if (json.get("route") instanceof  JSONObject)
            {
                stations.add(Station.build(json.getJSONObject("route")));
            }
            else
            {
                JSONArray array = json.getJSONArray("route");
                for (int i = 0; i < array.length(); ++i)
                {
                    stations.add(Station.build(array.getJSONObject(i)));
                }
            }
        }
        return stations;
    }

    private static LangPack parseLangPack(JSONObject json) throws JSONException
    {
        if(json.has("langpack"))
        {
            return LangPack.build(json);
        }
        return null;
    }

    public void setStations(List<Station> stations)
    {
        this.stations = stations;
    }

    public LangPack getLangPack()
    {
        return mLangpack;
    }
}
