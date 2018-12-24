package com.arellomobile.terminal.data.manager.service;

import com.arellomobile.android.libs.network.NetworkException;
import com.arellomobile.android.libs.network.utils.BasicServerApiImpl;
import com.arellomobile.android.libs.network.utils.LocalNetworkCache;
import com.arellomobile.android.libs.network.utils.ServerApiException;
import com.arellomobile.terminal.data.data.*;
import com.arellomobile.terminal.data.manager.datamanager.DataManager;
import com.arellomobile.terminal.data.manager.service.request.*;

import java.io.IOException;
import java.util.List;

/**
 * @author AndreyKo
 */
public class TerminalServerApiImpl extends BasicServerApiImpl implements ServerApi
{
    public TerminalServerApiImpl(LocalNetworkCache cache)
    {
        super(cache);
    }

    @Override
    public List<Company> getCompany() throws ServerApiException, NetworkException
    {
        return processRequest(new GetCompanyRequest(), null);
    }

    @Override
    public List<Language> getLanguage() throws ServerApiException, NetworkException
    {
        return processRequest(new GetLanguageRequest(), null);
    }

    @Override
    public List<Vehicle> getVehicle(long companyId) throws ServerApiException, NetworkException
    {
        return processRequest(new GetVehicleRequest(companyId), null);
    }

    @Override
    public List<Route> getRoute(long companyId) throws ServerApiException, NetworkException
    {
        return processRequest(new GetRouteRequest(companyId), null);
    }

    @Override
    public List<Trip> getTrip(long routeId) throws ServerApiException, NetworkException
    {
        return processRequest(new GetTripRequest(routeId), null);
    }

    @Override
    public TripInfo getHandShakeResponse(DataManager dataSource, long carId,
                                         long tripId, String pin) throws ServerApiException, NetworkException
    {
        return processRequest(new HandShakeRequest(dataSource, carId, tripId, pin), null);
    }

    @Override
    public TripInfo sentCommit(String commit) throws ServerApiException, NetworkException, IOException
    {
//        return processRequest(new CommitRequest(commit), null);
        CommitHands commitHands = new CommitHands();

        commitHands.makeEntityBody(commit);
        commitHands.makeHeader();

        return commitHands.send();
    }

    @Override
    public List<NearestStation> getCurrentStations(String type, double latitude,
                                                   double longitude) throws ServerApiException, NetworkException
    {
        return processRequest(new CurrentStationsRequest(type, latitude, longitude), null);
    }

    @Override
    public LangPack getLangPack() throws ServerApiException, NetworkException
    {
        return processRequest(new GetLangPackRequest(), null);
    }
}
