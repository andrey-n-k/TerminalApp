package com.arellomobile.terminal.data.data;

import com.arellomobile.android.libs.cache.ormlite.field.DataType;
import com.arellomobile.android.libs.cache.ormlite.field.DatabaseField;
import com.arellomobile.android.libs.cache.ormlite.table.DatabaseTable;

/**
 * User: MiG35
 * Date: 13.06.12
 * Time: 19:36
 */
@DatabaseTable(tableName = "UpdateTime")
public class UpdateTime
{
    @DatabaseField(id = true, columnName = "id")
    private int mId;
    @DatabaseField
    private String mLastUpdateTime;

    private UpdateTime()
    {
    }

    public UpdateTime(Integer id, String updateTime)
    {
        mId = id;
        mLastUpdateTime = updateTime;
    }

    public String getLastUpdateTime()
    {
        return mLastUpdateTime;
    }
}
