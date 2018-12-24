package com.arellomobile.terminal.data.manager.service;

import com.arellomobile.android.libs.network.NetworkException;
import com.arellomobile.android.libs.network.utils.ServerApiException;
import com.arellomobile.terminal.data.data.*;
import com.arellomobile.terminal.data.manager.datamanager.DataManager;

import java.io.IOException;
import java.util.List;

/**
 * @author AndreyKo
 */
public interface ServerApi
{
    public List<Company> getCompany() throws ServerApiException, NetworkException;
    public List<Language> getLanguage() throws ServerApiException, NetworkException;
    public List<Vehicle> getVehicle(long companyId) throws ServerApiException, NetworkException;
    public List<Route> getRoute(long companyId) throws ServerApiException, NetworkException;
    public List<Trip> getTrip(long routeId) throws ServerApiException, NetworkException;

    public TripInfo getHandShakeResponse(DataManager dataSource, long carId, long tripId, String pin) throws ServerApiException, NetworkException;

    TripInfo sentCommit(String commit) throws ServerApiException, NetworkException, IOException;

    List<NearestStation> getCurrentStations(String type, double latitude, double longitude) throws ServerApiException, NetworkException;

    LangPack getLangPack() throws ServerApiException, NetworkException;

}
