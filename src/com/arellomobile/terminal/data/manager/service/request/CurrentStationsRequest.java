package com.arellomobile.terminal.data.manager.service.request;

import com.arellomobile.android.libs.network.utils.ServerApiException;
import com.arellomobile.android.libs.network.utils.json.JsonDomRequest;
import com.arellomobile.android.libs.system.log.LogUtils;
import com.arellomobile.terminal.data.data.Language;
import com.arellomobile.terminal.data.data.NearestStation;
import com.arellomobile.terminal.data.data.Station;
import com.arellomobile.terminal.data.manager.service.URLs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: AndreyKo
 * Date: 20.06.12
 * Time: 20:37
 */
public class CurrentStationsRequest extends JsonDomRequest<List<NearestStation>>
{
    public CurrentStationsRequest(String type, double latitude, double longitude)
    {
        super(URLs.BASIC_URL + URLs.CURRENT_STATIONS_URL, GET);
        JSONObject obj = new JSONObject();
        try
        {
            obj.put("key", URLs.KEY);
            obj.put("type", type);
            obj.put("lat", String.valueOf(latitude));
            obj.put("lng", String.valueOf(longitude));
            obj.put("proximity_limit", 5);
        } catch (JSONException e)
        {
            Logger.getLogger(LogUtils.getErrorReport("competition build request error", e));
        }
        appendParameter("", obj.toString());
    }

    @Override
    protected List<NearestStation> convertJson(JSONObject obj) throws ServerApiException, JSONException, IOException
    {
        String status = obj.getString("status");
        if (status != null && status.equals("ok"))
        {
            JSONArray array = obj.getJSONArray("response");
            List<NearestStation> result = new ArrayList<NearestStation>();

            for (int i = 0; i < array.length(); ++i)
            {
                result.add(NearestStation.build(array.getJSONObject(i)));
            }
            return result;
        }
        else
        {
            Object o = obj.get("messages");
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
    protected List<NearestStation> convertJson(JSONArray obj) throws ServerApiException, JSONException, IOException
    {
        throw new ServerApiException("Invalid server response");
    }
}
