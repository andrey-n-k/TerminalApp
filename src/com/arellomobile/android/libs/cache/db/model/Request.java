/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.cache.db.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.arellomobile.android.libs.cache.db.ORMException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Swift
 */
public class Request {
	protected String request;
	protected AlphabetElementObject[] alphabet;
	protected String alphabetRequest;
	protected List<Field> fields = new ArrayList<Field>();
	protected Class presentationClass;

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
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
		return result;
	}

	public Object loadObject(SQLiteDatabase database, Cursor cursor, List<Object> loadedPull) throws ORMException {
		try {
			Object result = presentationClass.newInstance();
			loadedPull.add(result);
			for (Field field : fields) {
				field.loadFieldFromDB(database, cursor, result, loadedPull);
			}
			return result;
		} catch (InstantiationException e) {
			throw new ORMException(e);
		} catch (IllegalAccessException e) {
			throw new ORMException(e);
		}
	}
}