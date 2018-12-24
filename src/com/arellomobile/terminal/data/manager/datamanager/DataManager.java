package com.arellomobile.terminal.data.manager.datamanager;

import android.content.Context;
import com.arellomobile.android.libs.network.NetworkException;
import com.arellomobile.android.libs.network.utils.ServerApiException;
import com.arellomobile.terminal.data.data.*;

import java.util.List;

/**
 * User: AndreyKo
 * Date: 12.05.12
 */
public interface DataManager
{
    public static final String PREFERENCES = "TerminalPreferences";
    public static final String TRIP_INFO_PREFERENCES = "TerminalPreferences:tripInfo";

    public final static String LANGUAGE_APP_ID = "languageAppId";
    public final static String COMPANY_ID = "companyId";
    public final static String PASSWORD = "password";
    public final static String LOG_IN = "log_in";

    public final static String VEHICLE_ID = "vehicleId";
    public final static String ROUTE_ID = "routeId";
    public final static String TRIP_ID = "tripId";
    public static final String MD5PIN_ID = "md5pin_id";

    public final static String MIXER = "mixer";

    public void savePreferences(String langId, long companyId, String password, Context context);
    public void setLanguageAppId(Context context, String languageId);
    public void setCompanyId(Context context, long companyId);
    public void logInApp(Context context);
    public void logOutApp(Context context);

    public long getCompanyId(Context context);
    public String getLanguage(Context context);
    public boolean isLogIn(Context context);
    public Integer getItemFlag(String lang);

    public String getMD5Pin(String str);
    
    public void setCommit(String commit);
    public String getCommit();
    void setCommitCount(int commitCount);
    public int getCommitCount();
    
    public void setTripInfo(long vehicleId, long routeId, long tripId, String md5pin, Context context);
    public long getVehicleId(Context context);
    public long getRouteId(Context context);
    public long getTripId(Context context);
    public String getMD5PinId(Context context);

    String getPin(Context context);

}
