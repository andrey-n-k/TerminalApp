package com.arellomobile.terminal.data.manager.cache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.arellomobile.android.libs.cache.cache.BasicOrmLiteOpenHelper;
import com.arellomobile.android.libs.cache.ormlite.dao.Dao;
import com.arellomobile.android.libs.cache.ormlite.support.ConnectionSource;
import com.arellomobile.android.libs.cache.ormlite.table.TableUtils;
import com.arellomobile.android.libs.system.log.LogUtils;
import com.arellomobile.terminal.data.data.*;
import com.arellomobile.terminal.data.data.commit.CommitHelperObject;

import java.sql.SQLException;

/**
 * User: AndreyKo
 * Date: 17.05.12
 */
public class DatabaseHelper extends BasicOrmLiteOpenHelper
{
    private static final String DATABASE_NAME = "TerminalAndroid.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<Company, Long> companyDao;
    private Dao<Language, Integer> languageDao;
    private Dao<Vehicle, Long> vehicleDao;
    private Dao<Route, Long> routeDao;
    private Dao<Station, Integer> stationDao;
    private Dao<Trip, Long> tripDao;
    private Dao<PinMD5, Integer> pinDao;
    private Dao<OnlinePassenger, Long> onPassengerDao;
    private Dao<OfflinePassenger, Long> ofPassengerDao;
    private Dao<TripInfo, Integer> tripInfoDao;
    private Dao<UpdateTime, Integer> mUpdateTimeDao;
    private Dao<CommitHelperObject, Integer> mCommitHelpObject;
    private Dao<LangPack, Integer> mLangPackDao;

    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource)
    {
        super.onCreate(database, connectionSource);
        try
        {
            log.info("onCreate");
            TableUtils.createTable(connectionSource, Company.class);
            TableUtils.createTable(connectionSource, Language.class);
            TableUtils.createTable(connectionSource, Vehicle.class);
            TableUtils.createTable(connectionSource, Route.class);
            TableUtils.createTable(connectionSource, Station.class);
            TableUtils.createTable(connectionSource, Trip.class);
            TableUtils.createTable(connectionSource, PinMD5.class);
            TableUtils.createTable(connectionSource, OnlinePassenger.class);
            TableUtils.createTable(connectionSource, OfflinePassenger.class);
            TableUtils.createTable(connectionSource, TripInfo.class);
            TableUtils.createTable(connectionSource, UpdateTime.class);
            TableUtils.createTable(connectionSource, CommitHelperObject.class);
            TableUtils.createTable(connectionSource, LangPack.class);
        }
        catch (SQLException e)
        {
            log.severe(LogUtils.getErrorReport("Can't create database", e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion)
    {
        super.onUpgrade(database, connectionSource, oldVersion, newVersion);
        try
        {
            log.info("onUpgrade");
            TableUtils.dropTable(connectionSource, Company.class, true);
            TableUtils.dropTable(connectionSource, Language.class, true);
            TableUtils.dropTable(connectionSource, Vehicle.class, true);
            TableUtils.dropTable(connectionSource, Route.class, true);
            TableUtils.dropTable(connectionSource, Station.class, true);
            TableUtils.dropTable(connectionSource, Trip.class, true);
            TableUtils.dropTable(connectionSource, PinMD5.class, true);
            TableUtils.dropTable(connectionSource, OnlinePassenger.class, true);
            TableUtils.dropTable(connectionSource, OfflinePassenger.class, true);
            TableUtils.dropTable(connectionSource, TripInfo.class, true);
            TableUtils.dropTable(connectionSource, UpdateTime.class, true);
            TableUtils.dropTable(connectionSource, CommitHelperObject.class, true);
            TableUtils.dropTable(connectionSource, LangPack.class, true);
            // after we drop the old databases, we create the new ones
            onCreate(database, connectionSource);
        }
        catch (SQLException e)
        {
            log.severe(LogUtils.getErrorReport("Can't drop databases", e));
            throw new RuntimeException(e);
        }
    }

    public synchronized Dao<Company, Long> getCompanyDao() throws SQLException
    {
        if (companyDao == null)
        {
            companyDao = getDao(Company.class);
        }
        return companyDao;
    }

    public synchronized Dao<Language, Integer> getLanguageDao() throws SQLException
    {
        if (languageDao == null)
        {
            languageDao = getDao(Language.class);
        }
        return languageDao;
    }

    public synchronized Dao<Vehicle, Long> getVehicleDao() throws SQLException
    {
        if (vehicleDao == null)
        {
            vehicleDao = getDao(Vehicle.class);
        }
        return vehicleDao;
    }

    public synchronized Dao<Route, Long> getRouteDao() throws SQLException
    {
        if (routeDao == null)
        {
            routeDao = getDao(Route.class);
        }
        return routeDao;
    }

    public synchronized Dao<Station, Integer> getStationDao() throws SQLException
    {
        if (stationDao == null)
        {
            stationDao = getDao(Station.class);
        }
        return stationDao;
    }

    public synchronized Dao<Trip, Long> getTripDao() throws SQLException
    {
        if (tripDao == null)
        {
            tripDao = getDao(Trip.class);
        }
        return tripDao;
    }

    public synchronized Dao<PinMD5, Integer> getPinDao() throws SQLException
    {
        if (pinDao == null)
        {
            pinDao = getDao(PinMD5.class);
        }
        return pinDao;
    }

    public synchronized Dao<OnlinePassenger, Long> getOnlinePassengerDao() throws SQLException
    {
        if (onPassengerDao == null)
        {
            onPassengerDao = getDao(OnlinePassenger.class);
        }
        return onPassengerDao;
    }

    public synchronized Dao<OfflinePassenger, Long> getOfflinePassengerDao() throws SQLException
    {
        if (ofPassengerDao == null)
        {
            ofPassengerDao = getDao(OfflinePassenger.class);
        }
        return ofPassengerDao;
    }

    public synchronized Dao<TripInfo, Integer> getTripInfoDao() throws SQLException
    {
        if (tripInfoDao == null)
        {
            tripInfoDao = getDao(TripInfo.class);
        }
        return tripInfoDao;
    }

    public synchronized Dao<UpdateTime, Integer> getUpdateTimeDao() throws SQLException
    {
        if (mUpdateTimeDao == null)
        {
            mUpdateTimeDao = getDao(UpdateTime.class);
        }
        return mUpdateTimeDao;
    }

    public synchronized Dao<CommitHelperObject, Integer> getCommitHelpDao() throws SQLException
    {
        if (mCommitHelpObject == null)
        {
            mCommitHelpObject = getDao(CommitHelperObject.class);
        }
        return mCommitHelpObject;
    }

    public synchronized Dao<LangPack, Integer> getLangPackDao() throws SQLException
    {
        if (mLangPackDao == null)
        {
            mLangPackDao = getDao(LangPack.class);
        }
        return mLangPackDao;
    }
}
