/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.cache.db.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.arellomobile.android.libs.cache.db.ORMException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Field that don't have any presentation in class
 * @author Swift
 */
public class VirtualField extends Field{

	@Override
	public Method getGetterMethod() {
		return null;
	}

	@Override
	public void setGetterMethod(Method getterMethod) {
	}

	@Override
	public Method getSetterMethod() {
		return null;
	}

	@Override
	public void setSetterMethod(Method setterMethod) {
	}

	@Override
	public boolean isRef() {
		return false;
	}

	@Override
	public void loadFieldFromDB(SQLiteDatabase database, Cursor cursor, Object obj, List<Object> loadedPull) throws ORMException {
	}

	@Override
	public void buildStoreAndUpdateValues(SQLiteDatabase database, Map<Table, List<ContentValues>> store, Map<Table, List<ContentValues>> update, Object value, ContentValues values) throws ORMException {
	}
}
