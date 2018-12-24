package com.arellomobile.android.libs.cache.ormlite.stmt.query;

import java.sql.SQLException;
import java.util.List;

import com.arellomobile.android.libs.cache.ormlite.db.DatabaseType;
import com.arellomobile.android.libs.cache.ormlite.field.FieldType;
import com.arellomobile.android.libs.cache.ormlite.stmt.SelectArg;
import com.arellomobile.android.libs.cache.ormlite.stmt.Where;

/**
 * Internal class handling the SQL 'IS NULL' comparison query part. Used by {@link Where#isNull}.
 * 
 * @author graywatson
 */
public class IsNull extends BaseComparison {

	public IsNull(String columnName, FieldType fieldType) throws SQLException {
		super(columnName, fieldType, null);
	}

	@Override
	public StringBuilder appendOperation(StringBuilder sb) {
		sb.append("IS NULL ");
		return sb;
	}

	@Override
	public StringBuilder appendValue(DatabaseType databaseType, StringBuilder sb, List<SelectArg> selectArgList) {
		// there is no value
		return sb;
	}
}
