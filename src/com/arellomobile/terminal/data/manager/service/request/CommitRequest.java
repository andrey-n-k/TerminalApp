package com.arellomobile.terminal.data.manager.service.request;

import android.util.Log;
import com.arellomobile.android.libs.network.utils.ServerApiException;
import com.arellomobile.android.libs.network.utils.json.JsonDomRequest;
import com.arellomobile.terminal.data.data.TripInfo;
import com.arellomobile.terminal.data.manager.service.URLs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * User: AndreyKo
 * Date: 12.06.12
 * Time: 16:08
 */
public class CommitRequest extends JsonDomRequest<TripInfo>
{
    public CommitRequest(String commit)
    {
//        super("http://api.timetable.asia", POST);
        super(URLs.BASIC_URL + URLs.COMMIT_URL, GET);

//        appendParameter("cmd", URLs.COMMIT_URL);
//        appendParameter("q", commit);
	    Log.w("***REQUEST***", commit);
        appendParameter("", commit);
    }

    @Override
    protected TripInfo convertJson(JSONObject json) throws ServerApiException, JSONException, IOException
    {
        return CommitRequest.parseUnswer(json);
    }

    public static TripInfo parseUnswer(JSONObject json) throws ServerApiException, JSONException
    {
        String status = json.getString("status");
        Log.w("***ANSWER***", json.toString());
        if (status != null && status.equals("ok"))
        {
            if (json.opt("response") != null && json.opt("response") instanceof JSONObject)
            {
                return TripInfo.build(json.getJSONObject("response"));
            }
            else
            {
                return null;
            }
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
    protected TripInfo convertJson(JSONArray obj) throws ServerApiException, JSONException, IOException
    {
        throw new ServerApiException("Invalid server response");
    }
}
