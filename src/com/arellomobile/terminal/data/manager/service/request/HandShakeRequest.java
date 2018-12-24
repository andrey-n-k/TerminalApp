package com.arellomobile.terminal.data.manager.service.request;

import com.arellomobile.android.libs.network.utils.ServerApiException;
import com.arellomobile.android.libs.network.utils.json.JsonDomRequest;
import com.arellomobile.android.libs.system.log.LogUtils;
import com.arellomobile.terminal.data.data.TripInfo;
import com.arellomobile.terminal.data.manager.datamanager.DataManager;
import com.arellomobile.terminal.data.manager.service.URLs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.logging.Logger;

/**
 * User: AndreyKo
 * Date: 29.05.12
 */
public class HandShakeRequest extends JsonDomRequest<TripInfo>
{
    public HandShakeRequest(DataManager dataSource, long carId, long tripId, String pin)
    {
        super(URLs.BASIC_URL + URLs.COMMIT_URL, GET);
        JSONObject obj = new JSONObject();
        try
        {
            obj.put("key", URLs.KEY);
            obj.put("fleet_id", String.valueOf(carId));
            obj.put("trip_id", String.valueOf(tripId));
            obj.put("pin", pin);
            obj.put("stamp", String.valueOf(Calendar.getInstance().getTimeInMillis() / 1000));   // really is it right?
            obj.put("bookpack", "1111111111");
            obj.put("route", String.valueOf(1));
            obj.put("steward", 1);
            obj.put("route", 1);
        }
        catch (JSONException e)
        {
            Logger.getLogger(LogUtils.getErrorReport("competition build request error", e));
        }
        dataSource.setCommit(obj.toString());
        appendParameter("", obj.toString());
    }

    @Override
    protected TripInfo convertJson(JSONObject json) throws ServerApiException, JSONException
    {
        String status = json.getString("status");
        if (status != null && status.equals("ok"))
        {
            return TripInfo.build(json.getJSONObject("response"));
        }
        else
        {
            Object o = json.get("messages");
            if (o instanceof JSONArray)
            {
                String message = ((JSONArray) o).getJSONObject(0).getString("reason");
                throw new ServerApiException(message);
            }
            else
            {
                throw new ServerApiException("Invalid server response");
            }

        }
    }

    @Override
    protected TripInfo convertJson(JSONArray obj) throws ServerApiException, JSONException
    {
        throw new ServerApiException("Invalid server response");
    }
}
