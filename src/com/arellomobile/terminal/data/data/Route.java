package com.arellomobile.terminal.data.data;

import com.arellomobile.android.libs.cache.ormlite.field.DataType;
import com.arellomobile.android.libs.cache.ormlite.field.DatabaseField;
import com.arellomobile.android.libs.cache.ormlite.table.DatabaseTable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * User: AndreyKo
 * Date: 22.05.12
 */
@DatabaseTable
public class Route implements Serializable, Comparable<Route>
{
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private long operatorId;
    @DatabaseField
    private long routeId;
    @DatabaseField
    private String routeName;
    @DatabaseField
    private String routeNameLocal;
    @DatabaseField
    private String mType;
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private Date mLastSelectedTime;
    @DatabaseField(columnName = "company_id")
    private long companyId;

    private Route()
    {
        mLastSelectedTime = new Date(0);
    }

    public long getOperatorId()
    {
        return operatorId;
    }

    public long getRouteId()
    {
        return routeId;
    }

    public String getRouteName()
    {
        return routeName;
    }

    public String getRouteNameLocal()
    {
        return routeNameLocal;
    }


    public static Route build(JSONObject json) throws JSONException
    {
        Route result = new Route();
        result.operatorId = Long.valueOf(json.getString("operator_id"));
        result.routeId = Long.valueOf(json.getString("id"));
        result.routeName = json.getString("route_name");
        result.routeNameLocal = json.optString("route_name_local");
        result.mType = json.optString("type");
        result.companyId = result.operatorId;

        return result;
    }

    @Override
    public int compareTo(Route route)
    {
        int compare = -mLastSelectedTime.compareTo(route.mLastSelectedTime);
        if(compare != 0)
        {
//            return compare;
        }

        return routeName.compareToIgnoreCase(route.routeName);
    }

    @Override
    public String toString()
    {
        return routeName; // TODO
    }

    public String getType()
    {
        return mType;
    }

    public void setLastSelectedTime()
    {
        mLastSelectedTime = new Date();
    }

    public Date getLastSelectedTime()
    {
        return mLastSelectedTime;
    }

    public void setLastSelectedTime(Date lastSelectedTime)
    {
        mLastSelectedTime = lastSelectedTime;
    }

}
