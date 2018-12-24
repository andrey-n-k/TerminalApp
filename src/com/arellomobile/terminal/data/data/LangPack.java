package com.arellomobile.terminal.data.data;

import com.arellomobile.android.libs.cache.ormlite.field.DatabaseField;
import com.arellomobile.android.libs.cache.ormlite.table.DatabaseTable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * User: AndreyKo
 * Date: 20.06.12
 */
@DatabaseTable
public class LangPack implements Serializable
{
    @DatabaseField
    private String mJsonString;
    @DatabaseField
    private long mTimeStamp;
    @DatabaseField(generatedId = true)
    private int mId;

    private LangPack() {}

    public String getJsonString()
    {
        return mJsonString;
    }
    public long getTimeStamp()
    {
        return mTimeStamp;
    }

    public static LangPack build(JSONObject json) throws JSONException
    {
        LangPack result = new LangPack();
        result.mTimeStamp = 1111111111;
        result.mJsonString = json.getJSONObject("langpack").toString();

        return result;
    }
}
