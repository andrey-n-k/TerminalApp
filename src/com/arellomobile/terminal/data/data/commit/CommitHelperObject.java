package com.arellomobile.terminal.data.data.commit;

import android.content.res.Configuration;
import com.arellomobile.android.libs.cache.ormlite.field.DatabaseField;
import com.arellomobile.android.libs.cache.ormlite.table.DatabaseTable;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: AndreyKo
 * Date: 15.06.12
 * Time: 13:32
 */
@DatabaseTable(tableName = "CommitHelperObject")
public class CommitHelperObject implements Comparable<CommitHelperObject>
{
    @DatabaseField(id = true)
    private int mId;

    @DatabaseField
    private long mFleetId;
    @DatabaseField
    private long mTripId;
    @DatabaseField
    private String mPin;

    @DatabaseField
    private String mTimeStamp;
    @DatabaseField
    private String mBookPackTimeStamp;
    @DatabaseField
    private String mLangPackTimeStamp;

    @DatabaseField
    private String mTrackJsonArray;
    @DatabaseField
    private String mCheckInJsonArray;
    @DatabaseField
    private boolean mSteward;
    @DatabaseField
    private Integer mRoute;


    @Override
    public boolean equals(Object o)
    {
        if (o != null && o instanceof CommitHelperObject)
        {
            CommitHelperObject otherObject = (CommitHelperObject) o;
            return mFleetId == otherObject.mFleetId && mTripId == otherObject.mTripId && mPin.equals(otherObject.mPin);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return (int) (10 + mFleetId ^ mFleetId >>> 32);
    }

    public void setFleetId(long fleetId)
    {
        mFleetId = fleetId;
    }

    public void setTripId(long tripId)
    {
        mTripId = tripId;
    }

    public void setPin(String pin)
    {
        mPin = pin;
    }

    public void setTimeStamp(String timeStamp)
    {
        mTimeStamp = timeStamp;
    }

    public void setBookPackTimeStamp(String bookPackTimeStamp)
    {
        mBookPackTimeStamp = bookPackTimeStamp;
    }

    public void setTrackArray(String trackArray)
    {
        mTrackJsonArray = trackArray;
    }

    public void setCheckin(String checkin)
    {
        mCheckInJsonArray = checkin;
    }

    public JSONArray getTrackJsonArray()
    {
        if (null != mTrackJsonArray)
        {
            try
            {
                return new JSONArray(mTrackJsonArray);
            } catch (JSONException e)
            {
                Logger.getLogger(getClass().getName())
                        .log(Level.SEVERE, "can't parse json array in commit helper object");
            }
        }
        return new JSONArray();
    }

    @Override
    public int compareTo(CommitHelperObject commitHelperObject)
    {
        return mId - commitHelperObject.mId;
    }

    public long getFleetId()
    {
        return mFleetId;
    }

    public long getTripId()
    {
        return mTripId;
    }

    public String getPin()
    {
        return mPin;
    }

    public String getBookPackTimeStamp()
    {
        return mBookPackTimeStamp;
    }

    public JSONArray getCheckInJsonArray()
    {
        if (null != mCheckInJsonArray)
        {
            try
            {
                return new JSONArray(mCheckInJsonArray);
            } catch (JSONException e)
            {
                Logger.getLogger(getClass().getName())
                        .log(Level.SEVERE, "can't parse json array in commit helper object");
            }
        }
        return new JSONArray();
    }

    public int getId()
    {
        return mId;
    }

    public void setId(int id)
    {
        mId = id;
    }

    public String getTimeStamp()
    {
        return mTimeStamp;
    }

    public void setSteward(boolean steward)
    {
        this.mSteward = steward;
    }

    public boolean getSteward()
    {
        return mSteward;
    }

    public void setRoute(Integer route)
    {
        this.mRoute = route;
    }

    public Integer getRoute()
    {
        return mRoute;
    }

    public void setLangPackTimeStamp(String langPackTimeStamp)
    {
        this.mLangPackTimeStamp = langPackTimeStamp;
    }

    public String getLangPackTimeStamp()
    {
        return mLangPackTimeStamp;
    }
}
