/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.cache.db;

import com.arellomobile.android.libs.cache.db.model.*;
import com.arellomobile.android.libs.system.log.LogUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author Swift
 */
public class Database extends RawDatabase {


	protected Database(String fileName, Class[] tables, boolean createTables) throws ORMException {
		super(fileName, tables, createTables);
	}

	public Database(String fileName, Class[] tables, Class[] requests, boolean createTables) throws ORMException {
		super(fileName, tables, requests, createTables);
	}

	public <T> T findByPrimaryKey(Class<T> tableClass, Object key) throws ORMException {
		Table table = tablesMapping.get(tableClass);
		if (table == null) throw new ORMException(tableClass + " not managed");
		List<T> list = findByCriteria(tableClass, table.getName() + "." + table.getPrimaryKey().getName() + "='" + key + "'");
		if (list == null || list.size() == 0) return null;
		return list.get(0);
	}

	public void commit(Object value) throws ORMException {
		commit(Collections.nCopies(1,value));
	}

	public <T> List<T> findAll(Class<T> tableClass) throws ORMException {
		return findByCriteria(tableClass, null);
	}

	/**
	 * search objects by class field name
	 * @param tableClass
	 * @param fieldName
	 * @param fieldValue
	 * @param <T>
	 * @return
	 * @throws ORMException
	 */
	public <T> List<T> findByField(Class<T> tableClass, String fieldName, Object fieldValue) throws ORMException {
		Table table = tablesMapping.get(tableClass);
		if (fieldName == null || fieldName.length() == 0) return null;
		fieldName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
		Field[] fields = table.getFields();
		for (Field field : fields) {
			if (field.getSetterMethod() == null) continue;
			String s = field.getSetterMethod().getName();
			if (s.equals(fieldName)) {
				return findByFieldName(tableClass, field.getName(), fieldValue);
			}
		}
		return null;
	}

	/**
	 * Search Objects by Table field name
	 * @param tableClass
	 * @param fieldName
	 * @param fieldValue
	 * @param <T>
	 * @return
	 * @throws ORMException
	 */
	public <T> List<T> findByFieldName(Class<T> tableClass, String fieldName, Object fieldValue) throws ORMException {
		if (fieldName == null || fieldName.length() == 0) return null;
		Table table = tablesMapping.get(tableClass);
		if (table == null) throw new ORMException(tableClass + " not managed");
		if (fieldValue != null){
			String fieldValueString = convertToString(fieldValue);
			return findByCriteria(tableClass, table.getName() + "." + fieldName + " = '" + fieldValueString + "'");
		} else {
			return findByCriteria(tableClass, table.getName() + "." + fieldName + " is null");
		}
	}

	private String convertToString(Object fieldValue) {
		String fieldValueString;
		if (fieldValue instanceof String) {
			fieldValueString = fieldValue.toString();
		} else if (fieldValue instanceof Number) {
			fieldValueString = fieldValue.toString();
		} else if (fieldValue instanceof Date) {
			fieldValueString = ((Date) fieldValue).getTime() + "";
		} else if (fieldValue instanceof Boolean) {
			fieldValueString = ((Boolean) fieldValue)? "1" : "0";
		} else if (fieldValue instanceof Collection) {
			throw new ORMException("Unsupported type");
		} else {
			Table table1 = tablesMapping.get(fieldValue.getClass());
			if (table1 == null){
				throw new ORMException("Unsupported type");
			}
			try {
				Object invoke = table1.getPrimaryKey().getGetterMethod().invoke(fieldValue);
				return convertToString(invoke);
			} catch (IllegalAccessException e) {
				log.severe(LogUtils.getErrorReport(e.getMessage(), e));
				throw new ORMException(e);
			} catch (InvocationTargetException e) {
				log.severe(LogUtils.getErrorReport(e.getMessage(), e));
				throw new ORMException(e);
			}
		}
		return fieldValueString;
	}

	public void remove(Object value) throws ORMException {
		remove(Collections.nCopies(1,value));
	}



}
