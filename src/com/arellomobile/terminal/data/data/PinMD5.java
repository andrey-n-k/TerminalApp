package com.arellomobile.terminal.data.data;

import com.arellomobile.android.libs.cache.ormlite.field.DatabaseField;
import com.arellomobile.android.libs.cache.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * User: AndreyKo
 * Date: 05.06.12
 */
@DatabaseTable
public class PinMD5 implements Serializable
{
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private String hashString;

    public String getHashString()
    {
        return hashString;
    }

    private PinMD5() {}

    public PinMD5(String str)
    {
        hashString = str;
    }
}
