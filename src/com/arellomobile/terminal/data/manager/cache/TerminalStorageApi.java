package com.arellomobile.terminal.data.manager.cache;

import android.content.Context;
import com.arellomobile.android.libs.cache.cache.ORMLiteBasicCache;
import com.arellomobile.android.libs.system.log.LogUtils;
import com.arellomobile.terminal.data.data.*;
import com.arellomobile.terminal.data.data.commit.CommitHelperObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: AndreyKo
 * Date: 17.05.12
 */
public class TerminalStorageApi extends ORMLiteBasicCache<DatabaseHelper> implements StorageApi
{
    private static final Integer sBookPackId = 1;
    private static final Integer sLangPackId = 2;

    public TerminalStorageApi(Context context)
    {
        super(context, new DatabaseHelper(context));
    }

    private final Object companyMutex = new Object();
    private final Object languageMutex = new Object();
    private final Object vehicleMutex = new Object();
    private final Object routeMutex = new Object();
    private final Object stationMutex = new Object();
    private final Object tripMutex = new Object();
    private final Object pinMutex = new Object();
    private final Object onPassMutex = new Object();
    private final Object ofPassMutex = new Object();
    private final Object tripInfoMutex = new Object();
    private final Object mUpdateTimeMutex = new Object();
    private final Object mCommitHelpMutex = new Object();
    private final Object mLangPackCommit = new Object();

    @Override
    public List<Company> getCompanies()
    {
        synchronized (companyMutex)
        {
            try
            {
                return databaseHelper.getCompanyDao().queryForAll();
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
                return null;
            }
        }
    }

    @Override
    public Company getCompanyById(long id)
    {
        synchronized (companyMutex)
        {
            try
            {
                return databaseHelper.getCompanyDao().queryForId(id);
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
                return null;
            }
        }
    }

    @Override
    public void saveCompanies(List<Company> companies)
    {
        synchronized (companyMutex)
        {
            try
            {
                databaseHelper.getCompanyDao().delete(databaseHelper.getCompanyDao().deleteBuilder().prepare());
                for (Company c : companies)
                {
                    databaseHelper.getCompanyDao().create(c);
                }
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
            }
        }
    }

    @Override
    public void updateCompany(Company company)
    {
        synchronized (companyMutex)
        {
            try
            {
                databaseHelper.getCompanyDao().update(company);
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
            }
        }
    }


    @Override
    public List<Language> getLanguages()
    {
        synchronized (languageMutex)
        {
            try
            {
                return databaseHelper.getLanguageDao().queryForAll();
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
                return null;
            }
        }
    }

    @Override
    public void saveLanguages(List<Language> languages)
    {
        synchronized (languageMutex)
        {
            try
            {
                databaseHelper.getLanguageDao().delete(databaseHelper.getLanguageDao().deleteBuilder().prepare());
                for (Language l : languages)
                {
                    databaseHelper.getLanguageDao().create(l);
                }
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
            }
        }
    }

    @Override
    public List<Vehicle> getVehicles(long id)
    {
        synchronized (vehicleMutex)
        {
            try
            {
                List<Vehicle> gh = databaseHelper.getVehicleDao()
                        .query(databaseHelper.getVehicleDao().queryBuilder().where().eq("company_id", id).prepare());
                return gh;
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
                return null;
            }
        }
    }

    @Override
    public void saveVehicles(List<Vehicle> vehicles)
    {
        synchronized (vehicleMutex)
        {
            long id = vehicles.get(0).getOperatorId();
            try
            {
                List<Vehicle> oldVehicles = databaseHelper.getVehicleDao()
                        .query(databaseHelper.getVehicleDao().queryBuilder().where().eq("company_id", id).prepare());

                for (Vehicle v : vehicles)
                {
                    int index = oldVehicles.indexOf(v);
                    if (-1 != index)
                    {
                        databaseHelper.getVehicleDao().update(v);
                        Vehicle oldVehicle = oldVehicles.get(index);
                        v.setLastSelectedTime(oldVehicle.getLastSelectedTime());
                        oldVehicles.remove(index);
                    }
                    else
                    {
                        databaseHelper.getVehicleDao().create(v);
                    }
                }

                databaseHelper.getVehicleDao().delete(oldVehicles);
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
            }
        }
    }

    @Override
    public List<Route> getRoutes(long id)
    {
        synchronized (routeMutex)
        {
            try
            {
                List<Route> gh = databaseHelper.getRouteDao()
                        .query(databaseHelper.getRouteDao().queryBuilder().where().eq("company_id", id).prepare());

                return gh;
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
                return null;
            }
        }
    }

    @Override
    public void saveRoutes(List<Route> routes)
    {
        synchronized (routeMutex)
        {
            long id = routes.get(0).getOperatorId();
            try
            {
                List<Route> oldRoutes = databaseHelper.getRouteDao().query(
                        databaseHelper.getRouteDao().queryBuilder().where().eq(
                                "company_id", id).prepare());

                for (Route r : routes)
                {
                    int index = oldRoutes.indexOf(r);
                    if (-1 != index)
                    {
                        databaseHelper.getRouteDao().update(r);
                        Route oldVehicle = oldRoutes.get(index);
                        r.setLastSelectedTime(oldVehicle.getLastSelectedTime());
                        oldRoutes.remove(index);
                        oldRoutes.remove(index);
                    }
                    else
                    {
                        databaseHelper.getRouteDao().create(r);
                    }
                }

                databaseHelper.getRouteDao().delete(oldRoutes);
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
            }
        }
    }

    @Override
    public List<Station> getStations(long id)
    {
        synchronized (stationMutex)
        {
            try
            {
                List<Station> gh = databaseHelper.getStationDao()
                        .query(databaseHelper.getStationDao().queryBuilder().where().eq("trip_id", id).prepare());
                return gh;
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
                return null;
            }
        }
    }

    @Override
    public void saveStations(List<Station> stations, long id)
    {
        synchronized (stationMutex)
        {
            try
            {
                databaseHelper.getStationDao().delete(databaseHelper.getStationDao()
                                                              .query(databaseHelper.getStationDao().queryBuilder()
                                                                             .where()
                                                                             .eq("trip_id", id).prepare()));
                for (Station s : stations)
                {
                    s.setTripId(id);
                    databaseHelper.getStationDao().create(s);
                }
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
            }
        }
    }

    @Override
    public List<Trip> getTrips(long id)
    {
        synchronized (tripMutex)
        {
            try
            {
                List<Trip> gh = databaseHelper.getTripDao()
                        .query(databaseHelper.getTripDao().queryBuilder().where().eq("route_id", id).prepare());
                for (Trip t : gh)
                {
                    t.setStations(getStations(t.getId()));
                }
                return gh;
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
                return null;
            }
        }
    }

    @Override
    public void saveTrips(List<Trip> trips)
    {
        synchronized (tripMutex)
        {
            long id = trips.get(0).getRouteId();
            try
            {
                List<Trip> oldTrips = databaseHelper.getTripDao()
                        .query(databaseHelper.getTripDao().queryBuilder().where()
                                       .eq("route_id", id).prepare());

                for (Trip trip : trips)
                {
                    int index = oldTrips.indexOf(trip);
                    if (-1 != index)
                    {
                        databaseHelper.getTripDao().update(trip);
                        Trip oldVehicle = oldTrips.get(index);
                        if(trip.getLastSelectedTime().getTime() < oldVehicle.getLastSelectedTime().getTime())
                        {
                            trip.setLastSelectedTime(oldVehicle.getLastSelectedTime());
                        }
                        oldTrips.remove(index);
                    }
                    else
                    {
                        databaseHelper.getTripDao().create(trip);
                    }
                }

                databaseHelper.getTripDao().delete(oldTrips);
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
            }
        }
    }

    @Override
    public void addPin(String pin)
    {
        synchronized (pinMutex)
        {
            try
            {
                int n = databaseHelper.getPinDao().create(new PinMD5(pin));
                if (n != 1)
                {
                    log.warning("Alarm already exists");
                }
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
            }
        }
    }

    @Override
    public boolean checkPin(String pin)
    {
        synchronized (pinMutex)
        {
            List<PinMD5> list;
            try
            {
                list = databaseHelper.getPinDao().queryForAll();
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
                return false;
            }

            if (list != null && list.size() > 0)
            {
                for (PinMD5 p : list)
                {
                    if (p.getHashString().equals(pin))
                    {
                        return true;
                    }
                }
                return false;
            }
            else
            {
                return false;
            }
        }
    }

    @Override
    public List<OnlinePassenger> getOnlinePassengers(long id)
    {
        synchronized (onPassMutex)
        {
            try
            {
                List<OnlinePassenger> gh = databaseHelper.getOnlinePassengerDao().queryForAll();
                return gh;
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
                return null;
            }
        }
    }

    @Override
    public void saveOnlinePassengers(List<OnlinePassenger> passengers, long id)
    {
        synchronized (onPassMutex)
        {
            try
            {
                databaseHelper.getOnlinePassengerDao().delete(databaseHelper.getOnlinePassengerDao().queryForAll());

                for (OnlinePassenger p : passengers)
                {
                    databaseHelper.getOnlinePassengerDao().create(p);
                }

                UpdateTime updateTime = new UpdateTime(sBookPackId, String.valueOf(new Date().getTime() / 1000));
                try
                {
                    databaseHelper.getUpdateTimeDao().create(updateTime);
                } catch (Exception e)
                {
                    databaseHelper.getUpdateTimeDao().update(updateTime);
                }
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
            }
        }
    }

    @Override
    public List<OfflinePassenger> getOfflinePassengers(long id)
    {
        synchronized (ofPassMutex)
        {
            try
            {
                List<OfflinePassenger> gh = databaseHelper.getOfflinePassengerDao().queryForAll();
                return gh;
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
                return null;
            }
        }
    }

    @Override
    public void saveOfflinePassengers(List<OfflinePassenger> passengers, long id)
    {
        synchronized (ofPassMutex)
        {
            try
            {
                databaseHelper.getOfflinePassengerDao().delete(databaseHelper.getOfflinePassengerDao().queryForAll());

                for (OfflinePassenger p : passengers)
                {
                    databaseHelper.getOfflinePassengerDao().create(p);
                }

                UpdateTime updateTime = new UpdateTime(sBookPackId, String.valueOf(new Date().getTime() / 1000));
                try
                {
                    databaseHelper.getUpdateTimeDao().create(updateTime);
                } catch (Exception e)
                {
                    databaseHelper.getUpdateTimeDao().update(updateTime);
                }
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
            }
        }
    }

    @Override
    public void addOfflinePassenger(OfflinePassenger passenger, long id)
    {
        synchronized (ofPassMutex)
        {
            try
            {
                List<OfflinePassenger> gh = databaseHelper.getOfflinePassengerDao().queryForAll();
                gh.add(passenger);
                databaseHelper.getOfflinePassengerDao().delete(databaseHelper.getOfflinePassengerDao().queryForAll());
                for (OfflinePassenger p : gh)
                {
                    databaseHelper.getOfflinePassengerDao().create(p);
                }
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
            }
        }
    }

    @Override
    public void saveTripInfo(TripInfo tripInfo, long tripId)
    {
        synchronized (tripInfoMutex)
        {
            try
            {
                databaseHelper.getTripInfoDao().delete(new TripInfo());
                databaseHelper.getTripInfoDao().create(tripInfo);
                saveOnlinePassengers(tripInfo.getOnlinePassengers(), tripInfo.getId());
//                saveOfflinePassengers(tripInfo.getOfflinePassengers(), tripInfo.getId());
                if (tripInfo.getStations() != null && tripInfo.getStations().size() > 0)
                {
                    saveStations(tripInfo.getStations(), tripId);
                }
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
            }
        }
    }

    @Override
    public TripInfo getTripInfo(long tripId)
    {
        synchronized (tripInfoMutex)
        {
            try
            {
                List<TripInfo> tripInfos = databaseHelper.getTripInfoDao().queryForAll();
                if (tripInfos != null && tripInfos.size() > 0)
                {
                    TripInfo tripInfo = tripInfos.get(0);
                    tripInfo.setOnlinePassengers(databaseHelper.getOnlinePassengerDao().queryForAll());
                    tripInfo.setStations(getStations(tripId));
//                    tripInfo.setOfflinePassengers(databaseHelper.getOfflinePassengerDao().queryForAll());
                    return tripInfo;
                }
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
            }
            return null;
        }
    }

    @Override
    public Vehicle getVehicleById(long vehicleId) throws SQLException
    {
        synchronized (vehicleMutex)
        {
            try
            {
                Vehicle vehicle = databaseHelper.getVehicleDao().queryForId(vehicleId);
                if (null == vehicle)
                {
                    throw new SQLException();
                }
                return vehicle;
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
                throw e;
            }
        }
    }

    @Override
    public Route getRouteById(long routeId) throws SQLException
    {
        synchronized (routeMutex)
        {
            try
            {
                Route route = databaseHelper.getRouteDao().query(databaseHelper.getRouteDao().queryBuilder()
                                                                         .where().eq("routeId", routeId).prepare())
                        .get(0);
                return route;
            } catch (Exception e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
                throw new SQLException();
            }
        }
    }

    @Override
    public Trip getTripById(long tripId) throws SQLException
    {
        synchronized (tripMutex)
        {
            try
            {
                Trip trip = databaseHelper.getTripDao().queryForId(tripId);
                if (null == trip)
                {
                    throw new SQLException();
                }
                trip.setStations(getStations(trip.getId()));
                return trip;
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
                throw e;
            }
        }
    }

    @Override
    public String getBookPackTime()
    {
        synchronized (mUpdateTimeMutex)
        {
            try
            {
                UpdateTime updateTime = databaseHelper.getUpdateTimeDao().queryForId(sBookPackId);
                if (null != updateTime)
                {
                    return updateTime.getLastUpdateTime();
                }
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
            }
            return null;
        }
    }

    @Override
    public String getLangPackTime()
    {
        synchronized (mUpdateTimeMutex)
        {
            try
            {
                UpdateTime updateTime = databaseHelper.getUpdateTimeDao().queryForId(sLangPackId);
                if (null != updateTime)
                {
                    return updateTime.getLastUpdateTime();
                }
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
            }
            return null;
        }
    }

    @Override
    public List<CommitHelperObject> getCommitHelperObjects()
    {
        synchronized (mCommitHelpMutex)
        {
            try
            {
                return databaseHelper.getCommitHelpDao().queryForAll();
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
            }
            return new ArrayList<CommitHelperObject>();
        }
    }

    @Override
    public void removeCommitHelpObject(CommitHelperObject commitHelperObject)
    {
        synchronized (mCommitHelpMutex)
        {
            try
            {
                databaseHelper.getCommitHelpDao().delete(commitHelperObject);
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
            }
        }
    }

    @Override
    public void addCommitHelpObject(CommitHelperObject commit)
    {
        synchronized (mCommitHelpMutex)
        {
            try
            {
                List<CommitHelperObject> commitHelperObjects = getCommitHelperObjects();

                if (commitHelperObjects.contains(commit))
                {
                    mergeToCommitFiles(commit, commitHelperObjects.get(commitHelperObjects.indexOf(commit)));
                }
                try
                {
                    databaseHelper.getCommitHelpDao().create(commit);
                } catch (SQLException e)
                {
                    databaseHelper.getCommitHelpDao().update(commit);
                }

            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
            }
        }
    }

    @Override
    public void removeTripInfo()
    {
        try
        {
            databaseHelper.getTripInfoDao().delete(new TripInfo());
            databaseHelper.getOfflinePassengerDao().delete(databaseHelper.getOfflinePassengerDao().queryForAll());
            databaseHelper.getOnlinePassengerDao().delete(databaseHelper.getOnlinePassengerDao().queryForAll());
        } catch (SQLException e)
        {
            // pass
        }
    }

    @Override
    public void saveLangPack(LangPack langPack)
    {
        synchronized (mLangPackCommit)
        {
            try
            {
                databaseHelper.getLangPackDao().delete(databaseHelper.getLangPackDao().queryForAll());
                databaseHelper.getLangPackDao().create(langPack);
                UpdateTime updateTime = new UpdateTime(sLangPackId, String.valueOf(new Date().getTime() / 1000));
                try
                {
                    databaseHelper.getUpdateTimeDao().create(updateTime);
                } catch (Exception e)
                {
                    databaseHelper.getUpdateTimeDao().update(updateTime);
                }
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
            }
        }
    }

    @Override
    public LangPack getLangPack()
    {
        synchronized (mLangPackCommit)
        {
            try
            {
                List<LangPack> tmp = databaseHelper.getLangPackDao().queryForAll();
                if (tmp != null && tmp.size() > 0)
                {
                    return tmp.get(0);
                }
                return null;
            } catch (SQLException e)
            {
                log.warning(LogUtils.getErrorReport(e.getMessage(), e));
            }
            return null;
        }
    }

    @Override
    public void updatePassenger(Passenger passenger)
    {
        try
        {
            if (passenger instanceof OnlinePassenger)
            {
                databaseHelper.getOnlinePassengerDao().update((OnlinePassenger) passenger);
            }
            else if (passenger instanceof OfflinePassenger)
            {
                databaseHelper.getOfflinePassengerDao().update((OfflinePassenger) passenger);
            }
        } catch (SQLException e)
        {
            // pass
        }
    }

    @Override
    public void updateVehicle(Vehicle vehicle)
    {
        try
        {
            databaseHelper.getVehicleDao().update(vehicle);
        } catch (SQLException e)
        {
            // pass
        }
    }

    @Override
    public void updateRoute(Route route)
    {
        try
        {
            databaseHelper.getRouteDao().update(route);
        } catch (SQLException e)
        {
            // pass
        }
    }

    @Override
    public void updateTrip(Trip trip)
    {
        try
        {
            databaseHelper.getTripDao().update(trip);
        } catch (SQLException e)
        {
            // pass
        }
    }

    private void mergeToCommitFiles(CommitHelperObject toCommit, CommitHelperObject fromCommit)
    {
        toCommit.setId(fromCommit.getId());
        toCommit.setTimeStamp(String.valueOf(new Date().getTime() / 1000));

        JSONArray toTrackArray = toCommit.getTrackJsonArray();
        JSONArray fromTrackArray = fromCommit.getTrackJsonArray();
        mergeToArray(toTrackArray, fromTrackArray);
        toCommit.setTrackArray(toTrackArray.toString());

        JSONArray toCheckInArray = toCommit.getCheckInJsonArray();
        JSONArray fromCheckInArray = fromCommit.getCheckInJsonArray();
        removeEqualsCheckIns(toCheckInArray, fromCheckInArray);
        toCommit.setCheckin(toCheckInArray.toString());

        if ("1111111111".equals(fromCommit.getBookPackTimeStamp()))
        {
            toCommit.setBookPackTimeStamp(fromCommit.getBookPackTimeStamp());
        }
        if ("1111111111".equals(fromCommit.getLangPackTimeStamp()))
        {
            toCommit.setLangPackTimeStamp(fromCommit.getLangPackTimeStamp());
        }
        if (fromCommit.getSteward())
        {
            toCommit.setSteward(true);
        }
        if (null != fromCommit.getRoute() && null == toCommit.getRoute())
        {
            toCommit.setRoute(fromCommit.getRoute());
        }
    }

    private void removeEqualsCheckIns(JSONArray toCheckInArray, JSONArray fromCheckInArray)
    {
        JSONArray fromCheckInFiltered = new JSONArray();

        for (int i = 0; i < fromCheckInArray.length(); ++i)
        {
            try
            {
                JSONObject fromCheckIn = fromCheckInArray.getJSONObject(i);
                JSONObject toCheckIn = null;
                for (int j = 0; j < toCheckInArray.length(); ++j)
                {
                    JSONObject toCheckInTmp = toCheckInArray.getJSONObject(i);

                    if (fromCheckIn.getInt("bid") == toCheckInTmp.getInt("bid"))
                    {
                        toCheckIn = toCheckInTmp;
                        break;
                    }
                }

                if (null == toCheckIn)
                {
                    fromCheckInFiltered.put(fromCheckIn);
                }
            } catch (JSONException e)
            {
                // pass
            }
        }

        mergeToArray(toCheckInArray, fromCheckInFiltered);
    }

    private void mergeToArray(JSONArray toTrackArray, JSONArray fromTrackArray)
    {
        try
        {
            for (int i = 0; i < fromTrackArray.length(); ++i)
            {
                toTrackArray.put(fromTrackArray.getJSONObject(i));
            }
        } catch (JSONException e)
        {
            // pass
        }
    }
}
