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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Swift
 */
public class Table {
	protected String name;
	protected AlphabetElementObject[] alphabet;
	protected String alphabetRequest;
	protected Field primaryKey;
	protected List<Field> fields = new ArrayList<Field>();
	protected Class presentationClass;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AlphabetElementObject[] getAlphabet() {
		return alphabet;
	}

	public void setAlphabet(AlphabetElementObject[] alphabet) {
		this.alphabet = alphabet;
	}

	public String getAlphabetRequest() {
		return alphabetRequest;
	}

	public void setAlphabetRequest(String alphabetRequest) {
		this.alphabetRequest = alphabetRequest;
	}

	public Class getPresentationClass() {
		return presentationClass;
	}

	public void setPresentationClass(Class presentationClass) {
		this.presentationClass = presentationClass;
	}

	public Field getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(Field primaryKey) throws ORMException {
		if (!fields.contains(primaryKey)) throw new ORMException("Primary key is not a field of table");
		this.primaryKey = primaryKey;
	}

	public void addField(Field field) {
		fields.add(field);
	}

	public void removeField(Field o) {
		fields.remove(o);
	}

	public Field[] getFields() {
		return fields.toArray(new Field[fields.size()]);
	}

	public void clearFields() {
		fields.clear();
	}

	public String[] getFieldNames() {
		String[] result = new String[fields.size()];
		int i = 0;
		for (Field field : fields) {
			result[i] = field.getName();
			i++;
		}
		if (result.length > 1 && result[0] == null) {
			for (int j = 1; j < result.length; ++j) {
				if (result[j] != null) {
					String tmp = result[j];
					result[j] = result[0];
					result[0] = tmp;
					break;
				}
			}
		}
		return result;
	}

	public Cursor findByPrimaryKey(SQLiteDatabase database, Object key) {
		// TODO avoid reference pk
		return database.query(name, getFieldNames(), name + "." + primaryKey.getName() + "='" + key + "'", null, null, null, null);
	}

	public Object loadObject(SQLiteDatabase database, Cursor cursor, List<Object> loadedPull) throws ORMException {
		try {
			Object result = presentationClass.newInstance();
			primaryKey.loadFieldFromDB(database, cursor, result, loadedPull);
			for (Object loadedObject : loadedPull) {
				if (presentationClass.isInstance(loadedObject)){
					if (primaryKey.getGetterMethod().invoke(loadedObject).equals(primaryKey.getGetterMethod().invoke(result))) {
						return loadedObject;
					}
				}
			}
			loadedPull.add(result);
			for (Field field : fields) {
				if (field != primaryKey) {
					field.loadFieldFromDB(database, cursor, result, loadedPull);
				}
			}
			return result;
		} catch (InstantiationException e) {
			throw new ORMException(e);
		} catch (IllegalAccessException e) {
			throw new ORMException(e);
		} catch (InvocationTargetException e) {
			throw new ORMException(e);
		}
	}

	public void buildStoreAndUpdateValues(SQLiteDatabase database, Map<Table,List<ContentValues>> store, Map<Table,List<ContentValues>> update, Object value) throws ORMException {
		if (value == null) return;
		Cursor cursor = null;
		try {
			cursor = findByPrimaryKey(database, primaryKey.getGetterMethod().invoke(value));
			boolean updateObj = cursor.getCount() != 0;
			List<ContentValues> valuesList;
			if (updateObj) {
				valuesList = update.get(this);
				if (valuesList == null) {
					valuesList = new ArrayList<ContentValues>();
					update.put(this, valuesList);
				}
			} else {
				valuesList = store.get(this);
				if (valuesList == null) {
					valuesList = new ArrayList<ContentValues>();
					store.put(this, valuesList);
				}
			}

			ContentValues values = new ContentValues();
			primaryKey.buildStoreAndUpdateValues(database, store, update, value, values);

			for (ContentValues contentValues : valuesList) {
				if (contentValues.get(primaryKey.getName()).equals(values.get(primaryKey.getName()))) {
					return;
				}
			}
			valuesList.add(values);
			for (Field field : fields) {
				field.buildStoreAndUpdateValues(database, store, update, value, values);
			}
		} catch (IllegalAccessException e) {
			throw new ORMException(e);
		} catch (InvocationTargetException e) {
			throw new ORMException(e);
		} finally {
			if (cursor != null){
				cursor.close();
			}
		}
	}
}
