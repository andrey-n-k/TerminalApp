package com.arellomobile.android.libs.cache.ormlite.stmt.mapped;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.arellomobile.android.libs.cache.ormlite.db.DatabaseType;
import com.arellomobile.android.libs.cache.ormlite.field.FieldType;
import com.arellomobile.android.libs.cache.ormlite.table.TableInfo;

/**
 * Mapped statement for updating an object.
 * 
 * @author graywatson
 */
public class MappedUpdate<T, ID> extends BaseMappedStatement<T, ID> {

	private MappedUpdate(TableInfo<T, ID> tableInfo, String statement, List<FieldType> argFieldTypeList) {
		super(tableInfo, statement, argFieldTypeList);
	}

	public static <T, ID> MappedUpdate<T, ID> build(DatabaseType databaseType, TableInfo<T, ID> tableInfo)
			throws SQLException {
		FieldType idField = tableInfo.getIdField();
		if (idField == null) {
			throw new SQLException("Cannot update " + tableInfo.getDataClass() + " because it doesn't have an id field");
		}
		if (tableInfo.getFieldTypes().length == 1) {
			throw new SQLException("Cannot update " + tableInfo.getDataClass()
					+ " with only the id field.  You should use updateId().");
		}
		StringBuilder sb = new StringBuilder();
		List<FieldType> argFieldTypeList = new ArrayList<FieldType>();
		appendTableName(databaseType, sb, "UPDATE ", tableInfo.getTableName());
		boolean first = true;
		for (FieldType fieldType : tableInfo.getFieldTypes()) {
			// we never update the idField
			if (fieldType == idField) {
				continue;
			}
			if (first) {
				sb.append("SET ");
				first = false;
			} else {
				sb.append(", ");
			}
			appendFieldColumnName(databaseType, sb, fieldType, argFieldTypeList);
			sb.append("= ?");
		}
		sb.append(' ');
		appendWhereId(databaseType, idField, sb, argFieldTypeList);
		return new MappedUpdate<T, ID>(tableInfo, sb.toString(), argFieldTypeList);
	}
}
