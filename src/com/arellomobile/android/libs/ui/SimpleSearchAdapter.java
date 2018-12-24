/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.ui;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

import java.util.*;

/**
 * Extension of String array adapter witch implements SectionIndexer interface and can be used to implement fast scroll functionality
 * @author Swift
 */
public class SimpleSearchAdapter extends ArrayAdapter<String> implements SectionIndexer {
	Map<String, Integer> lettersMap;
	String[] letters;
	List<String> strings;

	public SimpleSearchAdapter(Context context, int layout, int textViewResourceId, List<String> objects) {
		super(context, layout, textViewResourceId, objects);
		this.strings = objects;
		Collections.sort(objects);
		lettersMap = new HashMap<String, Integer>();
		String currentLetter = "";
		for (int i = 0, objectsSize = objects.size(); i < objectsSize; i++) {
			String str = objects.get(i);
			if (str.length() == 0) continue;
			String newLetter = str.substring(0,1);
			if (!newLetter.equals(currentLetter)){
				currentLetter = newLetter;
				lettersMap.put(currentLetter, i);
			}
		}
		List<String> lettersList = new ArrayList<String>();
		lettersList.addAll(lettersMap.keySet());
		Collections.sort(lettersList);
		letters = new String[lettersList.size()];
		letters = lettersList.toArray(letters);
	}

	/**
	 * This provides the list view with an array of section objects. In the simplest case these are Strings, each containing one letter of the alphabet. They could be more complex objects that indicate the grouping for the adapter's consumption. The list view will call toString() on the objects to get the preview letter to display while scrolling.
	 *
	 * @return the the array of objects that indicate the different sections of the list.
	 */
	public Object[] getSections() {
		return letters;
	}

	/**
	 * Provides the starting index in the list for a given section.
	 * @param section the index of the section to jump to.
	 * @return the starting position of that section. If the section is out of bounds, the position must be clipped to fall within the size of the list.
	 */
	public int getPositionForSection(int section) {
		return lettersMap.get(letters[section]);
	}

	/**
	 * This is a reverse mapping to fetch the section index for a given position in the list.
	 * @param position the position for which to return the section
	 * @return the section index. If the position is out of bounds, the section index must be clipped to fall within the size of the section array.
	 */
	public int getSectionForPosition(int position) {
		return Arrays.binarySearch(letters, strings.get(position).substring(0,1));
	}
}
