package com.arellomobile.terminal.tasks.callback;

import com.arellomobile.terminal.data.data.Vehicle;

import java.util.List;

/**
 * User: AndreyKo
 * Date: 25.05.12
 */
public interface LoadListCallBack<T> extends AsyncTaskCallBack
{
    public void setData(List<T> data);
}
