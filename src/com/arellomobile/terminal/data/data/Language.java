package com.arellomobile.terminal.data.data;

import com.arellomobile.android.libs.cache.ormlite.field.DatabaseField;
import com.arellomobile.android.libs.cache.ormlite.table.DatabaseTable;
import com.arellomobile.terminal.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * User: AndreyKo
 * Date: 12.05.12
 */
@DatabaseTable
public class Language implements Serializable, Comparable<Language>
{
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private String langID;
    @DatabaseField
    private String name;
    @DatabaseField
    private String locale;

    private Language(){}

    public String getLangID()
    {
        return langID;
    }
    public String getName()
    {
        return name;
    }
    public String getLocale()
    {
        return locale;
    }

    public static Language build(JSONObject json) throws JSONException
    {
        Language result = new Language();
        result.langID = json.getString("id");
        result.name = json.getString("name");
        result.locale = json.optString("locale");

        return result;
    }

    @Override
    public int compareTo(Language language)
    {
        return name.compareToIgnoreCase(language.name);
    }

    @Override
    public String toString()
    {
        return name;
    }
}
