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
public class Vehicle implements Serializable, Comparable<Vehicle>
{
    @DatabaseField
    private long operatorId;
    @DatabaseField(id = true)
    private long carId;
    @DatabaseField
    private String type;
    @DatabaseField
    private String classCar;
    @DatabaseField
    private String routeName;
    @DatabaseField
    private String capacity;
    @DatabaseField
    private String regNo;
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private Date mLastSelectedTime;
    @DatabaseField(columnName = "company_id")
    private long companyId;

    private Vehicle()
    {
        mLastSelectedTime = new Date(0);
    }

    public long getOperatorId()
    {
        return operatorId;
    }

    public long getCarId()
    {
        return carId;
    }

    public String getType()
    {
        return type;
    }

    public String getClassCar()
    {
        return classCar;
    }

    public String getRouteName()
    {
        return routeName;
    }

    public String getCapacity()
    {
        return capacity;
    }

    public String getRegNo()
    {
        return regNo;
    }

    public static Vehicle build(JSONObject json) throws JSONException
    {
        Vehicle result = new Vehicle();
        result.operatorId = Long.valueOf(json.getString("operator_id"));
        result.carId = Long.valueOf(json.getString("id"));
        result.type = json.getString("type");
        result.classCar = json.getString("class");
        result.routeName = json.getString("routename");
        result.capacity = json.getString("capacity");
        result.regNo = json.getString("regno");
        result.companyId = result.operatorId;

        return result;
    }

    @Override
    public int compareTo(Vehicle vehicle)
    {
        int compare = -mLastSelectedTime.compareTo(vehicle.mLastSelectedTime);
        if(compare != 0)
        {
            return compare;
        }

        return Long.valueOf(carId).compareTo(Long.valueOf(vehicle.getCarId()));
    }

    @Override
    public boolean equals(Object o)
    {
        if(o != null && o instanceof Vehicle)
        {
            return carId == ((Vehicle) o).getCarId();
        }
        return false;
    }

    @Override
    public String toString()
    {
        return regNo; // TODO
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
