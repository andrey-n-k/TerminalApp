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
import com.arellomobile.android.libs.cache.db.DataSource;
import com.arellomobile.android.libs.cache.db.ORMException;
import com.arellomobile.android.libs.cache.db.annotations.PrimaryKey;
import com.arellomobile.android.libs.cache.db.util.LazyLoadList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Swift
 */
public class RefField extends Field {
	protected Class refClass;
	protected Table refTable;
	protected Field refField;

	// is this is a reference to a list of objects (one-to-many)
	protected boolean isList = false;
	private final Logger log = Logger.getLogger(getClass().getName());

	@Override
	public String getName() {
		return isList?null:super.getName();
	}

	@Override
	public void setGetterMethod(Method getterMethod) {
		super.setGetterMethod(getterMethod);
		isList = Collection.class.isAssignableFrom(getterMethod.getReturnType());
	}

	public Field getRefField() {
		return refField;
	}

	public void setRefField(Field refField) {
		this.refField = refField;
	}

	public Class getRefClass() {
		return refClass;
	}

	public void setRefTable(Table refTable) {
		this.refTable = refTable;
	}

	@Override
	public void setType(Class type) throws ORMException {
		Method[] methods = type.getMethods();
		boolean pkFind = false;
		for (Method method : methods) {
			if (method.isAnnotationPresent(PrimaryKey.class)) {
				if (method.getName().startsWith("set")) {
					pkFind = true;
					super.setType(method.getParameterTypes()[0]);
				} else {
					pkFind = true;
					super.setType(method.getReturnType());
				}
			}
		}
		if (!pkFind) throw new ORMException("No primary key for " + type);
		refClass = type;
	}

	@Override
	public boolean isRef() {
		return true;
	}

	@Override
	public void loadFieldFromDB(final SQLiteDatabase database, Cursor cursor, Object obj, List<Object> loadedPull) throws ORMException {
		if (!isList) {
			Cursor refCursor = null;
			try {
				synchronized (database) {
				refCursor = refTable.findByPrimaryKey(database, loadValue(cursor));
				}
				if (refCursor.getCount() == 0) {
					return;
				}
				refCursor.moveToFirst();
				setterMethod.invoke(obj, refTable.loadObject(database, refCursor, loadedPull));
			} catch (InvocationTargetException e) {
				throw new ORMException(e);
			} catch (IllegalAccessException e) {
				throw new ORMException(e);
			} finally {
				if (refCursor != null) {
					refCursor.close();
				}
			}
		} else {
			try {
				setterMethod.invoke(obj, lazyLoadListFactory(refClass, database.getPath(), getOwner().getPrimaryKey().getGetterMethod().invoke(obj)));
			} catch (InvocationTargetException e) {
				throw new ORMException(e);
			} catch (IllegalAccessException e) {
				throw new ORMException(e);
			}
		}
	}

	protected <T> LazyLoadList<T> lazyLoadListFactory(Class<T> clazz, String databasePath, Object pkValue) throws ORMException {
		return new LazyLoadList<T>(clazz, DataSource.getInstance().getDatabase(databasePath, null), refField.getName(), pkValue);
	}

	@Override
	public void buildStoreAndUpdateValues(final SQLiteDatabase database, Map<Table, List<ContentValues>> store, Map<Table, List<ContentValues>> update, Object value, ContentValues values) throws ORMException {
		try {
			if (!isList) {
				Object refObject = getterMethod.invoke(value);
				if (refObject == null) return;
				refTable.buildStoreAndUpdateValues(database, store, update, refObject);
				Object fieldValue = refTable.getPrimaryKey().getGetterMethod().invoke(refObject);
				if (fieldValue instanceof String) {
					values.put(name, (String)fieldValue);
				}
				if (fieldValue instanceof Integer) {
					values.put(name, (Integer)fieldValue);
				}
				if (fieldValue instanceof Double) {
					values.put(name, (Double)fieldValue);
				}
				if (fieldValue instanceof Boolean) {
					values.put(name, (Boolean)fieldValue?1:0);
				}
			} else {
				Collection refObjects = (Collection) getterMethod.invoke(value);
				if (refObjects == null) return;
				List<Object> refs = new ArrayList<Object>();
				Object thisPk = owner.getPrimaryKey().getGetterMethod().invoke(value);
				for (Object refObject : refObjects) {
					refTable.buildStoreAndUpdateValues(database, store, update, refObject);
					Object refPk = refTable.getPrimaryKey().getGetterMethod().invoke(refObject);
					refs.add(refPk);
					updateRelation(store, refPk, thisPk);
					updateRelation(update, refPk, thisPk);
				}


//				Cursor c;
//				if (refs.size() == 0) {
//					c = database.query(refTable.getName(), new String[]{refTable.getPrimaryKey().getName()}, refTable.getName() + "." + refField.getName() + " = '" + thisPk + "'", null,null,null,null);
//				} else {
//					String refsString = "";
//					for (Object next : refs) {
//						refsString += "\'" + next + "\'" + ",";
//				}
//					refsString = "(" + refsString.substring(0, refsString.length() - 1) + ")";
//					c = database.query(refTable.getName(), new String[]{refTable.getPrimaryKey().getName()}, refTable.getName() + "." + refField.getName() + " = '" + thisPk + "' and " + refTable.getName() + "." + refTable.getPrimaryKey().getName() + " not in " + refsString, null,null,null,null);
//				}

				Cursor c;
				synchronized (database) {
					c = database.query(refTable.getName(), refTable.getFieldNames(), refTable.getName() + "." + refField.getName() + " = '" + thisPk + "'", null,null,null,null);
				}


				try {
					log.config( "start update relation");
					if (c.getCount() == 0) {
						log.config( "end update relation");
						return;
					}
					c.moveToFirst();
					while (!c.isAfterLast()) {
						Object refPk = refTable.getPrimaryKey().loadValue(c);
						if (refs.contains(refPk)) {
							c.moveToNext();
							continue;
						}
						ContentValues removeRelationValues = new ContentValues();
						Field[] fields = refTable.getFields();
						for (Field field : fields) {
							//if current field is list, it does not have a name
							if (field.isRef() && ((RefField)field).isList) continue;

							if (field == refField) {
								removeRelationValues.put(field.getName(), (String) null);
							} else {
								switch (field.getTypeCode()) {
									case String:
										removeRelationValues.put(field.getName(), c.getString(c.getColumnIndex(field.getName())));
										break;
									case Integer:
									case Boolean:
										removeRelationValues.put(field.getName(), c.getInt(c.getColumnIndex(field.getName())));
										break;
									case Long:
									case Date:
										removeRelationValues.put(field.getName(), c.getLong(c.getColumnIndex(field.getName())));
										break;
									case Double:
										removeRelationValues.put(field.getName(), c.getDouble(c.getColumnIndex(field.getName())));
										break;

								}
							}
						}
						
						if(update.get(refTable) == null) {
							List<ContentValues> valuesList = new ArrayList<ContentValues>();
							update.put(refTable, valuesList);
						}
						update.get(refTable).add(removeRelationValues);
						c.moveToNext();
					}
					log.config( "end update relation");
				} finally {
					c.close();
				}
			}
		} catch (IllegalAccessException e) {
			throw new ORMException(e);
		} catch (InvocationTargetException e) {
			throw new ORMException(e);
		}
	}

	private void updateRelation(Map<Table, List<ContentValues>> mapping, Object refObjectPK, Object thisPk) throws IllegalAccessException, InvocationTargetException {
		List<ContentValues> storeValues = mapping.get(refTable);
		if (storeValues == null) return;
		for (ContentValues storeValue : storeValues) {
			if (storeValue.get(refTable.getPrimaryKey().getName()).equals(refObjectPK)) {
				if (thisPk instanceof String) {
					storeValue.put(refField.getName(), (String) thisPk);
				}
				if (thisPk instanceof Integer) {
					storeValue.put(refField.getName(), (Integer) thisPk);
				}
				if (thisPk instanceof Long) {
					storeValue.put(refField.getName(), (Long) thisPk);
				}
				if (thisPk instanceof Date) {
					storeValue.put(refField.getName(), ((Date) thisPk).getTime());
				}
				if (thisPk instanceof Double) {
					storeValue.put(refField.getName(), (Double) thisPk);
				}
				if (thisPk instanceof Boolean) {
					storeValue.put(refField.getName(), (Boolean) thisPk ?1:0);
				}
			}
		}
	}
}
