/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.cache.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import com.arellomobile.android.libs.cache.db.annotations.*;
import com.arellomobile.android.libs.cache.db.model.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author Swift
 */
public class DatabaseCore {
	protected final SQLiteDatabase database;
	protected Map<Class,Table> tablesMapping = new HashMap<Class, Table>();
	protected Map<Class,Request> requestMapping = new HashMap<Class, Request>();
	protected SQLiteDatabase.CursorFactory cursorFactory = new SQLiteDatabase.CursorFactory() {
		public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery, String editTable, SQLiteQuery query) {
			return new SQLiteCursor(db, masterQuery, editTable, query);
		}
	};
	private final Logger log = Logger.getLogger(getClass().getName());
    protected final Object mSyncObject = new Object();

	protected DatabaseCore(String fileName, Class[] tables, boolean createTables) throws ORMException {
		try {
			database = SQLiteDatabase.openOrCreateDatabase(fileName, cursorFactory);
			makeModelTables(tables, tablesMapping);
			makeModelTables(tables, tablesMapping);
			if (createTables) {
				Collection<Table> tableModels = tablesMapping.values();
				for (Table tableModel : tableModels) {
					String createSql = "CREATE TABLE IF NOT EXISTS " + tableModel.getName() + " (";
					Field[] fields = tableModel.getFields();
					String fieldsList = "";
					for (Field field : fields) {
						if (field.getName() != null && field.getName().length() > 0) {
							if (fieldsList.length() > 0) fieldsList += ", ";
							fieldsList += field.getName() + " " + field.getType();
							if (field.equals(tableModel.getPrimaryKey())) fieldsList += " PRIMARY KEY";
						}
					}
					createSql += fieldsList + ");";
					log.config(createSql);
					synchronized (mSyncObject) {
						database.execSQL(createSql);
					}
				}
			}
		} finally {
			SQLiteDatabase.releaseMemory();
		}
	}

	protected DatabaseCore(String fileName, Class[] tables, Class[] requests, boolean createTables) throws ORMException {
		this(fileName, tables, createTables);
		makeModelRequests(requests, tablesMapping, requestMapping);
	}
	/**
	 * Main method to generate class mapping
	 * @param tables classes to add to mapping
	 * @param tablesMapping currently generated mapping to avoid cycles in dependencies
	 * @throws ORMException if model is not valid
	 * @return tablesMapping object with new tables if needed
	 */
	@SuppressWarnings({"unchecked"})
	protected Map<Class,Table> makeModelTables(Class[] tables, Map<Class,Table> tablesMapping) throws ORMException {
		// splash iterator
		for (Class table : tables) {
			if (tablesMapping.get(table) != null) continue;
			// new object to add in mapping
			Table result = new Table();

			// Name of table
			if (!table.isAnnotationPresent(TableName.class)) throw new ORMException("TableName is not present on type " + table.getName());
			result.setName(((TableName) table.getAnnotation(TableName.class)).value());
			result.setPresentationClass(table);

			if (table.isAnnotationPresent(FastSearchable.class)) {
				AlphabetElement[] elements = ((FastSearchable) table.getAnnotation(FastSearchable.class)).alphabet();
				AlphabetElementObject[] elementsResult = new AlphabetElementObject[elements.length];
				for (int i = 0; i < elements.length; i++) {
					AlphabetElement element = elements[i];
					elementsResult[i] = new AlphabetElementObject(element.name(), element.count());
				}
				result.setAlphabet(elementsResult);
				result.setAlphabetRequest(((FastSearchable) table.getAnnotation(FastSearchable.class)).alphabetRequest());
			}

			// all presented methods
			Method[] getters = table.getMethods();
			// Iterate all methods to find fields of table
			for (Method getter : getters) {
				Annotation[] annotations = getter.getAnnotations();
				Field field;
				if (getter.isAnnotationPresent(Reference.class)) {
					field = new RefField();
					if (getter.getAnnotation(Reference.class).referenceClass() != null) {
						field.setType(getter.getAnnotation(Reference.class).referenceClass());
					}
				} else {
					field = new Field();
				}
				field.setOwner(result);
				// description flags of the current field
				boolean fieldAdd = false;
				boolean primaryKey = false;
				boolean reference = false;
				for (Annotation annotation : annotations) {
					// if annotation is a field name (that method maps on the table field)
					if (annotation instanceof FieldName) {
						field.setName(((FieldName) annotation).value());
						Class fieldType = null;
						String fieldName = "";
						// parse name of method
						if (getter.getName().startsWith("get")){
							fieldType = getter.getReturnType();
							fieldName = getter.getName().substring(3);
						}
						if (getter.getName().startsWith("is")){
							fieldType = getter.getReturnType();
							fieldName = getter.getName().substring(2);
						}
						if (getter.getName().startsWith("set")){
							fieldType = getter.getParameterTypes()[0];
							fieldName = getter.getName().substring(3);
						}
						// if field type is not set
						if (field.getType() == null || field.getType().length() == 0) {
							field.setType(fieldType);
						}
						// add methods to the field
						try {
							field.setGetterMethod(!(boolean.class.equals(fieldType) || Boolean.class.equals(fieldType))?table.getMethod("get" + fieldName):table.getMethod("is" + fieldName));
						} catch (NoSuchMethodException e) {
							throw new ORMException("No getter method: " + fieldName + " in object " + table.getSimpleName(), e);
						}
						try {
							field.setSetterMethod(table.getMethod("set" + fieldName, fieldType));
						} catch (NoSuchMethodException e) {
							throw new ORMException("No setter method: " + fieldName + " in object " + table.getSimpleName(), e);
						}
						fieldAdd = true;
					}
					// indicate that field is pk
					if (annotation instanceof PrimaryKey) {
						primaryKey = true;
					}
					// indicate that field is reference
					if (annotation instanceof Reference) {
						reference = true;
					}
				}
				// field needs to be added
				if (fieldAdd) {
					result.addField(field);
				}
				if (primaryKey) {
					result.setPrimaryKey(field);
					// Types in FK in One-to-Many
					Field[] fields = result.getFields();
					for (Field iterateField : fields) {
						if (iterateField instanceof RefField && ((RefField) iterateField).getRefField() != null) {
							((RefField) iterateField).getRefField().setType(field.getGetterMethod().getReturnType());
						}
					}
				}
				tablesMapping.put(table, result);
				if (reference) {
					makeModelTables(new Class[]{((RefField) field).getRefClass()}, tablesMapping);
					Table refTable = tablesMapping.get(((RefField) field).getRefClass());
					((RefField) field).setRefTable(refTable);

					// temporary without mappings fields (creating virtual field one to many)
					if (getter.getAnnotation(Reference.class).type() == ReferenceType.ONE_TO_MANY) {
						VirtualField refField = new VirtualField();
						refField.setName(result.getName() + "_" + getter.getAnnotation(FieldName.class).value());
						if (result.getPrimaryKey() != null) {
							refField.setType(result.getPrimaryKey().getGetterMethod().getReturnType());
						}
						refTable.addField(refField);
						((RefField) field).setRefField(refField);
					}
				}
			}
		}
		return tablesMapping;
	}

	protected Map<Class,Request> makeModelRequests(Class[] requests, Map<Class,Table> tablesMapping, Map<Class,Request> requestsMapping) throws ORMException {
		// splash iterator
		for (Class request : requests) {
			if (requestsMapping.get(request) != null) continue;
			// new object to add in mapping
			Request result = new Request();

			// Name of table
			if (!request.isAnnotationPresent(SqlResult.class)) throw new ORMException("SqlResult is not present on type " + request.getName());
			result.setRequest(((SqlResult) request.getAnnotation(SqlResult.class)).sql());

			if (request.isAnnotationPresent(FastSearchable.class)) {
				AlphabetElement[] elements = ((FastSearchable) request.getAnnotation(FastSearchable.class)).alphabet();
				AlphabetElementObject[] elementsResult = new AlphabetElementObject[elements.length];
				for (int i = 0; i < elements.length; i++) {
					AlphabetElement element = elements[i];
					elementsResult[i] = new AlphabetElementObject(element.name(), element.count());
				}
				result.setAlphabet(elementsResult);
				result.setAlphabetRequest(((FastSearchable) request.getAnnotation(FastSearchable.class)).alphabetRequest());
			}
			result.setPresentationClass(request);

			// all presented methods
			Method[] getters = request.getMethods();
			// Iterate all methods to find fields of table
			for (Method getter : getters) {
				Annotation[] annotations = getter.getAnnotations();
				Field field;
				if (getter.isAnnotationPresent(Reference.class)) {
					field = new RefField();
					if (getter.getAnnotation(Reference.class).referenceClass() != null) {
						field.setType(getter.getAnnotation(Reference.class).referenceClass());
					}
				} else {
					field = new Field();
				}
				// description flags of the current field
				boolean fieldAdd = false;
				boolean reference = false;
				for (Annotation annotation : annotations) {
					// if annotation is a field name (that method maps on the table field)
					if (annotation instanceof FieldName) {
						field.setName(((FieldName) annotation).value());
						Class fieldType = null;
						String fieldName = "";
						// parse name of method
						if (getter.getName().startsWith("get")){
							fieldType = getter.getReturnType();
							fieldName = getter.getName().substring(3);
						}
						if (getter.getName().startsWith("is")){
							fieldType = getter.getReturnType();
							fieldName = getter.getName().substring(2);
						}
						if (getter.getName().startsWith("set")){
							fieldType = getter.getParameterTypes()[0];
							fieldName = getter.getName().substring(3);
						}
						// if field type is not set
						if (field.getType() == null || field.getType().length() == 0) {
							field.setType(fieldType);
						}
						// add methods to the field
						try {
							field.setGetterMethod(!(boolean.class.equals(fieldType) || Boolean.class.equals(fieldType))?request.getMethod("get" + fieldName):request.getMethod("is" + fieldName));
						} catch (NoSuchMethodException e) {
							throw new ORMException("No getter method: " + fieldName + " in object " + request.getSimpleName(), e);
						}
						try {
							field.setSetterMethod(request.getMethod("set" + fieldName, fieldType));
						} catch (NoSuchMethodException e) {
							throw new ORMException("No setter method: " + fieldName + " in object " + request.getSimpleName(), e);
						}
						fieldAdd = true;
					}
					// indicate that field is reference
					if (annotation instanceof Reference) {
						reference = true;
					}
				}
				// field needs to be added
				if (fieldAdd) {
					result.addField(field);
				}
				requestsMapping.put(request, result);
				if (reference) {
					makeModelTables(new Class[]{((RefField) field).getRefClass()}, tablesMapping);
					Table refTable = tablesMapping.get(((RefField) field).getRefClass());
					((RefField) field).setRefTable(refTable);

					// temporary without mappings fields (creating virtual field one to many)
					if (getter.getAnnotation(Reference.class).type() == ReferenceType.ONE_TO_MANY) {
						throw new ORMException(request.getSimpleName() + " no collections available in sql result");
					}
				}
			}
		}
		return requestsMapping;
	}

	@Override
	protected void finalize() throws Throwable {
		database.close();
		super.finalize();
	}
}
