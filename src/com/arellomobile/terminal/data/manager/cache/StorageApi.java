package com.arellomobile.terminal.data.manager.cache;

import android.content.Context;
import com.arellomobile.android.libs.network.utils.LocalNetworkCache;
import com.arellomobile.terminal.data.data.*;
import com.arellomobile.terminal.data.data.commit.CommitHelperObject;
import com.arellomobile.terminal.service.CommitFileHelper;

import java.sql.SQLException;
import java.util.List;

/**
 * User: AndreyKo
 * Date: 17.05.12
 */
public interface StorageApi extends LocalNetworkCache
{
    public List<Company> getCompanies();
    public Company getCompanyById(long id);
    public void saveCompanies(List<Company> companies);
    public void updateCompany(Company company);

    public List<Language> getLanguages();
    public void saveLanguages(List<Language> languages);

    public List<Vehicle> getVehicles(long id);
    public void saveVehicles(List<Vehicle> vehicles);

    public List<Route> getRoutes(long id);
    public void saveRoutes(List<Route> routes);
    public List<Station> getStations(long id);
    public void saveStations(List<Station> stations, long id);

    public List<Trip> getTrips(long id);
    public void saveTrips(List<Trip> trips);
    
    public void addPin(String pin);
    public boolean checkPin(String pin);

    public List<OnlinePassenger> getOnlinePassengers(long id);
    public void saveOnlinePassengers(List<OnlinePassenger> passengers, long id);
    public List<OfflinePassenger> getOfflinePassengers(long id);
    public void saveOfflinePassengers(List<OfflinePassenger> passengers, long id);
    public void addOfflinePassenger(OfflinePassenger passenger, long id);

    
    public void saveTripInfo(TripInfo tripInfo, long tripId);
    public TripInfo getTripInfo(long tripId);

    Vehicle getVehicleById(long vehicleId) throws SQLException;
    Route getRouteById(long routeId) throws SQLException;
    Trip getTripById(long tripId) throws SQLException;

    String getBookPackTime();
    String getLangPackTime();

    List<CommitHelperObject> getCommitHelperObjects();

    void removeCommitHelpObject(CommitHelperObject commitHelperObject);

    void addCommitHelpObject(CommitHelperObject commit);

    void removeTripInfo();

    void saveLangPack(LangPack langPack);
    LangPack getLangPack();

    void updatePassenger(Passenger passenger);

    void updateVehicle(Vehicle vehicle);

    void updateRoute(Route route);

    void updateTrip(Trip trip);
}
