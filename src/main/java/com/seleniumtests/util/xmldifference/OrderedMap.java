/*
 * Copyright 2015 www.seleniumtests.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seleniumtests.util.xmldifference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Class representing Ordered List ADT.
 *
 * @author   trivedr
 * @version  1.0 02/16/2003 5:20 PM CST
 */
public class OrderedMap implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 14563568984654L;

	// Thread safe OrderedMap
    public static final int TYPE_SYNCHRONIZED = 1;

    // Unsynchronized OrderedMap
    public static final int TYPE_UNSYNCHRONIZED = 2;

    // Unsychronized and allows Mutiple Object Values
    public static final int TYPE_UNSYNCHRONIZED_MOV = 3;

    private ArrayList<Object> omElementOrder = null;
    private HashMap<Object, Object> omElements = null;
    private int omType = -1;

    /**
     * Constructor.
     *
     * @param  type  the type of the OrderedMap, possible values are TYPE_SYNCHRONIZED, TYPE_UNSYNCHRONIZED,
     *               TYPE_UNSYNCHRONIZED_MOV
     */
    public OrderedMap(final int type) {
        if (type == TYPE_SYNCHRONIZED) {
            omElementOrder = (ArrayList<Object>)Collections.synchronizedList(new ArrayList<Object>());
            omElements = (HashMap<Object, Object>)Collections.synchronizedMap(new HashMap<Object, Object>());
        } else if (type == TYPE_UNSYNCHRONIZED) {
            omElementOrder = new ArrayList<>();
            omElements = new HashMap<>();
        } else if (type == TYPE_UNSYNCHRONIZED_MOV) {
            omElementOrder = new ArrayList<>();
            omElements = new MOVMap();
        } else {
            throw new IllegalArgumentException("Unrecongnized OrderedMap type");
        }
    }

    /**
     * Gets size of the ordered list.
     *
     * @return  size of this list
     */
    public int size() {
        return omElementOrder.size();
    }

    /**
     * Clears the Ordered List.
     */
    public void clear() {
        omElementOrder.clear();
        omElements.clear();
    }

    /**
     * Gets the type of the OrderedList.
     */
    public int getType() {
        return omType;
    }

    /**
     * Checks to see if the element is in the Ordered List.
     *
     * @param   element  Element to be checked
     *
     * @return  true if element exists in the list, false otherwise
     */
    public boolean contains(final Object element) {
        return omElements.containsValue(element);
    }

    /**
     * Checks to see if the element key exists.
     *
     * @param   eKey  Element key
     *
     * @return  true if element key exists, false otherwise
     */
    public boolean containsKey(final Object eKey) {
        return omElementOrder.contains(eKey);
    }

    /**
     * Checks if elementkey exists in the Ordered List.
     *
     * @param   eKey  Element key
     *
     * @return  true if element key exists, false otherwise
     */
    public boolean containsElementKey(final Object eKey) {
        return omElementOrder.contains(eKey);
    }

    /**
     * . Gets Elements Order<br>
     * Returns synchronized or unsynchronized list depending on how the instance was created
     *
     * @return  Vector containing elements key in the order which they are added
     *
     * @see     OrderdedList() Contructor
     */
    public List<Object> getElementOrder() {
        return omElementOrder;
    }

    /**
     * Gets Element for the Element Key.
     *
     * @param   elementKey  the element key
     *
     * @return  Element whose key is elementKey
     */
    public Object getElement(final Object elementKey) {
        if (elementKey != null) {
            return omElements.get(elementKey);
        } else {
            return null;
        }
    }

    /**
     * Gets Element at a given position.
     *
     * @param   position  the position of the element
     *
     * @return  Element at a given position
     */
    public Object getElement(final int position) {
        if ((position > omElementOrder.size()) || (position < 0)) {
            return null;
        }

        Object elementKey = omElementOrder.get(position);

        return getElement(elementKey);
    }

    /**
     * Gets all the elements in the Ordered List in the Order in which they were entered.
     *
     * @return  Array of elements
     */
    public Object[] elements() {
        Object[] objects = new Object[omElementOrder.size()];

        for (int i = 0; i < omElementOrder.size(); i++) {
            objects[i] = omElements.get(omElementOrder.get(i));
        }

        return objects;
    }

    /**
     * Gets Element keys.
     *
     * @return  Enumeration of the element keys
     */
    public Iterator<Object> elementKeys() {
        return omElementOrder.iterator();
    }

    /**
     * Adds element with the key into the Ordered List.
     */
    public void add(final Object eKey, final Object element) {
        if ((!omElements.containsKey(eKey)) && (getType() != TYPE_UNSYNCHRONIZED_MOV)) {
            omElementOrder.add(eKey);
        }

        omElements.put(eKey, element);
    }

    /**
     * Inserts element at a position.
     *
     * @param  eKey      Element Key
     * @param  element   Element to be inserted
     * @param  position  position at which element is to be inserted
     */
    public void insert(final Object eKey, final Object element, final int position) {
        omElementOrder.add(position, eKey);
        omElements.put(eKey, element);
    }

    /**
     * Gets index of a given element with element key.
     *
     * @param   eKey  the element key
     *
     * @return  position of the element key in this ordered list
     */
    public int indexOf(final Object eKey) {
        return omElementOrder.indexOf(eKey);
    }
}
