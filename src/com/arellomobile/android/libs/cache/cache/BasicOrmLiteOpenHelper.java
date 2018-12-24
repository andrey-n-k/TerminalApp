package com.arellomobile.android.libs.cache.cache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.arellomobile.android.libs.cache.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.arellomobile.android.libs.cache.ormlite.dao.Dao;
import com.arellomobile.android.libs.cache.ormlite.support.ConnectionSource;
import com.arellomobile.android.libs.cache.ormlite.table.TableUtils;
import com.arellomobile.android.libs.system.log.LogUtils;

import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 *
 * @author GrafNick
 */
public abstract class BasicOrmLiteOpenHelper extends OrmLiteSqliteOpenHelper {

	protected Logger log = Logger.getLogger(getClass().getName());

	// the DAO object we use to access the Alarm table
	private Dao<NetworkToken, String> networkTokenDao = null;

	protected BasicOrmLiteOpenHelper(Context context, String databaseName, SQLiteDatabase.CursorFactory cursorFactory, int databaseVersion) {
		super(context, databaseName, cursorFactory, databaseVersion);
	}

	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to create
	 * the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
		try {
			log.info("onCreate");
			TableUtils.createTable(connectionSource, NetworkToken.class);
		} catch (SQLException e) {
			log.severe(LogUtils.getErrorReport("Can't create database", e));
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			log.info("onUpgrade");
			TableUtils.dropTable(connectionSource, NetworkToken.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(database, connectionSource);
		} catch (SQLException e) {
			log.severe(LogUtils.getErrorReport("Can't drop databases", e));
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the Database Access Object (DAO) for our NetworkToken class. It will create it or just give the cached
	 * value.
	 */
	protected Dao<NetworkToken, String> getNetworkTokenDao() throws SQLException {
		if (networkTokenDao == null) {
			networkTokenDao = getDao(NetworkToken.class);
		}
		return networkTokenDao;
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		networkTokenDao = null;
	}

}
