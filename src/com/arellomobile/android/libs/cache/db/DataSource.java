/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.cache.db;

import com.arellomobile.android.libs.cache.db.annotations.SqlResult;
import com.arellomobile.android.libs.cache.db.annotations.TableName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory to get database implementations.
 * Returns {@link com.arellomobile.android.libs.cache.db.Database} object.
 * Syncronized
 * Singletone
 * Override is not allowed via protected access for all database constructors
 * @author Swift
 */
public final class DataSource {
	/**
	 * instance of class
	 */
	private static DataSource instance = null;

	/**
	 * Getter for instance. Lazy loading.
	 * @return instance of object
	 */
	public static DataSource getInstance() {
		if (instance == null) instance = new DataSource();
		return instance;
	}

	/**
	 * Private constructor to protect multiple instancing
	 */
	private DataSource() {
	}

	/**
	 * Mapping current loaded database
	 */
	protected Map<String,Database> databaseMap = new HashMap<String, Database>();
	protected Map<String,ListDatabase> listDatabaseMap = new HashMap<String, ListDatabase>();

	/**
	 * Factory method to create db object
	 * @param fileName name of the database file
	 * @param tables classes accepted for storage
	 * @return created database
	 * @throws ORMException if there are any errors in class declarations
	 */
	public final synchronized Database getDatabase(String fileName, Class[] tables) throws ORMException {
		return getDatabase(fileName, tables, true);
	}

	/**
	 * Factory method to create db object
	 * @param fileName name of the database file
	 * @param classes classes accepted for storage
	 * @param createTables is need to create tables
	 * @return created database
	 * @throws ORMException if there are any errors in class declarations
	 */
	public final synchronized Database getDatabase(String fileName, Class[] classes, boolean createTables) throws ORMException {
		if (databaseMap.get(fileName) == null) {
			ArrayList<Class> tables = new ArrayList<Class>();
			ArrayList<Class> requests = new ArrayList<Class>();

			for (Class aClass : classes) {
				if (aClass.isAnnotationPresent(TableName.class)) {
					tables.add(aClass);
				} else if (aClass.isAnnotationPresent(SqlResult.class)) {
					requests.add(aClass);
				} else {
					throw new ORMException();
				}
			}
			databaseMap.put(fileName, new Database(fileName, tables.toArray(new Class[tables.size()]),requests.toArray(new Class[requests.size()]), createTables));
		}

		return databaseMap.get(fileName);
	}

	/**
	 * Factory method to create db object
	 * @param fileName name of the database file
	 * @param classes classes accepted for storage (tables and requests)
	 * @param createTables is need to create tables
	 * @return created database
	 * @throws ORMException if there are any errors in class declarations
	 */
	public final synchronized ListDatabase getListDatabase(String fileName, Class[] classes, boolean createTables) throws ORMException {
		if (listDatabaseMap.get(fileName) == null) {
			ArrayList<Class> tables = new ArrayList<Class>();
			ArrayList<Class> requests = new ArrayList<Class>();

			for (Class aClass : classes) {
				if (aClass.isAnnotationPresent(TableName.class)) {
					tables.add(aClass);
				} else if (aClass.isAnnotationPresent(SqlResult.class)) {
					requests.add(aClass);
				} else {
					throw new ORMException();
				}
			}
			listDatabaseMap.put(fileName, new ListDatabase(fileName, tables.toArray(new Class[tables.size()]),requests.toArray(new Class[requests.size()]), createTables));
		}

		return listDatabaseMap.get(fileName);
	}
}
