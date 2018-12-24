package com.arellomobile.terminal.data.data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * User: AndreyKo
 * Date: 14.06.12
 */
public class OnlinePassenger extends Passenger
{
    private OnlinePassenger() {}

    public static OnlinePassenger getPassenger(JSONObject json)  throws JSONException
    {
        OnlinePassenger result = new OnlinePassenger();
        result.build(json);
        return result;
    }
}
