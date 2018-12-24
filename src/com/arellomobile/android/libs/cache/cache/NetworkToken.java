/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.cache.cache;

import com.arellomobile.android.libs.cache.db.annotations.FieldName;
import com.arellomobile.android.libs.cache.db.annotations.PrimaryKey;
import com.arellomobile.android.libs.cache.db.annotations.TableName;
import com.arellomobile.android.libs.cache.ormlite.field.DatabaseField;
import com.arellomobile.android.libs.cache.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * @author Swift
 */
@TableName("NetworkToken")
@DatabaseTable(tableName = "NetworkToken")
public class NetworkToken {

	@DatabaseField(id = true)
	private String id;
	@DatabaseField
	private Date storeDate;
	@DatabaseField
	private String eTag;

	public NetworkToken() {
	}

	public NetworkToken(String id, Date storeDate, String eTag) {
		this.id = id;
		this.storeDate = storeDate;
		this.eTag = eTag;
	}

	@FieldName("id")
	@PrimaryKey
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@FieldName("storeDate")
	public Date getStoreDate() {
		return storeDate;
	}

	public void setStoreDate(Date storeDate) {
		this.storeDate = storeDate;
	}

	@FieldName("eTag")
	public String geteTag() {
		return eTag;
	}

	public void seteTag(String eTag) {
		this.eTag = eTag;
	}
}
