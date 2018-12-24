package com.arellomobile.terminal.data.manager.langservice;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * User: AndreyKo
 * Date: 20.06.12
 */
public class JsonFactory implements LangFactory
{
    private JSONObject mJson;
    private String mLanId;
    private Context mContext;

    private JsonFactory()
    {
    }

    public JsonFactory(String json, String langId, Context context) throws JSONException
    {
        mJson = new JSONObject(json);
        mLanId = langId;
        mContext = context;
    }

    @Override
    public void setLangid(String langId)
    {
        mLanId = langId;
    }

    @Override
    public void setJson(String json)
    {
        try
        {
            mJson = new JSONObject(json);
        } catch (Exception e)
        {
            // pass
        }
    }

    @Override
    public String getString(String label)
    {
        if (mJson != null)
        {
            JSONObject obj = mJson.optJSONObject(label);
            if (obj != null)
            {
                if (obj.optString(mLanId) != null && obj.optString(mLanId).trim().length() > 0)
                {
                    try
                    {
                        return obj.getString(mLanId);
                    } catch (JSONException e)
                    {
                        // pass
                    }
                }
                if (obj.optString("en") != null && obj.optString("en").trim().length() > 0)
                {
                    try
                    {
                        return obj.getString("en");
                    } catch (JSONException e)
                    {
                        // pass
                    }
                }
            }
        }
        return mContext.getResources()
                .getString(mContext.getResources().getIdentifier(label, "string", mContext.getPackageName()));
    }
}
