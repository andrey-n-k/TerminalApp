package com.arellomobile.terminal.data.data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * User: AndreyKo
 * Date: 14.06.12
 */
public class OfflinePassenger extends Passenger
{
    private OfflinePassenger() {}

    public OfflinePassenger(boolean isMale, boolean isLocal, String age, long fromStationId, long toStationId)
    {
        super(isMale, isLocal, age, fromStationId, toStationId);
    }

    public static OfflinePassenger getPassenger(JSONObject json)  throws JSONException
    {
        OfflinePassenger result = new OfflinePassenger();
        result.build(json);
        return  result;
    }
}
