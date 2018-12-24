package com.arellomobile.terminal.service;

import com.arellomobile.terminal.data.data.commit.CommitHelperObject;
import com.arellomobile.terminal.data.manager.service.URLs;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * User: AndreyKo
 * Date: 15.06.12
 * Time: 14:33
 */
public class CommitHelperObjectGenerator
{
    private static final String sFleetId = "fleet_id";
    private static final String sTripId = "trip_id";
    private static final String sPin = "pin";

    private static final String sBookPack = "bookpack";
    private static final String sTrack = "track";
    private static final String sCheckIn = "checkin";

    private static final String sSteward = "steward";
    private static final String sRoute = "route";
    private static final String sLangPack = "langpack";

    public static CommitHelperObject generateFromJsonString(String commit)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(commit);

            CommitHelperObject commitHelperObject = new CommitHelperObject();

            commitHelperObject.setFleetId(jsonObject.getInt(sFleetId));
            commitHelperObject.setTripId(jsonObject.getInt(sTripId));
            commitHelperObject.setPin(jsonObject.getString(sPin));

            if (jsonObject.has(sBookPack))
            {
                commitHelperObject.setBookPackTimeStamp(jsonObject.getString(sBookPack));
            }

            if (jsonObject.has(sTrack))
            {
                commitHelperObject.setTrackArray(jsonObject.getString(sTrack));
            }

            if (jsonObject.has(sCheckIn))
            {
                commitHelperObject.setCheckin(jsonObject.getString(sCheckIn));
            }

            if (jsonObject.has(sSteward))
            {
                commitHelperObject.setSteward(jsonObject.getInt(sSteward) == 1);
            }
            if (jsonObject.has(sRoute))
            {
                commitHelperObject.setRoute(jsonObject.getInt(sRoute));
            }

            return commitHelperObject;
        } catch (JSONException e)
        {
            // pass
        }
        return null;
    }

    public static String generateJSONStringFromHelper(CommitHelperObject commitHelperObject)
    {
        try
        {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put(sFleetId, String.valueOf(commitHelperObject.getFleetId()));
            jsonObject.put(sTripId, String.valueOf(commitHelperObject.getTripId()));
            jsonObject.put(sPin, String.valueOf(commitHelperObject.getPin()));

            jsonObject.put("stamp", commitHelperObject.getTimeStamp());

            jsonObject.put("key", URLs.KEY);

            String bookPack = commitHelperObject.getBookPackTimeStamp();
            if (null == bookPack)
            {
                bookPack = "1111111111";
            }
            jsonObject.put(sBookPack, bookPack);

            String langpack = commitHelperObject.getLangPackTimeStamp();
            if (null == langpack)
            {
                langpack = "1111111111";
            }
            jsonObject.put(sLangPack, langpack);

            if (null != commitHelperObject.getTrackJsonArray())
            {
                jsonObject.put(sTrack, commitHelperObject.getTrackJsonArray());
            }

            if (null != commitHelperObject.getCheckInJsonArray())
            {
                jsonObject.put(sCheckIn, commitHelperObject.getCheckInJsonArray());
            }

            if (commitHelperObject.getSteward())
            {
                jsonObject.put(sSteward, String.valueOf(1));
            }

            if (null != commitHelperObject.getRoute())
            {
                jsonObject.put(sRoute, String.valueOf(commitHelperObject.getRoute()));
            }

            return jsonObject.toString();
        } catch (JSONException e)
        {
            return null;
        }
    }
}
