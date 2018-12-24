package com.arellomobile.terminal.tasks.callback;

import com.arellomobile.terminal.data.data.Route;
import com.arellomobile.terminal.data.data.Trip;
import com.arellomobile.terminal.data.data.Vehicle;

import java.util.List;

/**
 * User: AndreyKo
 * Date: 25.05.12
 */
public interface LoadObjectCallBack<T> extends AsyncTaskCallBack
{
    public void setData(T data, Vehicle vehicle, Route route, Trip trip);
}