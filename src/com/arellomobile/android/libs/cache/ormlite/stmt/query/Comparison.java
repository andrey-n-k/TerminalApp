package com.arellomobile.android.libs.cache.ormlite.stmt.query;

import java.sql.SQLException;
import java.util.List;

import com.arellomobile.android.libs.cache.ormlite.db.DatabaseType;
import com.arellomobile.android.libs.cache.ormlite.stmt.SelectArg;

/**
 * Internal interfaces which define a comparison operation.
 * 
 * @author graywatson
 */
interface Comparison extends Clause {

	/**
	 * Return the column-name associated with the comparison.
	 */
	public String getColumnName();

	/**
	 * Add the operation used in this comparison to the string builder.
	 */
	public StringBuilder appendOperation(StringBuilder sb);

	/**
	 * Add the value of the comparison to the string builder.
	 */
	public StringBuilder appendValue(DatabaseType databaseType, StringBuilder sb, List<SelectArg> selectArgList)
			throws SQLException;
}
