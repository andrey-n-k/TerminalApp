package com.arellomobile.terminal.data.data;

import com.arellomobile.android.libs.cache.ormlite.field.DatabaseField;
import com.arellomobile.android.libs.cache.ormlite.table.DatabaseTable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * User: AndreyKo
 * Date: 06.06.12
 */
@DatabaseTable
public class Passenger implements Serializable
{
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private boolean isMale;
    @DatabaseField
    private boolean isLocal;
    @DatabaseField
    private String age;
    @DatabaseField
    private long fromStationId;
    @DatabaseField
    private long toStationId;
    @DatabaseField
    private String name;
    @DatabaseField
    private String status;
    @DatabaseField
    private long bid;

    protected Passenger()
    {
    }

    protected Passenger(boolean isMale, boolean isLocal, String age, long fromStationId, long toStationId)
    {
        this.isMale = isMale;
        this.isLocal = isLocal;
        this.name = "Offline";
        this.status = "BOARDED";

        this.age = age;
        this.fromStationId = fromStationId;
        this.toStationId = toStationId;
    }

    public boolean isMale()
    {
        return isMale;
    }

    public boolean isLocal()
    {
        return isLocal;
    }

    public String getAge()
    {
        return age;
    }

    public long getToStationId()
    {
        return toStationId;
    }

    public String getName()
    {
        return name;
    }

    public String getStatus()
    {
        return status;
    }

    public long getBid()
    {
        return bid;
    }

    public long getFromStationId()
    {
        return fromStationId;
    }

    protected void build(JSONObject json) throws JSONException
    {
        bid = Long.valueOf(json.getString("bid"));
        name = json.getString("name");
        toStationId = Long.valueOf(json.getString("to"));
        fromStationId = Long.valueOf(json.getString("from"));
        status = json.getString("status");
        isMale = json.getString("gender").equals("m") ? true : false;

        String tmp = json.optString("local");
        if (tmp != null && !tmp.equals(""))
        {
            isLocal = Integer.valueOf(tmp) == 1 ? true : false;
        }
        else
        {
            isLocal = false;
        }
        age = json.optString("age");
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public JSONObject getJSON()
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("bid", bid);
            jsonObject.put("name", name);
            jsonObject.put("to", toStationId);
            jsonObject.put("from", fromStationId);
            jsonObject.put("status", status);
            jsonObject.put("gender", isMale);
            jsonObject.put("local", isLocal ? 1 : 0);
            jsonObject.put("age", age);
        } catch (JSONException e)
        {

        }
        return jsonObject;
    }
}
