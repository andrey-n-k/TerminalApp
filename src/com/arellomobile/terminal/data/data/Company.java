package com.arellomobile.terminal.data.data;

import com.arellomobile.android.libs.cache.ormlite.field.DatabaseField;
import com.arellomobile.android.libs.cache.ormlite.table.DatabaseTable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * User: AndreyKo
 * Date: 12.05.12
 */
@DatabaseTable
public class Company implements Serializable, Comparable<Company>
{
    @DatabaseField(id = true)
    private long id;
    @DatabaseField
    private String name;
    @DatabaseField
    private String name_local;
    @DatabaseField
    private String url;

    private Company(){}
    
    public Company(long id, String name)
    {
        this.id = id;
        this.name = name;
    }
    
    public long getId()
    {
        return id;
    }
    public String getName()
    {
        return name;
    }
    public String getNameLocal()
    {
        return name_local;
    }
    public String getUrl()
    {
        return url;
    }

    public static Company build(JSONObject json) throws JSONException
    {
        Company result = new Company();
        result.id = Long.parseLong(json.getString("id"));
        result.name = json.getString("name");
        result.name_local = json.optString("name_local");
        result.url = json.optString("url");

        return result;
    }

    @Override
    public int compareTo(Company company)
    {
        return name.compareToIgnoreCase(company.name);
    }
    
    @Override
    public String toString()
    {
        return name;
    }
}
