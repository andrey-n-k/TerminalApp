package com.arellomobile.android.libs.cache.ormlite.stmt.query;

import com.arellomobile.android.libs.cache.ormlite.stmt.Where;

/**
 * Internal class handling the SQL 'OR' operation which takes two {@link Clause} parts. Used by {@link Where#or}.
 * 
 * @author graywatson
 */
public class Or extends BaseBinaryClause {

	public Or(Clause left) {
		super(left);
	}

	public Or(Clause left, Clause right) {
		super(left, right);
	}

	@Override
	public StringBuilder appendOperation(StringBuilder sb) {
		sb.append("OR ");
		return sb;
	}
}
