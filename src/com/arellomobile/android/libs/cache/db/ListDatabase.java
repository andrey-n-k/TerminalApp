/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.cache.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.arellomobile.android.libs.cache.db.model.AlphabetElementObject;
import com.arellomobile.android.libs.cache.db.model.Request;
import com.arellomobile.android.libs.cache.db.model.Table;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author Swift
 */
public class ListDatabase extends DatabaseCore {
	private final Logger log = Logger.getLogger(getClass().getName());
    private final Object mSyncObject = new Object();

	public ListDatabase(String fileName, Class[] tables, boolean createTables) throws ORMException {
		super(fileName, tables, createTables);
	}

	public ListDatabase(String fileName, Class[] tables, Class[] requests, boolean createTables) throws ORMException {
		super(fileName, tables, requests, createTables);
	}

	public ListAdapter getListAdapter(Class tableClass, String criteria,  Context context, int layout, String[] fieldsFrom, int[] to) {
		Table table = tablesMapping.get(tableClass);
		if (table != null) {
			StringBuilder sb = new StringBuilder();
			String[] fieldNames = table.getFieldNames();
			for (String fieldName : fieldNames) {
				if (fieldName.equals(table.getPrimaryKey().getName())) continue;
				sb.append(fieldName).append(",");
			}
			String fieldNamesString = sb.toString().substring(0, sb.toString().length() - 1);
			Cursor c1;
			synchronized (mSyncObject) {
				if (criteria != null) {
					c1 = database.rawQuery("select " + table.getPrimaryKey().getName() + " as _id, " + fieldNamesString + " from " + table.getName() + " where " + criteria, null);
				} else {
					c1 = database.rawQuery("select " + table.getPrimaryKey().getName() + " as _id, " + fieldNamesString + " from " + table.getName() , null);
				}
			}
			return new SimpleCursorAdapter(context, layout, c1, fieldsFrom, to);
		} else {
			Request request = requestMapping.get(tableClass);
			Cursor c1;
			synchronized (mSyncObject) {
				c1 = database.rawQuery(request.getRequest() , null);
			}
			return new SimpleCursorAdapter(context, layout, c1, fieldsFrom, to);
		}
	}

	/**
	 * TODO: improve
	 * @param tableClass
	 * @param criteria
	 * @param context
	 * @param layout
	 * @param fieldsFrom
	 * @param to
	 * @param orderColumn
	 * @return
	 */
	public ListAdapter getListAdapterWithQuickSearch(Class tableClass, String criteria, Context context, int layout, String[] fieldsFrom, int[] to, String orderColumn) {
		Table table = tablesMapping.get(tableClass);
		if (table != null) {
			StringBuilder sb = new StringBuilder();
			String[] fieldNames = table.getFieldNames();
			for (String fieldName : fieldNames) {
				if (fieldName.equals(table.getPrimaryKey().getName())) continue;
				sb.append(fieldName).append(",");
			}
			String fieldNamesString = sb.toString().substring(0, sb.toString().length() - 1);
			Cursor listCursor;
			synchronized (mSyncObject) {
				if (criteria != null) {
					listCursor = database.rawQuery("select " + table.getPrimaryKey().getName() + " as _id, " + fieldNamesString + " from " + table.getName() + " where " + criteria + " order by " + orderColumn, null);
				} else {
					listCursor = database.rawQuery("select " + table.getPrimaryKey().getName() + " as _id, " + fieldNamesString + " from " + table.getName() + " order by " + orderColumn, null);
				}
			}

			// If we have predefined alphabet
			if (table.getAlphabet() != null && table.getAlphabet().length > 0) {
				return new DatabaseListAdapter((SQLiteCursor) listCursor, context, layout, fieldsFrom, to, table.getAlphabet(), orderColumn, true);
			}

			// If we have a query already
			Cursor alphaSearch;
			synchronized (mSyncObject) {
				if (table.getAlphabetRequest() != null && table.getAlphabetRequest().length() > 0){
					alphaSearch = database.rawQuery(table.getAlphabetRequest(), null);
				} else {
					if (criteria == null){
						alphaSearch = database.rawQuery("select substr(" + orderColumn + ",1,1) as letter from " + table.getName() + " group by letter order by letter", null);
					} else {
						alphaSearch = database.rawQuery("select substr(" + orderColumn + ",1,1) as letter from " + table.getName() + " where " + criteria + " group by letter order by letter", null);
					}
				}
			}
//			if (alphaSearch.getCount() == 0) return new SimpleCursorAdapter(context, layout, listCursor, fieldsFrom, to);
			AlphabetElementObject[] alphabet = new AlphabetElementObject[alphaSearch.getCount()];
			alphaSearch.moveToFirst();
			int i = 0;
			while (!alphaSearch.isAfterLast()){
				alphabet[i] = new AlphabetElementObject(alphaSearch.getString(alphaSearch.getColumnIndex("letter")), 0);
				i++;
				alphaSearch.moveToNext();
			}
			alphaSearch.close();
			return new DatabaseListAdapter((SQLiteCursor) listCursor, context, layout, fieldsFrom, to, alphabet, orderColumn, false);
		} else {
			Request request = requestMapping.get(tableClass);
			log.config( "request.getRequest() start");
			Cursor listCursor;
			synchronized (mSyncObject) {
				listCursor = database.rawQuery(request.getRequest() , null);
			}
			log.config( "request.getRequest() end");

			if (request.getAlphabet().length == 0) {
				return new SimpleCursorAdapter(context, layout, listCursor, fieldsFrom, to);
			}
			log.config( "convert To Alpha Array end");

			// If we have predefined alphabet
			if (request.getAlphabet() != null && request.getAlphabet().length > 0) {
				return new DatabaseListAdapter((SQLiteCursor) listCursor, context, layout, fieldsFrom, to, request.getAlphabet(), orderColumn, true);
			}

			// If we have a query already
			Cursor alphaSearch;
			if (request.getAlphabetRequest() != null && request.getAlphabetRequest().length() > 0){
				synchronized (mSyncObject) {
					alphaSearch = database.rawQuery(request.getAlphabetRequest(), null);
				}
			} else {
				return new SimpleCursorAdapter(context, layout, listCursor, fieldsFrom, to);
			}
			if (alphaSearch.getCount() == 0) {
				alphaSearch.close();
				return new SimpleCursorAdapter(context, layout, listCursor, fieldsFrom, to);
			}
			AlphabetElementObject[] alphabet = new AlphabetElementObject[alphaSearch.getCount()];
			alphaSearch.moveToFirst();
			int i = 0;
			while (!alphaSearch.isAfterLast()){
				alphabet[i] = new AlphabetElementObject(alphaSearch.getString(alphaSearch.getColumnIndex("letter")), 0);
				i++;
				alphaSearch.moveToNext();
			}
			alphaSearch.close();
			return new DatabaseListAdapter((SQLiteCursor) listCursor, context, layout, fieldsFrom, to, alphabet, orderColumn, false);
		}
	}

	protected class DatabaseListAdapter extends BaseAdapter implements SectionIndexer {
		protected Map<String, Integer> alphabetPositions = new HashMap<String, Integer>();
		private Context context;
		private int layout;
		private String[] from;
		private int[] to;
		protected String[] alphabet = null;
		protected Cursor dataCursor;
		protected String orderColumn;

		public DatabaseListAdapter(SQLiteCursor dataCursor, Context context, int layout, String[] from, int[] to, AlphabetElementObject[] alphabet, String orderColumn, boolean initializePositions) {
			this.context = context;
			this.layout = layout;
			this.from = from;
			this.to = to;
			this.alphabet = new String[alphabet.length];
			if (initializePositions) {
				int pos = 0;
				for (int i = 0, alphabetLength = alphabet.length; i < alphabetLength; i++) {
					AlphabetElementObject alphabetElementObject = alphabet[i];
					this.alphabet[i] = alphabetElementObject.getName();
					alphabetPositions.put(alphabetElementObject.getName(), pos);
					pos += alphabetElementObject.getCount();
				}
			} else {
				for (int i = 0, alphabetLength = alphabet.length; i < alphabetLength; i++) {
					AlphabetElementObject alphabetElementObject = alphabet[i];
					this.alphabet[i] = alphabetElementObject.getName();
				}
			}
			if (alphabet.length > 0) {
				alphabetPositions.put(alphabet[0].toString(), 0);
			}
			this.orderColumn = orderColumn;
			this.dataCursor = dataCursor;
		}

		public Object[] getSections() {
			return alphabet;
		}

		public int getPositionForSection(int index) {
			log.config( "getPositionForSection = " + index);
			log.config( "getPositionForSection = " + alphabet[index]);
			if (alphabetPositions.get(alphabet[index]) == null) {
				// Dataset position
				int startSearchPosition = 0;
				int endSearchPosition = dataCursor.getCount() - 1;

				// Alphabet position
				int startIndex = 0;
				int endIndex = alphabet.length - 1;

				//Determine the range of the search (if we have one from previous or next elements, search between them)
				for (int i = index - 1; i >= 0; i--) {
					if (alphabetPositions.containsKey(alphabet[i])) {
						startSearchPosition = alphabetPositions.get(alphabet[i]);
						startIndex = i;
						break;
					}
				}
				for (int i = index + 1; i < alphabet.length; i++) {
					if (alphabetPositions.containsKey(alphabet[i])) {
						endSearchPosition = alphabetPositions.get(alphabet[i]) - 1;
						endIndex = i - 1;
						break;
					}
				}

				// Search by dividing in parts. 
				while(endSearchPosition - startSearchPosition > 1) {
					log.config( "startIndex = " + startIndex);
					log.config( "startSearchPosition = " + startSearchPosition);
					log.config( "endIndex = " + endIndex);
					log.config( "endSearchPosition = " + endSearchPosition);
					if (endIndex - startIndex <= 0) throw new IllegalArgumentException("Error in search algorithm");
					//Calculate new search position
					int newPosition = (int) Math.round(startSearchPosition + ((double)(endSearchPosition - startSearchPosition)) * (index - startIndex) / (endIndex - startIndex + 1));
					dataCursor.moveToPosition(newPosition);
					String currentValue = dataCursor.getString(dataCursor.getColumnIndex(orderColumn));
					//Recalculate start position if current value is lower
					if (currentValue.compareTo(alphabet[index]) < 0) {
						startSearchPosition = newPosition;
						for (int i = index - 1; i > startIndex; i--) {
							if (currentValue.compareTo(alphabet[i]) >= 0) {
								startIndex = i;
								break;
							}
						}
						continue;
					}
					//Recalculate end position if current value is greater
					if (currentValue.compareTo(alphabet[index]) > 0) {
						endSearchPosition = newPosition;
						for (int i = index + 1; i <= endIndex; i++) {
							if (currentValue.compareTo(alphabet[i]) < 0) {
								endIndex = i - 1;
								break;
							}
						}
						continue;
					}
					//Equal values - success

					endSearchPosition = newPosition;
					startSearchPosition = newPosition - 1;
				}
				log.config( "startIndex = " + startIndex);
				log.config( "startSearchPosition = " + startSearchPosition);
				log.config( "endIndex = " + endIndex);
				log.config( "endSearchPosition = " + endSearchPosition);
				alphabetPositions.put(alphabet[index], endSearchPosition);
			}
			return alphabetPositions.get(alphabet[index]);
		}

		public int getSectionForPosition(int index) {
			dataCursor.moveToPosition(index);
			String value = dataCursor.getString(dataCursor.getColumnIndex(orderColumn));
			for(int i = 0; i < alphabet.length - 1; i++){
				if (value.compareTo(alphabet[i]) > 0) {
					return i;
				}
			}
			return alphabet.length;
		}


		public int getCount() {
			log.config( "getCount");
			return dataCursor.getCount();
		}

		public Object getItem(int i) {
			log.config( "getItem = " + i);
			dataCursor.moveToPosition(i);
			return dataCursor;
		}

		public long getItemId(int i) {
			log.config( "getItemId");
			dataCursor.moveToPosition(i);
			return dataCursor.getString(dataCursor.getColumnIndex(orderColumn)).hashCode();
		}

		public View getView(int i, View view, ViewGroup viewGroup) {
			log.config( "getView = " + i);
			if (view != null && ((Long)getItemId(i)).equals(view.getTag())) return view;
			dataCursor.moveToPosition(i);
			view = View.inflate(context, layout, null);
			for (int j = 0; j < from.length; j++) {
				((TextView) view.findViewById(to[j])).setText(dataCursor.getString(dataCursor.getColumnIndex(from[j])));
			}
			view.setTag(getItemId(i));
			return view;
		}
	}
}
