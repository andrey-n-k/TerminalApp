package com.arellomobile.android.libs.cache.ormlite.stmt.query;

import java.sql.SQLException;
import java.util.List;

import com.arellomobile.android.libs.cache.ormlite.db.DatabaseType;
import com.arellomobile.android.libs.cache.ormlite.field.FieldType;
import com.arellomobile.android.libs.cache.ormlite.stmt.SelectArg;
import com.arellomobile.android.libs.cache.ormlite.stmt.StatementBuilder;

/**
 * Internal class handling the SQL SET part used by UPDATE statements. Used by
 * {@link StatementBuilder#updateColumnExpression(String, String)}.
 * 
 * <p>
 * It's not a comparison per se but does have a columnName = value form so it works.
 * </p>
 * 
 * @author graywatson
 */
public class SetExpression extends BaseComparison {

	public SetExpression(String columnName, String string) throws SQLException {
		super(columnName, null, string);
	}

	@Override
	public StringBuilder appendOperation(StringBuilder sb) {
		sb.append("= ");
		return sb;
	}

	@Override
	protected void appendArgOrValue(DatabaseType databaseType, FieldType fieldType, StringBuilder sb,
			List<SelectArg> selectArgList, Object argOrValue) {
		// we know it is a string so just append it
		sb.append(argOrValue).append(' ');
	}
}
