package com.arellomobile.android.libs.cache.ormlite.stmt.query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.arellomobile.android.libs.cache.ormlite.db.DatabaseType;
import com.arellomobile.android.libs.cache.ormlite.field.FieldType;
import com.arellomobile.android.libs.cache.ormlite.stmt.QueryBuilder.InternalQueryBuilderWrapper;
import com.arellomobile.android.libs.cache.ormlite.stmt.SelectArg;
import com.arellomobile.android.libs.cache.ormlite.stmt.Where;

/**
 * Internal class handling the SQL 'EXISTS' query part. Used by {@link Where#exists}.
 * 
 * @author graywatson
 */
public class Exists implements Clause {

	private final InternalQueryBuilderWrapper subQueryBuilder;

	public Exists(InternalQueryBuilderWrapper subQueryBuilder) {
		this.subQueryBuilder = subQueryBuilder;
	}

	public void appendSql(DatabaseType databaseType, StringBuilder sb, List<SelectArg> selectArgList)
			throws SQLException {
		sb.append("EXISTS (");
		List<FieldType> resultFieldTypeList = new ArrayList<FieldType>();
		subQueryBuilder.buildStatementString(sb, resultFieldTypeList, selectArgList);
		sb.append(") ");
	}
}
