package com.arellomobile.android.libs.cache.ormlite.stmt;

import com.arellomobile.android.libs.cache.ormlite.dao.Dao;

/**
 * Interface returned by the {@link UpdateBuilder#prepare()} which supports custom UPDATE statements. This should be in
 * turn passed to the {@link Dao#update(PreparedUpdate)} method.
 * 
 * @param T
 *            The class that the code will be operating on.
 * @author graywatson
 */
public interface PreparedUpdate<T> extends PreparedStmt<T> {
}
