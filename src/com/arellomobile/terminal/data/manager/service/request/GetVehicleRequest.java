package com.arellomobile.terminal.data.manager.service.request;

import com.arellomobile.android.libs.network.utils.ServerApiException;
import com.arellomobile.android.libs.network.utils.json.JsonDomRequest;
import com.arellomobile.android.libs.system.log.LogUtils;
import com.arellomobile.terminal.data.data.Vehicle;
import com.arellomobile.terminal.data.manager.service.URLs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: AndreyKo
 * Date: 22.05.12
 */
public class GetVehicleRequest extends JsonDomRequest<List<Vehicle>>
{
    public GetVehicleRequest(long companyId)
    {
        super(URLs.BASIC_URL + URLs.VEHICLE_URL, GET);
        JSONObject obj = new JSONObject();
        try
        {
            obj.put("key", URLs.KEY);
            obj.put("operator_id", String.valueOf(companyId));
        }
        catch (JSONException e)
        {
            Logger.getLogger(LogUtils.getErrorReport("competition build request error", e));
        }
        appendParameter("", obj.toString());
    }

    @Override
    protected List<Vehicle> convertJson(JSONObject json) throws ServerApiException, JSONException
    {
        String status = json.getString("status");
        if (status != null && status.equals("ok"))
        {
            JSONArray array = json.getJSONArray("response");
            List<Vehicle> result = new ArrayList<Vehicle>();

            for (int i = 0; i < array.length(); ++i)
            {
                result.add(Vehicle.build(array.getJSONObject(i)));
            }
            return result;
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
    protected List<Vehicle> convertJson(JSONArray obj) throws ServerApiException, JSONException
    {
        throw new ServerApiException("Invalid server response");
    }
}
