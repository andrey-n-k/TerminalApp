package com.arellomobile.android.libs.cache.ormlite.stmt.query;

/**
 * Internal interface which defines a clause that consumes a future clause. This allows us to do:
 * 
 * <p>
 * 
 * <pre>
 * where.not();
 * where.eq(&quot;id&quot;, 1234);
 * </pre>
 * 
 * and
 * 
 * <pre>
 * where.eq(&quot;id&quot;, 1234);
 * where.and();
 * where.gt(&quot;age&quot;, 44);
 * </pre>
 * 
 * </p>
 * 
 * @author graywatson
 */
public interface NeedsFutureClause extends Clause {

	/**
	 * Set the right side of the binary operation.
	 */
	public void setMissingClause(Clause right);
}
