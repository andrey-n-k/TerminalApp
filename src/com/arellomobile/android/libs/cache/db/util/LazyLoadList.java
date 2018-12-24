/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.cache.db.util;

import com.arellomobile.android.libs.cache.db.Database;
import com.arellomobile.android.libs.cache.db.ORMException;

import java.util.*;

/**
 * Implements lazy load for collections
 * @author Swift
 */
public class LazyLoadList<E> extends Vector<E> implements java.lang.Cloneable, java.io.Serializable{

	protected Vector<E> content = null;
	protected Class<E> classRef;
	protected Database database;
	protected String fieldName;
	protected Object refValue;

	public LazyLoadList(Class<E> classRef, Database database, String fieldName, Object refValue) {
		this.classRef = classRef;
		this.database = database;
		this.fieldName = fieldName;
		this.refValue = refValue;
	}

	protected void initialize() {
		if (content != null) return; 
		content = new Vector<E>();
		try {
			content.addAll(database.findByFieldName(classRef, fieldName, refValue));
		} catch (ORMException e) {
			throw new IllegalStateException(e);
		}
	}

	public void add(int location, E object) {
		initialize();
		content.add(location, object);
	}

	public boolean add(E object) {
		initialize();
		return content.add(object);
	}

	public boolean addAll(int location, Collection<? extends E> es) {
		initialize();
		return content.addAll(location, es);
	}

	public boolean addAll(Collection<? extends E> es) {
		initialize();
		return content.addAll(es);
	}

	public void addElement(E object) {
		initialize();
		content.addElement(object);
	}

	public int capacity() {
		initialize();
		return content.capacity();
	}

	public void clear() {
		initialize();
		content.clear();
	}

	public boolean contains(Object object) {
		initialize();
		return content.contains(object);
	}

	public boolean containsAll(Collection<?> collection) {
		initialize();
		return content.containsAll(collection);
	}

	public void copyInto(Object[] elements) {
		initialize();
		content.copyInto(elements);
	}

	public E elementAt(int location) {
		initialize();
		return content.elementAt(location);
	}

	public Enumeration<E> elements() {
		initialize();
		return content.elements();
	}

	public void ensureCapacity(int minimumCapacity) {
		initialize();
		content.ensureCapacity(minimumCapacity);
	}

	public E firstElement() {
		initialize();
		return content.firstElement();
	}

	public E get(int location) {
		initialize();
		return content.get(location);
	}

	public int hashCode() {
		initialize();
		return content.hashCode();
	}

	public int indexOf(Object object) {
		initialize();
		return content.indexOf(object);
	}

	public int indexOf(Object object, int location) {
		initialize();
		return content.indexOf(object, location);
	}

	public void insertElementAt(E object, int location) {
		initialize();
		content.insertElementAt(object, location);
	}

	public boolean isEmpty() {
		initialize();
		return content.isEmpty();
	}

	public E lastElement() {
		initialize();
		return content.lastElement();
	}

	public int lastIndexOf(Object object) {
		initialize();
		return content.lastIndexOf(object);
	}

	public int lastIndexOf(Object object, int location) {
		initialize();
		return content.lastIndexOf(object, location);
	}

	public E remove(int location) {
		initialize();
		return content.remove(location);
	}

	public boolean remove(Object object) {
		initialize();
		return content.remove(object);
	}

	public boolean removeAll(Collection<?> collection) {
		initialize();
		return content.removeAll(collection);
	}

	public void removeAllElements() {
		initialize();
		content.removeAllElements();
	}

	public boolean removeElement(Object object) {
		initialize();
		return content.removeElement(object);
	}

	public void removeElementAt(int location) {
		initialize();
		content.removeElementAt(location);
	}

	public boolean retainAll(Collection<?> collection) {
		initialize();
		return content.retainAll(collection);
	}

	public E set(int location, E object) {
		initialize();
		return content.set(location, object);
	}

	public void setElementAt(E object, int location) {
		initialize();
		content.setElementAt(object, location);
	}

	public void setSize(int length) {
		initialize();
		content.setSize(length);
	}

	public int size() {
		initialize();
		return content.size();
	}

	public List<E> subList(int start, int end) {
		initialize();
		return content.subList(start, end);
	}

	public Object[] toArray() {
		initialize();
		return content.toArray();
	}

	public <T> T[] toArray(T[] contents) {
		initialize();
		return content.toArray(contents);
	}

	public String toString() {
		initialize();
		return content.toString();
	}

	public void trimToSize() {
		initialize();
		content.trimToSize();
	}

	public Iterator<E> iterator() {
		initialize();
		return content.iterator();
	}

	public ListIterator<E> listIterator() {
		initialize();
		return content.listIterator();
	}

	public ListIterator<E> listIterator(int location) {
		initialize();
		return content.listIterator(location);
	}

	
}
