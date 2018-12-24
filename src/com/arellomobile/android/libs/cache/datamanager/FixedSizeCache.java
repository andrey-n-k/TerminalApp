/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.cache.datamanager;


import java.util.*;

/**
 * Fast cache to store data in a heap.
 * Take links for some objects   
 *
 * @author Swift
 */
public class FixedSizeCache<T> {
	/**
	 * Size of the cache in objects
	 */
	private int cacheSize = 50;
	/**
	 * List to store the data in memory 
	 */
	private LinkedList<QueueElement> wallpapers = new LinkedList<QueueElement>();

	/**
	 * Constructor with default parameters
	 */
	public FixedSizeCache() {
	}

	/**
	 * Constructor with cache size
	 * @param cacheSize size of the cache
	 */
	public FixedSizeCache(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	/**
	 * checks if object associated with this id is in the cache
	 * @param id id of object
	 * @return if object is in the internal list
	 */
	public synchronized boolean isImageCached(int id){
		return wallpapers.contains(new QueueElement(id, null));
	}

	/**
	 * returns object with specified id
	 *
	 * @param id id of object
	 * @return object associated with this id
	 */
	public synchronized T getObject(int id){
		// find object by id
		int index = wallpapers.indexOf(new QueueElement(id, null));
		if (index == -1) return null;
		// return object by id 
		return wallpapers.get(index).getElement();
	}

	/**
	 * Stores object in the cache
	 * @param id id to identify the object
	 * @param object object to store 
	 */
	public synchronized void cacheObject(int id, T object){
		QueueElement newElement = new QueueElement(id, object);
		// remove old copy of association
		wallpapers.remove(newElement);
		wallpapers.add(newElement);

		while (wallpapers.size() > cacheSize) wallpapers.poll();
	}

	/**
	 * container to associate objects and id
	 */
	protected class QueueElement {
		/**
		 * Object id
		 */
		protected int id;
		/**
		 * associated object
		 */
		protected T element;

		/**
		 * constructor with initial parameters
		 * 
		 * @param id object id
		 * @param element object to store
		 */
		public QueueElement(int id, T element) {
			this.id = id;
			this.element = element;
		}

		/**
		 * Getter for id
		 * @return current id value
		 */
		public int getId() {
			return id;
		}

		/**
		 * Getter for object
		 * @return current object value
		 */
		public T getElement() {
			return element;
		}

		/**
		 * compares only by id
		 *
		 * @param o another object
		 * @return if objects have identical ids
		 */
		@SuppressWarnings({"unchecked"})
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			QueueElement that = (QueueElement) o;

			return id == that.id;

		}

		@Override
		public int hashCode() {
			return id;
		}
	}
}
