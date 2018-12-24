package com.arellomobile.android.libs.cache.ormlite.stmt.query;

import java.sql.SQLException;
import java.util.List;

import com.arellomobile.android.libs.cache.ormlite.db.DatabaseType;
import com.arellomobile.android.libs.cache.ormlite.stmt.SelectArg;

/**
 * Internal marker class for query clauses.
 * 
 * @author graywatson
 */
public interface Clause {

	/**
	 * Add to the string-builder the appropriate SQL for this clause.
	 */
	public void appendSql(DatabaseType databaseType, StringBuilder sb, List<SelectArg> selectArgList)
			throws SQLException;
}
