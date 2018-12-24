package com.arellomobile.terminal.data.manager.service.request;

import com.arellomobile.android.libs.network.utils.ServerApiException;
import com.arellomobile.android.libs.network.utils.json.JsonDomRequest;
import com.arellomobile.android.libs.system.log.LogUtils;
import com.arellomobile.terminal.data.data.LangPack;
import com.arellomobile.terminal.data.manager.service.URLs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Logger;

/**
 * User: AndreyKo
 * Date: 20.06.12
 */
public class GetLangPackRequest extends JsonDomRequest<LangPack>
{
    public GetLangPackRequest()
    {
        super(URLs.BASIC_URL + URLs.LANGPACK_URL, GET);
        JSONObject obj = new JSONObject();
        try
        {
            obj.put("key", URLs.KEY);
            obj.put("langpack", String.valueOf(1111111111));
        }
        catch (JSONException e)
        {
            Logger.getLogger(LogUtils.getErrorReport("competition build request error", e));
        }
        appendParameter("", obj.toString());
    }
    @Override
    protected LangPack convertJson(JSONObject json) throws ServerApiException, JSONException, IOException
    {
        String status = json.getString("status");
        if (status != null && status.equals("ok"))
        {
            if (json.get("response") instanceof  JSONObject)
            {
                return LangPack.build(json.getJSONObject("response"));
            }
            return null;
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
    protected LangPack convertJson(JSONArray obj) throws ServerApiException, JSONException, IOException
    {
        throw new ServerApiException("Invalid server response");
    }
}
