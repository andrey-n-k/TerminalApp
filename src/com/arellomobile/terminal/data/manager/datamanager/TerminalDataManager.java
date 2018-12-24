package com.arellomobile.terminal.data.manager.datamanager;

import android.content.Context;
import android.content.SharedPreferences;
import com.arellomobile.terminal.R;
import com.arellomobile.terminal.data.manager.langservice.LangFactory;
import com.arellomobile.terminal.helper.MD5Pin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * User: AndreyKo
 * Date: 12.05.12
 */
public class TerminalDataManager implements DataManager
{
    private Logger log = Logger.getLogger(getClass().getName());

    private String mCommit;
    private int mCommitCount;

    private static Map<String, Integer> flags;

    static
    {
        flags = new HashMap<String, Integer>();
        flags.put("th", R.drawable.th);
        flags.put("cn", R.drawable.cn);
        flags.put("de", R.drawable.de);
        flags.put("fr", R.drawable.fr);
        flags.put("hi", R.drawable.hi);
        flags.put("jp", R.drawable.jp);
        flags.put("ru", R.drawable.ru);
        flags.put("en", R.drawable.en);
    }

    public TerminalDataManager(Context context)
    {
        mCommitCount = 0;
    }

    @Override
    public void savePreferences(String langId, long companyId, String password, Context context)
    {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = pref.edit();
        if (prefEditor != null)
        {
            prefEditor.putString(LANGUAGE_APP_ID, langId);
            prefEditor.putLong(COMPANY_ID, companyId);
            prefEditor.putString(PASSWORD, password);
            prefEditor.commit();
        }
    }

    @Override
    public void setLanguageAppId(Context context, String languageId)
    {
        SharedPreferences.Editor prefEditor = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit();
        if (prefEditor != null)
        {
            prefEditor.putString(LANGUAGE_APP_ID, languageId);
            prefEditor.commit();
        }
    }

    @Override
    public void setCompanyId(Context context, long companyId)
    {
        SharedPreferences.Editor prefEditor = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit();
        if (prefEditor != null)
        {
            prefEditor.putLong(COMPANY_ID, companyId);
            prefEditor.commit();
        }
    }

    @Override
    public void logInApp(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = pref.edit();
        if (prefEditor != null)
        {
            prefEditor.putBoolean(LOG_IN, true);
            prefEditor.commit();
        }
    }

    @Override
    public void logOutApp(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = pref.edit();
        if (prefEditor != null)
        {
            prefEditor.putBoolean(LOG_IN, false);
            prefEditor.commit();
        }
    }

    @Override
    public long getCompanyId(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return pref.getLong(COMPANY_ID, -1);
    }

    @Override
    public String getLanguage(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return pref.getString(LANGUAGE_APP_ID, "en");
    }

    @Override
    public boolean isLogIn(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return pref.getBoolean(LOG_IN, false);
    }

    @Override
    public Integer getItemFlag(String lang)
    {
        return flags.get(lang);
    }

    @Override
    public String getMD5Pin(String str)
    {
        try
        {
            return MD5Pin.getMD5Pin(MIXER + str);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public void setCommit(String commit)
    {
        mCommit = commit;
    }

    @Override
    public String getCommit()
    {
        String commit = mCommit;
        mCommit = null;
        return commit;
    }

    @Override
    public synchronized void setCommitCount(int commitCount)
    {
        mCommitCount = commitCount;
    }

    @Override
    public synchronized int getCommitCount()
    {
        return mCommitCount;
    }

    @Override
    public void setTripInfo(long vehicleId, long routeId, long tripId, String md5pin, Context context)
    {
        SharedPreferences pref = context.getSharedPreferences(TRIP_INFO_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = pref.edit();
        if (prefEditor != null)
        {
            prefEditor.putLong(VEHICLE_ID, vehicleId);
            prefEditor.putLong(ROUTE_ID, routeId);
            prefEditor.putLong(TRIP_ID, tripId);
            prefEditor.putString(MD5PIN_ID, md5pin);
            prefEditor.commit();
        }
    }

    @Override
    public long getVehicleId(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences(TRIP_INFO_PREFERENCES, Context.MODE_PRIVATE);
        return pref.getLong(VEHICLE_ID, -1);
    }

    @Override
    public long getRouteId(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences(TRIP_INFO_PREFERENCES, Context.MODE_PRIVATE);
        return pref.getLong(ROUTE_ID, -1);
    }

    @Override
    public long getTripId(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences(TRIP_INFO_PREFERENCES, Context.MODE_PRIVATE);
        return pref.getLong(TRIP_ID, -1);
    }

    @Override
    public String getMD5PinId(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences(TRIP_INFO_PREFERENCES, Context.MODE_PRIVATE);
        return pref.getString(MD5PIN_ID, null);
    }

    @Override
    public String getPin(Context context)
    {
//        return "86f5eee57b128767299d2503146b970a";
        SharedPreferences pref = context.getSharedPreferences(TRIP_INFO_PREFERENCES, Context.MODE_PRIVATE);
        return pref.getString(MD5PIN_ID, null);
    }
}
