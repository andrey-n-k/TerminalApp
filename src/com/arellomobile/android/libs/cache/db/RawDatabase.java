/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.cache.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.arellomobile.android.libs.cache.db.model.Request;
import com.arellomobile.android.libs.cache.db.model.Table;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Swift
 */
public class RawDatabase extends DatabaseCore {
	final Logger log = Logger.getLogger(getClass().getName());

	protected RawDatabase(String fileName, Class[] tables, boolean createTables) throws ORMException {
		super(fileName, tables, createTables);
	}

	public RawDatabase(String fileName, Class[] tables, Class[] requests, boolean createTables) throws ORMException {
		super(fileName, tables, requests, createTables);
	}

	public <T> List<T> findByCriteria(Class<T> tableClass, String criteria) throws ORMException {
		Cursor cursor = null;
		try {
			List<T> result = new ArrayList<T>();
			Table table = tablesMapping.get(tableClass);
			if (table != null) {
				synchronized (mSyncObject) {
					cursor = database.query(true,table.getName(), table.getFieldNames(), criteria, null, null, null, null, null);
//                    Field[] fields = table.getFields();
//                    String fieldsList = "";
//                    for (Field field : fields) {
//                        if (field.getName() != null && field.getName().length() > 0) {
//                            if (fieldsList.length() > 0) fieldsList += ", ";
//                            fieldsList += field.getName();
//                        }
//                    }
//                    if (criteria != null && criteria.length() > 0) {
//                        String s = "Select distinct " + fieldsList + " from " + table.getName() + " where " + criteria;
//                        Logger.getLogger(getClass().getName()).config( "query = " + s);
//                        cursor = database.rawQuery(s, null);
//                        Logger.getLogger(getClass().getName()).config( "cursor.count = " + cursor.getCount());
//                    } else {
//                        cursor = database.rawQuery("Select distinct " + fieldsList + " from " + table.getName(), null);
//                    }

				}
				if (cursor.getCount() == 0) {
					return result;
				}
				cursor.moveToFirst();
				while(!cursor.isAfterLast()) {
					result.add((T) table.loadObject(database, cursor, new ArrayList<Object>()));
					cursor.moveToNext();
				}
			} else {
                log.config( "findByCriteria table is null");

				Request request = requestMapping.get(tableClass);
				synchronized (mSyncObject) {
					cursor = database.rawQuery(request.getRequest(), null);
				}
				if (cursor.getCount() == 0) {
					return result;
				}
				cursor.moveToFirst();
				while(!cursor.isAfterLast()) {
					result.add((T) request.loadObject(database, cursor, new ArrayList<Object>()));
					cursor.moveToNext();
				}
			}
			return result;
		} finally {
			if (cursor != null) {
				cursor.close();
				SQLiteDatabase.releaseMemory();
			}
		}
	}

	public int countByCriteria(Class tableClass, String criteria){
		Cursor cursor = null;
		try {
			Table table = tablesMapping.get(tableClass);
			synchronized (mSyncObject) {
				if (table != null) {
					cursor = database.query(true,table.getName(), table.getFieldNames(), criteria, null, null, null, null, null);
				} else {
					Request request = requestMapping.get(tableClass);
					cursor = database.rawQuery(request.getRequest(), null);
				}
			}
			return cursor.getCount();
		} finally {
			SQLiteDatabase.releaseMemory();
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public void commit(List<? extends Object> values) throws ORMException {
		Cursor cursor = null;
		try {
			HashMap<Table, List<ContentValues>> storeMap = new HashMap<Table, List<ContentValues>>();
			HashMap<Table, List<ContentValues>> updateMap = new HashMap<Table, List<ContentValues>>();
			for (Object value : values) {
				log.config( "Commit object for object = " + value);
				log.config( "Commit object for class = " + value.getClass());
				Table table1 = tablesMapping.get(value.getClass());

				if (table1 == null) throw new ORMException(value.getClass().getSimpleName() + " not managed by this database");
				table1.buildStoreAndUpdateValues(database, storeMap, updateMap, value);
			}

			synchronized (mSyncObject) {
				log.config( "Transaction begins");
				database.beginTransaction();

				Set<Table> tables = storeMap.keySet();
				for (Table table : tables) {
					List<ContentValues> valuesList = storeMap.get(table);
					for (ContentValues cValues : valuesList) {
						database.insert(table.getName(),null, cValues);
					}
				}
				tables = updateMap.keySet();
				for (Table table : tables) {
					List<ContentValues> valuesList = updateMap.get(table);
					for (ContentValues cValues : valuesList) {
						database.update(table.getName(), cValues, table.getName() + "." + table.getPrimaryKey().getName() + " = '" + cValues.get(table.getPrimaryKey().getName()) + "'", null);
					}
				}
				database.setTransactionSuccessful();
			}
		} finally {
			if (database.inTransaction()) {
				database.endTransaction();
			}
			log.config( "Transaction ends");
			SQLiteDatabase.releaseMemory();
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public void remove(List<? extends Object> values) throws ORMException {
		if (values.size() == 0) return;
		Cursor cursor = null;
		try {
			Table table = tablesMapping.get(values.get(0).getClass());
			synchronized (mSyncObject){
				for (Object value : values) {
					database.delete(table.getName(), table.getName() + "." + table.getPrimaryKey().getName() + " = '" + table.getPrimaryKey().getGetterMethod().invoke(value) + "'", null);
				}
			}
		} catch (InvocationTargetException e) {
			throw new ORMException(e);
		} catch (IllegalAccessException e) {
			throw new ORMException(e);
		} finally {
			SQLiteDatabase.releaseMemory();
			if (cursor != null) {
				cursor.close();
			}
		}
	}
}