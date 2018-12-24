package com.arellomobile.android.libs.cache.ormlite.android;

import java.sql.SQLException;

import android.database.sqlite.SQLiteOpenHelper;

import com.arellomobile.android.libs.cache.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.arellomobile.android.libs.cache.ormlite.db.DatabaseType;
import com.arellomobile.android.libs.cache.ormlite.db.SqliteAndroidDatabaseType;
import com.arellomobile.android.libs.cache.ormlite.logger.Logger;
import com.arellomobile.android.libs.cache.ormlite.logger.LoggerFactory;
import com.arellomobile.android.libs.cache.ormlite.support.BaseConnectionSource;
import com.arellomobile.android.libs.cache.ormlite.support.ConnectionSource;
import com.arellomobile.android.libs.cache.ormlite.support.DatabaseConnection;

/**
 * Android version of the connection source. Uses the standard Android SQLiteOpenHelper. For best results, use our
 * helper,
 * 
 * @see OrmLiteSqliteOpenHelper
 * 
 * @author kevingalligan, graywatson
 */
public class AndroidConnectionSource extends BaseConnectionSource implements ConnectionSource {

	private static final Logger logger = LoggerFactory.getLogger(AndroidConnectionSource.class);

	private SQLiteOpenHelper helper;
	private DatabaseConnection readOnlyConnection = null;
	private DatabaseConnection readWriteConnection = null;
	private DatabaseType databaseType = new SqliteAndroidDatabaseType();

	public AndroidConnectionSource(SQLiteOpenHelper helper) {
		this.helper = helper;
	}

	public DatabaseConnection getReadOnlyConnection() throws SQLException {
		DatabaseConnection conn = getSavedConnection();
		if (conn != null) {
			return conn;
		}
		if (readOnlyConnection == null) {
			readOnlyConnection = new AndroidDatabaseConnection(helper.getReadableDatabase(), false);
		}
		return readOnlyConnection;
	}

	public DatabaseConnection getReadWriteConnection() throws SQLException {
		DatabaseConnection conn = getSavedConnection();
		if (conn != null) {
			return conn;
		}
		if (readWriteConnection == null) {
			readWriteConnection = new AndroidDatabaseConnection(helper.getWritableDatabase(), true);
		}
		return readWriteConnection;
	}

	public void releaseConnection(DatabaseConnection connection) throws SQLException {
		// noop since connection management is handled by AndroidOS
	}

	public boolean saveSpecialConnection(DatabaseConnection connection) throws SQLException {
		return saveSpecial(connection);
	}

	public void clearSpecialConnection(DatabaseConnection connection) {
		clearSpecial(connection, logger);
	}

	public void close() {
		// the helper is closed so it calls close here, so this CANNOT be a call back to helper.close()
	}

	public DatabaseType getDatabaseType() {
		return databaseType;
	}
}
