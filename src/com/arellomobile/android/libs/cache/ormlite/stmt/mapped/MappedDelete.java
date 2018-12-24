package com.arellomobile.android.libs.cache.ormlite.stmt.mapped;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.arellomobile.android.libs.cache.ormlite.db.DatabaseType;
import com.arellomobile.android.libs.cache.ormlite.field.FieldType;
import com.arellomobile.android.libs.cache.ormlite.table.TableInfo;

/**
 * A mapped statement for deleting an object.
 * 
 * @author graywatson
 */
public class MappedDelete<T, ID> extends BaseMappedStatement<T, ID> {

	private MappedDelete(TableInfo<T, ID> tableInfo, String statement, List<FieldType> argFieldTypeList) {
		super(tableInfo, statement, argFieldTypeList);
	}

	public static <T, ID> MappedDelete<T, ID> build(DatabaseType databaseType, TableInfo<T, ID> tableInfo)
			throws SQLException {
		FieldType idField = tableInfo.getIdField();
		if (idField == null) {
			throw new SQLException("Cannot delete from " + tableInfo.getDataClass()
					+ " because it doesn't have an id field");
		}
		StringBuilder sb = new StringBuilder();
		List<FieldType> argFieldTypeList = new ArrayList<FieldType>();
		appendTableName(databaseType, sb, "DELETE FROM ", tableInfo.getTableName());
		appendWhereId(databaseType, idField, sb, argFieldTypeList);
		return new MappedDelete<T, ID>(tableInfo, sb.toString(), argFieldTypeList);
	}
}
