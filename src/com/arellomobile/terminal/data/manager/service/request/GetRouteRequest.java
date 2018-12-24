package com.arellomobile.terminal.data.manager.service.request;

import com.arellomobile.android.libs.network.utils.ServerApiException;
import com.arellomobile.android.libs.network.utils.json.JsonDomRequest;
import com.arellomobile.android.libs.system.log.LogUtils;
import com.arellomobile.terminal.data.data.Route;
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
public class GetRouteRequest extends JsonDomRequest<List<Route>>
{
    public GetRouteRequest(long companyId)
    {
        super(URLs.BASIC_URL + URLs.ROUTE_URL, GET);
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
    protected List<Route> convertJson(JSONObject json) throws ServerApiException, JSONException
    {
        String status = json.getString("status");
        if (status != null && status.equals("ok"))
        {
            JSONArray array = json.getJSONArray("response");
            List<Route> result = new ArrayList<Route>();

            for (int i = 0; i < array.length(); ++i)
            {
                result.add(Route.build(array.getJSONObject(i)));
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
    protected List<Route> convertJson(JSONArray obj) throws ServerApiException, JSONException
    {
        throw new ServerApiException("Invalid server response");
    }
}
