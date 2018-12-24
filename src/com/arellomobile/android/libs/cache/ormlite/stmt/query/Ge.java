package com.arellomobile.android.libs.cache.ormlite.stmt.query;

import java.sql.SQLException;

import com.arellomobile.android.libs.cache.ormlite.field.FieldType;
import com.arellomobile.android.libs.cache.ormlite.stmt.Where;

/**
 * Internal class handling the SQL '>=' comparison query part. Used by {@link Where#ge}.
 * 
 * @author graywatson
 */
public class Ge extends BaseComparison {

	public Ge(String columnName, FieldType fieldType, Object value) throws SQLException {
		super(columnName, fieldType, value);
	}

	@Override
	public StringBuilder appendOperation(StringBuilder sb) {
		sb.append(">= ");
		return sb;
	}
}
