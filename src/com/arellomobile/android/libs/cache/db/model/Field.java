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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Swift
 */
public class Field {
	public static enum FieldType {
		Undefined, String, Integer, Long, Date, Double, Boolean
	}

	protected String name;
	protected FieldType type = FieldType.Undefined;
	protected Table owner;
	protected Method getterMethod;
	protected Method setterMethod;

	public Table getOwner() {
		return owner;
	}

	public void setOwner(Table owner) {
		this.owner = owner;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Method getGetterMethod() {
		return getterMethod;
	}

	public void setGetterMethod(Method getterMethod) {
		this.getterMethod = getterMethod;
	}

	public Method getSetterMethod() {
		return setterMethod;
	}

	public void setSetterMethod(Method setterMethod) {
		this.setterMethod = setterMethod;
	}

	public FieldType getTypeCode() {
		return type;
	}

	public String getType() {
		switch (type) {
			case String:
				return "TEXT";
			case Long:
				return "LONG";
			case Date:
				return "LONG";
			case Integer:
				return "INTEGER";
			case Double:
				return "REAL";
			case Boolean:
				return "INTEGER";
		}
		return "";
	}

	public void setType(Class type) throws ORMException {
		if (String.class.equals(type)){
			this.type = FieldType.String;
			return;
		}
		if (Integer.class.equals(type) || int.class.equals(type)){
			this.type = FieldType.Integer;
			return;
		}
		if (Boolean.class.equals(type) || boolean.class.equals(type)){
			this.type = FieldType.Boolean;
			return;
		}
		if (Double.class.equals(type) || double.class.equals(type)){
			this.type = FieldType.Double;
			return;
		}
		if (Long.class.equals(type) || long.class.equals(type)){
			this.type = FieldType.Long;
			return;
		}
		if (Date.class.equals(type)){
			this.type = FieldType.Date;
			return;
		}
		this.type = FieldType.Integer;
	}

	public boolean isRef(){
		return false;
	}

	public void loadFieldFromDB(SQLiteDatabase database, Cursor cursor, Object obj, List<Object> loadedPull) throws ORMException {
		try {
			Object value = loadValue(cursor);
			if(value != null) {
				setterMethod.invoke(obj, value);
			}
		} catch (IllegalAccessException e) {
			throw new ORMException(e);
		} catch (InvocationTargetException e) {
			throw new ORMException(e);
		}
	}

	protected Object loadValue(Cursor cursor) {
		Object value = null;
		if (!cursor.isNull(cursor.getColumnIndex(name))) {
			switch (type) {
					case String:
						value = cursor.getString(cursor.getColumnIndex(name));
						break;
					case Long:
						value = cursor.getLong(cursor.getColumnIndex(name));
						break;
					case Date:
						value = cursor.getLong(cursor.getColumnIndex(name));
						if (value != null) value = new Date((Long) value);
						break;
					case Integer:
						value = cursor.getInt(cursor.getColumnIndex(name));
						break;
					case Double:
						value = cursor.getDouble(cursor.getColumnIndex(name));
						break;
					case Boolean:
						value = (cursor.getInt(cursor.getColumnIndex(name)) != 0);
						break;
				}
		}
		return value;
	}

	public void buildStoreAndUpdateValues(SQLiteDatabase database, Map<Table,List<ContentValues>> store, Map<Table,List<ContentValues>> update, Object value, ContentValues values) throws ORMException {
		try {
			switch (type) {
					case String:
						values.put(name, (String) getGetterMethod().invoke(value));
						break;
					case Long:
						values.put(name, (Long) getGetterMethod().invoke(value));
						break;
					case Date:
						Date date = (Date) getGetterMethod().invoke(value);
						values.put(name, date != null?date.getTime():null);
						break;
					case Integer:
						values.put(name, (Integer) getGetterMethod().invoke(value));
						break;
					case Double:
						values.put(name, (Double) getGetterMethod().invoke(value));
						break;
					case Boolean:
						Boolean invoke = (Boolean) getGetterMethod().invoke(value);
						if (invoke != null) {
							values.put(name, invoke ? 1 : 0);
						} else {
							values.remove(name);
						}
						break;
				}
		} catch (IllegalAccessException e) {
			throw new ORMException(e);
		} catch (InvocationTargetException e) {
			throw new ORMException(e);
		}
	}
}
