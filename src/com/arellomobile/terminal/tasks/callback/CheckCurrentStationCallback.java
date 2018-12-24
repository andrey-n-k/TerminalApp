package com.arellomobile.terminal.tasks.callback;

import com.arellomobile.terminal.data.data.NearestStation;
import com.arellomobile.terminal.data.data.Station;

import java.util.List;

/**
 * User: MiG35
 * Date: 20.06.12
 * Time: 20:14
 */
public interface CheckCurrentStationCallback extends AsyncTaskCallBack
{
    void setCurrentStations(List<NearestStation> stations);
}
