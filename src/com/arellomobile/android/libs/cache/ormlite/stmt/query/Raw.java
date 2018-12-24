package com.arellomobile.android.libs.cache.ormlite.stmt.query;

import java.sql.SQLException;
import java.util.List;

import com.arellomobile.android.libs.cache.ormlite.db.DatabaseType;
import com.arellomobile.android.libs.cache.ormlite.stmt.SelectArg;

/**
 * Raw part of the where to just stick in a string in the middle of the WHERE. It is up to the user to do so properly.
 * 
 * @author graywatson
 */
public class Raw implements Clause {

	private final String statement;

	public Raw(String statement) {
		this.statement = statement;
	}

	public void appendSql(DatabaseType databaseType, StringBuilder sb, List<SelectArg> selectArgList)
			throws SQLException {
		sb.append(statement);
		sb.append(' ');
	}
}
